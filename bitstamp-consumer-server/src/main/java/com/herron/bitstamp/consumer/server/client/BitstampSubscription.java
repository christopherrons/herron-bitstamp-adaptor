package com.herron.bitstamp.consumer.server.client;

import com.herron.bitstamp.consumer.server.api.MessageHandler;
import com.herron.bitstamp.consumer.server.messages.BitstampEventData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.herron.bitstamp.consumer.server.utils.BitstampUtils.getPartitionKey;

public class BitstampSubscription {
    private static final Logger LOGGER = LoggerFactory.getLogger(BitstampSubscription.class);
    private static final String SUBSCRIBE = "bts:subscribe";
    private static final String UNSUBSCRIBE = "bts:unsubscribe";
    private static final String HEART_BEAT = "bts:heartbeat";
    private static final BitstampJsonMessageDecoder BITSTAMP_JSON_MESSAGE_DECODER = new BitstampJsonMessageDecoder(BitstampEventData.class);
    private final String channel;
    private final URI uri;
    private final javax.websocket.MessageHandler messageHandler;
    private Session session;
    private boolean isSubscribed = false;
    private final ScheduledExecutorService heartBeatExecutorService = Executors.newScheduledThreadPool(1);

    public BitstampSubscription(MessageHandler messageHandler, String channel, String uri) throws DeploymentException, IOException, URISyntaxException {
        this.messageHandler = createMessageHandler(messageHandler);
        this.channel = channel;
        this.uri = new URI(uri);
        this.session = createSession();
        startHeartBeats();
    }

    private javax.websocket.MessageHandler createMessageHandler(MessageHandler messageHandler) {
        return new javax.websocket.MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                BitstampEventData event = BITSTAMP_JSON_MESSAGE_DECODER.decodeMessage(message);
                if (event.getEventDescriptionEnum() != null) {
                    handleEvent(event, messageHandler);
                } else {
                    LOGGER.info("Message: {} not decode-able.", message);
                }
            }
        };
    }

    private Session createSession() throws DeploymentException, IOException {
        LOGGER.info("Attempting to connect to: {}.", uri);

        final WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
        session = webSocketContainer.connectToServer(new CustomClientEndpoint(messageHandler), uri);

        if (session.isOpen()) {
            LOGGER.info("Successfully connected to: {}.", uri);
        }

        return session;
    }

    public void subscribe() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            int timeout = 30;
            int timeWaited = 0;
            while (!session.isOpen()) {
                try {
                    Thread.sleep(timeout * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timeWaited = timeWaited + timeout;
                LOGGER.info("Waiting for session to open before subscribing to: {}. Total time waited {}.", channel, timeWaited);
            }

            LOGGER.info("Attempting to subscribe to: {}.", channel);
            RemoteEndpoint.Basic basicRemoteEndpoint = session.getBasicRemote();
            try {
                basicRemoteEndpoint.sendObject(createSubscriptionJson());
            } catch (IOException | EncodeException e) {
                e.printStackTrace();
            }
        });
    }

    public void unsubscribe() {
        LOGGER.info("Attempting to unsubscribe to: {}", channel);
        RemoteEndpoint.Basic basicRemoteEndpoint = session.getBasicRemote();
        try {
            basicRemoteEndpoint.sendObject(createUnsubscribeJson());
            isSubscribed = false;
            heartBeatExecutorService.shutdown();
            session.close();
            LOGGER.info("Successfully unsubscribed to: {} and closed session.", channel);
        } catch (IOException | EncodeException e) {
            e.printStackTrace();
        }
    }

    private void startHeartBeats() {
        LOGGER.info("Starting heartbeats for {}! Session status: {}, isSubscribed status: {}", channel, session.isOpen(), isSubscribed);
        RemoteEndpoint.Basic basicRemoteEndpoint = session.getBasicRemote();
        heartBeatExecutorService.scheduleAtFixedRate(() -> {
            try {
                basicRemoteEndpoint.sendObject(createHeartBeatJson());
            } catch (Exception e) {
                LOGGER.warn("Could not run heartbeat for {}! Session status: {}, isSubscribed status: {}", channel, session.isOpen(), isSubscribed);
                try {
                    unsubscribe();
                    reconnect();
                } catch (DeploymentException | IOException | InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void reconnect() throws DeploymentException, IOException, InterruptedException {
        LOGGER.info("Reconnecting to {} in 10 seconds", channel);
        Thread.sleep(1000 * 10L);
        this.session = createSession();
        subscribe();
    }

    private void handleEvent(BitstampEventData event, MessageHandler messageHandler) {
        switch (event.getEventDescriptionEnum()) {
            case SUBSCRIPTION_SUCCEEDED -> {
                isSubscribed = true;
                LOGGER.info("Successfully subscribed to: {}.", channel);
            }
            case HEART_BEAT -> {
                if (event.isHeartbeatSuccessful()) {
                    LOGGER.debug("Heartbeat successful {}." + " Session status: {}, isSubscribed status: {}.", channel, session.isOpen(), isSubscribed);
                } else {
                    LOGGER.warn("Heartbeat NOT successful {}. Event: {}" + " Session status: {}, isSubscribed status: {}.", channel, event, session.isOpen(), isSubscribed);
                }
            }
            case FORCED_RECONNECT -> {
                LOGGER.warn("Forced reconnect received!");
                isSubscribed = false;
                subscribe();
            }
            case ORDER_CREATED -> messageHandler.handleMessage(event.getAddOrder(), getPartitionKey(event.channel()));
            case ORDER_UPDATED -> messageHandler.handleMessage(event.getUpdateOrder(), getPartitionKey(event.channel()));
            case ORDER_DELETED -> messageHandler.handleMessage(event.getCancelOrder(), getPartitionKey(event.channel()));
            case TRADE -> messageHandler.handleMessage(event.getTrade(), getPartitionKey(event.channel()));
            default -> LOGGER.warn("Unhandled Bitstamp event received {}: ", event);
        }
    }

    private String createSubscriptionJson() {
        return createSubscriptionRelatedJson(SUBSCRIBE);
    }

    private String createUnsubscribeJson() {
        return createSubscriptionRelatedJson(UNSUBSCRIBE);
    }

    private String createSubscriptionRelatedJson(String subscriptionType) {
        return Json.createObjectBuilder().add("event", subscriptionType).add("data", Json.createObjectBuilder().add("channel", channel)).build().toString();
    }

    private String createHeartBeatJson() {
        return Json.createObjectBuilder().add("event", HEART_BEAT).build().toString();
    }

    public boolean isSubscribed() {
        return isSubscribed && session.isOpen();
    }
}

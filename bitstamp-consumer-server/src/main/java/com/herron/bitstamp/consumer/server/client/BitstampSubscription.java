package com.herron.bitstamp.consumer.server.client;

import com.herron.bitstamp.consumer.server.eventhandler.EventHandler;
import com.herron.bitstamp.consumer.server.model.BitstampEventData;
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

public class BitstampSubscription {
    private static final Logger LOGGER = LoggerFactory.getLogger(BitstampSubscription.class);
    private static final String SUBSCRIBE = "bts:subscribe";
    private static final String UNSUBSCRIBE = "bts:unsubscribe";
    private static final String HEART_BEAT = "bts:heartbeat";
    private static final BitstampJsonMessageDecoder BITSTAMP_JSON_MESSAGE_DECODER = new BitstampJsonMessageDecoder(BitstampEventData.class);

    private final String fxCurrency;
    private final String cryptoCurrency;
    private final String channel;
    private final URI uri;

    private final MessageHandler messageHandler;

    private Session session;

    private boolean isSubscribed = false;

    private final ScheduledExecutorService heartBeatExecutorService = Executors.newScheduledThreadPool(1);


    public BitstampSubscription(EventHandler eventHandler, String fxCurrency, String cryptoCurrency, String channel, String uri) throws DeploymentException, IOException, URISyntaxException {
        this.messageHandler = createMessageHandler(eventHandler);
        this.fxCurrency = fxCurrency;
        this.cryptoCurrency = cryptoCurrency;
        this.channel = channel;
        this.uri = new URI(uri);
        this.session = createSession();
        startHeartBeats();
    }

    private MessageHandler createMessageHandler(EventHandler eventHandler) {
        return new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                BitstampEventData event = BITSTAMP_JSON_MESSAGE_DECODER.decodeMessage(message);
                if (event.getEventDescriptionEnum() != null) {
                    handleEvent(event, eventHandler);
                } else {
                    LOGGER.info(String.format("Message: %s not decodeable.", message));
                }
            }
        };
    }

    private Session createSession() throws DeploymentException, IOException {
        LOGGER.info(String.format("Attempting to connect to: %s.", uri));

        final WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
        session = webSocketContainer.connectToServer(new CustomClientEndpoint(messageHandler), uri);

        if (session.isOpen()) {
            LOGGER.info(String.format("Successfully connected to: %s.", uri));
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
                LOGGER.info(String.format("Waiting for session to open before subscribing to: %s. Total time waited %s.", createChannel(), timeWaited));
            }

            LOGGER.info(String.format("Attempting to subscribe to: %s.", createChannel()));
            RemoteEndpoint.Basic basicRemoteEndpoint = session.getBasicRemote();
            try {
                basicRemoteEndpoint.sendObject(createSubscriptionJson());
            } catch (IOException | EncodeException e) {
                e.printStackTrace();
            }
        });
    }

    public void unsubscribe() {
        LOGGER.info(String.format("Attempting to unsubscribe to: %s", createChannel()));
        RemoteEndpoint.Basic basicRemoteEndpoint = session.getBasicRemote();
        try {
            basicRemoteEndpoint.sendObject(createUnsubscribeJson());
            isSubscribed = false;
            heartBeatExecutorService.shutdown();
            session.close();
            LOGGER.info(String.format("Successfully unsubscribed to: %s and closed session.", createChannel()));
        } catch (IOException | EncodeException e) {
            e.printStackTrace();
        }
    }

    private void startHeartBeats() {
        RemoteEndpoint.Basic basicRemoteEndpoint = session.getBasicRemote();
        heartBeatExecutorService.scheduleAtFixedRate(() -> {
            LOGGER.info(String.format("Starting heartbeats for %s! Session status: %s, isSubscribed status: %s", getTradingPair(), session.isOpen(), isSubscribed));

            try {
                basicRemoteEndpoint.sendObject(createHeartBeatJson());
            } catch (Exception e) {
                LOGGER.warn(String.format("Could not run heartbeat for %s! Session status: %s, isSubscribed status: %s", getTradingPair(), session.isOpen(), isSubscribed));
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
        LOGGER.info(String.format("Reconnecting to %s in 10 seconds", getTradingPair()));
        Thread.sleep(1000 * 10L);
        this.session = createSession();
        subscribe();
    }

    private void handleEvent(BitstampEventData event, EventHandler eventHandler) {
        switch (event.getEventDescriptionEnum()) {
            case SUBSCRIPTION_SUCCEEDED -> {
                isSubscribed = true;
                LOGGER.info(String.format("Successfully subscribed to: %s.", createChannel()));
            }
            case HEART_BEAT -> {
                if (event.getHeartBeat().isSuccessful()) {
                    LOGGER.info(String.format("Heartbeat successful %s." + " Session status: %s, isSubscribed status: %s.", getTradingPair(), session.isOpen(), isSubscribed));
                } else {
                    LOGGER.warn(String.format("Heartbeat NOT successful %s. Event: %" +
                            " Session status: %s, isSubscribed status: %s.", getTradingPair(), event, session.isOpen(), isSubscribed));
                }
            }
            case FORCED_RECONNECT -> {
                LOGGER.warn("Forced reconnect received!");
                isSubscribed = false;
                subscribe();
            }
            case ORDER_CREATED, ORDER_DELETED, ORDER_UPDATED -> eventHandler.handleEvent(event.getOrder());
            case TRADE -> eventHandler.handleEvent(event.getTrade());
            default -> LOGGER.warn(String.format("Unhandled Bitstamp event received %s: ", event));
        }
    }

    private String createSubscriptionJson() {
        return createSubscriptionRelatedJson(SUBSCRIBE);
    }

    private String createUnsubscribeJson() {
        return createSubscriptionRelatedJson(UNSUBSCRIBE);
    }

    private String createSubscriptionRelatedJson(String subscriptionType) {
        return Json.createObjectBuilder().add("event", subscriptionType).add("data", Json.createObjectBuilder().add("channel", createChannel())).build().toString();
    }

    private String createHeartBeatJson() {
        return Json.createObjectBuilder().add("event", HEART_BEAT).build().toString();
    }

    private String createChannel() {
        return String.format("%s_%s", channel, getTradingPair());
    }

    private String getTradingPair() {
        return String.format("%s%s", cryptoCurrency, fxCurrency);
    }

    public boolean isSubscribed() {
        return isSubscribed && session.isOpen();
    }
}

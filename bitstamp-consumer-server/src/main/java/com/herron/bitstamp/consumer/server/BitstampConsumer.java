package com.herron.bitstamp.consumer.server;

import com.herron.bitstamp.consumer.server.client.BitstampSubscription;
import com.herron.bitstamp.consumer.server.config.BitstampConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BitstampConsumer {

    private final EventHandler eventHandler;
    private static final Logger LOGGER = LoggerFactory.getLogger(BitstampConsumer.class);
    private final BitstampConsumerConfig.SubscriptionDetailConfig subscriptionDetailConfig;
    private final Map<BitstampConsumerConfig.SubscriptionDetailConfig.SubscriptionDetail, BitstampSubscription> keyToSubscription = new ConcurrentHashMap<>();

    public BitstampConsumer(BitstampConsumerConfig.SubscriptionDetailConfig subscriptionDetailConfig, EventHandler eventHandler) {
        this.subscriptionDetailConfig = subscriptionDetailConfig;
        this.eventHandler = eventHandler;
    }

    public void init() {
        for (var details : subscriptionDetailConfig.getSubscriptionDetails()) {
            try {
                var subscription = new BitstampSubscription(
                        eventHandler,
                        details.fxCurrency(),
                        details.cryptoCurrency(),
                        details.channel(),
                        details.uri()
                );
                subscription.subscribe();
                keyToSubscription.computeIfAbsent(details, k -> subscription);

            } catch (DeploymentException | IOException | URISyntaxException e) {
                LOGGER.error("Unable to subscribe to channel {}: {}", details, e);
            }
        }
    }


}

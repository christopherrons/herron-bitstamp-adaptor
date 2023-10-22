package com.herron.exchange.eventgenerator.server.streaming;

import com.herron.exchange.eventgenerator.server.config.EventGeneratorConfig;
import com.herron.exchange.integrations.bitstamp.BitstampWebsocketClient;
import com.herron.exchange.integrations.bitstamp.model.BitstampWebsocketRequest;


public class BitstampConsumer {

    private final EventGeneratorConfig.SubscriptionDetailConfig subscriptionDetailConfig;
    private final BitstampBroadcaster bitstampMessageBroadcaster;
    private final BitstampWebsocketClient client;

    public BitstampConsumer(EventGeneratorConfig.SubscriptionDetailConfig subscriptionDetailConfig,
                            BitstampBroadcaster bitstampBroadcaster,
                            BitstampWebsocketClient client) {
        this.subscriptionDetailConfig = subscriptionDetailConfig;
        this.bitstampMessageBroadcaster = bitstampBroadcaster;
        this.client = client;
    }

    public void init() {
        subscriptionDetailConfig.getChannels().forEach(ch -> {
            var request = new BitstampWebsocketRequest(subscriptionDetailConfig.getUri(), ch);
            client.subscribe(bitstampMessageBroadcaster::handleMessage, request);
        });
    }
}

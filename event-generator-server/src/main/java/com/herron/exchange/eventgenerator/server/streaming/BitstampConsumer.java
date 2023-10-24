package com.herron.exchange.eventgenerator.server.streaming;

import com.herron.exchange.eventgenerator.server.config.EventGeneratorConfig;
import com.herron.exchange.integrations.bitstamp.BitstampWebsocketClient;
import com.herron.exchange.integrations.bitstamp.model.BitstampWebsocketRequest;


public class BitstampConsumer {

    private final EventGeneratorConfig.BitstampSubscriptionDetailConfig bitstampSubscriptionDetailConfig;
    private final BitstampBroadcaster bitstampMessageBroadcaster;
    private final BitstampWebsocketClient client;

    public BitstampConsumer(EventGeneratorConfig.BitstampSubscriptionDetailConfig bitstampSubscriptionDetailConfig,
                            BitstampBroadcaster bitstampBroadcaster,
                            BitstampWebsocketClient client) {
        this.bitstampSubscriptionDetailConfig = bitstampSubscriptionDetailConfig;
        this.bitstampMessageBroadcaster = bitstampBroadcaster;
        this.client = client;
    }

    public void init() {
        bitstampSubscriptionDetailConfig.getChannels().forEach(ch -> {
            var request = new BitstampWebsocketRequest(bitstampSubscriptionDetailConfig.getUri(), ch);
            client.subscribe(bitstampMessageBroadcaster::handleMessage, request);
        });
    }
}

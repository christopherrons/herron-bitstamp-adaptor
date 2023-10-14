package com.herron.event.generator.server.streaming;

import com.herron.event.generator.server.config.EventGeneratorConfig;
import com.herron.exchange.integrations.generator.bitstamp.BitstampWebsocketClient;
import com.herron.exchange.integrations.generator.bitstamp.model.BitstampWebsocketRequest;

public class BitstampConsumer {

    private final EventGeneratorConfig.SubscriptionDetailConfig subscriptionDetailConfig;
    private final BitsampBroadcaster bistampMessageBroadcaster;
    private final BitstampWebsocketClient client;

    public BitstampConsumer(EventGeneratorConfig.SubscriptionDetailConfig subscriptionDetailConfig,
                            BitsampBroadcaster bitsampBroadcaster,
                            BitstampWebsocketClient client) {
        this.subscriptionDetailConfig = subscriptionDetailConfig;
        this.bistampMessageBroadcaster = bitsampBroadcaster;
        this.client = client;
    }

    public void init() {
        subscriptionDetailConfig.getChannels().forEach(ch -> {
            var request = new BitstampWebsocketRequest(subscriptionDetailConfig.getUri(), ch);
            client.subscribe(bistampMessageBroadcaster::handleMessage, request);
        });
    }
}

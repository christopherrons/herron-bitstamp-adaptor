package com.herron.exchange.eventgenerator.server;

import com.herron.exchange.eventgenerator.server.emulation.OrderEventEmulatorBroadcaster;
import com.herron.exchange.eventgenerator.server.streaming.BitstampConsumer;

public class EventGenerationBootloader {

    private final BitstampConsumer bitstampConsumer;
    private final OrderEventEmulatorBroadcaster eventEmulator;

    public EventGenerationBootloader(BitstampConsumer bitstampConsumer, OrderEventEmulatorBroadcaster eventEmulator) {
        this.bitstampConsumer = bitstampConsumer;
        this.eventEmulator = eventEmulator;
    }

    public void init() {
        bitstampConsumer.init();
        eventEmulator.init();
    }
}

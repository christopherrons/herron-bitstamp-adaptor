package com.herron.event.generator.server;

import com.herron.event.generator.server.emulation.OrderEventEmulator;
import com.herron.event.generator.server.streaming.BitstampConsumer;

public class EventGenerationBootloader {

    private final BitstampConsumer bitstampConsumer;
    private final OrderEventEmulator eventEmulator;

    public EventGenerationBootloader(BitstampConsumer bitstampConsumer, OrderEventEmulator eventEmulator) {
        this.bitstampConsumer = bitstampConsumer;
        this.eventEmulator = eventEmulator;
    }

    public void init() throws InterruptedException {
        bitstampConsumer.init();
        eventEmulator.init();
    }
}

package com.herron.exchange.eventgenerator.server;

import com.herron.exchange.eventgenerator.server.emulation.OrderEventEmulator;
import com.herron.exchange.eventgenerator.server.streaming.BitstampConsumer;

public class EventGenerationBootloader {

    private final BitstampConsumer bitstampConsumer;
    private final OrderEventEmulator eventEmulator;

    public EventGenerationBootloader(BitstampConsumer bitstampConsumer, OrderEventEmulator eventEmulator) {
        this.bitstampConsumer = bitstampConsumer;
        this.eventEmulator = eventEmulator;
    }

    public void init() {
      //  bitstampConsumer.init();
        // eventEmulator.init();
    }
}

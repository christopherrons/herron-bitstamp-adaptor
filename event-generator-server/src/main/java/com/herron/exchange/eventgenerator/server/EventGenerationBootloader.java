package com.herron.exchange.eventgenerator.server;

import com.herron.exchange.common.api.common.bootloader.Bootloader;
import com.herron.exchange.eventgenerator.server.consumers.PreviousSettlementPriceConsumer;
import com.herron.exchange.eventgenerator.server.consumers.ReferenceDataConsumer;
import com.herron.exchange.eventgenerator.server.emulation.OrderEventEmulatorBroadcaster;
import com.herron.exchange.eventgenerator.server.streaming.BitstampSubscriptionHandler;

public class EventGenerationBootloader extends Bootloader {

    private final BitstampSubscriptionHandler bitstampSubscriptionHandler;
    private final OrderEventEmulatorBroadcaster eventEmulator;
    private final PreviousSettlementPriceConsumer previousSettlementPriceConsumer;
    private final ReferenceDataConsumer referenceDataConsumer;

    public EventGenerationBootloader(BitstampSubscriptionHandler bitstampSubscriptionHandler,
                                     OrderEventEmulatorBroadcaster eventEmulator,
                                     PreviousSettlementPriceConsumer previousSettlementPriceConsumer,
                                     ReferenceDataConsumer referenceDataConsumer) {
        super("Event-Generation");
        this.bitstampSubscriptionHandler = bitstampSubscriptionHandler;
        this.eventEmulator = eventEmulator;
        this.previousSettlementPriceConsumer = previousSettlementPriceConsumer;
        this.referenceDataConsumer = referenceDataConsumer;
    }

    @Override
    protected void bootloaderInit() {
        bitstampSubscriptionHandler.init();
        referenceDataConsumer.init();
        previousSettlementPriceConsumer.init();
        referenceDataConsumer.await();
        previousSettlementPriceConsumer.await();
        eventEmulator.init();
        bootloaderComplete();
    }
}

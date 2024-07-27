package com.herron.exchange.eventgenerator.server;

import com.herron.exchange.common.api.common.bootloader.Bootloader;
import com.herron.exchange.eventgenerator.server.consumers.PreviousSettlementPriceConsumer;
import com.herron.exchange.eventgenerator.server.consumers.ReferenceDataConsumer;
import com.herron.exchange.eventgenerator.server.emulation.OrderEventEmulatorBroadcaster;
import com.herron.exchange.eventgenerator.server.streaming.BitstampConsumer;

public class EventGenerationBootloader extends Bootloader {

    private final BitstampConsumer bitstampConsumer;
    private final OrderEventEmulatorBroadcaster eventEmulator;
    private final PreviousSettlementPriceConsumer previousSettlementPriceConsumer;
    private final ReferenceDataConsumer referenceDataConsumer;

    public EventGenerationBootloader(BitstampConsumer bitstampConsumer,
                                     OrderEventEmulatorBroadcaster eventEmulator,
                                     PreviousSettlementPriceConsumer previousSettlementPriceConsumer,
                                     ReferenceDataConsumer referenceDataConsumer) {
        super("Event-Generation");
        this.bitstampConsumer = bitstampConsumer;
        this.eventEmulator = eventEmulator;
        this.previousSettlementPriceConsumer = previousSettlementPriceConsumer;
        this.referenceDataConsumer = referenceDataConsumer;
    }

    @Override
    protected void bootloaderInit() {
        bitstampConsumer.init();
        referenceDataConsumer.init();
        previousSettlementPriceConsumer.init();
        referenceDataConsumer.await();
        previousSettlementPriceConsumer.await();
        //eventEmulator.init();
        bootloaderComplete();
    }
}

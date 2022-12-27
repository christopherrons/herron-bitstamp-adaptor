package com.herron.bitstamp.consumer.server.model;

import com.herron.bitstamp.consumer.server.api.BitstampMarketEvent;
import com.herron.bitstamp.consumer.server.enums.EvenTypeEnum;

public record BitstampStockInstrument(String instrumentId, String instrumentType, String tradingCurrency, long timeStampInMs) implements BitstampMarketEvent {

    public BitstampStockInstrument(String instrumentId, String tradingCurrency, long timeStampInMs) {
        this(instrumentId, "stock", tradingCurrency, timeStampInMs);
    }

    @Override
    public long getTimeStampMs() {
        return timeStampInMs;
    }

    @Override
    public String getId() {
        return instrumentId;
    }

    @Override
    public EvenTypeEnum getEventTypeEnum() {
        return EvenTypeEnum.INSTRUMENT;
    }
}

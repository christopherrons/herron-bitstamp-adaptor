package com.herron.bitstamp.consumer.server.model;

import com.herron.bitstamp.consumer.server.api.BitstampMarketEvent;
import com.herron.bitstamp.consumer.server.enums.EvenTypeEnum;

public record BitstampOrderbook(String orderbookId, String instrumentId, long timeStampInMs) implements BitstampMarketEvent {
    @Override
    public long getTimeStampMs() {
        return timeStampInMs;
    }
    @Override
    public String getId() {
        return orderbookId;
    }

    @Override
    public EvenTypeEnum getEventTypeEnum() {
        return EvenTypeEnum.ORDERBOOK;
    }
}

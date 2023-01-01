package com.herron.bitstamp.consumer.server.messages;

import com.herron.bitstamp.consumer.server.api.BitstampMarketEvent;
import com.herron.bitstamp.consumer.server.enums.EventTypeEnum;

public record BitstampStockInstrument(String instrumentId, String instrumentType, long timeStampInMs) implements BitstampMarketEvent {

    public BitstampStockInstrument(String instrumentId,  long timeStampInMs) {
        this(instrumentId, "stock", timeStampInMs);
    }

    @Override
    public String getId() {
        return instrumentId;
    }

    @Override
    public EventTypeEnum getEventTypeEnum() {
        return EventTypeEnum.INSTRUMENT;
    }

    @Override
    public String getMessageType() {
        return "BSSI";
    }
}

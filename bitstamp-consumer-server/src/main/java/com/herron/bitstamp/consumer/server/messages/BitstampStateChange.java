package com.herron.bitstamp.consumer.server.messages;

import com.herron.bitstamp.consumer.server.api.BitstampMarketEvent;
import com.herron.bitstamp.consumer.server.enums.EvenTypeEnum;

public record BitstampStateChange(String orderbookId, String stateChange, long timeStampInMs) implements BitstampMarketEvent {
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
        return EvenTypeEnum.STATE_CHANGE;
    }

    @Override
    public String getMessageType() {
        return "BSSC";
    }
}

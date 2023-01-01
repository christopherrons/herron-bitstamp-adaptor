package com.herron.bitstamp.consumer.server.messages;

import com.herron.bitstamp.consumer.server.api.BitstampMarketEvent;
import com.herron.bitstamp.consumer.server.enums.EventTypeEnum;

public record BitstampStateChange(String orderbookId, String stateChange, long timeStampInMs) implements BitstampMarketEvent {
    @Override
    public String getId() {
        return orderbookId;
    }

    @Override
    public EventTypeEnum getEventTypeEnum() {
        return EventTypeEnum.STATE_CHANGE;
    }

    @Override
    public String getMessageType() {
        return "BSSC";
    }
}

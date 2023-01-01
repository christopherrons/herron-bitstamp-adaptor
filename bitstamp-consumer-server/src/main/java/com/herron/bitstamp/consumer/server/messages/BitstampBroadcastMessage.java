package com.herron.bitstamp.consumer.server.messages;

import com.herron.bitstamp.consumer.server.api.BitstampMarketEvent;
import com.herron.bitstamp.consumer.server.enums.EventTypeEnum;

public record BitstampBroadcastMessage(BitstampMarketEvent serializedMessage,
                                       String serializedMessageTypeString,
                                       long sequenceNumber,
                                       long timeStampInMs) implements BitstampMarketEvent {

    @Override
    public String getId() {
        return "0";
    }

    @Override
    public EventTypeEnum getEventTypeEnum() {
        return EventTypeEnum.BROADCAST;
    }

    @Override
    public String getMessageType() {
        return "BSBM";
    }
}

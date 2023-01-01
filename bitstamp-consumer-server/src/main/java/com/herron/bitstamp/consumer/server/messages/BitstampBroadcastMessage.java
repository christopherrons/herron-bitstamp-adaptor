package com.herron.bitstamp.consumer.server.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.herron.bitstamp.consumer.server.api.BitstampMarketEvent;
import com.herron.bitstamp.consumer.server.enums.EventTypeEnum;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BitstampBroadcastMessage(BitstampMarketEvent serializedMessage,
                                       String serializedMessageTypeString,
                                       long sequenceNumber,
                                       long timeStampInMs) implements BitstampMarketEvent {
    public static final String MESSAGE_TYPE = "BSBM";

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
        return MESSAGE_TYPE;
    }
}

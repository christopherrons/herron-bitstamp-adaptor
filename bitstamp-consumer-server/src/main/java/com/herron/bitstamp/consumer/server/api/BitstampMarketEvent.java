package com.herron.bitstamp.consumer.server.api;

import com.herron.bitstamp.consumer.server.enums.EventTypeEnum;

public interface BitstampMarketEvent {

    long timeStampInMs();

    String getId();

    EventTypeEnum getEventTypeEnum();

    String getMessageType();
}

package com.herron.bitstamp.consumer.server.api;

import com.herron.bitstamp.consumer.server.enums.EvenTypeEnum;

public interface BitstampMarketEvent {

    long getTimeStampMs();

    String getId();

    EvenTypeEnum getEventTypeEnum();

    String getMessageType();
}

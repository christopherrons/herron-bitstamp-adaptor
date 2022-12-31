package com.herron.bitstamp.consumer.server.messages;

import com.herron.bitstamp.consumer.server.api.BitstampMarketEvent;
import com.herron.bitstamp.consumer.server.enums.EvenTypeEnum;

public record BitstampOrderbookData(String orderbookId,
                                    String instrumentId,
                                    String matchingAlgorithm,
                                    String tradingCurrency,
                                    double minTradeVolume,
                                    long timeStampInMs) implements BitstampMarketEvent {
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

    @Override
    public String getMessageType() {
        return "BSOB";
    }
}

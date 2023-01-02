package com.herron.bitstamp.consumer.server.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.herron.exchange.common.api.common.api.Message;
import com.herron.exchange.common.api.common.api.OrderbookData;
import com.herron.exchange.common.api.common.enums.MatchingAlgorithmEnum;
import com.herron.exchange.common.api.common.enums.MessageTypesEnum;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BitstampOrderbookData(String orderbookId,
                                    String instrumentId,
                                    MatchingAlgorithmEnum matchingAlgorithm,
                                    String tradingCurrency,
                                    double minTradeVolume,
                                    long timeStampInMs) implements OrderbookData {

    public BitstampOrderbookData(BitstampOrderbookData orderbookData) {
        this(orderbookData.orderbookId(),
                orderbookData.instrumentId(),
                orderbookData.matchingAlgorithm(),
                orderbookData.tradingCurrency(),
                orderbookData.minTradeVolume(),
                orderbookData.timeStampInMs());
    }

    @Override
    public Message getCopy() {
        return new BitstampOrderbookData(this);
    }

    @Override
    public MessageTypesEnum messageType() {
        return MessageTypesEnum.BITSTAMP_ORDERBOOK_DATA;
    }

}

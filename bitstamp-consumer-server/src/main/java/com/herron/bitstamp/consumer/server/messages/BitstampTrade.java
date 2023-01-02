package com.herron.bitstamp.consumer.server.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.herron.exchange.common.api.common.api.Message;
import com.herron.exchange.common.api.common.api.Trade;
import com.herron.exchange.common.api.common.enums.MessageTypesEnum;
import com.herron.exchange.common.api.common.model.MonetaryAmount;
import com.herron.exchange.common.api.common.model.Participant;

import java.util.Map;

import static com.herron.bitstamp.consumer.server.utils.BitstampUtils.*;

public record BitstampTrade(Participant bidParticipant,
                            Participant askParticipant,
                            String tradeId,
                            String buyOrderId,
                            String askOrderId,
                            boolean isBidSideAggressor,
                            double volume,
                            MonetaryAmount monetaryAmount,
                            long timeStampInMs,
                            String instrumentId,
                            String orderbookId) implements Trade {

    /*  Json Structure example of Bitstamp Live Trade
 {
    "data":{
       "id":216689415,
       "timestamp":"1641719887",
       "amount":500,
       "amount_str":"500.00000000",
       "price":0.74481,
       "price_str":"0.74481",
       "type":1,
       "microtimestamp":"1641719887925000",
       "buy_order_id":1445416603234305,
       "sell_order_id":1445416604745728
    },
    "channel":"live_trades_xrpusd",
    "event":"trade"
 }*/
    public BitstampTrade(@JsonProperty("data") Map<String, Object> data, @JsonProperty("channel") String channel, @JsonProperty("event") String event) {
        this(generateParticipant(),
                generateParticipant(),
                !data.isEmpty() ? (String) data.get("id") : "-1",
                !data.isEmpty() ? (String) data.get("buy_order_id") : "NONE",
                !data.isEmpty() ? (String) data.get("sell_order_id") : "NONE",
                !data.isEmpty() && (int) data.get("type") == 0,
                !data.isEmpty() ? Double.parseDouble((String) data.get("amount_str")) : -1.0,
                !data.isEmpty() ? new MonetaryAmount(Double.parseDouble((String) data.get("price_str")), channel.split("_")[2].substring(3, 6)) : new MonetaryAmount(0.0, channel.split("_")[2].substring(3, 6)),
                !data.isEmpty() ? Long.parseLong((String) data.get("microtimestamp")) / 1000 : -1L,
                createInstrumentId(channel),
                createOrderbookId(channel)
        );
    }

    public BitstampTrade(Trade trade) {
        this(trade.bidParticipant(),
                trade.askParticipant(),
                trade.tradeId(),
                trade.buyOrderId(),
                trade.askOrderId(),
                trade.isBidSideAggressor(),
                trade.volume(),
                trade.monetaryAmount(),
                trade.timeStampInMs(),
                trade.instrumentId(),
                trade.orderbookId());
    }


    @Override
    public Message getCopy() {
        return null;
    }

    @Override
    public MessageTypesEnum messageType() {
        return MessageTypesEnum.BITSTAMP_TRADE;
    }

    @Override
    public double price() {
        return monetaryAmount.value();
    }
}

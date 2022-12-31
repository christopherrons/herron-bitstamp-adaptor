package com.herron.bitstamp.consumer.server.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.herron.bitstamp.consumer.server.api.BitstampMarketEvent;
import com.herron.bitstamp.consumer.server.enums.EvenTypeEnum;
import com.herron.bitstamp.consumer.server.enums.OrderOperationEnum;

import java.util.Map;

import static com.herron.bitstamp.consumer.server.utils.BitstampUtils.*;
import static com.herron.bitstamp.consumer.server.utils.ParticipantGeneratorUtils.getUserFromPool;

public record BitstampOrder(OrderOperationEnum orderOperation,
                            String participant,
                            String orderId,
                            int orderSide,
                            double initialVolume,
                            double currentVolume,
                            double price,
                            String currency,
                            long timeStampInMs,
                            String instrumentId,
                            String orderbookId,
                            String orderExecutionType,
                            String orderType) implements BitstampMarketEvent {

    /*  Json Structure example of Bitstamp Live Order
{
     "data": {
             "id": 1441935087595520,
             "id_str": "1441935087595520",
             "order_type": 1,
             "datetime": "1640869916",
             "microtimestamp": "1640869915619000",
             "amount": 2162.61269107,
             "amount_str": "2162.61269107",
             "price": 0.84604,
             "price_str": "0.84604"
 },
     "channel": "live_orders_xrpusd",
     "event": "order_deleted"
 }*/
    public BitstampOrder(@JsonProperty("data") Map<String, Object> data, @JsonProperty("channel") String channel, @JsonProperty("event") String event) {
        this(OrderOperationEnum.extractValue(event),
                String.format("Bitstamp;%s", getUserFromPool()),
                !data.isEmpty() ? (String) data.get("id_str") : "NONE",
                !data.isEmpty() ? (int) data.get("order_type") : -1,
                !data.isEmpty() ? Double.parseDouble((String) data.get("amount_str")) : -1.0,
                !data.isEmpty() ? Double.parseDouble((String) data.get("amount_str")) : -1.0,
                !data.isEmpty() ? Double.parseDouble((String) data.get("price_str")) : -1.0,
                channel.split("_")[2].substring(3, 6),
                !data.isEmpty() ? Long.parseLong((String) data.get("microtimestamp")) / 1000 : -1L,
                createInstrumentId(channel),
                createOrderbookId(channel),
                generateOrderExecutionType(),
                generateOrderType()
        );
    }

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
        return EvenTypeEnum.ORDER;
    }

    @Override
    public double price() {
        if (orderType.equals("limit")) {
            return price;
        }
        return orderSide == 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
    }

    @Override
    public String getMessageType() {
        return "BSAO";
    }
}

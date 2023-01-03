package com.herron.bitstamp.consumer.server.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.herron.exchange.common.api.common.api.Message;
import com.herron.exchange.common.api.common.api.UpdateOrder;
import com.herron.exchange.common.api.common.enums.*;
import com.herron.exchange.common.api.common.model.MonetaryAmount;
import com.herron.exchange.common.api.common.model.Participant;

import java.util.Map;

import static com.herron.bitstamp.consumer.server.utils.BitstampUtils.*;
import static com.herron.exchange.common.api.common.enums.MessageTypesEnum.BITSTAMP_UPDATE_ORDER;

public record BitstampUpdateOrder(OrderOperationEnum orderOperation,
                                  Participant participant,
                                  String orderId,
                                  OrderSideEnum orderSide,
                                  double initialVolume,
                                  double currentVolume,
                                  MonetaryAmount monetaryAmount,
                                  long timeStampInMs,
                                  String instrumentId,
                                  String orderbookId,
                                  OrderExecutionTypeEnum orderExecutionType,
                                  OrderTypeEnum orderType,
                                  OrderUpdatedOperationTypeEnum updateOperationType) implements UpdateOrder {

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
    public BitstampUpdateOrder(@JsonProperty("data") Map<String, Object> data, @JsonProperty("channel") String channel, @JsonProperty("event") String event) {
        this(OrderOperationEnum.extractValue(event),
                generateParticipant(),
                !data.isEmpty() ? (String) data.get("id_str") : "NONE",
                !data.isEmpty() ? OrderSideEnum.fromValue((int) data.get("order_type")) : OrderSideEnum.INVALID_ORDER_SIDE,
                !data.isEmpty() ? Double.parseDouble((String) data.get("amount_str")) : -1.0,
                !data.isEmpty() ? Double.parseDouble((String) data.get("amount_str")) : -1.0,
                !data.isEmpty() ? new MonetaryAmount(Double.parseDouble((String) data.get("price_str")), channel.split("_")[2].substring(3, 6)) : new MonetaryAmount(0.0, channel.split("_")[2].substring(3, 6)),
                !data.isEmpty() ? Long.parseLong((String) data.get("microtimestamp")) / 1000 : -1L,
                createInstrumentId(channel),
                createOrderbookId(channel),
                generateOrderExecutionType(),
                generateOrderType(),
                OrderUpdatedOperationTypeEnum.EXTERNAL_UPDATE
        );
    }

    public BitstampUpdateOrder(UpdateOrder order) {
        this(order.orderOperation(),
                order.participant(),
                order.orderId(),
                order.orderSide(),
                order.initialVolume(),
                order.currentVolume(),
                order.monetaryAmount(),
                order.timeStampInMs(),
                order.instrumentId(),
                order.orderbookId(),
                order.orderExecutionType(),
                order.orderType(),
                order.updateOperationType());
    }

    @Override
    public double price() {
        if (orderType == OrderTypeEnum.LIMIT) {
            return monetaryAmount.value();
        }
        return orderSide == OrderSideEnum.BID ? Integer.MAX_VALUE : Integer.MIN_VALUE;
    }

    @Override
    public Message getCopy() {
        return new BitstampUpdateOrder(this);
    }

    @Override
    public MessageTypesEnum messageType() {
        return BITSTAMP_UPDATE_ORDER;
    }
}

package com.herron.exchange.eventgenerator.server.streaming;

import com.herron.exchange.common.api.common.api.Event;
import com.herron.exchange.common.api.common.messages.common.Price;
import com.herron.exchange.common.api.common.messages.common.Timestamp;
import com.herron.exchange.common.api.common.messages.common.Volume;
import com.herron.exchange.common.api.common.messages.trading.ImmutableLimitOrder;
import com.herron.exchange.common.api.common.messages.trading.ImmutableMarketOrder;
import com.herron.exchange.common.api.common.messages.trading.LimitOrder;
import com.herron.exchange.common.api.common.messages.trading.MarketOrder;
import com.herron.exchange.integrations.bitstamp.api.BitstampMessage;
import com.herron.exchange.integrations.bitstamp.messages.BitstampAddOrder;

import static com.herron.exchange.common.api.common.enums.EventType.USER;
import static com.herron.exchange.common.api.common.enums.OrderTypeEnum.MARKET;

public class BitstampUtil {

    public static Event mapMessage(BitstampMessage bitstampMessage) {
        // We only care for add orders as this application servers as an event generator
        if (bitstampMessage instanceof BitstampAddOrder order) {
            return order.orderType() == MARKET ? mapMarketOrder(order) : mapLimitOrder(order);
        }
        return null;
    }

    private static MarketOrder mapMarketOrder(BitstampAddOrder order) {
        return ImmutableMarketOrder.builder()
                .timeOfEvent(Timestamp.now())
                .orderId(order.orderId())
                .currentVolume(Volume.create(order.currentVolume()))
                .initialVolume(Volume.create(order.initialVolume()))
                .instrumentId(order.orderbookId())
                .orderSide(order.orderSide())
                .participant(order.participant())
                .orderbookId(order.orderbookId())
                .eventType(USER)
                .orderOperationCause(order.orderOperationCauseEnum())
                .orderOperation(order.orderOperation())
                .build();
    }

    private static LimitOrder mapLimitOrder(BitstampAddOrder order) {
        return ImmutableLimitOrder.builder()
                .timeOfEvent(Timestamp.now())
                .orderId(order.orderId())
                .currentVolume(Volume.create(order.currentVolume()))
                .initialVolume(Volume.create(order.initialVolume()))
                .instrumentId(order.orderbookId())
                .orderSide(order.orderSide())
                .price(Price.create(order.price()))
                .participant(order.participant())
                .timeInForce(order.timeInForce())
                .orderbookId(order.orderbookId())
                .eventType(USER)
                .orderOperationCause(order.orderOperationCauseEnum())
                .orderOperation(order.orderOperation())
                .build();
    }
}

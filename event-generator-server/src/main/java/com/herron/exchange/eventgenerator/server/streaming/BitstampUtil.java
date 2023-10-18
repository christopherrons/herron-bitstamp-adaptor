package com.herron.exchange.eventgenerator.server.streaming;

import com.herron.exchange.common.api.common.api.Event;
import com.herron.exchange.common.api.common.api.trading.orders.LimitOrder;
import com.herron.exchange.common.api.common.api.trading.orders.MarketOrder;
import com.herron.exchange.common.api.common.messages.common.Price;
import com.herron.exchange.common.api.common.messages.common.Volume;
import com.herron.exchange.common.api.common.messages.trading.ImmutableDefaultLimitOrder;
import com.herron.exchange.common.api.common.messages.trading.ImmutableDefaultMarketOrder;
import com.herron.exchange.integrations.generator.bitstamp.api.BitstampMessage;
import com.herron.exchange.integrations.generator.bitstamp.messages.BitstampAddOrder;

import static com.herron.exchange.common.api.common.enums.OrderTypeEnum.MARKET;

public class BitstampUtil {
    private static final String ID = "bitstamp_equity_btcusd";

    public static Event mapMessage(BitstampMessage bitstampMessage) {
        // We only care for add orders as this application servers as an event generator
        if (bitstampMessage instanceof BitstampAddOrder order) {
            return order.orderType() == MARKET ? mapMarketOrder(order) : mapLimitOrder(order);
        }
        return null;
    }

    private static MarketOrder mapMarketOrder(BitstampAddOrder order) {
        return ImmutableDefaultMarketOrder.builder()
                .timeOfEventMs(order.timeStampInMs())
                .orderId(order.orderId())
                .currentVolume(Volume.create(order.currentVolume()))
                .initialVolume(Volume.create(order.initialVolume()))
                .instrumentId(ID)
                .orderSide(order.orderSide())
                .participant(order.participant())
                .orderbookId(ID)
                .orderOperationCause(order.orderOperationCauseEnum())
                .orderOperation(order.orderOperation())
                .build();
    }

    private static LimitOrder mapLimitOrder(BitstampAddOrder order) {
        return ImmutableDefaultLimitOrder.builder()
                .timeOfEventMs(order.timeStampInMs())
                .orderId(order.orderId())
                .currentVolume(Volume.create(order.currentVolume()))
                .initialVolume(Volume.create(order.initialVolume()))
                .instrumentId(ID)
                .orderSide(order.orderSide())
                .price(Price.create(order.price()))
                .participant(order.participant())
                .timeInForce(order.timeInForce())
                .orderbookId(ID)
                .orderOperationCause(order.orderOperationCauseEnum())
                .orderOperation(order.orderOperation())
                .build();
    }
}

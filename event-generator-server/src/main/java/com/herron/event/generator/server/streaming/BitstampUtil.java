package com.herron.event.generator.server.streaming;

import com.herron.exchange.common.api.common.api.Event;
import com.herron.exchange.common.api.common.api.trading.orders.AddOrder;
import com.herron.exchange.common.api.common.messages.trading.ImmutableHerronAddOrder;
import com.herron.exchange.common.api.common.model.Price;
import com.herron.exchange.common.api.common.model.Volume;
import com.herron.exchange.integrations.generator.bitstamp.api.BitstampMessage;
import com.herron.exchange.integrations.generator.bitstamp.messages.BitstampAddOrder;

public class BitstampUtil {
    private static final String ID = "bitstamp_equity_btcusd";

    public static Event mapMessage(BitstampMessage bitstampMessage) {
        // We only care for add orders as this application servers as an event generator
        if (bitstampMessage instanceof BitstampAddOrder addOrder) {
            return mapAddOrder(addOrder);
        }
        return null;
    }

    private static AddOrder mapAddOrder(BitstampAddOrder addOrder) {
        return ImmutableHerronAddOrder.builder()
                .timeOfEventMs(addOrder.timeStampInMs())
                .orderId(addOrder.orderId())
                .currentVolume(Volume.create(addOrder.currentVolume()))
                .initialVolume(Volume.create(addOrder.initialVolume()))
                .instrumentId(ID)
                .orderSide(addOrder.orderSide())
                .price(Price.create(addOrder.price()))
                .participant(addOrder.participant())
                .orderType(addOrder.orderType())
                .orderExecutionType(addOrder.orderExecutionType())
                .addOperationType(addOrder.addOperationType())
                .orderbookId(ID)
                .build();
    }
}

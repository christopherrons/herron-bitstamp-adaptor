package com.herron.event.generator.server.emulation;

import com.herron.event.generator.server.utils.Utils;
import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
import com.herron.exchange.common.api.common.api.trading.orders.AddOrder;
import com.herron.exchange.common.api.common.enums.OrderAddOperationTypeEnum;
import com.herron.exchange.common.api.common.enums.OrderExecutionTypeEnum;
import com.herron.exchange.common.api.common.enums.OrderSideEnum;
import com.herron.exchange.common.api.common.messages.trading.ImmutableHerronAddOrder;
import com.herron.exchange.common.api.common.model.Price;
import com.herron.exchange.common.api.common.model.Volume;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static com.herron.exchange.common.api.common.enums.OrderExecutionTypeEnum.*;
import static com.herron.exchange.common.api.common.enums.OrderTypeEnum.LIMIT;
import static com.herron.exchange.common.api.common.enums.OrderTypeEnum.MARKET;

public class EmulationUtil {
    private static final Random RANDOM_GENERATOR = new Random(17);
    private static final AtomicLong ORDER_ID_GENERATOR = new AtomicLong(0);

    public static AddOrder mapInitialAddOrder(OrderbookData orderbookData, double price, OrderSideEnum sideEnum) {
        double volume = RANDOM_GENERATOR.nextDouble(orderbookData.minTradeVolume(), 100.0);
        return ImmutableHerronAddOrder.builder()
                .addOperationType(OrderAddOperationTypeEnum.NEW_ORDER)
                .timeOfEventMs(Instant.now().toEpochMilli())
                .orderId(String.valueOf(ORDER_ID_GENERATOR.getAndIncrement()))
                .currentVolume(Volume.create(volume))
                .initialVolume(Volume.create(volume))
                .instrumentId(orderbookData.instrument().instrumentId())
                .orderSide(sideEnum)
                .price(Price.create(price))
                .participant(Utils.generateParticipant())
                .orderType(LIMIT)
                .orderExecutionType(FILL)
                .orderbookId(orderbookData.orderbookId())
                .build();
    }

    public static AddOrder mapAddOrder(OrderbookData orderbookData, double price, OrderSideEnum sideEnum) {

        double volume = RANDOM_GENERATOR.nextDouble(orderbookData.minTradeVolume(), 100.0);

        OrderExecutionTypeEnum executionTypeEnum = FILL;
        if (RANDOM_GENERATOR.nextDouble() <= 0.05) {
            executionTypeEnum = FAK;
        } else if (RANDOM_GENERATOR.nextDouble() >= 0.95) {
            executionTypeEnum = FOK;
        }

        return ImmutableHerronAddOrder.builder()
                .addOperationType(OrderAddOperationTypeEnum.NEW_ORDER)
                .timeOfEventMs(Instant.now().toEpochMilli())
                .orderId(String.valueOf(ORDER_ID_GENERATOR.getAndIncrement()))
                .currentVolume(Volume.create(volume))
                .initialVolume(Volume.create(volume))
                .instrumentId(orderbookData.instrument().instrumentId())
                .orderSide(sideEnum)
                .price(Price.create(price))
                .participant(Utils.generateParticipant())
                .orderType(RANDOM_GENERATOR.nextDouble() < 0.01 ? MARKET : LIMIT)
                .orderExecutionType(executionTypeEnum)
                .orderbookId(orderbookData.orderbookId())
                .build();
    }
}

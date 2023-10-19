package com.herron.exchange.eventgenerator.server.emulation;

import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
import com.herron.exchange.common.api.common.api.trading.orders.LimitOrder;
import com.herron.exchange.common.api.common.api.trading.orders.MarketOrder;
import com.herron.exchange.common.api.common.api.trading.orders.Order;
import com.herron.exchange.common.api.common.enums.OrderSideEnum;
import com.herron.exchange.common.api.common.enums.TimeInForceEnum;
import com.herron.exchange.common.api.common.math.MathUtils;
import com.herron.exchange.common.api.common.messages.common.Price;
import com.herron.exchange.common.api.common.messages.common.Volume;
import com.herron.exchange.common.api.common.messages.trading.ImmutableDefaultLimitOrder;
import com.herron.exchange.common.api.common.messages.trading.ImmutableDefaultMarketOrder;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static com.herron.exchange.common.api.common.enums.OrderOperationCauseEnum.NEW_ORDER;
import static com.herron.exchange.common.api.common.enums.OrderOperationEnum.INSERT;
import static com.herron.exchange.common.api.common.enums.TimeInForceEnum.*;
import static com.herron.exchange.eventgenerator.server.utils.EventGeneratorUtils.generateParticipant;

public class EmulationUtil {
    private static final Random RANDOM_GENERATOR = new Random(17);
    private static final AtomicLong ORDER_ID_GENERATOR = new AtomicLong(0);

    public static LimitOrder mapLimitOrder(OrderbookData orderbookData, double price, OrderSideEnum sideEnum) {
        return mapLimitOrder(orderbookData, price, sideEnum, SESSION);
    }

    public static LimitOrder mapLimitOrder(OrderbookData orderbookData, double price, OrderSideEnum sideEnum, TimeInForceEnum timeInForceEnum) {
        double volume = MathUtils.roundDouble(RANDOM_GENERATOR.nextDouble(orderbookData.minTradeVolume(), 100.0), 5);
        return ImmutableDefaultLimitOrder.builder()
                .timeOfEventMs(Instant.now().toEpochMilli())
                .orderId(String.valueOf(ORDER_ID_GENERATOR.getAndIncrement()))
                .currentVolume(Volume.create(volume))
                .initialVolume(Volume.create(volume))
                .instrumentId(orderbookData.instrument().instrumentId())
                .orderSide(sideEnum)
                .price(Price.create(price))
                .participant(generateParticipant())
                .timeInForce(timeInForceEnum)
                .orderOperation(INSERT)
                .orderOperationCause(NEW_ORDER)
                .orderbookId(orderbookData.orderbookId())
                .build();
    }

    public static MarketOrder mapMarketOrder(OrderbookData orderbookData, OrderSideEnum sideEnum) {
        double volume = MathUtils.roundDouble(RANDOM_GENERATOR.nextDouble(orderbookData.minTradeVolume(), 100.0), 5);
        return ImmutableDefaultMarketOrder.builder()
                .timeOfEventMs(Instant.now().toEpochMilli())
                .orderId(String.valueOf(ORDER_ID_GENERATOR.getAndIncrement()))
                .currentVolume(Volume.create(volume))
                .initialVolume(Volume.create(volume))
                .instrumentId(orderbookData.instrument().instrumentId())
                .orderSide(sideEnum)
                .participant(generateParticipant())
                .orderOperation(INSERT)
                .orderOperationCause(NEW_ORDER)
                .orderbookId(orderbookData.orderbookId())
                .build();
    }

    public static Order mapAddOrder(OrderbookData orderbookData, double price, OrderSideEnum sideEnum) {

        TimeInForceEnum timeInForceEnum = SESSION;
        if (RANDOM_GENERATOR.nextDouble() <= 0.05) {
            timeInForceEnum = FAK;
        } else if (RANDOM_GENERATOR.nextDouble() >= 0.95) {
            timeInForceEnum = FOK;
        }

        return RANDOM_GENERATOR.nextDouble() < 0.01 ?
                mapMarketOrder(orderbookData, sideEnum) : mapLimitOrder(orderbookData, price, sideEnum, timeInForceEnum);
    }
}

package com.herron.exchange.eventgenerator.server.emulation;

import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
import com.herron.exchange.common.api.common.api.trading.orders.AddOrder;
import com.herron.exchange.common.api.common.cache.ReferenceDataCache;
import com.herron.exchange.common.api.common.enums.KafkaTopicEnum;
import com.herron.exchange.common.api.common.enums.OrderSideEnum;
import com.herron.exchange.common.api.common.kafka.KafkaBroadcastHandler;
import com.herron.exchange.common.api.common.messages.common.PartitionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.herron.exchange.common.api.common.enums.OrderSideEnum.ASK;
import static com.herron.exchange.common.api.common.enums.OrderSideEnum.BID;
import static com.herron.exchange.eventgenerator.server.emulation.EmulationUtil.mapAddOrder;
import static com.herron.exchange.eventgenerator.server.emulation.EmulationUtil.mapInitialAddOrder;

public class OrderEventEmulatorBroadcaster {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventEmulatorBroadcaster.class);
    public static final PartitionKey KEY = new PartitionKey(KafkaTopicEnum.ORDER_DATA, 0);
    private static final Random RANDOM_GENERATOR = new Random(17);
    private static final int PRICE_LEVELS_PER_SIDE = 10;
    private static final double ORDER_TRADE_RATIO = 1 / 10.0;
    private final KafkaBroadcastHandler broadcastHandler;
    private final CountDownLatch emulationCountdownLatch;
    private final PreviousSettlementPriceConsumer settlementPriceConsumer;
    private final Thread emulatorThread;

    public OrderEventEmulatorBroadcaster(KafkaBroadcastHandler broadcastHandler,
                                         CountDownLatch emulationCountdownLatch,
                                         PreviousSettlementPriceConsumer settlementPriceConsumer) {
        this.broadcastHandler = broadcastHandler;
        this.emulationCountdownLatch = emulationCountdownLatch;
        this.settlementPriceConsumer = settlementPriceConsumer;
        this.emulatorThread = new Thread(this::runSimulation, this.getClass().getSimpleName());
    }

    public void init() {
        emulatorThread.start();
    }

    private void runSimulation() {
        try {
            emulationCountdownLatch.await();
        } catch (InterruptedException ignore) {
            //Ignore
        }

        LOGGER.info("Init emulation.");

        Map<OrderbookData, AddOrder> orderbookToInitialOrder = createAndBroadcastInitialOrders();
        List<OrderbookData> orderbookDataList = new ArrayList<>(orderbookToInitialOrder.keySet());

        while (true) {
            generateEvent(orderbookToInitialOrder, orderbookDataList);
        }
    }

    private Map<OrderbookData, AddOrder> createAndBroadcastInitialOrders() {
        var instrumentIdToSettlementPrice = settlementPriceConsumer.getInstrumentIdToPreviousSettlementPrices();
        Map<OrderbookData, AddOrder> orderbookToInitialOrder = new HashMap<>();
        for (var orderbookData : ReferenceDataCache.getCache().getOrderbookData()) {
            if (!instrumentIdToSettlementPrice.containsKey(orderbookData.instrument().instrumentId())) {
                continue;
            }

            var startPrice = instrumentIdToSettlementPrice.get(orderbookData.instrument().instrumentId()).price().getValue();
            var addOrder = mapInitialAddOrder(orderbookData, startPrice, RANDOM_GENERATOR.nextBoolean() ? BID : ASK);
            orderbookToInitialOrder.put(orderbookData, addOrder);
            broadcastHandler.broadcastMessage(KEY, addOrder);
        }
        return orderbookToInitialOrder;
    }

    private void generateEvent(Map<OrderbookData, AddOrder> orderbookToInitialOrder, List<OrderbookData> orderbookDataList) {
        var instrumentIdToSettlementPrice = settlementPriceConsumer.getInstrumentIdToPreviousSettlementPrices();
        var orderbookData = orderbookDataList.get(RANDOM_GENERATOR.nextInt(orderbookDataList.size()));
        var initialOrder = orderbookToInitialOrder.get(orderbookData);
        if (!instrumentIdToSettlementPrice.containsKey(orderbookData.instrument().instrumentId())) {
            return;
        }

        var startPrice = instrumentIdToSettlementPrice.get(orderbookData.instrument().instrumentId()).price().getValue();
        var price = startPrice + (orderbookData.tickSize() * RANDOM_GENERATOR.nextInt(0, PRICE_LEVELS_PER_SIDE));

        OrderSideEnum side = RANDOM_GENERATOR.nextBoolean() ? BID : ASK;
        if (initialOrder.orderSide() == side && initialOrder.orderSide() == BID && price > startPrice) {
            side = RANDOM_GENERATOR.nextDouble() <= ORDER_TRADE_RATIO ? BID : ASK;
        } else if (initialOrder.orderSide() == side && initialOrder.orderSide() == ASK && price < startPrice) {
            side = RANDOM_GENERATOR.nextDouble() <= ORDER_TRADE_RATIO ? ASK : BID;
        }

        var addOrder = mapAddOrder(orderbookData, price, side);
        broadcastHandler.broadcastMessage(KEY, addOrder);
    }
}

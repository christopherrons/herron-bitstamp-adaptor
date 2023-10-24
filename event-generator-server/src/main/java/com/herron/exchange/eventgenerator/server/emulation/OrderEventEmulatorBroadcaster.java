package com.herron.exchange.eventgenerator.server.emulation;

import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
import com.herron.exchange.common.api.common.api.trading.Order;
import com.herron.exchange.common.api.common.cache.ReferenceDataCache;
import com.herron.exchange.common.api.common.enums.KafkaTopicEnum;
import com.herron.exchange.common.api.common.enums.OrderSideEnum;
import com.herron.exchange.common.api.common.kafka.KafkaBroadcastHandler;
import com.herron.exchange.common.api.common.messages.common.PartitionKey;
import com.herron.exchange.common.api.common.wrappers.ThreadWrapper;
import com.herron.exchange.eventgenerator.server.consumers.PreviousSettlementPriceConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.herron.exchange.common.api.common.enums.OrderSideEnum.ASK;
import static com.herron.exchange.common.api.common.enums.OrderSideEnum.BID;
import static com.herron.exchange.eventgenerator.server.emulation.EmulationUtil.mapAddOrder;
import static com.herron.exchange.eventgenerator.server.emulation.EmulationUtil.mapLimitOrder;

public class OrderEventEmulatorBroadcaster {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventEmulatorBroadcaster.class);
    public static final PartitionKey KEY = new PartitionKey(KafkaTopicEnum.USER_ORDER_DATA, 0);
    private static final Random RANDOM_GENERATOR = new Random(17);
    private static final int PRICE_LEVELS_PER_SIDE = 10;
    private static final int MAX_EVENTS_PER_SECOND = 5000;
    private static final double ORDER_TRADE_RATIO = 1 / 20.0;
    private final KafkaBroadcastHandler broadcastHandler;
    private final PreviousSettlementPriceConsumer settlementPriceConsumer;
    private final ExecutorService service;

    public OrderEventEmulatorBroadcaster(KafkaBroadcastHandler broadcastHandler,
                                         PreviousSettlementPriceConsumer settlementPriceConsumer) {
        this.broadcastHandler = broadcastHandler;
        this.settlementPriceConsumer = settlementPriceConsumer;
        this.service = Executors.newSingleThreadExecutor(new ThreadWrapper("EMULATION"));
    }

    public void init() {
        service.execute(this::runSimulation);
    }

    private void runSimulation() {
        LOGGER.info("Init emulation.");
        Map<OrderbookData, Order> orderbookToInitialOrder = createAndBroadcastInitialOrders();
        List<OrderbookData> orderbookDataList = new ArrayList<>(orderbookToInitialOrder.keySet());

        runSimulation(orderbookToInitialOrder, orderbookDataList);
    }

    private void runSimulation(Map<OrderbookData, Order> orderbookToInitialOrder, List<OrderbookData> orderbookDataList) {
        long nrOfEventsGenerated = 0;
        while (nrOfEventsGenerated < Long.MAX_VALUE) {
            if (nrOfEventsGenerated % MAX_EVENTS_PER_SECOND == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {

                }
            }
            generateEvent(orderbookToInitialOrder, orderbookDataList);
            nrOfEventsGenerated++;
        }
    }

    private Map<OrderbookData, Order> createAndBroadcastInitialOrders() {
        var instrumentIdToSettlementPrice = settlementPriceConsumer.getInstrumentIdToPreviousSettlementPrices();
        Map<OrderbookData, Order> orderbookToInitialOrder = new HashMap<>();
        for (var orderbookData : ReferenceDataCache.getCache().getOrderbookData()) {
            if (!instrumentIdToSettlementPrice.containsKey(orderbookData.instrument().instrumentId())) {
                continue;
            }

            var startPrice = instrumentIdToSettlementPrice.get(orderbookData.instrument().instrumentId()).price();
            var addOrder = mapLimitOrder(orderbookData, startPrice, RANDOM_GENERATOR.nextBoolean() ? BID : ASK);
            orderbookToInitialOrder.put(orderbookData, addOrder);
            broadcastHandler.broadcastMessage(KEY, addOrder);
        }
        return orderbookToInitialOrder;
    }

    private void generateEvent(Map<OrderbookData, Order> orderbookToInitialOrder, List<OrderbookData> orderbookDataList) {
        var instrumentIdToSettlementPrice = settlementPriceConsumer.getInstrumentIdToPreviousSettlementPrices();
        var orderbookData = orderbookDataList.get(RANDOM_GENERATOR.nextInt(orderbookDataList.size()));
        var initialOrder = orderbookToInitialOrder.get(orderbookData);
        if (!instrumentIdToSettlementPrice.containsKey(orderbookData.instrument().instrumentId())) {
            return;
        }

        var startPrice = instrumentIdToSettlementPrice.get(orderbookData.instrument().instrumentId()).price();
        var price = startPrice.add(orderbookData.tickSize() * RANDOM_GENERATOR.nextInt(0, PRICE_LEVELS_PER_SIDE));

        OrderSideEnum side = RANDOM_GENERATOR.nextBoolean() ? BID : ASK;
        if (initialOrder.orderSide() == side && initialOrder.orderSide() == BID && price.gt(startPrice)) {
            side = RANDOM_GENERATOR.nextDouble() <= ORDER_TRADE_RATIO ? BID : ASK;
        } else if (initialOrder.orderSide() == side && initialOrder.orderSide() == ASK && price.lt(startPrice)) {
            side = RANDOM_GENERATOR.nextDouble() <= ORDER_TRADE_RATIO ? ASK : BID;
        }

        var addOrder = mapAddOrder(orderbookData, price, side);
        broadcastHandler.broadcastMessage(KEY, addOrder);
    }
}

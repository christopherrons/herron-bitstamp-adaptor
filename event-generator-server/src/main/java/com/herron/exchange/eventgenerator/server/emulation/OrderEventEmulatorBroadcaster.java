package com.herron.exchange.eventgenerator.server.emulation;

import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
import com.herron.exchange.common.api.common.api.trading.Order;
import com.herron.exchange.common.api.common.cache.ReferenceDataCache;
import com.herron.exchange.common.api.common.enums.KafkaTopicEnum;
import com.herron.exchange.common.api.common.enums.OrderSideEnum;
import com.herron.exchange.common.api.common.kafka.KafkaBroadcastHandler;
import com.herron.exchange.common.api.common.messages.common.PartitionKey;
import com.herron.exchange.common.api.common.messages.common.Price;
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
    private static final PartitionKey KEY = new PartitionKey(KafkaTopicEnum.USER_ORDER_DATA, 0);
    private static final Random RANDOM_GENERATOR = new Random(17);
    private static final int PRICE_LEVELS_PER_SIDE = 15;
    private static final double ORDER_TRADE_RATIO = 1 / 20.0;
    private final KafkaBroadcastHandler broadcastHandler;
    private final PreviousSettlementPriceConsumer settlementPriceConsumer;
    private final ExecutorService service;
    private final int maxEventsPerSecond;
    private final Map<OrderbookData, TreeSet<Price>> orderbookToBidPrices = new HashMap<>();
    private final Map<OrderbookData, TreeSet<Price>> orderbookToAskPrices = new HashMap<>();


    public OrderEventEmulatorBroadcaster(int maxEventsPerSecond,
                                         KafkaBroadcastHandler broadcastHandler,
                                         PreviousSettlementPriceConsumer settlementPriceConsumer) {
        this.maxEventsPerSecond = maxEventsPerSecond;
        this.broadcastHandler = broadcastHandler;
        this.settlementPriceConsumer = settlementPriceConsumer;
        this.service = Executors.newSingleThreadExecutor(new ThreadWrapper("EMULATION"));
    }

    public void init() {
        service.execute(this::runEmulation);
    }

    private void runEmulation() {
        LOGGER.info("Init emulation.");
        Map<OrderbookData, Order> orderbookToInitialOrder = createAndBroadcastInitialOrders();
        List<OrderbookData> orderbookDataList = new ArrayList<>(orderbookToInitialOrder.keySet());

        runEmulation(orderbookDataList);
    }

    private void runEmulation(List<OrderbookData> orderbookDataList) {
        long nrOfEventsGenerated = 0;
        long startTime = System.currentTimeMillis();
        while (nrOfEventsGenerated < Long.MAX_VALUE) {
            generateEvent(orderbookDataList);
            nrOfEventsGenerated++;

            if (nrOfEventsGenerated % maxEventsPerSecond == 0) {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;
                long sleepTime = 1000 - elapsedTime;
                if (sleepTime > 0) {
                    long end = currentTime + sleepTime;
                    while (System.currentTimeMillis() < end) ;
                }
                startTime = System.currentTimeMillis();
            }
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
            var side = RANDOM_GENERATOR.nextBoolean() ? BID : ASK;
            switch (side) {
                case BID ->
                        orderbookToBidPrices.computeIfAbsent(orderbookData, k -> new TreeSet<>(Comparator.comparing(Price::getValue).reversed())).add(startPrice);
                case ASK -> orderbookToAskPrices.computeIfAbsent(orderbookData, k -> new TreeSet<>(Comparator.comparing(Price::getValue))).add(startPrice);
            }
            var addOrder = mapLimitOrder(orderbookData, startPrice, side);
            orderbookToInitialOrder.put(orderbookData, addOrder);
            broadcastHandler.broadcastMessage(KEY, addOrder);
        }
        return orderbookToInitialOrder;
    }

    private void generateEvent(List<OrderbookData> orderbookDataList) {
        var instrumentIdToSettlementPrice = settlementPriceConsumer.getInstrumentIdToPreviousSettlementPrices();
        var orderbookData = orderbookDataList.get(RANDOM_GENERATOR.nextInt(orderbookDataList.size()));
        if (!instrumentIdToSettlementPrice.containsKey(orderbookData.instrument().instrumentId())) {
            return;
        }

        var bidPrices = orderbookToBidPrices.computeIfAbsent(orderbookData, k -> new TreeSet<>(Comparator.comparing(Price::getValue).reversed()));
        var askPrices = orderbookToAskPrices.computeIfAbsent(orderbookData, k -> new TreeSet<>(Comparator.comparing(Price::getValue)));
        var newBidPrice = bidPrices.isEmpty()
                ? askPrices.first().add(orderbookData.tickSize() * RANDOM_GENERATOR.nextInt(0, PRICE_LEVELS_PER_SIDE))
                : bidPrices.first().add(orderbookData.tickSize() * RANDOM_GENERATOR.nextInt(0, PRICE_LEVELS_PER_SIDE));
        var newAskPrice = askPrices.isEmpty()
                ? bidPrices.first().add(orderbookData.tickSize() * RANDOM_GENERATOR.nextInt(0, PRICE_LEVELS_PER_SIDE))
                : askPrices.first().add(orderbookData.tickSize() * RANDOM_GENERATOR.nextInt(0, PRICE_LEVELS_PER_SIDE));

        OrderSideEnum side = RANDOM_GENERATOR.nextBoolean() ? BID : ASK;
        if ((side == BID && !askPrices.isEmpty() && newBidPrice.geq(askPrices.first())) || (side == ASK && !bidPrices.isEmpty() && newAskPrice.leq(bidPrices.first()))) {
            if (RANDOM_GENERATOR.nextDouble() <= ORDER_TRADE_RATIO) {
                switch (side) {
                    case BID -> askPrices.removeIf(price -> price.leq(newBidPrice));
                    case ASK -> bidPrices.removeIf(price -> price.geq(newAskPrice));
                }
            } else {
                return;
            }
        }

        var addOrder = switch (side) {
            case BID -> {
                bidPrices.add(newBidPrice);
                yield mapAddOrder(orderbookData, newBidPrice, side);
            }
            case ASK -> {
                askPrices.add(newAskPrice);
                yield mapAddOrder(orderbookData, newAskPrice, side);
            }
        };
        broadcastHandler.broadcastMessage(KEY, addOrder);
    }
}

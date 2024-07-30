package com.herron.exchange.eventgenerator.server.emulation;

import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
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

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.herron.exchange.common.api.common.enums.OrderSideEnum.ASK;
import static com.herron.exchange.common.api.common.enums.OrderSideEnum.BID;
import static com.herron.exchange.eventgenerator.server.emulation.EmulationUtil.mapAddOrder;

public class OrderEventEmulatorBroadcaster {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventEmulatorBroadcaster.class);
    private static final PartitionKey KEY = new PartitionKey(KafkaTopicEnum.USER_ORDER_DATA, 0);
    private static final Random RANDOM_GENERATOR = new Random(17);
    private static final double MIN_ORDER_TRADE_RATIO = 1 / 50.0;
    private static final double MAX_ORDER_TRADE_RATIO = 1 / 20.0;
    private static final double SHOCK_RATIO = 1 / 10.0;
    private static final int PRICE_LEVELS = 15;
    private final KafkaBroadcastHandler broadcastHandler;
    private final PreviousSettlementPriceConsumer settlementPriceConsumer;
    private final ExecutorService service;
    private final int maxEventsPerSecond;


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
        Set<PriceGenerator> priceGenerators = createPriceGenerator();

        runEmulation(priceGenerators.stream().toList());
    }

    private void runEmulation(List<PriceGenerator> priceGenerators) {
        long nrOfEventsGenerated = 0;
        long startTime = System.currentTimeMillis();
        while (nrOfEventsGenerated < Long.MAX_VALUE) {
            var priceGenerator = priceGenerators.get(RANDOM_GENERATOR.nextInt(priceGenerators.size()));
            if (RANDOM_GENERATOR.nextDouble() <= SHOCK_RATIO) {
                priceGenerator.shockPrice();
            }
            generateEvent(priceGenerator);
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

    private Set<PriceGenerator> createPriceGenerator() {
        Set<PriceGenerator> priceGenerators = new HashSet<>();
        var instrumentIdToSettlementPrice = settlementPriceConsumer.getInstrumentIdToPreviousSettlementPrices();
        for (var orderbookData : ReferenceDataCache.getCache().getOrderbookData()) {
            if (!instrumentIdToSettlementPrice.containsKey(orderbookData.instrument().instrumentId())) {
                continue;
            }

            var centerPrice = instrumentIdToSettlementPrice.get(orderbookData.instrument().instrumentId());
            var spread = Price.create(orderbookData.tickSize());
            var priceGenerator = new PriceGenerator(
                    orderbookData,
                    centerPrice.price(),
                    spread,
                    Math.min(MIN_ORDER_TRADE_RATIO, RANDOM_GENERATOR.nextDouble(MAX_ORDER_TRADE_RATIO))
            );
            priceGenerators.add(priceGenerator);

        }
        return priceGenerators;
    }

    private void generateEvent(PriceGenerator priceGenerator) {
        var instrumentIdToSettlementPrice = settlementPriceConsumer.getInstrumentIdToPreviousSettlementPrices();

        var orderbookData = priceGenerator.getOrderbookData();
        if (!instrumentIdToSettlementPrice.containsKey(orderbookData.instrument().instrumentId())) {
            return;
        }

        var price = priceGenerator.generatePrice();
        var side = priceGenerator.generateSide(price);
        var addOrder = mapAddOrder(orderbookData, price, side);

        broadcastHandler.broadcastMessage(KEY, addOrder);
    }

    private static class PriceGenerator {
        private final OrderbookData orderbookData;
        private final Price spread;
        private final double orderTradeRatio;
        private Price centerPrice;

        public PriceGenerator(OrderbookData orderbookData, Price centerPrice, Price spread, double orderTradeRatio) {
            this.orderbookData = orderbookData;
            this.centerPrice = centerPrice;
            this.spread = spread;
            this.orderTradeRatio = orderTradeRatio;
        }

        private void shockPrice() {
            centerPrice = centerPrice.add(spread.multiply(RANDOM_GENERATOR.nextInt(1, PRICE_LEVELS)).multiply(RANDOM_GENERATOR.nextBoolean() ? 1 : -1));
        }

        private Price generatePrice() {
            var level = RANDOM_GENERATOR.nextInt(PRICE_LEVELS);
            level = RANDOM_GENERATOR.nextBoolean() ? level : level * -1;
            return centerPrice.add(spread.multiply(level));
        }

        private OrderSideEnum generateSide(Price price) {
            boolean crossCenter = RANDOM_GENERATOR.nextDouble() <= orderTradeRatio;
            if (crossCenter) {
                return (price.lt(centerPrice)) ? ASK : BID;
            } else {
                return (price.lt(centerPrice)) ? BID : ASK;
            }
        }

        public OrderbookData getOrderbookData() {
            return orderbookData;
        }
    }
}

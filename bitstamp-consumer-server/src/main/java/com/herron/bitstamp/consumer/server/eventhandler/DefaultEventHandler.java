package com.herron.bitstamp.consumer.server.eventhandler;

import com.herron.bitstamp.consumer.server.api.BitstampMarketEvent;
import com.herron.bitstamp.consumer.server.api.EventHandler;
import com.herron.bitstamp.consumer.server.enums.OrderOperationEnum;
import com.herron.bitstamp.consumer.server.enums.TopicEnum;
import com.herron.bitstamp.consumer.server.messages.BitstampBroadcastMessage;
import com.herron.bitstamp.consumer.server.messages.BitstampOrder;
import com.herron.bitstamp.consumer.server.messages.BitstampTrade;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultEventHandler implements EventHandler {
    private static final int TIME_IN_QUEUE_MS = 10000;
    private final EventLogger eventLogging;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Set<String> orderIds = new HashSet<>();
    private final Map<String, TimeBoundPriorityQueue<BitstampMarketEvent>> idToEventPriorityQueue = new ConcurrentHashMap<>();

    private final AtomicLong sequenceNumberHandler = new AtomicLong();

    public DefaultEventHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        this(kafkaTemplate, new EventLogger());
    }

    public DefaultEventHandler(KafkaTemplate<String, Object> kafkaTemplate, EventLogger eventLogging) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventLogging = eventLogging;
    }

    public void handleEvent(BitstampMarketEvent events) {
        TimeBoundPriorityQueue<BitstampMarketEvent> queue = findOrCreateQueue(events);
        handleEvents(queue.addItemThenPurge(events));
    }

    public void handleEvents(List<BitstampMarketEvent> events) {
        for (var event : events) {
            switch (event.getEventTypeEnum()) {
                case ORDER -> handleOrder((BitstampOrder) event);
                case TRADE -> handleTrade((BitstampTrade) event);
                default -> publish(event);

            }
        }
    }

    private void handleOrder(BitstampOrder order) {
        if (order.orderOperation() == OrderOperationEnum.CREATE) {//|| (order.orderOperation() == OrderOperationEnum.UPDATE && orderIds.contains(order.orderId()))) {
            if (order.orderType().equals("limit") && order.price() > 99_999_999.0) {
                return;
            }
            // We only handle updates if we have received the initial create
            // orderIds.add(order.orderId());
            publish(order);
        }
    }

    private void handleTrade(BitstampTrade trade) {
        // We do not want to process trades where we have never seen the order
        if (orderIds.contains(trade.askOrderId()) && orderIds.contains(trade.buyOrderId())) {
            publish(trade);
        }
    }

    private void publish(BitstampMarketEvent event) {
        var broadCast = new BitstampBroadcastMessage(event, event.getMessageType(), sequenceNumberHandler.getAndIncrement(), Instant.now().toEpochMilli());
        kafkaTemplate.send(TopicEnum.BITSTAMP_MARKET_DATA.getTopicName(), broadCast.getMessageType(), broadCast);
        eventLogging.logEvent();
    }

    private TimeBoundPriorityQueue<BitstampMarketEvent> findOrCreateQueue(final BitstampMarketEvent event) {
        return idToEventPriorityQueue.computeIfAbsent(
                event.getId(),
                e -> new TimeBoundPriorityQueue<>(TIME_IN_QUEUE_MS, new EventComparator<>())
        );
    }

}


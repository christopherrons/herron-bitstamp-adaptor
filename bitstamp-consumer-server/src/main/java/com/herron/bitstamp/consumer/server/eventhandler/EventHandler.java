package com.herron.bitstamp.consumer.server.eventhandler;

import com.herron.bitstamp.consumer.server.api.BitstampMarketEvent;
import com.herron.bitstamp.consumer.server.enums.OrderOperationEnum;
import com.herron.bitstamp.consumer.server.model.BitstampOrder;
import com.herron.bitstamp.consumer.server.model.BitstampTrade;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventHandler {
    private static final int TIME_IN_QUEUE_MS = 10000;
    private final EventLogger eventLogging;
    private final Set<String> orderIds = new HashSet<>();
    private final Map<String, TimeBoundPriorityQueue<BitstampMarketEvent>> idToEventPriorityQueue = new ConcurrentHashMap<>();

    public EventHandler() {
        this(new EventLogger());
    }

    public EventHandler(EventLogger eventLogging) {
        this.eventLogging = eventLogging;
    }

    public void handleEvent(BitstampMarketEvent messages) {
        TimeBoundPriorityQueue<BitstampMarketEvent> queue = findOrCreateQueue(messages);
        handleEvents(queue.addItemThenPurge(messages));
    }

    public void handleEvents(List<BitstampMarketEvent> events) {
        for (var event : events) {
            switch (event.getEventTypeEnum()) {
                case ORDER -> {
                    BitstampOrder order = ((BitstampOrder) event);
                    if (order.orderOperation() == OrderOperationEnum.CREATE ||
                            (order.orderOperation() == OrderOperationEnum.UPDATE && orderIds.contains(order.orderId()))) {
                        // We only handle updates if we have received the initial create
                        orderIds.add(order.orderId());
                        eventLogging.logEvent();
                    }
                }
                case TRADE -> {
                    BitstampTrade trade = ((BitstampTrade) event);
                    // We do not want to process trades where we have never seen the order
                    if (orderIds.contains(trade.askOrderId()) && orderIds.contains(trade.buyOrderId())) {
                        eventLogging.logEvent();
                    }
                }
                default -> eventLogging.logEvent();
            }
        }
    }

    private TimeBoundPriorityQueue<BitstampMarketEvent> findOrCreateQueue(final BitstampMarketEvent event) {
        return idToEventPriorityQueue.computeIfAbsent(
                event.getId(),
                e -> new TimeBoundPriorityQueue<>(TIME_IN_QUEUE_MS, new EventComparator<>())
        );
    }

}


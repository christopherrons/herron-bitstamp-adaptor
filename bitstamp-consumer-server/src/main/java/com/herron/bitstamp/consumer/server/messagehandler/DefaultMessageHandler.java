package com.herron.bitstamp.consumer.server.messagehandler;

import com.herron.bitstamp.consumer.server.api.MessageHandler;
import com.herron.bitstamp.consumer.server.messages.BitstampOrder;
import com.herron.bitstamp.consumer.server.messages.BitstampTrade;
import com.herron.exchange.common.api.common.api.Message;
import com.herron.exchange.common.api.common.comparator.MessageComparator;
import com.herron.exchange.common.api.common.datastructures.TimeBoundPriorityQueue;
import com.herron.exchange.common.api.common.enums.OrderOperationEnum;
import com.herron.exchange.common.api.common.enums.OrderTypeEnum;
import com.herron.exchange.common.api.common.logging.EventLogger;
import com.herron.exchange.common.api.common.messages.HerronBroadcastMessage;
import com.herron.exchange.common.api.common.model.PartitionKey;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultMessageHandler implements MessageHandler {
    private static final int TIME_IN_QUEUE_MS = 10000;
    private final EventLogger eventLogging;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Set<String> orderIds = new HashSet<>();
    private final Map<PartitionKey, TimeBoundPriorityQueue<Message>> partitionKeyToEventPriorityQueue = new ConcurrentHashMap<>();
    private final Map<PartitionKey, AtomicLong> partitionKeyToSequenceNumberHandler = new ConcurrentHashMap<>();

    public DefaultMessageHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        this(kafkaTemplate, new EventLogger());
    }

    public DefaultMessageHandler(KafkaTemplate<String, Object> kafkaTemplate, EventLogger eventLogging) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventLogging = eventLogging;
    }

    public void handleMessage(Message message, PartitionKey partitionKey) {
        if (message == null) {
            return;
        }
        TimeBoundPriorityQueue<Message> queue = findOrCreateQueue(partitionKey);
        handleMessages(queue.addItemThenPurge(message), partitionKey);
    }

    public void handleMessages(List<Message> messages, PartitionKey partitionKey) {
        for (var message : messages) {
            if (message == null) {
                continue;
            }

            if (message instanceof BitstampOrder order) {
                handleOrder(order, partitionKey);
            } else if (message instanceof BitstampTrade trade) {
                handleTrade(trade, partitionKey);
            } else {
                publish(message, partitionKey);
            }
        }
    }

    private void handleOrder(BitstampOrder order, PartitionKey partitionKey) {
        if (order.orderOperation() == OrderOperationEnum.CREATE) {//|| (order.orderOperation() == OrderOperationEnum.UPDATE && orderIds.contains(order.orderId()))) {
            if (order.orderType() == OrderTypeEnum.LIMIT && order.price() > 99_999_999.0) {
                return;
            }
            // We only handle updates if we have received the initial create
            // orderIds.add(order.orderId());
            publish(order, partitionKey);
        }
    }

    private void handleTrade(BitstampTrade trade, PartitionKey partitionKey) {
        // We do not want to process trades where we have never seen the order
        if (orderIds.contains(trade.askOrderId()) && orderIds.contains(trade.buyOrderId())) {
            publish(trade, partitionKey);
        }
    }

    private synchronized void publish(Message message, PartitionKey partitionKey) {
        var broadCast = new HerronBroadcastMessage(message, message.messageType().getMessageTypeId(), getSequenceNumber(partitionKey), Instant.now().toEpochMilli());
        kafkaTemplate.send(partitionKey.topicEnum().getTopicName(), partitionKey.partitionId(), broadCast.messageType().getMessageTypeId(), broadCast);
        eventLogging.logEvent();
    }

    private TimeBoundPriorityQueue<Message> findOrCreateQueue(PartitionKey partitionKey) {
        return partitionKeyToEventPriorityQueue.computeIfAbsent(
                partitionKey,
                e -> new TimeBoundPriorityQueue<>(TIME_IN_QUEUE_MS, new MessageComparator<>())
        );
    }

    private long getSequenceNumber(PartitionKey partitionKey) {
        return partitionKeyToSequenceNumberHandler.computeIfAbsent(partitionKey, k -> new AtomicLong(1)).getAndIncrement();
    }

}


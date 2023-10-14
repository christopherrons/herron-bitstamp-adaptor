package com.herron.event.generator.server.streaming;

import com.herron.exchange.common.api.common.api.Event;
import com.herron.exchange.common.api.common.api.trading.orders.AddOrder;
import com.herron.exchange.common.api.common.datastructures.TimeBoundBlockingPriorityQueue;
import com.herron.exchange.common.api.common.enums.KafkaTopicEnum;
import com.herron.exchange.common.api.common.kafka.KafkaBroadcastHandler;
import com.herron.exchange.common.api.common.model.PartitionKey;
import com.herron.exchange.integrations.generator.bitstamp.api.BitstampMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BitsampBroadcaster {
    private static final Logger LOGGER = LoggerFactory.getLogger(BitsampBroadcaster.class);
    private static final int TIME_IN_QUEUE_MS = 10000;
    private static final PartitionKey KEY = new PartitionKey(KafkaTopicEnum.HERRON_ORDER_DATA, 0);
    private final TimeBoundBlockingPriorityQueue<Event> eventPriorityQueue = new TimeBoundBlockingPriorityQueue<>(TIME_IN_QUEUE_MS);
    private final KafkaBroadcastHandler broadcastHandler;

    public BitsampBroadcaster(KafkaBroadcastHandler broadcastHandler) {
        this.broadcastHandler = broadcastHandler;
    }

    public void handleMessage(BitstampMessage bitstampMessage) {
        if (bitstampMessage == null) {
            return;
        }

        var message = BitstampUtil.mapMessage(bitstampMessage);

        try {
            var messages = eventPriorityQueue.addItemThenPurge(message);
            handleEvent(messages);
        } catch (Exception e) {
            LOGGER.warn("Unable to handle message: {}. {}", message, e);
        }
    }

    private void handleEvent(List<Event> messages) {
        for (var message : messages) {
            if (message == null) {
                continue;
            }

            if (message instanceof AddOrder order) {
                // Since we have our own trading engine we only handle add orders
                handleOrder(order);
            }
        }
    }

    private void handleOrder(AddOrder order) {
        if (order.currentVolume().volume() <= 0 || order.price().price() <= 0) {
            return;
        }

        broadcastHandler.broadcastMessage(KEY, order);
    }
}


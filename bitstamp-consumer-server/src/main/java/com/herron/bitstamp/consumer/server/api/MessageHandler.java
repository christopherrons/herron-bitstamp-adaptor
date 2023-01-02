package com.herron.bitstamp.consumer.server.api;

import com.herron.exchange.common.api.common.api.Message;
import com.herron.exchange.common.api.common.model.PartitionKey;

import java.util.List;

public interface MessageHandler {

    void handleMessage(Message messages, PartitionKey partitionKey);

    void handleMessages(List<Message> events, PartitionKey partitionKey);
}

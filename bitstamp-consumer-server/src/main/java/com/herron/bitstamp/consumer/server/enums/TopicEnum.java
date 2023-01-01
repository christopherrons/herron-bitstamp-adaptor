package com.herron.bitstamp.consumer.server.enums;

public enum TopicEnum {
    BITSTAMP_MARKET_DATA("bitstamp-market-data");

    private final String topicName;

    TopicEnum(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }

    public static int getPartition(String currency) {
        return currency.contains("eur") ? 0 : 1;
    }
}

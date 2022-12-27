package com.herron.bitstamp.consumer.server.utils;

public class BitstampUtils {

    public static String createInstrumentId(String channel) {
        return String.format("stock_%s", channel);
    }

    public static String createOrderbookId(String channel) {
        return String.format("bitstamp_stock-%s", channel);
    }
}

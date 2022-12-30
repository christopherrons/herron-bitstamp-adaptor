package com.herron.bitstamp.consumer.server.utils;

import java.util.List;
import java.util.Random;

public class BitstampUtils {

    private static final List<String> ORDER_EXECUTION_TYPES = List.of("fill-or-kill", "fill-and-kill", "fill");
    private static final List<String> ORDER_TYPES = List.of("market", "limit");
    private static final Random RANDOM_UNIFORM = new Random();

    public static String createInstrumentId(String channel) {
        return String.format("stock_%s", channel);
    }

    public static String createOrderbookId(String channel) {
        return String.format("bitstamp_stock_%s", channel);
    }

    public static String generateOrderType() {
        return ORDER_TYPES.get(RANDOM_UNIFORM.nextInt(ORDER_TYPES.size()));
    }

    public static String generateOrderExecutionType() {
        return ORDER_EXECUTION_TYPES.get(RANDOM_UNIFORM.nextInt(ORDER_EXECUTION_TYPES.size()));
    }

}

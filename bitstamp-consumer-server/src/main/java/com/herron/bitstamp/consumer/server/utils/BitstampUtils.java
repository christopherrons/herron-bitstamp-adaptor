package com.herron.bitstamp.consumer.server.utils;

import com.github.javafaker.Faker;
import com.herron.exchange.common.api.common.enums.KafkaTopicEnum;
import com.herron.exchange.common.api.common.enums.OrderExecutionTypeEnum;
import com.herron.exchange.common.api.common.enums.OrderTypeEnum;
import com.herron.exchange.common.api.common.model.Participant;
import com.herron.exchange.common.api.common.model.PartitionKey;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class BitstampUtils {
    private static final Faker NAME_FAKER = new Faker();
    private static final List<String> USER_POOL = IntStream.range(0, 2000).mapToObj(k -> generateUser()).toList();

    private static final List<OrderExecutionTypeEnum> ORDER_EXECUTION_TYPES = List.of(OrderExecutionTypeEnum.FOK, OrderExecutionTypeEnum.FAK, OrderExecutionTypeEnum.FILL);
    private static final List<OrderTypeEnum> ORDER_TYPES = List.of(OrderTypeEnum.MARKET, OrderTypeEnum.LIMIT);
    private static final Random RANDOM_UNIFORM = new Random();

    public static String createInstrumentId(String channel) {
        return String.format("stock_%s", channel);
    }

    public static String createOrderbookId(String channel) {
        return String.format("bitstamp_stock_%s", channel);
    }

    public static OrderTypeEnum generateOrderType() {
        return ORDER_TYPES.get(RANDOM_UNIFORM.nextInt(ORDER_TYPES.size()));
    }

    public static OrderExecutionTypeEnum generateOrderExecutionType() {
        return ORDER_EXECUTION_TYPES.get(RANDOM_UNIFORM.nextInt(ORDER_EXECUTION_TYPES.size()));
    }

    public static String generateUser() {
        return String.format("%s%s", NAME_FAKER.funnyName().name(), NAME_FAKER.name().lastName());
    }

    public static String getUserFromPool() {
        return USER_POOL.get(RANDOM_UNIFORM.nextInt(0, USER_POOL.size()));
    }

    public static Participant generateParticipant() {
        return new Participant("Bistamp", generateUser());
    }

    public static PartitionKey getPartitionKey(String channel) {
        return new PartitionKey(KafkaTopicEnum.BITSTAMP_MARKET_DATA, channel.contains("eur") ? 0 : 1);
    }

}

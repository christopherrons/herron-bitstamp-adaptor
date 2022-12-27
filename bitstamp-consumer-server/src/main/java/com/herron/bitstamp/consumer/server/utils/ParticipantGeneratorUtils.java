package com.herron.bitstamp.consumer.server.utils;

import com.github.javafaker.Faker;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class ParticipantGeneratorUtils {
    private static final Faker NAME_FAKER = new Faker();
    private static final Random RANDOM_UNIFORM = new Random();
    private static final List<String> USER_POOL = IntStream.range(0, 2000).mapToObj(k -> generateUser()).toList();

    private ParticipantGeneratorUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String generateUser() {
        return String.format("%s%s", NAME_FAKER.funnyName().name(), NAME_FAKER.name().lastName());
    }

    public static String getUserFromPool() {
        return USER_POOL.get(RANDOM_UNIFORM.nextInt(0, USER_POOL.size()));
    }
}

package com.herron.exchange.eventgenerator.server.utils;

import com.github.javafaker.Faker;
import com.herron.exchange.common.api.common.messages.common.Participant;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class EventGeneratorUtils {

    private static final Faker NAME_FAKER = new Faker();
    private static final List<String> USER_POOL = IntStream.range(0, 5000).mapToObj(k -> generateUser()).toList();

    private static final Random RANDOM_UNIFORM = new Random();

    public static Participant generateParticipant() {
        return new Participant("Emulation", generateUser());
    }

    public static Participant generateParticipant(String member) {
        return new Participant(member, generateUser());
    }

    public static String generateUser() {
        return NAME_FAKER.name().fullName();
    }

    public static String getUserFromPool() {
        return USER_POOL.get(RANDOM_UNIFORM.nextInt(0, USER_POOL.size()));
    }
}

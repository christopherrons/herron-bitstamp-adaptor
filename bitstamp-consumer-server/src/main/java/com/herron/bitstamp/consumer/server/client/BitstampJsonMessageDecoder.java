package com.herron.bitstamp.consumer.server.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.herron.bitstamp.consumer.server.model.BitstampEvent;

public class BitstampJsonMessageDecoder {
    private final Class<? extends BitstampEvent> classToBeDecoded;

    public BitstampJsonMessageDecoder(Class<? extends BitstampEvent> classToBeDecoded) {
        this.classToBeDecoded = classToBeDecoded;
    }

    public BitstampEvent decodeMessage(final String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(message, classToBeDecoded);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

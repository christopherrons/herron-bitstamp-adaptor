package com.herron.bitstamp.consumer.server.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.herron.bitstamp.consumer.server.model.BitstampEventData;

public class BitstampJsonMessageDecoder {
    private final Class<? extends BitstampEventData> classToBeDecoded;

    public BitstampJsonMessageDecoder(Class<? extends BitstampEventData> classToBeDecoded) {
        this.classToBeDecoded = classToBeDecoded;
    }

    public BitstampEventData decodeMessage(final String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(message, classToBeDecoded);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

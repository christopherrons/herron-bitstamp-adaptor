package com.herron.bitstamp.consumer.server;

import com.herron.bitstamp.consumer.server.client.EventLogging;

import java.util.Map;

public class EventHandler {
    private final EventLogging eventLogging;

    public EventHandler() {
        this(new EventLogging());
    }

    public EventHandler(EventLogging eventLogging) {
        this.eventLogging = eventLogging;
    }

    public void handleEvent(String event) {
        eventLogging.logEvent();
    }

    public void handleEvent(Map<String, Object> event) {
        eventLogging.logEvent();
    }

}


package com.herron.bitstamp.consumer.server.eventhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class EventLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventLogger.class);

    private static final double MILLI_TO_SEK = 1 / 1000.0;
    private static final int MESSAGE_UPDATE_INTERVAL = 1000;
    private final AtomicLong totalNrOfEvents = new AtomicLong();
    private final Instant startTime = Instant.now();
    private final String eventLoggDescription;
    private Instant lastLogUpdateTime = Instant.now();
    private AtomicLong lastUpdateTimeNrOfEvents = new AtomicLong();

    public EventLogger() {
        this("");
    }

    public EventLogger(String eventLoggDescription) {
        this.eventLoggDescription = eventLoggDescription;
    }

    public void logEvent() {
        try {
            long currentNrOfEvents = totalNrOfEvents.incrementAndGet();
            if (currentNrOfEvents % MESSAGE_UPDATE_INTERVAL == 0) {
                Instant currentTime = Instant.now();
                LOGGER.info("{}: Messages received: {}. Current event rate {}/s, average event rate {}/s",
                        eventLoggDescription, totalNrOfEvents.get(), (long) getCurrentEventsPerSecond(currentTime), (long) getAverageEventsPerSecond(currentTime));
                lastLogUpdateTime = currentTime;
                lastUpdateTimeNrOfEvents = new AtomicLong(currentNrOfEvents);
            }
        } catch (ArithmeticException ignore) {
        }
    }

    private double getCurrentEventsPerSecond(Instant currentTime) {
        return (totalNrOfEvents.get() - lastUpdateTimeNrOfEvents.get()) / (MILLI_TO_SEK * (currentTime.toEpochMilli() - lastLogUpdateTime.toEpochMilli()));
    }

    private double getAverageEventsPerSecond(Instant currentTime) {
        return totalNrOfEvents.get() / (MILLI_TO_SEK * (currentTime.toEpochMilli() - startTime.toEpochMilli()));
    }
}

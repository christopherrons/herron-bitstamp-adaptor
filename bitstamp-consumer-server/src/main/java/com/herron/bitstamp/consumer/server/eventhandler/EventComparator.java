package com.herron.bitstamp.consumer.server.eventhandler;

import com.herron.bitstamp.consumer.server.api.BitstampMarketEvent;

import java.util.Comparator;

public class EventComparator<T extends BitstampMarketEvent> implements Comparator<T> {
    @Override
    public int compare(T event, T otherEvent) {
        if (event.timeStampInMs() < otherEvent.timeStampInMs()) {
            return -1;
        }
        if (event.timeStampInMs() > otherEvent.timeStampInMs()) {
            return 1;
        }

        return 0;
    }
}

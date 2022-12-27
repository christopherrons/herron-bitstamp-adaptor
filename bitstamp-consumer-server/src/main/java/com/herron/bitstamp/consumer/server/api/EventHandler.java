package com.herron.bitstamp.consumer.server.api;

import java.util.List;

public interface EventHandler {

    void handleEvent(BitstampMarketEvent messages);

    void handleEvents(List<BitstampMarketEvent> events);
}

package com.herron.bitstamp.consumer.server.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.herron.bitstamp.consumer.server.client.BitstampSubscription;
import com.herron.bitstamp.consumer.server.enums.EventDescriptionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public record BitstampEventData(@JsonProperty("data") Map<String, Object> data, @JsonProperty("channel") String channel, @JsonProperty("event") String event) {

    private static final Logger LOGGER = LoggerFactory.getLogger(BitstampSubscription.class);

    public EventDescriptionEnum getEventDescriptionEnum() {
        return EventDescriptionEnum.getEventDescriptionEnum(event);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getChannel() {
        return channel;
    }

    public String getEvent() {
        return event;
    }

    public BitstampUpdateOrder getOrder() {
        try {
            return new BitstampUpdateOrder(data, channel, event);
        } catch (Exception e) {
            LOGGER.warn("Unable to parse Order: Data: {}, Channel: {}, Event: {}", data, channel, event);
            return null;
        }
    }

    public BitstampTrade getTrade() {
        try {
            return new BitstampTrade(data, channel, event);
        } catch (Exception e) {
            LOGGER.warn("Unable to parse Trade: Data: {}, Channel: {}, Event: {}", data, channel, event);
            return null;
        }
    }

    public boolean isHeartbeatSuccessful() {
        try {
            return new BitstampHeartbeat(data, channel, event).isSuccessful();
        } catch (Exception e) {
            LOGGER.warn("Unable to parse Heartbeat: Data: {}, Channel: {}, Event: {}", data, channel, event);
            return false;
        }
    }

    @Override
    public String toString() {
        return "BitstampEvent{" +
                "eventDescriptionEnum=" + getEventDescriptionEnum() +
                ", data=" + data +
                ", channel='" + channel + '\'' +
                ", event='" + event + '\'' +
                '}';
    }
}

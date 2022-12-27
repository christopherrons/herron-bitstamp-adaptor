package com.herron.bitstamp.consumer.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.herron.bitstamp.consumer.server.enums.EventDescriptionEnum;

import java.util.Map;

public record BitstampEventData(@JsonProperty("data") Map<String, Object> data, @JsonProperty("channel") String channel, @JsonProperty("event") String event) {

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

    public BitstampOrder getOrder() {
        return new BitstampOrder(data, channel, event);
    }

    public BitstampTrade getTrade() {
        return new BitstampTrade(data, channel, event);
    }

    public BitstampHeartbeat getHeartBeat() {
        return new BitstampHeartbeat(data, channel, event);
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

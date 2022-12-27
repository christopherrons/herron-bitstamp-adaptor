package com.herron.bitstamp.consumer.server.config;

import com.herron.bitstamp.consumer.server.BitstampConsumer;
import com.herron.bitstamp.consumer.server.EventHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
public class BitstampConsumerConfig {

    @Bean
    public EventHandler eventHandler() {
        return new EventHandler();
    }

    @Bean(initMethod = "init")
    public BitstampConsumer bitstampConsumer(SubscriptionDetailConfig tradingPairs, EventHandler eventHandler) {
        return new BitstampConsumer(tradingPairs, eventHandler);
    }

    @Component
    @ConfigurationProperties(prefix = "subscription-config")
    public static class SubscriptionDetailConfig {

        private final List<SubscriptionDetail> subscriptionDetails = new ArrayList<>();

        public record SubscriptionDetail(String fxCurrency, String cryptoCurrency, String channel, String uri) {

        }

        public List<SubscriptionDetail> getSubscriptionDetails() {
            return subscriptionDetails;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SubscriptionDetailConfig that)) return false;
            return Objects.equals(subscriptionDetails, that.subscriptionDetails);
        }

        @Override
        public int hashCode() {
            return Objects.hash(subscriptionDetails);
        }
    }
}

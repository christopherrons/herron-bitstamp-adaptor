package com.herron.exchange.eventgenerator.server.config;

import com.herron.exchange.eventgenerator.server.EventGenerationBootloader;
import com.herron.exchange.eventgenerator.server.emulation.OrderEventEmulator;
import com.herron.exchange.eventgenerator.server.emulation.PreviousSettlementPriceConsumer;
import com.herron.exchange.eventgenerator.server.emulation.ReferenceDataConsumer;
import com.herron.exchange.eventgenerator.server.streaming.BitstampBroadcaster;
import com.herron.exchange.eventgenerator.server.streaming.BitstampConsumer;
import com.herron.exchange.common.api.common.kafka.KafkaBroadcastHandler;
import com.herron.exchange.integrations.generator.bitstamp.BitstampWebsocketClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Configuration
public class EventGeneratorConfig {

    @Bean
    public CountDownLatch emulationCountdownLatch() {
        return new CountDownLatch(2);
    }

    @Bean
    public ReferenceDataConsumer referenceDataConsumer(CountDownLatch emulationCountdownLatch) {
        return new ReferenceDataConsumer(emulationCountdownLatch);
    }

    @Bean
    public PreviousSettlementPriceConsumer previousSettlementPriceConsumer(CountDownLatch emulationCountdownLatch) {
        return new PreviousSettlementPriceConsumer(emulationCountdownLatch);
    }

    @Bean
    public KafkaBroadcastHandler kafkaBroadcastHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaBroadcastHandler(kafkaTemplate);
    }

    @Bean
    public OrderEventEmulator orderEventEmulator(KafkaBroadcastHandler kafkaBroadcastHandler,
                                                 CountDownLatch emulationCountdownLatch,
                                                 PreviousSettlementPriceConsumer previousSettlementPriceConsumer) {
        return new OrderEventEmulator(kafkaBroadcastHandler, emulationCountdownLatch, previousSettlementPriceConsumer);
    }

    @Bean
    public BitstampBroadcaster bistampMessageBroadcaster(KafkaBroadcastHandler kafkaBroadcastHandler) {
        return new BitstampBroadcaster(kafkaBroadcastHandler);
    }

    @Bean
    public BitstampWebsocketClient bitstampWebsocketClient() {
        return new BitstampWebsocketClient();
    }

    @Bean
    public BitstampConsumer bitstampConsumer(SubscriptionDetailConfig subscriptionDetailConfig,
                                             BitstampBroadcaster bitstampBroadcaster,
                                             BitstampWebsocketClient bitstampWebsocketClient) {
        return new BitstampConsumer(subscriptionDetailConfig, bitstampBroadcaster, bitstampWebsocketClient);
    }

    @Bean(initMethod = "init")
    public EventGenerationBootloader eventGenerationBootloader(BitstampConsumer bitstampConsumer, OrderEventEmulator orderEventEmulator) {
        return new EventGenerationBootloader(bitstampConsumer, orderEventEmulator);
    }

    @Component
    @ConfigurationProperties(prefix = "subscription-config")
    public static class SubscriptionDetailConfig {

        private String uri;
        private final List<String> channels = new ArrayList<>();

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getUri() {
            return uri;
        }

        public List<String> getChannels() {
            return channels;
        }

    }
}

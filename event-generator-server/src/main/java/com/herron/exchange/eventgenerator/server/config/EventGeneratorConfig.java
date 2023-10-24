package com.herron.exchange.eventgenerator.server.config;

import com.herron.exchange.common.api.common.api.MessageFactory;
import com.herron.exchange.common.api.common.kafka.KafkaBroadcastHandler;
import com.herron.exchange.common.api.common.kafka.KafkaConsumerClient;
import com.herron.exchange.common.api.common.mapping.DefaultMessageFactory;
import com.herron.exchange.eventgenerator.server.EventGenerationBootloader;
import com.herron.exchange.eventgenerator.server.consumers.PreviousSettlementPriceConsumer;
import com.herron.exchange.eventgenerator.server.consumers.ReferenceDataConsumer;
import com.herron.exchange.eventgenerator.server.emulation.OrderEventEmulatorBroadcaster;
import com.herron.exchange.eventgenerator.server.streaming.BitstampBroadcaster;
import com.herron.exchange.eventgenerator.server.streaming.BitstampConsumer;
import com.herron.exchange.integrations.bitstamp.BitstampWebsocketClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.herron.exchange.common.api.common.enums.KafkaTopicEnum.PREVIOUS_SETTLEMENT_PRICE_DATA;
import static com.herron.exchange.common.api.common.enums.KafkaTopicEnum.REFERENCE_DATA;

@Configuration
public class EventGeneratorConfig {

    @Bean
    public MessageFactory messageFactory() {
        return new DefaultMessageFactory();
    }

    @Bean
    public ReferenceDataConsumer referenceDataConsumer(KafkaConsumerClient kafkaConsumerClient, KafkaConfig.KafkaConsumerConfig config) {
        return new ReferenceDataConsumer(kafkaConsumerClient, config.getDetails(REFERENCE_DATA));
    }

    @Bean
    public PreviousSettlementPriceConsumer previousSettlementPriceConsumer(KafkaConsumerClient kafkaConsumerClient, KafkaConfig.KafkaConsumerConfig config) {
        return new PreviousSettlementPriceConsumer(kafkaConsumerClient, config.getDetails(PREVIOUS_SETTLEMENT_PRICE_DATA));
    }

    @Bean
    public OrderEventEmulatorBroadcaster orderEventEmulator(KafkaBroadcastHandler kafkaBroadcastHandler, PreviousSettlementPriceConsumer previousSettlementPriceConsumer) {
        return new OrderEventEmulatorBroadcaster(kafkaBroadcastHandler, previousSettlementPriceConsumer);
    }

    @Bean
    public BitstampBroadcaster bitstampBroadcaster(KafkaBroadcastHandler kafkaBroadcastHandler) {
        return new BitstampBroadcaster(kafkaBroadcastHandler);
    }

    @Bean
    public BitstampWebsocketClient bitstampWebsocketClient() {
        return new BitstampWebsocketClient();
    }

    @Bean
    public BitstampConsumer bitstampConsumer(BitstampSubscriptionDetailConfig bitstampSubscriptionDetailConfig,
                                             BitstampBroadcaster bitstampBroadcaster,
                                             BitstampWebsocketClient bitstampWebsocketClient) {
        return new BitstampConsumer(bitstampSubscriptionDetailConfig, bitstampBroadcaster, bitstampWebsocketClient);
    }

    @Bean(initMethod = "init")
    public EventGenerationBootloader eventGenerationBootloader(BitstampConsumer bitstampConsumer,
                                                               OrderEventEmulatorBroadcaster orderEventEmulatorBroadcaster,
                                                               PreviousSettlementPriceConsumer previousSettlementPriceConsumer,
                                                               ReferenceDataConsumer referenceDataConsumer) {
        return new EventGenerationBootloader(bitstampConsumer, orderEventEmulatorBroadcaster, previousSettlementPriceConsumer, referenceDataConsumer);
    }

    @Component
    @ConfigurationProperties(prefix = "bitstamp.subscription-config")
    public static class BitstampSubscriptionDetailConfig {

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

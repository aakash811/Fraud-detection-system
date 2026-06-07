package com.frauddetection.auditor.config;

import com.frauddetection.common.event.DecisionEvent;
import com.frauddetection.common.event.TransactionEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, TransactionEvent> transactionConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TransactionEvent.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "compliance-auditor-transactions");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConsumerFactory<String, DecisionEvent> decisionConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DecisionEvent.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "compliance-auditor-decisions");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionEvent> transactionKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TransactionEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(transactionConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DecisionEvent> decisionKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DecisionEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(decisionConsumerFactory());
        return factory;
    }
}
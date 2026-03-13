package com.quinzex.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic inventoryReserveTopic() {
        return new NewTopic("inventoryReserveTopic", 3, (short) 1);
    }


    @Bean
    public NewTopic inventoryReleaseTopic() {
        return new NewTopic("inventoryReleaseTopic", 3, (short) 1);
    }

    @Bean
    public NewTopic inventoryFailedTopic() {
        return new NewTopic("inventoryFailedTopic", 3, (short) 1);
    }
    @Bean
    public NewTopic inventoryReservedTopic() {
        return new NewTopic("inventoryReservedTopic", 3, (short) 1);
    }
}

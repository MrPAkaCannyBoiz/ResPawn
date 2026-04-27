package com.respawn.services.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig
{
    @Bean
    public NewTopic welcomeEmailTopic() {
        return TopicBuilder.name("welcomeEmail")
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic testTopic(){
        return TopicBuilder.name("test")
                .partitions(3)
                .replicas(3)
                .build();
    }


}

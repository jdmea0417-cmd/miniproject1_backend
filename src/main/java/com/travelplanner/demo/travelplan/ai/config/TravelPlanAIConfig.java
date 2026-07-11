package com.travelplanner.demo.travelplan.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TravelPlanAIConfig {

    @Bean
    public ChatClient travelPlanChatClient(ChatClient.Builder builder) {
        return builder
                .build();
    }
}
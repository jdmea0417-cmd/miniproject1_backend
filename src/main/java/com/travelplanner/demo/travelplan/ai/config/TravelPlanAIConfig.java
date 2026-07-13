package com.travelplanner.demo.travelplan.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TravelPlanAIConfig {

    @Bean
    public ChatClient travelPlanChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("당신은 전문 여행 플래너입니다.")
                .build();
    }
}
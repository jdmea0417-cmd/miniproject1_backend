package com.travelplanner.demo;

import com.travelplanner.demo.travelplan.ai.agent.TravelPlanAIAgent;
import com.travelplanner.demo.travelplan.dto.TravelPlanRequest;
import com.travelplanner.demo.travelplan.dto.TravelPlanResponse;
import com.travelplanner.demo.destination.dto.DestinationRequest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest(properties = {
    "spring.ai.openai.api-key=${OPEN_AI_KEY}",
    "spring.ai.openai.chat.options.model=${OPEN_AI_MODEL}"
})
public class TravelPlanAIAgentIntegrationTest {

    @Autowired
    private TravelPlanAIAgent travelPlanAIAgent;

    @Test
    public void testGenerateTravelPlan_Gumi() {
        System.out.println("=================================================");
        System.out.println("Running TravelPlanAIAgent Integration Test (Gumi)...");
        System.out.println("=================================================");

        // 구미, 선산곱창, 금오산, 원평동 조건 생성
        TravelPlanRequest request = TravelPlanRequest.builder()
                .area("구미")
                .startDate(LocalDate.of(2026, 7, 11))
                .endDate(LocalDate.of(2026, 7, 12))
                .destinations(List.of(
                        DestinationRequest.builder().keyword("선산곱창").build(),
                        DestinationRequest.builder().keyword("금오산").build(),
                        DestinationRequest.builder().keyword("원평동").build()
                ))
                .build();

        // 3단계 AI 호출 프로세스 실행
        TravelPlanResponse response = travelPlanAIAgent.generateTravelPlan(request);

        System.out.println("=================== TEST RESULT ===================");
        System.out.println("Area: " + response.getArea());
        System.out.println("StartDate: " + response.getStartDate());
        System.out.println("EndDate: " + response.getEndDate());
        System.out.println("Destinations List Size: " + (response.getDestinations() != null ? response.getDestinations().size() : 0));
        
        if (response.getDestinations() != null) {
            response.getDestinations().forEach(dest -> {
                System.out.printf("[%s %s] %s%n", dest.getDate(), dest.getTime(), dest.getPlace());
            });
        }
        System.out.println("=================================================");
    }
}

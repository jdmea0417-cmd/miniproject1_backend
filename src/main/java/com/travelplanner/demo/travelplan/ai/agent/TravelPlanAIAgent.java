package com.travelplanner.demo.travelplan.ai.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.travelplanner.demo.travelplan.dto.TravelPlanRequest;
import com.travelplanner.demo.travelplan.dto.TravelPlanResponse;

import java.util.stream.Collectors;

import com.travelplanner.demo.destination.dto.DestinationRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelPlanAIAgent {

    // agent config에서 tool 이 등록된 ChatClient
    private final ChatClient chatClient;
    @Value("${spring.ai.openai.api-key}")
    private String key;
    @Value("${spring.ai.openai.chat.options.model}")
    private String model;
    private String endPoint = "https://api.openai.com/v1/chat/completions";

    /**
     * 여행 계획 요청을 기반으로 AI에게 여행 계획을 생성하도록 요청합니다.
     *
     * @param request 여행 계획 요청 (지역, 시작일, 종료일, 목적지 키워드 목록)
     * @return AI가 생성한 여행 계획 내용 (JSON 형태의 문자열)
     */
    public TravelPlanResponse generateTravelPlan(TravelPlanRequest request) {
        log.debug(">>>> travel plan agent generateTravelPlan start");
        System.out.println(">>>> debug openai service  model    : " + model);
        System.out.println(">>>> debug openai service  endPoint : " + endPoint);

        // 시스템 프롬프트: JSON 형식으로 결과 출력 지시
        TravelPlanResponse response = chatClient.prompt()
                .system("""
                                반드시 json 형태로 응답
                        """)
                .user("""
                            너는 여행지에 따라 상세계획을 짜주는 전문가야
                            동선과 식사 시간, 등을 고려하여
                            식당, 숙소를 포함,
                            조건과 아래 규칙에 맞게 응답할 것
                            조건
                            - 지역 : "%s"
                            - 여행 시작일 : "%s"
                            - 여행 종료일 : "%s"
                            - 필수 방문지 : "%s"
                            출력예시
                            {
                                "area" : "여행지역",
                                "startDate" : "여행 시작일",
                                "endDate" : "여행 종료일",
                                "destinations" : [
                                    {
                                        "date" : "여행지 방문일",
                                        "time" : "여행지 방문시간",
                                        "place" : "여행지"
                                    }
                                ]
                            }
                        """.formatted(
                                request.getArea(), 
                                request.getStartDate(), 
                                request.getEndDate(), 
                                request.getDestinations().stream()
                                    .map(DestinationRequest::getKeyword)
                                    .collect(Collectors.joining(", "))))
                .call()
                .entity(TravelPlanResponse.class);

        log.debug("AI generated travel plan: {}", response);
        return response;
    }
}
package com.travelplanner.demo.travelplan.ai.agent;

import com.travelplanner.demo.travelplan.ai.dto.AiTravelPlanResponse;
import com.travelplanner.demo.travelplan.dto.TravelPlanRequest;
import com.travelplanner.demo.destination.dto.DestinationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class TravelPlanAIAgent {

    private static final Logger log = LoggerFactory.getLogger(TravelPlanAIAgent.class);

    private final ChatClient chatClient;

    public TravelPlanAIAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public AiTravelPlanResponse generatePlan(TravelPlanRequest request) {
        System.out.println(">>>> debug ai agent call");
        String destinationsText = request.getDestinations() != null
            ? request.getDestinations().stream()
                .map(DestinationRequest::getKeyword)
                .reduce((a, b) -> a + ", " + b)
                .orElse("")
            : "";

        long days = java.time.temporal.ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        String systemPrompt = """
            당신은 전문 여행 플래너입니다. 사용자의 요청에 따라 상세한 여행 일정을 JSON 형식으로 생성해주세요.
            
            응답 규칙:
            1. 반드시 유효한 JSON만 출력하세요 (마크다운 코드블록 없이)
            2. 한국어로 작성하세요
            3. 현실적이고 구체적인 장소/시간/비용을 제시하세요
            4. 이동 시간과 동선을 고려하세요
            5. 현지 맛집, 명소, 체험을 포함하세요
            6. 예산 내 비용 산정 (식비, 입장료, 교통비 등)
            
            응답 스키마:
            {
              "title": "여행 계획 제목",
              "summary": "전체 일정 요약",
              "estimatedCost": 예상총비용_만원,
              "dailyPlans": [
                {
                  "day": 1,
                  "date": "YYYY-MM-DD",
                  "title": "일차 제목",
                  "schedule": [
                    {
                      "time": "HH:mm",
                      "place": "장소명",
                      "activity": "활동내용",
                      "duration": 소요시간_분,
                      "note": "비고"
                    }
                  ],
                  "transport": "이동수단",
                  "estimatedCost": 예상비용_만원
                }
              ],
              "recommendedArea": "추천숙소지역",
              "tips": "여행팁"
            }
            """;

        String userPrompt = String.format("""
            다음 조건으로 %d일 여행 일정을 짜주세요.
            
            【기본 정보】
            - 지역: %s
            - 기간: %s ~ %s (%d박 %d일)
            - 필수 방문 장소: %s
            
            【요구사항】
            1. 각 일차별로 오전/오후/저녁 시간대별 일정 구성
            2. 이동 거리와 시간 고려한 효율적인 동선
            3. 현지 맛집, 명소, 체험 포함
            4. 예산 내 비용 산정 (식비, 입장료, 교통비 등)
            5. 실존하는 구체적 장소명 사용
            6. 날씨/계절 고려한 추천
            """,
            days,
            request.getArea(),
            request.getStartDate(),
            request.getEndDate(),
            days,
            days,
            destinationsText.isEmpty() ? "없음" : destinationsText
        );

        try {
            AiTravelPlanResponse response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .entity(AiTravelPlanResponse.class);

            log.info("AI 여행 계획 생성 완료: {}", response.getTitle());
            return response;
        } catch (Exception e) {
            log.error("AI 여행 계획 생성 실패", e);
            return buildFallbackResponse(request, destinationsText, days);
        }
    }

    private AiTravelPlanResponse buildFallbackResponse(TravelPlanRequest request, String destinationsText, long days) {
        return AiTravelPlanResponse.builder()
            .title(request.getArea() + " " + days + "일 여행 추천")
            .summary("AI 추천 생성 중 오류가 발생했습니다. 기본 템플릿을 제공합니다.")
            .estimatedCost(50)
            .dailyPlans(Collections.emptyList())
            .recommendedArea(request.getArea() + " 시내")
            .tips("직접 일정을 계획해보세요. 주요 관광지 검색 추천")
            .build();
    }
}
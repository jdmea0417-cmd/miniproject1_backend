package com.travelplanner.demo.travelplan.ai.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.travelplanner.demo.travelplan.dto.TravelPlanRequest;
import com.travelplanner.demo.travelplan.dto.TravelPlanResponse;
import com.travelplanner.demo.destination.dto.DestinationRequest;
import com.travelplanner.demo.travelplan.ai.dto.*;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelPlanAIAgent {

    private final ChatClient chatClient;

    @Value("${spring.ai.openai.api-key}")
    private String key;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    /**
     * 여행 계획 요청을 기반으로 AI에게 여행 계획을 생성하도록 요청합니다.
     *
     * @param request 여행 계획 요청 (지역, 시작일, 종료일, 목적지 키워드 목록)
     * @return AI가 생성한 여행 계획 내용 (JSON 형태의 문자열)
     */
    public TravelPlanResponse generateTravelPlan(TravelPlanRequest request) {
        log.debug(">>>> travel plan agent generateTravelPlan start");
        System.out.println(">>>> debug openai service  model    : " + model);

        return finalResponse;
    }

    private CandidatePlaceResponse listUpCandidatePlaces(
            TravelPlanRequest request,
            String requiredDestinations) {
        return chatClient.prompt()
                .system("""
                        너는 여행 후보 장소를 고르는 역할이다.
                        출력은 JSON만 반환한다.
                        설명, markdown, 주석은 금지한다.
                        """)
                .user("""
                            너는 여행지에 따라 상세계획을 짜주는 전문가
                            조건과 아래 규칙에 맞게 응답할 것.

                            조건
                            - 지역 : "%s"
                            - 여행 시작일 : "%s"
                            - 여행 종료일 : "%s"
                            - 필수 방문지 : "%s"
                            1. 동선을 고려할 것.
                            2. 계획의 시작은 항상 여행지로 도착일 것(공항, 역 등)
                            3. 계획의 끝은 항상 여행지에서 출발일 것(공항, 역 등)
                            4. 필수 여행지를 첫 순서로 배치할 필요는 없으며, 여러번 방문할 필요도 없음
                            5. 일과 중 반드시 아침, 점심 저녁 식사가 포함되어야 하며, 정확한 식당명을 응답할 것.
                            6. 일과의 마무리는 항상 숙소여야 함. 정확한 숙소 명을 응답할 것.
                            
                            출력예시
                            {
                              "name": "국립중앙박물관",
                              "type": "관광지",
                              "estimatedStayMinutes": 90,
                              "areaHint": "용산",
                              "note": "영업시간 확인 필요"
                            }
                          ]
                        }
                        """.formatted(
                                request.getArea(), 
                                request.getStartDate(), 
                                request.getEndDate(), 
                                request.getDestinations().stream()
                                    .flatMap(dr -> dr.getKeywords().stream())
                                    .collect(Collectors.joining(", "))))
                .call()
                .entity(CandidatePlaceResponse.class);
    }

    private DraftScheduleResponse makeDraftSchedule(
            TravelPlanRequest request,
            CandidatePlaceResponse candidates) {
        return chatClient.prompt()
                .system("""
                        너는 여행 일정을 시간순으로 배치하는 역할이다.
                        출력은 JSON만 반환한다.
                        설명, markdown, 주석은 금지한다.
                        """)
                .user("""
                        입력
                        - 지역: "%s"
                        - 여행 시작일: "%s"
                        - 여행 종료일: "%s"
                        - 후보 장소: %s

                        일정 작성 규칙
                        1. 하루 일정은 별도 입력이 없으면 10:00:00 기준으로 작성한다.
                        2. 가까운 곳, 이동이 쉬운 곳 순서로 배치한다.
                        3. 각 장소의 체류시간을 현실적으로 반영한다.
                        4. 이동 항목을 반드시 일정에 포함한다.
                        5. 이동 항목은 "A -> B 이동 (교통수단 N분)" 형식으로 작성한다.
                        6. 점심은 12:00-13:30 사이에 반드시 포함한다.
                        7. 저녁은 17:00 -19:00 사이에 반드시 포함한다.
                        8. 식사 항목은 "점심-식당명" 또는 "저녁-식당명" 형식으로 작성한다.
                        9. 영업시간이나 휴무일이 불확실한 장소는 무리한 시간대에 배치하지 않는다.
                        10. 같은 장소를 불필요하게 반복하지 않는다.

                        출력 예시 (컨텐즈 자체는 무시, 형식과 맥락만 기억) 
                        {
                          "items": [
                            {
                              "date": "2026-07-11",
                              "time": "10:00",
                              "place": "국립중앙박물관"
                            },
                            {
                              "date": "2026-07-11",
                              "time": "12:00",
                              "place": "국립중앙박물관 -> 아이파크몰 용산점 이동 (버스 15분)"
                            },
                            {
                              "date": "2026-07-11",
                              "time": "12:30",
                              "place": "점심-용산아이파크몰"
                            }
                          ]
                        }
                        """.formatted(
                        request.getArea(),
                        request.getStartDate(),
                        request.getEndDate(),
                        candidates))
                .call()
                .entity(DraftScheduleResponse.class);
    }

    private TravelPlanResponse finalizeSchedule(
            TravelPlanRequest request,
            DraftScheduleResponse draftSchedule) {
        return chatClient.prompt()
                .system("""
                        너는 여행 일정 JSON을 최종 검수하는 역할이다.
                        출력은 JSON 객체만 반환한다.
                        설명, markdown, 주석은 금지한다.
                        """)
                .user("""
                        입력
                        - 지역: "%s"
                        - 여행 시작일: "%s"
                        - 여행 종료일: "%s"
                        - 일정 초안: %s

                        검수 규칙
                        1. area, startDate, endDate, destinations 필드만 사용한다.
                        2. destinations는 시간순으로 정렬한다.
                        3. 각 항목은 date, time, place만 가진다.
                        4. time은 HH:mm 형식으로 작성한다.
                        5. 점심과 저녁이 없으면 추가한다.
                        6. 이동 시간이 누락된 구간은 이동 항목을 추가한다.
                        7. 비현실적으로 촘촘한 일정은 조정한다.
                        8. JSON 파싱 가능한 형태로만 반환한다.

                        출력 예시 (컨텐즈 자체는 무시, 형식과 맥락만 기억)
                        {
                          "area": "여행지역",
                          "startDate": "여행 시작일",
                          "endDate": "여행 종료일",
                          "destinations": [
                            {
                              "date": "여행지 방문일",
                              "time": "10:30",
                              "place": "국립중앙박물관"
                            }
                          ]
                        }
                        """.formatted(
                        request.getArea(),
                        request.getStartDate(),
                        request.getEndDate(),
                        draftSchedule))
                .call()
                .entity(TravelPlanResponse.class);
    }

}

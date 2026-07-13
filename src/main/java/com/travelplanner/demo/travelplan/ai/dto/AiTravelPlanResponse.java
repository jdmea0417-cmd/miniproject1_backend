package com.travelplanner.demo.travelplan.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI 여행 계획 응답")
public class AiTravelPlanResponse {

    @Schema(description = "여행 계획 제목", example = "제주도 3박 4일 힐링 여행")
    private String title;

    @Schema(description = "전체 일정 요약", example = "제주도의 자연과 맛집을 즐기는 힐링 코스")
    private String summary;

    @Schema(description = "예상 총 비용 (만원)", example = "45")
    private Integer estimatedCost;

    @Schema(description = "일자별 상세 일정")
    private List<DailyPlan> dailyPlans;

    @Schema(description = "추천 숙소 지역", example = "서귀포시 중문관광단지")
    private String recommendedArea;

    @Schema(description = "여행 팁", example = "렌트카 예약은 미리 하세요. 우도 배편은 날씨 확인 필수")
    private String tips;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "일자별 상세 일정")
    public static class DailyPlan {

        @Schema(description = "일차", example = "1")
        private Integer day;

        @Schema(description = "날짜", example = "2026-07-15")
        private String date;

        @Schema(description = "일정 제목", example = "제주 도착 & 서쪽 해안 드라이브")
        private String title;

        @Schema(description = "시간대별 일정")
        private List<TimeSlot> schedule;

        @Schema(description = "이동 수단", example = "렌트카")
        private String transport;

        @Schema(description = "예상 비용 (만원)", example = "15")
        private Integer estimatedCost;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "시간대별 일정")
    public static class TimeSlot {

        @Schema(description = "시간", example = "09:00")
        private String time;

        @Schema(description = "장소", example = "용두암")
        private String place;

        @Schema(description = "활동 내용", example = "용두암 관람 및 사진 촬영")
        private String activity;

        @Schema(description = "소요 시간(분)", example = "60")
        private Integer duration;

        @Schema(description = "비고", example = "주차장 이용 가능")
        private String note;
    }
}
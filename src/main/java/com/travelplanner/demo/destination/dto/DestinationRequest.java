package com.travelplanner.demo.destination.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "여행지 요청 (키워드 기반)")
public class DestinationRequest {

    @Schema(description = "검색 키워드", example = "경복궁", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Keyword is required")
    private String keyword;

    @Schema(description = "방문 날짜 (YYYY-MM-DD)", example = "2026-07-08")
    private String date;

    @Schema(description = "방문 시간 (HH:mm:ss)", example = "10:00:00")
    private String time;
}
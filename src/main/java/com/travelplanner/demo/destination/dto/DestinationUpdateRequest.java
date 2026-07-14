package com.travelplanner.demo.destination.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "개별 여행지(목적지) 수정 요청")
public class DestinationUpdateRequest {

    @Schema(description = "수정할 여행지 이름", example = "경복궁", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Place is required")
    private String place;

    @Schema(description = "수정할 방문 날짜 (YYYY-MM-DD)", example = "2026-07-15")
    private String date;

    @Schema(description = "수정할 방문 시간 (HH:mm)", example = "14:00")
    private String time;
}

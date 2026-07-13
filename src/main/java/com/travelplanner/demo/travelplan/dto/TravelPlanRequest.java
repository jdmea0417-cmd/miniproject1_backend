package com.travelplanner.demo.travelplan.dto;

import com.travelplanner.demo.travelplan.entity.TravelPlanEntity;
import com.travelplanner.demo.user.entity.UserEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

import com.travelplanner.demo.destination.dto.DestinationRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Schema(description = "여행 계획 생성/수정 요청")
public class TravelPlanRequest {

    @Schema(description = "여행 지역", example = "서울", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Area is required")
    private String area;

    @Schema(description = "여행 시작일", example = "2026-07-08", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2026-07-14", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Schema(description = "여행지 키워드 목록")
    @NotEmpty(message = "At least one destination keyword is required")
    private List<DestinationRequest> destinations;

    @AssertTrue(message = "시작일은 종료일 이전이거나 같아야 합니다")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !startDate.isAfter(endDate);
    }
}
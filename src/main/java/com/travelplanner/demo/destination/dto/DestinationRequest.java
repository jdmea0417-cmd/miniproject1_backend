package com.travelplanner.demo.destination.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

import com.travelplanner.demo.destination.entity.DestinationEntity;
import com.travelplanner.demo.travelplan.entity.TravelPlanEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Schema(description = "여행지 요청 (키워드 기반)")
public class DestinationRequest {
    
    @Schema(description = "여행지 ID", example = "1")
    private Integer id;

    @Schema(description = "검색 키워드 목록", example = "[\"경복궁\", \"북촌한옥마을\"]")
    private List<String> keywords;

    @Schema(description = "방문 날짜 (YYYY-MM-DD)", example = "2026-07-08")
    private String date;

    @Schema(description = "방문 시간 (HH:mm:ss)", example = "10:00:00")
    private String time;

    @Schema(description = "장소명 (keywords 대신 직접 장소명 지정 시 사용)", example = "인천국제공항")
    private String place;

    @AssertTrue(message = "키워드 목록 또는 장소명 중 하나는 필수입니다")
    public boolean isPlaceOrKeywordsPresent() {
        return (keywords != null && !keywords.isEmpty()) || (place != null && !place.isBlank());
    }

    public DestinationEntity toEntity(TravelPlanEntity travelPlan) {
        // place 필드가 있으면 그걸 사용, 없으면 keywords를 콤마로 연결
        String placeValue = (this.place != null && !this.place.isBlank()) 
            ? this.place 
            : (this.keywords != null && !this.keywords.isEmpty() ? String.join(", ", this.keywords) : "");
        return DestinationEntity.builder()
                .id(this.id)
                .travelPlan(travelPlan)
                .date(this.date)
                .time(this.time)
                .place(placeValue)
                .build();
    }
}
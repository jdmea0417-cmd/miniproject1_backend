package com.travelplanner.demo.destination.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Schema(description = "여행지 요청 (키워드 기반)")
public class DestinationRequest {

    @Schema(description = "검색 키워드 목록", example = "[\"경복궁\", \"북촌한옥마을\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "At least one keyword is required")
    private List<String> keyword;

    @Schema(description = "방문 날짜 (YYYY-MM-DD)", example = "2026-07-08")
    private String date;

    @Schema(description = "방문 시간 (HH:mm:ss)", example = "10:00:00")
    private String time;

    /**
     * Set a single keyword as a list for backward compatibility with string input.
     * @param keyword a single keyword
     */
    public void setKeyword(String keyword) {
        this.keyword = Collections.singletonList(keyword);
    }
}
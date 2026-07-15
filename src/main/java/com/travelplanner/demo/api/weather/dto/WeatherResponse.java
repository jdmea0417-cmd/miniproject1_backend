package com.travelplanner.demo.api.weather.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Schema(description = "날씨 응답")
public class WeatherResponse {

    @Schema(description = "날짜", example = "2026-07-15")
    private LocalDate date;

    @Schema(description = "날씨 설명", example = "맑음")
    private String description;
}
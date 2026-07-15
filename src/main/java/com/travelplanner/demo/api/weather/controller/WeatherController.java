package com.travelplanner.demo.api.weather.controller;

import com.travelplanner.demo.travelplan.dto.TravelPlanRequest;
import com.travelplanner.demo.api.weather.dto.WeatherResponse;
import com.travelplanner.demo.api.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Weather", description = "날씨 조회 API")
@RestController
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @Operation(summary = "여행 계획 기간 날씨 조회", description = "여행 계획 요청을 기반으로 여행 기간 동안의 날씨 정보를 조회합니다.")
    @PostMapping("/travel-plan")
    public List<WeatherResponse> getWeatherByTravelPlan(
            @Parameter(description = "여행 계획 요청", required = true)
            @RequestBody TravelPlanRequest request) {
        System.out.println(">>>> debug weather controller get weather");
        return weatherService.getWeatherByTravelPlan(request);
    }
}
package com.travelplanner.demo.api.weather.service;

import com.travelplanner.demo.travelplan.dto.TravelPlanRequest;
import com.travelplanner.demo.api.weather.dto.WeatherResponse;
import com.travelplanner.demo.api.weather.dto.WeatherApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient weatherWebClient;

    @Value("${weather.api.key:}")
    private String apiKey;

    private static final String UNKNOWN_WEATHER = "알 수 없음";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<WeatherResponse> getWeatherByTravelPlan(TravelPlanRequest request) {
        System.out.println(">>>> debug weather service get weather");
        if (request == null || request.getStartDate() == null || request.getEndDate() == null) {
            log.warn("Invalid travel plan request: missing dates");
            return Collections.emptyList();
        }

        String area = request.getArea();
        if (area == null || area.isBlank()) {
            log.warn("Area is required for weather lookup");
            return Collections.emptyList();
        }

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Weather API key not configured, returning unknown weather for all dates");
            return generateUnknownWeatherList(request.getStartDate(), request.getEndDate());
        }

        try {
            String cityName = convertAreaToCityName(area);
            WeatherApiResponse apiResponse = fetchWeatherForecast(cityName);
            
            if (apiResponse == null || apiResponse.getList() == null || apiResponse.getList().isEmpty()) {
                log.warn("No weather data returned from API for area: {}", area);
                return generateUnknownWeatherList(request.getStartDate(), request.getEndDate());
            }

            return processWeatherData(apiResponse, request.getStartDate(), request.getEndDate());
        } catch (Exception e) {
            log.error("Error fetching weather data for area: {}", area, e);
            return generateUnknownWeatherList(request.getStartDate(), request.getEndDate());
        }
    }

    private WeatherApiResponse fetchWeatherForecast(String cityName) {
        return weatherWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("q", cityName + ",KR")
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .queryParam("lang", "kr")
                        .build())
                .retrieve()
                .bodyToMono(WeatherApiResponse.class)
                .onErrorResume(e -> {
                    log.error("Weather API call failed for city: {}", cityName, e);
                    return Mono.empty();
                })
                .block();
    }

    private List<WeatherResponse> processWeatherData(WeatherApiResponse apiResponse, 
                                                      LocalDate startDate, 
                                                      LocalDate endDate) {
        Map<LocalDate, List<WeatherApiResponse.WeatherItem>> dailyWeatherMap = new LinkedHashMap<>();

        for (WeatherApiResponse.WeatherItem item : apiResponse.getList()) {
            LocalDate date = LocalDate.parse(item.getDtTxt().split(" ")[0], DATE_FORMATTER);
            
            if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                dailyWeatherMap.computeIfAbsent(date, k -> new ArrayList<>()).add(item);
            }
        }

        List<WeatherResponse> result = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            List<WeatherApiResponse.WeatherItem> dayItems = dailyWeatherMap.get(currentDate);
            String description = UNKNOWN_WEATHER;

            if (dayItems != null && !dayItems.isEmpty()) {
                description = getMostFrequentWeather(dayItems);
            }

            result.add(WeatherResponse.builder()
                    .date(currentDate)
                    .description(description)
                    .build());

            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

    private String getMostFrequentWeather(List<WeatherApiResponse.WeatherItem> items) {
        Map<String, Long> weatherCount = items.stream()
                .flatMap(item -> item.getWeather().stream())
                .collect(Collectors.groupingBy(
                        w -> w.getDescription(),
                        Collectors.counting()
                ));

        return weatherCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .map(this::translateWeather)
                .orElse(UNKNOWN_WEATHER);
    }

    private String translateWeather(String description) {
        if (description == null) return UNKNOWN_WEATHER;
        
        String lower = description.toLowerCase();
        if (lower.contains("clear") || lower.contains("맑")) return "맑음";
        if (lower.contains("cloud") || lower.contains("구름") || lower.contains("흐림")) return "흐림";
        if (lower.contains("rain") || lower.contains("비")) return "비";
        if (lower.contains("snow") || lower.contains("눈")) return "눈";
        if (lower.contains("thunder") || lower.contains("뇌우")) return "뇌우";
        if (lower.contains("mist") || lower.contains("안개") || lower.contains("fog")) return "안개";
        if (lower.contains("drizzle") || lower.contains("이슬비")) return "이슬비";
        
        return description;
    }

    private List<WeatherResponse> generateUnknownWeatherList(LocalDate startDate, LocalDate endDate) {
        List<WeatherResponse> result = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            result.add(WeatherResponse.builder()
                    .date(current)
                    .description(UNKNOWN_WEATHER)
                    .build());
            current = current.plusDays(1);
        }
        return result;
    }

    private String convertAreaToCityName(String area) {
        Map<String, String> areaToCity = Map.ofEntries(
                Map.entry("서울", "Seoul"),
                Map.entry("부산", "Busan"),
                Map.entry("대구", "Daegu"),
                Map.entry("인천", "Incheon"),
                Map.entry("광주", "Gwangju"),
                Map.entry("대전", "Daejeon"),
                Map.entry("울산", "Ulsan"),
                Map.entry("세종", "Sejong"),
                Map.entry("경기", "Gyeonggi-do"),
                Map.entry("강원", "Gangwon-do"),
                Map.entry("충북", "Chungcheongbuk-do"),
                Map.entry("충남", "Chungcheongnam-do"),
                Map.entry("전북", "Jeollabuk-do"),
                Map.entry("전남", "Jeollanam-do"),
                Map.entry("경북", "Gyeongsangbuk-do"),
                Map.entry("경남", "Gyeongsangnam-do"),
                Map.entry("제주", "Jeju")
        );

        return areaToCity.getOrDefault(area, "Seoul");
    }
}
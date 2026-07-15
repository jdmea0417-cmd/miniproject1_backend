package com.travelplanner.demo.api.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherApiResponse {

    @JsonProperty("cod")
    private String cod;

    @JsonProperty("message")
    private int message;

    @JsonProperty("cnt")
    private int cnt;

    @JsonProperty("list")
    private List<WeatherItem> list;

    @JsonProperty("city")
    private City city;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeatherItem {
        @JsonProperty("dt")
        private long dt;

        @JsonProperty("dt_txt")
        private String dtTxt;

        @JsonProperty("main")
        private Main main;

        @JsonProperty("weather")
        private List<Weather> weather;

        @JsonProperty("clouds")
        private Clouds clouds;

        @JsonProperty("wind")
        private Wind wind;

        @JsonProperty("visibility")
        private int visibility;

        @JsonProperty("pop")
        private double pop;

        @JsonProperty("sys")
        private Sys sys;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Main {
        @JsonProperty("temp")
        private double temp;

        @JsonProperty("feels_like")
        private double feelsLike;

        @JsonProperty("temp_min")
        private double tempMin;

        @JsonProperty("temp_max")
        private double tempMax;

        @JsonProperty("pressure")
        private int pressure;

        @JsonProperty("sea_level")
        private int seaLevel;

        @JsonProperty("grnd_level")
        private int grndLevel;

        @JsonProperty("humidity")
        private int humidity;

        @JsonProperty("temp_kf")
        private double tempKf;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Weather {
        @JsonProperty("id")
        private int id;

        @JsonProperty("main")
        private String main;

        @JsonProperty("description")
        private String description;

        @JsonProperty("icon")
        private String icon;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Clouds {
        @JsonProperty("all")
        private int all;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Wind {
        @JsonProperty("speed")
        private double speed;

        @JsonProperty("deg")
        private int deg;

        @JsonProperty("gust")
        private double gust;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Sys {
        @JsonProperty("pod")
        private String pod;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class City {
        @JsonProperty("id")
        private int id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("coord")
        private Coord coord;

        @JsonProperty("country")
        private String country;

        @JsonProperty("population")
        private int population;

        @JsonProperty("timezone")
        private int timezone;

        @JsonProperty("sunrise")
        private long sunrise;

        @JsonProperty("sunset")
        private long sunset;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Coord {
        @JsonProperty("lat")
        private double lat;

        @JsonProperty("lon")
        private double lon;
    }
}
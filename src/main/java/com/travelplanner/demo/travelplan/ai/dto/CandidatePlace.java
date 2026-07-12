package com.travelplanner.demo.travelplan.ai.dto;

public record CandidatePlace(
        String name,
        String type,
        Integer estimatedStayMinutes,
        String areaHint,
        String note
) {
}

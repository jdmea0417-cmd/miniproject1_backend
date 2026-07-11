package com.travelplanner.demo.travelplan.ai.dto;

import java.util.List;

public record DraftScheduleResponse(
        List<DraftScheduleItem> items
) {
}

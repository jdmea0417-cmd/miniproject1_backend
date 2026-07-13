package com.travelplanner.demo.travelplan.controller;

import com.travelplanner.demo.travelplan.dto.TravelPlanRequest;
import com.travelplanner.demo.travelplan.dto.TravelPlanResponse;
import com.travelplanner.demo.travelplan.service.TravelPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Travel Planner", description = "여행 계획 CRUD API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }
        return null;
    }

    @Operation(summary = "여행 계획 생성", description = "새로운 여행 계획을 생성합니다. (JWT 인증 필요)")
    @PostMapping("/travel-plan")
    public ResponseEntity<TravelPlanResponse> createTravelPlan(
            @Parameter(description = "요청 바디", required = true) @Valid @RequestBody TravelPlanRequest request) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        TravelPlanResponse response = travelPlanService.create(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "여행 계획 목록 조회", description = "인증된 사용자의 여행 계획 목록을 조회합니다. (JWT 인증 필요)")
    @GetMapping("/travel-plan")
    public ResponseEntity<List<TravelPlanResponse>> getTravelPlans() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<TravelPlanResponse> responses = travelPlanService.getTravelPlans(userId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "여행 계획 상세 조회", description = "여행 계획 ID로 상세 정보를 조회합니다. (JWT 인증 필요)")
    @GetMapping("/travel-plan/{id}")
    public ResponseEntity<TravelPlanResponse> getTravelPlan(
            @Parameter(description = "여행 계획 ID", example = "1", required = true) @PathVariable Integer id) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        TravelPlanResponse response = travelPlanService.getTravelPlan(id, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "여행 계획 수정", description = "기존 여행 계획을 수정합니다. (JWT 인증 필요)")
    @PutMapping("/travel-plan/{id}")
    public ResponseEntity<TravelPlanResponse> updateTravelPlan(
            @Parameter(description = "여행 계획 ID", example = "1", required = true) @PathVariable Integer id,
            @Valid @RequestBody TravelPlanRequest request) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        TravelPlanResponse response = travelPlanService.updateTravelPlan(id, userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "여행 계획 삭제", description = "여행 계획을 삭제합니다. (JWT 인증 필요)")
    @DeleteMapping("/travel-plan/{id}")
    public ResponseEntity<Void> deleteTravelPlan(
            @Parameter(description = "여행 계획 ID", example = "1", required = true) @PathVariable Integer id) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        travelPlanService.deleteTravelPlan(id, userId);
        return ResponseEntity.noContent().build();
    }
}
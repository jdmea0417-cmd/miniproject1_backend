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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Travel Planner", description = "여행 계획 CRUD API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/travel-plan")
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
    @PostMapping
    public ResponseEntity<?> createTravelPlan(
            @Parameter(description = "요청 바디", required = true) @Valid @RequestBody TravelPlanRequest request) {
        System.out.println(">>>> debug travel plan controller createTravelPlan");
        System.out.println(">>>> debug param : " + request);
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            TravelPlanResponse response = travelPlanService.create(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "여행 계획 목록 조회", description = "인증된 사용자의 여행 계획 목록을 조회합니다. (JWT 인증 필요)")
    @GetMapping
    public ResponseEntity<?> getTravelPlans() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<TravelPlanResponse> responses = travelPlanService.getTravelPlans(userId);
        if (responses.size() != 0) {
            return ResponseEntity.status(HttpStatus.OK).body(responses);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @Operation(summary = "여행 계획 상세 조회", description = "여행 계획 ID로 상세 정보를 조회합니다. (JWT 인증 필요)")
    @GetMapping("/{id}")
    public ResponseEntity<?> getTravelPlan(
            @Parameter(description = "여행 계획 ID", example = "1", required = true) @PathVariable Integer id) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        TravelPlanResponse response = travelPlanService.getTravelPlan(id, userId);
        if (response != null) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @Operation(summary = "여행 계획 수정", description = "기존 여행 계획을 수정합니다. (JWT 인증 필요)")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTravelPlan(
            @Parameter(description = "여행 계획 ID", example = "1", required = true) @PathVariable Integer id,
            @Valid @RequestBody TravelPlanRequest request) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        TravelPlanResponse response = travelPlanService.updateTravelPlan(id, userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "개별 여행지 수정", description = "타임라인에서 특정 목적지 카드 하나를 수정합니다. (JWT 인증 필요)")
    @PutMapping("/destination/{destinationId}")
    public ResponseEntity<?> updateDestination(
            @Parameter(description = "목적지 ID", example = "1", required = true) @PathVariable Integer destinationId,
            @Valid @RequestBody com.travelplanner.demo.destination.dto.DestinationUpdateRequest request) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            travelPlanService.updateDestination(destinationId, userId, request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "여행 계획 삭제", description = "여행 계획을 삭제합니다. (JWT 인증 필요)")
    @DeleteMapping("/{id}")
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
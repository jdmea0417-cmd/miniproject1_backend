package com.travelplanner.demo.travelplan.service;

import com.travelplanner.demo.travelplan.ai.agent.TravelPlanAIAgent;
import com.travelplanner.demo.travelplan.dto.TravelPlanRequest;
import com.travelplanner.demo.travelplan.dto.TravelPlanResponse;
import com.travelplanner.demo.travelplan.entity.TravelPlanEntity;
import com.travelplanner.demo.travelplan.repository.TravelPlanRepository;
import com.travelplanner.demo.user.entity.UserEntity;
import com.travelplanner.demo.user.repository.UserRepository;
import com.travelplanner.demo.destination.dto.DestinationResponse;
import com.travelplanner.demo.destination.dto.DestinationUpdateRequest;
import com.travelplanner.demo.destination.entity.DestinationEntity;
import com.travelplanner.demo.destination.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TravelPlanService {

    private final UserRepository userRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final TravelPlanAIAgent travelPlanAIAgent;
    private final DestinationRepository destinationRepository;

    public TravelPlanResponse create(String userId, TravelPlanRequest request) {
        log.info("여행 계획 생성 요청: userId={}, area={}", userId, request.getArea());

        // AI로 여행 계획 생성 (TravelPlanResponse 반환)
        TravelPlanResponse aiResponse = travelPlanAIAgent.generateTravelPlan(request);

        // UserEntity 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // AI 응답을 Entity로 변환하여 저장
        TravelPlanEntity plan = TravelPlanEntity.builder()
                .user(user)
                .area(request.getArea())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        if (aiResponse.getDestinations() != null) {
            for (DestinationResponse destResp : aiResponse.getDestinations()) {
                DestinationEntity destination = DestinationEntity.builder()
                        .travelPlan(plan)
                        .place(destResp.getPlace())
                        .date(destResp.getDate())
                        .time(destResp.getTime())
                        .build();
                plan.addDestination(destination);
            }
        }

        TravelPlanEntity savedPlan = travelPlanRepository.save(plan);
        return TravelPlanResponse.fromEntity(savedPlan);
    }

    @Transactional(readOnly = true)
    public List<TravelPlanResponse> getTravelPlans(String userId) {
        return travelPlanRepository.findByUser_UserIdOrderByIdDesc(userId).stream()
                .map(TravelPlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TravelPlanResponse getTravelPlan(Integer id, String userId) {
        TravelPlanEntity travelPlan = travelPlanRepository.findByIdAndUser_UserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Travel plan not found: " + id));
        return TravelPlanResponse.fromEntity(travelPlan);
    }

    public TravelPlanResponse updateTravelPlan(Integer id, String userId, TravelPlanRequest request) {
        TravelPlanEntity travelPlan = travelPlanRepository.findByIdAndUser_UserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Travel plan not found: " + id));

        // Update basic fields
        travelPlan.setArea(request.getArea());
        travelPlan.setStartDate(request.getStartDate());
        travelPlan.setEndDate(request.getEndDate());

        // Clear existing destinations and replace with new ones
        travelPlan.getDestinations().clear();

        if (request.getDestinations() != null) {
            for (com.travelplanner.demo.destination.dto.DestinationRequest destReq : request.getDestinations()) {
                DestinationEntity destination = DestinationEntity.builder()
                        .travelPlan(travelPlan)
                        .place(String.join(", ", destReq.getKeywords()))
                        .date(destReq.getDate())
                        .time(destReq.getTime())
                        .build();
                travelPlan.addDestination(destination);
            }
        }

        TravelPlanEntity saved = travelPlanRepository.save(travelPlan);
        return TravelPlanResponse.fromEntity(saved);
    }

    public void deleteTravelPlan(Integer id, String userId) {
        TravelPlanEntity travelPlan = travelPlanRepository.findByIdAndUser_UserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Travel plan not found: " + id));
        travelPlanRepository.delete(travelPlan);
    }

    public void updateDestination(Integer destinationId, String userId, DestinationUpdateRequest request) {
        DestinationEntity destination = destinationRepository.findByIdAndTravelPlan_User_UserId(destinationId, userId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Destination not found or access denied: " + destinationId));

        destination.setPlace(request.getPlace());
        if (request.getDate() != null) {
            destination.setDate(request.getDate());
        }
        if (request.getTime() != null) {
            destination.setTime(request.getTime());
        }

        destinationRepository.save(destination);
    }
}
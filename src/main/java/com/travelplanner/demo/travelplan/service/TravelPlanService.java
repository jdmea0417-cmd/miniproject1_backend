package com.travelplanner.demo.travelplan.service;

import com.travelplanner.demo.travelplan.ai.agent.TravelPlanAIAgent;
import com.travelplanner.demo.travelplan.dto.TravelPlanRequest;
import com.travelplanner.demo.travelplan.dto.TravelPlanResponse;
import com.travelplanner.demo.travelplan.entity.TravelPlanEntity;
import com.travelplanner.demo.travelplan.repository.TravelPlanRepository;
import com.travelplanner.demo.user.entity.UserEntity;
import com.travelplanner.demo.user.repository.UserRepository;
import com.travelplanner.demo.destination.dto.DestinationResponse;
import com.travelplanner.demo.destination.entity.DestinationEntity;
import com.travelplanner.demo.destination.repository.DestinationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TravelPlanService {

    private final UserRepository userRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final DestinationRepository destinationRepository;
    private final TravelPlanAIAgent travelPlanAIAgent;
    private final ObjectMapper objectMapper;

    public TravelPlanResponse create(String userId, TravelPlanRequest request) {
        System.out.println(">>>> debug travel plan service create");
        System.out.println(">>>> debug request param : "+request);
        log.info("여행 계획 생성 요청: userId={}, area={}", userId, request.getArea());

        // AI로 여행 계획 생성 (JSON 문자열 반환)
        TravelPlanResponse response = travelPlanAIAgent.generateTravelPlan(request);

        // UserEntity 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // AI 응답을 Entity로 변환하여 저장
        TravelPlanEntity savedPlan = saveAiResponse(user, request, response);

        // Response로 변환하여 반환
        return toResponse(savedPlan);
    }

    private TravelPlanEntity saveAiResponse(UserEntity user, TravelPlanRequest request, TravelPlanResponse aiResponse) {
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
        return travelPlanRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public List<TravelPlanResponse> getTravelPlans(String userId) {
        return travelPlanRepository.findByUser_UserIdOrderByIdDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TravelPlanResponse getTravelPlan(Integer id, String userId) {
        TravelPlanEntity travelPlan = travelPlanRepository.findByIdAndUser_UserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Travel plan not found: " + id));
        return toResponse(travelPlan);
    }

    public TravelPlanResponse updateTravelPlan(Integer id, String userId, TravelPlanRequest request) {
        TravelPlanEntity travelPlan = travelPlanRepository.findByIdAndUser_UserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Travel plan not found: " + id));

        travelPlan.setArea(request.getArea());
        travelPlan.setStartDate(request.getStartDate());
        travelPlan.setEndDate(request.getEndDate());

        destinationRepository.deleteAll(travelPlan.getDestinations());
        travelPlan.getDestinations().clear();

        if (request.getDestinations() != null && !request.getDestinations().isEmpty()) {
            List<DestinationEntity> destinations = request.getDestinations().stream()
                    .map(destReq -> DestinationEntity.builder()
                            .travelPlan(travelPlan)
                            .place(destReq.getKeyword())
                            .date(request.getStartDate().toString())
                            .time(java.time.LocalTime.now().toString())
                            .build())
                    .collect(Collectors.toList());

            travelPlan.getDestinations().addAll(destinations);
            destinationRepository.saveAll(destinations);
        }

        TravelPlanEntity updated = travelPlanRepository.save(travelPlan);
        return toResponse(updated);
    }

    public void deleteTravelPlan(Integer id, String userId) {
        TravelPlanEntity travelPlan = travelPlanRepository.findByIdAndUser_UserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Travel plan not found: " + id));

        destinationRepository.deleteAll(travelPlan.getDestinations());
        travelPlanRepository.delete(travelPlan);
    }

    private TravelPlanResponse toResponse(TravelPlanEntity travelPlan) {
        List<DestinationResponse> destinations = travelPlan.getDestinations().stream()
                .map(dest -> DestinationResponse.builder()
                        .id(dest.getId())
                        .place(dest.getPlace())
                        .date(dest.getDate())
                        .time(dest.getTime())
                        .build())
                .collect(Collectors.toList());

        return TravelPlanResponse.builder()
                .id(travelPlan.getId())
                .userId(travelPlan.getUser().getUserId())
                .area(travelPlan.getArea())
                .startDate(travelPlan.getStartDate())
                .endDate(travelPlan.getEndDate())
                .destinations(destinations)
                .build();
    }
}
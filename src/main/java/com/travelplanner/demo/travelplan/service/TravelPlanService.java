package com.travelplanner.demo.travelplan.service;

import com.travelplanner.demo.travelplan.ai.agent.TravelPlanAIAgent;
import com.travelplanner.demo.travelplan.ai.dto.AiTravelPlanResponse;
import com.travelplanner.demo.travelplan.dto.TravelPlanRequest;
import com.travelplanner.demo.travelplan.dto.TravelPlanResponse;
import com.travelplanner.demo.travelplan.entity.TravelPlanEntity;
import com.travelplanner.demo.travelplan.repository.TravelPlanRepository;
import com.travelplanner.demo.user.entity.UserEntity;
import com.travelplanner.demo.user.repository.UserRepository;
import com.travelplanner.demo.destination.dto.DestinationResponse;
import com.travelplanner.demo.destination.entity.DestinationEntity;
import com.travelplanner.demo.destination.repository.DestinationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public TravelPlanResponse create(String userId, TravelPlanRequest request) {
        System.out.println(">>>> debug travel plan service create");
        System.out.println(">>>> debug request param : "+request);
        log.info("여행 계획 생성 요청: userId={}, area={}", userId, request.getArea());

        // AI로 여행 계획 생성
        AiTravelPlanResponse aiResponse = travelPlanAIAgent.generatePlan(request);

        // UserEntity 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // AI 응답을 Entity로 변환하여 저장
        TravelPlanEntity savedPlan = saveAiResponse(user, request, aiResponse);

        // Response로 변환하여 반환
        return toResponse(savedPlan);
    }

    private TravelPlanEntity saveAiResponse(UserEntity user, TravelPlanRequest request, AiTravelPlanResponse aiResponse) {
        TravelPlanEntity plan = TravelPlanEntity.builder()
                .user(user)
                .area(request.getArea())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        if (aiResponse.getDailyPlans() != null) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate currentDate = request.getStartDate();

            for (AiTravelPlanResponse.DailyPlan dailyPlan : aiResponse.getDailyPlans()) {
                if (dailyPlan.getSchedule() != null) {
                    for (AiTravelPlanResponse.TimeSlot slot : dailyPlan.getSchedule()) {
                        DestinationEntity destination = DestinationEntity.builder()
                                .travelPlan(plan)
                                .place(slot.getPlace())
                                .date(currentDate.format(dateFormatter))
                                .time(slot.getTime())
                                .build();
                        plan.addDestination(destination);
                    }
                }
                currentDate = currentDate.plusDays(1);
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

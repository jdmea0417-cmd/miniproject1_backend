package com.travelplanner.demo.travelplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelplanner.demo.travelplan.entity.TravelPlanEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlanEntity, Integer> {

    List<TravelPlanEntity> findByUser_UserIdOrderByIdDesc(String userId);
    Optional<TravelPlanEntity> findByIdAndUser_UserId(Integer id, String userId);
}
package com.travelplanner.demo.destination.repository;

import com.travelplanner.demo.destination.entity.DestinationEntity;
import com.travelplanner.demo.travelplan.entity.TravelPlanEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DestinationRepository extends JpaRepository<DestinationEntity, Integer> {

    List<DestinationEntity> findByTravelPlanOrderByDateAscTimeAsc(TravelPlanEntity travelPlan);
}
package com.travelplanner.demo.travelplan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.travelplanner.demo.destination.entity.DestinationEntity;
import com.travelplanner.demo.user.entity.UserEntity;

@Entity
@Table(name = "Travel_Plan_TBL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private UserEntity user;

    @Column(name = "AREA", length = 20, nullable = false)
    private String area;

    @Column(name = "STARTDATE")
    private LocalDate startDate;

    @Column(name = "ENDDATE")
    private LocalDate endDate;

    @OneToMany(mappedBy = "travelPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DestinationEntity> destinations = new ArrayList<>();

    public void addDestination(DestinationEntity destination) {
        destinations.add(destination);
        destination.setTravelPlan(this);
    }
}
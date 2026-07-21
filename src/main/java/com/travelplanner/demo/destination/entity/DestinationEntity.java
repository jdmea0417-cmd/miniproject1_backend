package com.travelplanner.demo.destination.entity;

import com.travelplanner.demo.travelplan.entity.TravelPlanEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Destination_TBL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Travel_ID", nullable = false)
    private TravelPlanEntity travelPlan;

    @Column(name = "WEATHER")
    private String weather;

    @Column(name = "TIME")
    private String time;

    @Column(name = "DATE")
    private String date;

    @Column(name = "PLACE", length = 100)
    private String place;
}
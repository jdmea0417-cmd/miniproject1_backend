package com.travelplanner.demo.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelplanner.demo.user.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    boolean existsByUserId(String userId);
    
    Optional<UserEntity> findByUserId(String userId);
}
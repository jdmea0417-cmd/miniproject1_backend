package com.travelplanner.demo.user.service;

import com.travelplanner.demo.common.service.RedisService;
import com.travelplanner.demo.common.token.JwtProvider;
import com.travelplanner.demo.user.dto.LoginRequest;
import com.travelplanner.demo.user.dto.LoginResponse;
import com.travelplanner.demo.user.dto.RegisterRequest;
import com.travelplanner.demo.user.dto.UserResponse;
import com.travelplanner.demo.user.entity.UserEntity;
import com.travelplanner.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request) {
        System.out.println(">>>> debug user service register");
        System.out.println(">>>> debug request param : "+request);
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User ID already exists: " + request.getUserId());
        }

        UserEntity user = UserEntity.builder()
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();

        UserEntity saved = userRepository.save(user);

        return UserResponse.builder()
                .userId(saved.getUserId())
                .name(saved.getName())
                .password("***")
                .build();
    }

    public LoginResponse login(LoginRequest request) {
                System.out.println(">>>> debug user service login");
        System.out.println(">>>> debug request param : "+request);
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid userId or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid userId or password");
        }

        // Access Token & Refresh Token 생성
        String accessToken = jwtProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());

        // Refresh Token을 Redis에 저장
        redisService.saveRefreshToken(user.getUserId(), refreshToken);

        return LoginResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Refresh Token으로 Access Token 재발급
     */
    public LoginResponse refreshAccessToken(String refreshToken) {
        // Refresh Token 유효성 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String userId = jwtProvider.getUserIdFromRefreshToken(refreshToken);

        // Redis에 저장된 Refresh Token과 일치하는지 확인
        if (!redisService.validateRefreshToken(userId, refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        // 새로운 Access Token & Refresh Token 발급 (Rotation)
        String newAccessToken = jwtProvider.createAccessToken(userId);
        String newRefreshToken = jwtProvider.createRefreshToken(userId);

        // 새로운 Refresh Token 저장 (기존 것 덮어쓰기)
        redisService.saveRefreshToken(userId, newRefreshToken);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return LoginResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(String accessToken) {
        System.out.println(">>>> debug user service logout");
        String userId = jwtProvider.getUserIdFromAccessToken(accessToken);
        redisService.deleteRefreshToken(userId);

        // Access Token을 블랙리스트에 등록 (만료 시간까지 유지)
        long ttlMillis = jwtProvider.getExpiration(accessToken).getTime() - System.currentTimeMillis();
        if (ttlMillis > 0) {
            redisService.addToBlacklist(accessToken, ttlMillis);
        }
    }

    public UserResponse getUserById(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .password("***")
                .build();
    }
}
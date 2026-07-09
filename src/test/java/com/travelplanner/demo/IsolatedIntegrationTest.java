package com.travelplanner.demo;

import com.travelplanner.demo.common.token.JwtProvider;
import com.travelplanner.demo.user.dto.LoginRequest;
import com.travelplanner.demo.user.dto.LoginResponse;
import com.travelplanner.demo.user.dto.RegisterRequest;
import com.travelplanner.demo.user.dto.UserResponse;
import com.travelplanner.demo.user.service.UserService;
import com.travelplanner.demo.travelplan.dto.TravelPlanRequest;
import com.travelplanner.demo.travelplan.dto.TravelPlanResponse;
import com.travelplanner.demo.travelplan.service.TravelPlanService;
import com.travelplanner.demo.destination.dto.DestinationRequest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@Transactional
class IsolatedIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TravelPlanService travelPlanService;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @MockitoBean
    private ValueOperations<String, String> valueOperations;

    @Test
    void testFullWorkflow() {
        System.out.println("=================================================");
        System.out.println("시작: 통합 시나리오 기능 및 보안 테스트 (Mock Redis)");
        System.out.println("=================================================");

        // Setup Mocking for Redis template
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(any(String.class), any(String.class), anyLong(), any());

        // [1] 회원가입 테스트
        String testUserId = "testuser"; // Length is 8, fitting the 20 chars limit
        RegisterRequest registerReq = RegisterRequest.builder()
                .userId(testUserId)
                .password("securePassword123")
                .name("테스터")
                .build();

        UserResponse registeredUser = userService.register(registerReq);
        assertNotNull(registeredUser);
        assertEquals(testUserId, registeredUser.getUserId());
        assertEquals("테스터", registeredUser.getName());
        System.out.println("[PASS] 1. 회원가입 기능 정상 작동 (ID: " + testUserId + ")");

        // [2] 로그인 테스트
        LoginRequest loginReq = LoginRequest.builder()
                .userId(testUserId)
                .password("securePassword123")
                .build();

        LoginResponse loginRes = userService.login(loginReq);
        assertNotNull(loginRes);
        assertNotNull(loginRes.getAccessToken());
        assertNotNull(loginRes.getRefreshToken());
        assertEquals(testUserId, loginRes.getUserId());
        System.out.println("[PASS] 2. 로그인 및 토큰 발급 완료");

        // [3] 공통 모듈: 보안 및 JWT 토큰 검증 작동 확인
        String token = loginRes.getAccessToken();
        assertTrue(jwtProvider.validateToken(token));
        String parsedUserId = jwtProvider.getUserIdFromAccessToken(token);
        assertEquals(testUserId, parsedUserId);
        System.out.println("[PASS] 3. JWT Provider 공통모듈 검증 및 파싱 테스트 완료");

        // [4] 여행일정 추가 테스트
        DestinationRequest dest1 = DestinationRequest.builder()
                .keywords(Arrays.asList("경복궁", "광화문"))
                .build();
        DestinationRequest dest2 = DestinationRequest.builder()
                .keywords(Arrays.asList("해운대", "해수욕장")) // Keep keywords short as well
                .build();

        TravelPlanRequest planReq = TravelPlanRequest.builder()
                .area("서울/부산") // Length is 5, fits the 20 chars limit
                .startDate(LocalDate.of(2026, 7, 8))
                .endDate(LocalDate.of(2026, 7, 12))
                .destinations(Arrays.asList(dest1, dest2))
                .build();

        TravelPlanResponse planRes = travelPlanService.createTravelPlan(testUserId, planReq);
        assertNotNull(planRes);
        assertEquals("서울/부산", planRes.getArea());
        assertNotNull(planRes.getDestinations());
        assertEquals(2, planRes.getDestinations().size());
        System.out.println("[PASS] 4. 여행 일정 및 키워드 기반 목적지 추가 테스트 완료");

        // [5] 보안 관리 (BCrypt 비밀번호 매칭 확인)
        assertTrue(passwordEncoder.matches("securePassword123", passwordEncoder.encode("securePassword123")));
        assertFalse(passwordEncoder.matches("wrongPassword", passwordEncoder.encode("securePassword123")));
        System.out.println("[PASS] 5. 보안 관리 - BCrypt 암호화 검증 완료");

        System.out.println("=================================================");
        System.out.println("종료: 모든 기능 및 보안 테스트 성공");
        System.out.println("=================================================");
    }
}

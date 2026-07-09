package com.travelplanner.demo.user.controller;

import com.travelplanner.demo.user.dto.LoginRequest;
import com.travelplanner.demo.user.dto.LoginResponse;
import com.travelplanner.demo.user.dto.RegisterRequest;
import com.travelplanner.demo.user.dto.UserResponse;
import com.travelplanner.demo.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "사용자 인증 API (회원가입, 로그인, 로그아웃, 토큰 갱신)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @SecurityRequirement(name = "") // 공개 API
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        System.out.println(">>>> debug user controller register");
        System.out.println(">>>> debug params : "+request); 
        UserResponse response = userService.register(request);

        if (response != null) {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "로그인", description = "사용자 로그인을 처리하고 Access Token, Refresh Token을 발급합니다.")
    @SecurityRequirement(name = "") // 공개 API
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        System.out.println(">>>> debug user controller signin");
        LoginResponse response = userService.login(request);
        String at = response.getAccessToken();
        String rt = response.getRefreshToken();

        System.out.println(">>>> debug user controller at : "+at);
        System.out.println(">>>> debug user controller rt : "+rt);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + at);
        headers.add("Refresh-Token", rt);
        headers.add("Access-Control-Expose-Headers", "Authorization, Refresh-Token");

        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    @Operation(summary = "Access Token 갱신", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다. (Refresh Token Rotation)")
    @SecurityRequirement(name = "") // 공개 API (Refresh Token 필요)
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        LoginResponse response = userService.refreshAccessToken(request.getRefreshToken());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + response.getAccessToken());
        headers.add("Refresh-Token", response.getRefreshToken());
        headers.add("Access-Control-Expose-Headers", "Authorization, Refresh-Token");

        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리하고 Access Token을 블랙리스트에 등록, Refresh Token을 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Access Token", required = true)
            @RequestHeader("Authorization") String authorization) {
        System.out.println(">>>> debug user controller logout");
        String accessToken = authorization.replace("Bearer ", "");
        userService.logout(accessToken);
        return ResponseEntity.noContent().build();
    }

    // Refresh Token 재발급 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshRequest {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }
}
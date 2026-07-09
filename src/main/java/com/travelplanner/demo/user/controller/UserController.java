package com.travelplanner.demo.user.controller;

import com.travelplanner.demo.user.dto.LoginRequest;
import com.travelplanner.demo.user.dto.LoginResponse;
import com.travelplanner.demo.user.dto.RegisterRequest;
import com.travelplanner.demo.user.dto.UserResponse;
import com.travelplanner.demo.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "사용자 인증 API (회원가입, 로그인, 로그아웃)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        System.out.println(">>>> debug user controller register");
        System.out.println(">>>> debug params : "+request); 
        UserResponse response = userService.register(request);
        
        if(response != null ) {
            return ResponseEntity.status(HttpStatus.CREATED).build() ;     
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build() ; 
        }
    }

    @Operation(summary = "로그인", description = "사용자 로그인을 처리하고 Access Token, Refresh Token을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        System.out.println(">>>> debug user controller login "); 
        System.out.println(">>>> debug params : "+request); 
        LoginResponse response = userService.login(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + response.getAccessToken());
        headers.add("Refresh-Token", response.getRefreshToken());
        headers.add("Access-Control-Expose-Headers", "Authorization, Refresh-Token");

        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리하고 Refresh Token을 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Access Token", required = true)
            @RequestHeader("Authorization") String authorization) {
        String accessToken = authorization.replace("Bearer ", "");
        userService.logout(accessToken);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자 조회", description = "사용자 ID로 사용자 정보를 조회합니다.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }
}
package com.travelplanner.demo.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Schema(description = "로그인 응답 (Access Token, Refresh Token 포함)")
public class LoginResponse {

    @Schema(description = "사용자 ID", example = "user01")
    private String userId;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "Access Token (JWT)")
    private String accessToken;

    @Schema(description = "Refresh Token (JWT)")
    private String refreshToken;
}
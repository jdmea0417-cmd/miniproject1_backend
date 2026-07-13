package com.travelplanner.demo.user.dto;

import com.travelplanner.demo.user.entity.UserEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Schema(description = "회원가입 요청")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RegisterRequest {

    @Schema(description = "사용자 ID", example = "user123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "User ID is required")
    @Size(min = 1, max = 20, message = "User ID must be between 1 and 20 characters")
    private String userId;

    @Schema(description = "비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 1, max = 20, message = "Password must be between 1 and 20 characters")
    private String password;

    @Schema(description = "이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 20, message = "Name must be between 1 and 20 characters")
    private String name;

    public static UserEntity toEntity(RegisterRequest request) {
        return UserEntity.builder()
                    .userId(request.getUserId()) 
                    .password(request.getPassword())
                    .name(request.getName())
                    .build() ;
    }
}
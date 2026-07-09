package com.travelplanner.demo.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8000}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        // JWT Bearer Token 보안 스키마 정의
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT Authorization header using the Bearer scheme. Example: \"Bearer {token}\"");

        // 전역 보안 요구사항 (전역적으로 Bearer Auth 요구)
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("Travel Planner API")
                        .version("v1.0.0")
                        .description("여행 플래너 백엔드 API 문서")
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("로컬 개발 서버"),
                        new Server().url("https://api.travelplanner.example.com").description("프로덕션 서버")
                ))
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth));
                // 전역 보안 요구사항 미적용 → 각 컨트롤러/메서드에 @SecurityRequirement로 개별 지정
    }
}
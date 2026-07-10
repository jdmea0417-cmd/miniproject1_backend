package com.travelplanner.demo.common.filter;

import com.travelplanner.demo.common.service.RedisService;
import com.travelplanner.demo.common.token.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    private static final List<String> WHITE_LIST = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // CORS preflight 요청은 통과
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 화이트리스트 경로는 토큰 검증 없이 통과
        if (isWhiteList(path)) {
            log.debug("WhiteList path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더 확인
        String authHeader = request.getHeader("Authorization");
        log.debug("Auth header: {}", authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);
        log.debug("Extracted token: {}...", token.substring(0, Math.min(20, token.length())));

        // 토큰 검증
        if (!jwtProvider.validateToken(token)) {
            log.warn("Token validation failed");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 블랙리스트 체크 (로그아웃된 토큰 차단)
        if (redisService.isBlacklisted(token)) {
            log.warn("Token is blacklisted");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 사용자 ID 추출 및 SecurityContext에 인증 정보 설정
        String userId = jwtProvider.getUserIdFromAccessToken(token);
        log.debug("Authenticated userId: {}", userId);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of()
        );

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream()
                .anyMatch(pattern -> {
                    if (pattern.endsWith("/**")) {
                        String prefix = pattern.substring(0, pattern.length() - 3);
                        return path.startsWith(prefix);
                    }
                    return path.equals(pattern);
                });
    }
}
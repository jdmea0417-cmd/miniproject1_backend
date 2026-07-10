# Travel Planner Backend — 코드 분석서

**분석 대상:** `C:\inspire\miniproject_backend\demo`
**프로젝트:** LG CNS INSPIRE Bootcamp 1차 프로젝트 (여행 계획 웹앱 백엔드)
**분석일:** 2026-07-09

---

## 1. 프로젝트 개요

지도에서 선택한 장소를 중심으로 여행 계획을 세우는 웹앱의 백엔드.
Layered Architecture (Controller → Service → Repository) + JWT 인증 + Redis(Refresh Token 저장).

### 기술 스택
| 구분 | 내용 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.4 |
| ORM | Spring Data JPA (Hibernate) |
| DB | MariaDB (`ddl-auto: update`) |
| 인증 | Spring Security + JWT (jjwt 0.11.5) |
| 캐시/토큰 | Redis (Refresh Token) |
| 문서화 | SpringDoc OpenAPI 2.7.0 (Swagger UI) |
| 기타 | Lombok, Validation, dotenv-java 3.0.0 |
| 포트 | 8000 |

### 패키지 구조 (도메인형)
```
com.travelplanner.demo
├── common/           # 공통 인프라
│   ├── config/       # SecurityConfig, OpenApiConfig, DotEnvEnvironmentPostProcessor
│   ├── filter/       # JwtAuthenticationFilter
│   ├── token/        # JwtProvider
│   └── service/      # RedisService
├── user/             # 회원/인증 도메인
├── travelplan/       # 여행 계획 도메인
└── destination/      # 여행지 도메인
```

---

## 2. 도메인 모델 (ERD 관점)

```
User_TBL (1) ──< Travel_Plan_TBL (1) ──< Destination_TBL
   PK userId          PK id (auto)          PK id (auto)
                      FK userId(문자열)      FK Travel_ID
```

- **UserEntity**: PK = `userId`(String, 자연키). password(BCrypt), name.
- **TravelPlan**: PK = `id`(IDENTITY). `userId`는 **FK 관계가 아닌 단순 String 컬럼**. destinations와 `@OneToMany(cascade=ALL, orphanRemoval=true)`.
- **DestinationEntity**: `@ManyToOne` → TravelPlan. place, date(String), time(String).

> ⚠️ User ↔ TravelPlan은 JPA 연관관계가 아니라 문자열 userId로만 연결됨(느슨한 결합). 의도적일 수 있으나 참조 무결성은 DB가 보장 못 함.

---

## 3. 인증 흐름 (핵심)

### 3.1 회원가입 `POST /api/v1/auth/register`
UserService.register → 중복 체크(existsByUserId) → BCrypt 인코딩 → 저장 → 201.

### 3.2 로그인 `POST /api/v1/auth/login`
1. userId로 조회, `passwordEncoder.matches`로 검증
2. Access Token(9h) + Refresh Token(7d) 생성
3. Refresh Token을 Redis `RT:{userId}`에 TTL 7일 저장
4. 응답: **바디(LoginResponse) + 헤더(Authorization, Refresh-Token)** 이중 전달

### 3.3 인증된 요청
`JwtAuthenticationFilter`(OncePerRequestFilter)가 SecurityFilterChain 앞단에서:
- OPTIONS / 화이트리스트 → 통과
- `Authorization: Bearer` 없거나 검증 실패 → 401
- 성공 → `SecurityContextHolder`에 `userId`를 principal(String)로 세팅

컨트롤러는 `SecurityContextHolder`에서 userId를 꺼내 소유권 기반 CRUD 수행.

### 3.4 로그아웃 `POST /api/v1/auth/logout`
Access Token에서 userId 추출 → Redis에서 Refresh Token 삭제.

---

## 4. 발견된 이슈 (심각도순)

### 🔴 Critical

**C-1. `.env`에 실제 시크릿 커밋 위험**
`.env`에 `DB_PASSWORD`, `OPEN_AI_KEY`(sk-...), `JWT_SECRET_KEY`가 평문 존재. README는 gitignore 등록됐다고 하나, 실제 `.git`에 커밋됐는지 반드시 확인 필요. OpenAI 키가 유출되면 과금 피해.
→ `git log --all -- .env`로 이력 확인, 유출됐다면 **키 즉시 폐기/재발급**.

**C-2. Refresh Token 미검증 / 재발급 엔드포인트 부재**
로그인 시 Refresh Token을 Redis에 저장하지만, 이를 사용해 Access Token을 재발급하는 API가 **아예 없음**. `RedisService.validateRefreshToken()`은 정의됐으나 **어디서도 호출 안 됨(dead code)**. Access Token 9시간 만료 후 재로그인 외 방법 없음 → Refresh Token 도입 목적이 무의미.

### 🟠 High

**H-1. 로그아웃이 Access Token 블랙리스트 처리 안 함**
로그아웃은 Redis의 Refresh Token만 삭제. 이미 발급된 Access Token은 만료(최대 9시간)까지 계속 유효 → 실질적 로그아웃 불가. 블랙리스트(Redis) 또는 토큰 버전 관리 필요.

**H-2. 전역 예외 처리기 부재**
README엔 `common/exception/GlobalExceptionHandler` 언급되나 **실제 파일 없음**. 서비스가 던지는 `IllegalArgumentException`("User not found", "Invalid password" 등)이 모두 **500 Internal Server Error**로 반환됨. 400/401/404로 매핑하는 `@RestControllerAdvice` 필요.

**H-3. `@Valid` 미적용 → Validation 무력화**
DTO에 `@NotBlank`, `@Size` 등 제약이 걸려 있으나, 컨트롤러 파라미터에 `@Valid`/`@Validated`가 **하나도 없음**. 결과적으로 모든 Bean Validation이 동작 안 함(검증 껍데기만 존재).
→ `register(@Valid @RequestBody RegisterRequest ...)` 등 전 컨트롤러 수정 필요.

**H-4. 로그인 시 존재하지 않는 userId와 잘못된 비밀번호 구분 노출**
"User not found" vs "Invalid password" 메시지가 다름 → 사용자 열거(enumeration) 공격 가능. 동일 메시지("아이디 또는 비밀번호가 올바르지 않습니다")로 통일 권장.

### 🟡 Medium

**M-1. 민감정보 콘솔 로깅**
`UserController`에서 `System.out.println`으로 로그인 요청 파라미터, Access/Refresh Token 전체를 출력. 비밀번호(request.toString())와 토큰이 로그에 남음. 제거 또는 로거+마스킹 필요.

**M-2. 날짜/시간 설계 부실**
- `TravelPlanRequest`에 startDate/endDate(LocalDate) 받지만 **TravelPlan 엔티티에 저장 안 함**. Response DTO의 startDate/endDate는 **항상 null**로 반환됨.
- Destination의 date/time을 `LocalTime.now()`, `startDate.toString()`으로 채움 → 실제 여행 일정과 무관한 값. 설계 의도 불명확.

**M-3. `extractPlaceFromKeywords` = 키워드 단순 join**
DestinationRequest는 keywords 리스트를 받지만, place에 `String.join(", ", keywords)`로 붙일 뿐. 지도/장소 검색 로직 없음(미완성으로 추정).

**M-4. OpenAI 의존성 설정만 존재, 미사용**
`application.yml`에 `spring.ai.openai` 설정 있으나 `build.gradle`에 **spring-ai 의존성 없음**, 코드에서 OpenAI 호출 없음. 죽은 설정.

**M-5. MyBatis 의존성 있으나 미사용**
`mybatis-spring-boot-starter` 추가됐으나 Mapper/XML 전무. JPA만 사용 중 → 불필요한 의존성.

### 🟢 Low

- **L-1.** `DestinationRepository.findByTravelPlanOrderByDateAscTimeAsc` 정의됐으나 미사용(date/time이 String이라 정렬도 사전식).
- **L-2.** `TravelPlan.addDestination()` 편의 메서드 정의됐으나 서비스에서 미사용(대신 `getDestinations().addAll()` 직접 조작).
- **L-3.** CORS를 `Customizer.withDefaults()`로만 설정 → `CorsConfigurationSource` Bean 없음. 프론트(별도 origin) 연동 시 CORS 커스텀 설정 필요 가능성.
- **L-4.** `UserResponse`에 password 필드(항상 "***") 포함 → 응답에 불필요.
- **L-5.** id 타입 `Integer` 사용(대량 데이터 시 Long 권장).
- **L-6.** `ddl-auto: update` — 운영 환경엔 부적합(schema 마이그레이션 도구 권장).

---

## 5. 잘된 점

- 도메인별 패키지 분리 명확, Layered 구조 일관성 유지.
- Entity ↔ DTO 분리 철저(요청/응답 DTO 구분).
- BCrypt 비밀번호 해싱 적용.
- `EnvironmentPostProcessor`로 `.env` 로드 — application.yml 파싱 전에 주입되도록 정석 구현.
- Stateless 세션 정책 + JWT 필터 체인 배치 정확.
- Swagger 문서화(`@Operation`, `@Schema`) 꼼꼼.
- 소유권 검증(`findByIdAndUserId`)으로 타 사용자 데이터 접근 차단.

---

## 6. 우선 조치 권장 순서

1. **(C-1)** `.env` git 커밋 여부 확인 → 유출 시 키 재발급 + `.gitignore` 점검
2. **(H-3)** 컨트롤러에 `@Valid` 추가 (검증 활성화)
3. **(H-2)** `GlobalExceptionHandler` 구현 (에러 → 적절한 HTTP status)
4. **(C-2)** Refresh Token 재발급 엔드포인트 추가 (`/api/v1/auth/refresh`)
5. **(H-1)** 로그아웃 시 Access Token 블랙리스트
6. **(M-1)** 콘솔 로깅 제거/마스킹
7. **(M-2)** 여행 날짜 저장 로직 정비 (엔티티에 startDate/endDate 필드 추가)
8. **(M-4/M-5)** 미사용 의존성(spring-ai 설정, mybatis) 정리

---

*이 문서는 정적 코드 분석 기준이며, 실제 런타임 동작은 DB/Redis 연결 상태에 따라 달라질 수 있음.*

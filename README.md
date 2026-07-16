# 프로젝트 소개 

여행플래너는 AI를 활용해 사용자의 여행 일정을 생성하고 관리할 수 있는 여행 계획 서비스입니다.

사용자는 여행 지역, 기간, 원하는 여행지를 입력하여 일정 초안을 생성할 수 있으며, 생성된 일정은 직접 수정하거나 삭제하는 등 자유롭게 관리할 수 있습니다. 또한 일정별 날씨 정보를 함께 제공하여 여행 계획을 더욱 편리하게 확인할 수 있습니다.

여행 일정 생성부터 관리까지 하나의 서비스에서 제공하여, 보다 쉽고 효율적인 여행 계획 수립을 지원하는 것을 목표로 합니다.




# 미니프로젝트 백엔드 (Spring Boot 3.5.4)

## 개요

- Java 17, Spring Boot 3.5.4 기반 여행 계획 백엔드
- MariaDB (주요 데이터), Redis (캐시 및 JWT 블랙리스트)
- JWT 인증 (jjwt 0.11.5), Stateless 인증 + 리프레시 토큰 로테이션
- Spring Security (필터 기반) + GlobalExceptionHandler
- MyBatis 3.0.4 (XML 매퍼 또는 어노테이션)
- SpringDoc OpenAPI 2.7.0 (Swagger UI)
- Spring AI (OpenAI 1.0.0-M6) 활용한 AI 여행 일정 생성
- Lombok, Dotenv (cdimascio:dotenv-java) + EnvironmentPostProcessor로 .env 로드
- 서버 포트: 8000 (application.properties에서 설정 가능)
- 도커/WSL2 환경에서 host.docker.internal 사용 권장

## 주요 기능
- 회원가입 / 로그인 (`/api/v1/auth/register`, `/api/v1/auth/login`)
- JWT 발급 (액세스 토큰 9시간, 리프레시 토큰 7일, 로테이션 + 블랙리스트)
- 여행 계획 CRUD (`/api/v1/travel-planner`):
  - POST: 새 계획 생성 (AI 기반 일정 생성 포함)
  - GET: 사용자 자신의 계획 목록 페이징
  - GET/{id}: 특정 계획 상세 조회
  - PUT/{id}: 계획 수정
  - DELETE/{id}: 계획 삭제 (JWT 검증 및 소유자 확인)
- AI 일정 생성 엔드포인트 (`/api/v1/ai/recommend`): area, 날짜, 선호도, 동행인 수, 예산, 특수 요청 입력 → 구조화된 일정 JSON 반환
- Swagger UI: `http://<host>:8000/swagger-ui.html`
- 글로벌 예외 처리 및 에러 응답 표준화 (ErrorResponse)
- 요청 파라미터 디버그 로그 (디버그 레벨 활성화 시)
- 보안 화이트리스트: 인증 불필요 경로 (`/api/v1/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`)

## 디렉터리 구조 (핵심)
```
src/main/java/com/travelplanner/demo/
 ├─ common/              # 공통 설정, 예외, 필터, 토큰 utils
 │    ├─ config/
 │    ├─ exception/
 │    ├─ filter/
 │    ├─ service/ (RedisService 등)
 │    ├─ token/
 │    └─ utils/
 ├─ destination/         # 여행지 엔티티, DTO, 레포지토리, 컨트롤러
 ├─ travelplan/          # 여행 계획 엔티티, DTO, 레포지토리, 컨트롤러, 서비스, AI 연동
 └─ user/                # 사용자 엔티티, 레포지토리
```

## 환경 변수 (`.env`)
```
# DB
DB_URL=jdbc:mariadb://host.docker.internal:3306/travelplanner?useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=your_user
DB_PASSWORD=your_password

# Redis
REDIS_HOST=host.docker.internal
REDIS_PORT=6379

# JWT
JWT_SECRET=your_strong_secret_key_change_me
JWT_ACCESS_TOKEN_EXPIRE_MINUTES=540   # 9시간
JWT_REFRESH_TOKEN_EXPIRE_DAYS=7

# OpenAI (Spring AI)
OPENAI_API_KEY=your_openai_api_key

# 기타
SERVER_PORT=8000
```

> **Note**: `src/main/resources/META-INF/spring.factories` 또는 `src/main/resources/META-INF/spring/org.springframework.boot.factory.SpringFactoriesLoader`에 `EnvironmentPostProcessor` 구현 클래스를 등록하여 `.env` 파일을 읽어 환경 변수에 주입합니다.

## 빌드 및 실행
```bash
# Gradle 래퍼 사용 (프로젝트 루트에서)
./gradlew clean build   # 빌드
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar   # 실행

# 또는 Docker
docker build -t travel-planner-backend .
docker run -p 8000:8000 --env-file .env travel-planner-backend
```

## API 문서
- 서버 실행 후 Swagger UI: `http://localhost:8000/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8000/v3/api-docs`

## 주요 설정 파일
- `src/main/resources/application.properties` (또는 `application.yml`): 기본 프로퍼티
- `src/main/java/com/travelplanner/demo/common/config/DotEnvEnvironmentPostProcessor.java`: .env 로드
- `src/main/java/com/travelplanner/demo/common/config/SecurityConfig.java`: JWT 필터 및 Stateless 설정
- `src/main/java/com/travelplanner/demo/common/config/OpenApiConfig.java`: SpringDoc 설정

## 테스트
- 단위 테스트: JUnit 5 + Mockito (테스트 디렉터리 `src/test`)
- 통합 테스트: `@SpringBootTest` + `TestContainers` (선택 사항)

## 트러블슈팅
- **DB 연결 실패**: `host.docker.internal`이 호스트 머신의 도커 게이트웨이를 가리키는지 확인 (WSL2에서는 보통 작동).
- **JWT 토큰 검증 오류**: 시크릿 키가 일치하는지, 토큰 만료 시각 확인.
- **Redis 연결 실패**: Redis가 실행 중인지, 포트 및 호스트 확인.
- **AI 호출 오류**: OpenAI API 키 유효성 및 quota 확인, 네트워크 프록시 설정.
- **배포 시 프로파일**: `application-prod.properties` 등 프로파일별 설정 활용 가능.

## 라이선스
MIT

# [테스트 로드맵 및 계획]
- 작성일: 2026-07-08
- 대상 프로젝트: miniproject1_backend
- 테스트 환경 구성: H2 Memory Database, Mockito Framework (Redis Mocking)
- 담당자: 황윤서

---

## 📌 테스트 계획 요약
1. **[1단계] 격리 통합 테스트 (완료)**
   - 외부 인프라가 미설치된 환경에서 비즈니스 로직(회원가입 -> 로그인 -> 토큰 발급 -> 암호화 대조 -> 여행 일정 추가)의 논리적인 통합 흐름을 고속으로 검증.
2. **[2단계] API 컨트롤러 테스트 (계획)**
   - `MockMvc`를 활용하여 요청 검증 및 잘못된 데이터 유형 입력 시 `400 Bad Request` 에러 응답 유효성 검사.
3. **[3단계] DB Repository 전용 테스트 (계획)**
   - Custom JPA Repository 메소드의 쿼리 정상 작동 여부 검사.
4. **[4단계] 실서버 연동 통합 테스트 (계획)**
   - 실제 MariaDB 데이터베이스와 캐싱 Redis 연동 흐름 확인.

---
      -- 이하는 필수테스트 아닌 권장 사항 --
---




5. **[5단계] 예외(Edge Case) 테스트 (계획)**
   - 비즈니스 흐름 중 발생할 수 있는 데이터 정합성 위배 및 비정상 요청에 대한 예외 처리를 구체적으로 검증. (상세 내역 하단 기술)
   - 2단계는 요청 형식 및 Validation을 검증하며, 5단계는 Service 계층의 비즈니스 예외를 검증한다.

---

## ⚡ [5단계] 예외(Edge Case) 구체적 테스트 항목

사용자 관리(`UserService`) 및 여행 계획(`TravelPlanService`) 로직 중 발생할 수 있는 주요 예외 케이스들을 정의하고 검증하도록 구성합니다.

### 1. 사용자 관련 예외 (User Entity Exceptions)

*   **T-U1: 중복 회원가입 예외 테스트**
    *   **조건:** 이미 존재하는 User ID(`testuser`)로 다시 회원가입(`register`)을 요청할 때.
    *   **기대 예외:** `IllegalArgumentException` 발생
    *   **메시지 검증:** `"User ID already exists: testuser"`

*   **T-U2: 존재하지 않는 사용자 로그인 예외 테스트**
    *   **조건:** DB에 저장되지 않은 User ID(`unknownuser`)로 로그인(`login`)을 요청할 때.
    *   **기대 예외:** `IllegalArgumentException` 발생
    *   **메시지 검증:** `"User not found: unknownuser"`

*   **T-U3: 비밀번호 불일치 로그인 예외 테스트**
    *   **조건:** 존재하는 User ID에 대해 잘못된 패스워드로 로그인(`login`)을 요청할 때.
    *   **기대 예외:** `IllegalArgumentException` 발생
    *   **메시지 검증:** `"Invalid password"`

*   **T-U4: 존재하지 않는 사용자 정보 조회 예외 테스트**
    *   **조건:** 로그인 세션/토큰 파싱은 통과했으나 (ex- 사용자가 로그인한 후 계정이 삭제됨) 실제 DB에는 존재하지 않는 ID로 `getUserById`를 조회할 때.
    *   **기대 예외:** `IllegalArgumentException` 발생
    *   **메시지 검증:** `"User not found: <userId>"`

---

### 2. 여행 계획 관련 예외 (TravelPlan Entity Exceptions)

*   **T-P1: 타인의 여행 계획 조회 예외 테스트**
    *   **조건:** 사용자 A가 만든 여행 일정 ID를 사용자 B가 조회(`getTravelPlan`)하려고 할 때.
    *   **기대 예외:** `IllegalArgumentException` 발생
    *   **메시지 검증:** `"Travel plan not found: <id>"` (ID와 User ID 복합 조건으로 조회 실패 유도)

*   **T-P2: 존재하지 않는 여행 계획 수정 예외 테스트**
    *   **조건:** 임의의 잘못된 여행 계획 ID를 입력하여 수정(`updateTravelPlan`)을 요청할 때.
    *   **기대 예외:** `IllegalArgumentException` 발생
    *   **메시지 검증:** `"Travel plan not found: <id>"`

*   **T-P3: 존재하지 않는 여행 계획 삭제 예외 테스트**
    *   **조건:** 이미 삭제되었거나 존재하지 않는 여행 계획 ID로 삭제(`deleteTravelPlan`)를 요청할 때.
    *   **기대 예외:** `IllegalArgumentException` 발생
    *   **메시지 검증:** `"Travel plan not found: <id>"`

---

## 🔍 [추가 권장] 수동 코드 리뷰 및 성능 검증 항목

자동화 테스트 스위트 외에 프로젝트 완성도를 높이기 위해 보완적으로 진행하는 영역입니다.

### 1. 코드 리뷰 요구사항 (수동 검증)

*   **보안 설정 검토**
    *   인증 및 인가(`SecurityConfig`, JWT Filter)가 요구사항에 맞게 적용되었는지 확인한다.
    *   공개 API와 권한이 필요한 API의 접근 제어가 올바르게 설정되었는지 검토한다.
*   **트랜잭션 및 데이터 처리 검토**
    *   데이터 변경 로직에 적절한 `@Transactional`이 적용되었는지 확인한다.
    *   조회 전용 로직의 트랜잭션 설정과 연관 객체(Lazy Loading) 사용이 적절한지 검토한다.
*   **예외 처리 검토**
    *   비즈니스 예외가 적절한 HTTP 상태 코드와 응답 형식으로 처리되는지 확인한다.
    *   예상하지 못한 시스템 예외가 사용자에게 내부 정보 없이 안전하게 처리되는지 검토한다.

### 2. 성능 테스트 (추후 고려 가능)



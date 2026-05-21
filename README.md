# Minidooray Team8 Gateway

Minidooray 프로젝트의 관문 역할을 하는 API Gateway 서비스입니다. Spring Cloud Gateway를 기반으로 구축되었으며, 마이크로서비스 아키텍처(MSA)에서 요청 라우팅과 세션 검증을 담당합니다.

## 주요 기능

1.  **API 라우팅 (Routing)**:
    *   `/accounts/**` -> Account API (`localhost:8081`)
    *   `/projects/**` -> Task API (`localhost:8082`)
2.  **세션 검증 (Session Validation)**:
    *   `GlobalFilter`인 `SessionValidationFilter`를 통해 모든 API 요청에 대해 Redis 세션 유효성을 검사합니다.
    *   유효한 세션인 경우, 세션에서 `USER_ID`를 추출하여 백엔드 서비스가 사용할 수 있도록 `X-User-Id` HTTP 헤더를 주입합니다.
3.  **로그인/회원가입 예외 처리**:
    *   로그인 및 회원가입 관련 경로(`/accounts/login`, `/accounts/signup`)는 세션 검증 없이 통과되도록 설정되어 있습니다.

## 기술 스택

*   **Framework**: Spring Boot 3.x, Spring Cloud Gateway
*   **Reactive Stack**: Spring WebFlux, Project Reactor
*   **Storage**: Redis (Session Store)
*   **Build Tool**: Maven

## 아키텍처 구조

현재 프로젝트는 **Front-First** 구조를 따르고 있습니다.

1.  **사용자**는 Front 서비스(`:8080`)에 접속합니다.
2.  **Front 서비스**에서 인증(로그인)이 수행되며, 세션은 공유 **Redis**에 저장됩니다.
3.  Front 서비스가 데이터를 조회하기 위해 **Gateway(`:8000`)**로 요청을 보냅니다.
4.  **Gateway**는 쿠키에 담긴 `SESSION` 값을 읽어 Redis에서 유효성을 확인한 후, 백엔드 API로 라우팅합니다.

## 주요 설정

### `application.properties`
```properties
server.port=8000
spring.application.name=minidooray-team8-gateway

# Redis 설정
spring.data.redis.host=10.116.64.14
spring.data.redis.port=6379
spring.data.redis.database=35
spring.session.store-type=redis
```

### 라우팅 설정 (`RouteLocatorConfig`)
모든 API 요청은 게이트웨이의 `8000` 포트를 통해 적절한 서비스로 배분됩니다.

## 필터 상세: `SessionValidationFilter`

이 필터는 게이트웨이의 핵심 보안 계층입니다.

1.  **세션 쿠키 추출**: 요청에서 `SESSION` 쿠키를 찾습니다.
2.  **Base64 디코딩**: Spring Session 형식에 맞춰 쿠키 값을 디코딩하여 실제 세션 ID를 얻습니다.
3.  **Redis 조회**: `spring:session:sessions:{sessionId}` 키를 사용하여 Redis에서 `sessionAttr:USER_ID` 해시 필드를 조회합니다.
4.  **헤더 주입**: 조회된 유저 ID를 `X-User-Id` 헤더에 담아 다음 필터나 서비스로 전달합니다.
5.  **예외 처리**: 세션이 없거나 유효하지 않으면 `401 Unauthorized`를 반환합니다.

## 시작하기

1.  Redis 서버가 실행 중인지 확인합니다.
2.  `mvn clean package` 명령어로 빌드합니다.
3.  `java -jar target/minidooray-team8-gateway-0.0.1-SNAPSHOT.jar` 명령어로 실행합니다.

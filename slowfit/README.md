# SlowFit - 슬로우조깅&식단 커뮤니티

슬로우조깅과 식단 관리를 함께 할 수 있는 커뮤니티 플랫폼입니다.

---

## 프로젝트 소개

**SlowFit**은 슬로우조깅 애호가들과 식단 관리에 관심 있는 사용자들이 경험과 정보를 공유할 수 있는 커뮤니티 플랫폼입니다. 사용자들은 게시판을 통해 조깅 기록과 식단 정보를 공유하고, 실시간 채팅으로 커뮤니티 멤버들과 소통할 수 있습니다.

### 핵심 기능

- 📝 **게시판**: JOGGING, DIET, CHAT 세 가지 카테고리의 게시판
- 💬 **댓글 및 멘션**: 게시물에 댓글을 작성하고 특정 사용자를 멘션
- ❤️ **좋아요 기능**: 게시물에 좋아요를 표시
- 💬 **실시간 채팅**: WebSocket 기반 실시간 커뮤니티 채팅
- 👤 **사용자 관리**: 회원가입, 로그인, 권한 관리 (ADMIN/USER)

---

## 기술 스택

### Backend

- **Framework**: Spring Boot 3.5.16
- **Language**: Java 17
- **Database**: MariaDB
- **Cache**: Redis
- **Authentication**: JWT (JSON Web Token)
- **WebSocket**: Spring WebSocket + STOMP
- **API Documentation**: Springdoc OpenAPI (Swagger UI)
- **Security**: Spring Security
- **ORM**: JPA/Hibernate
- **Build**: Gradle

### Frontend

- **Framework**: React 18+
- **Language**: TypeScript
- **Build Tool**: Vite
- **HTTP Client**: Axios
- **Routing**: React Router
- **UI Library**: Material-UI (MUI)
- **WebSocket Client**: SockJS

### DevOps

- **Version Control**: Git
- **Repository**: GitHub

### AI 필터링

- **Text Filtering**: OpenAI Moderation API - **Image Filtering**: Google Cloud Vision API

## 주요 기능

### 1. 사용자 관리

- 회원가입 (username, password, name, age, gender, email)
- 로그인 (JWT 토큰 기반 인증)
- 역할 기반 접근 제어 (ADMIN/USER)
- ADMIN: 모든 게시글/댓글 삭제 가능

### 2. 게시판 시스템

- **멀티 카테고리 게시판**
  - JOGGING: 슬로우조깅 기록 및 정보 공유 (페이스, 코스 URL, 이미지 업로드)
  - DIET: 식단 관리 및 영양 정보 (이미지 업로드)
  - CHAT: 실시간 채팅방으로 이동
- **페이지네이션**: 게시글 목록 페이지네이션 (기본 10개씩)
- **조회수**: Redis INCR로 카운트 → 1분마다 DB 동기화
- **좋아요**: Redis Set으로 중복 방지 → 1분마다 DB 동기화
- **Redis 캐싱**: 게시글 상세 조회 캐싱 (TTL 10분)

### 3. 게시글 기능

- 게시글 작성, 수정, 삭제
- 페이스 입력 (JOGGING 전용, 예: 7m30s/km)
- 코스 URL 입력 (JOGGING 전용, 링크로 표시)
- 이미지 업로드 (Google Vision API SafeSearch 필터링)
- 텍스트 필터링 (OpenAI Moderation API)

### 4. 댓글 시스템

- 게시글에 댓글 작성
- **사용자 멘션**: 댓글 작성자 클릭 시 @username 자동 입력
- 댓글 작성자/관리자만 삭제 가능
- 텍스트 필터링 (OpenAI Moderation API)

### 5. 실시간 채팅

- WebSocket을 통한 실시간 메시징
- STOMP 프로토콜 기반 메시지 라우팅
- 사용자 입장/퇴장 메시지
- 채팅 메시지 타임스탬프

### 6. AI 콘텐츠 필터링

- 텍스트 필터링: 게시글/댓글 작성 시 OpenAI Moderation API로 욕설·혐오·폭력 콘텐츠 감지 및 차단
- 이미지 필터링: 이미지 업로드 시 Google Cloud Vision SafeSearch로 부적절한 이미지 차단 (LIKELY 이상)
- @Async 비동기 처리로 응답 지연 최소화

---

## 아키텍처

### Backend 구조

```
com.slowfit.slowfit/
├── domain/
│   ├── user/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── entity/
│   │   ├── dto/
│   │   └── repository/
│   ├── post/
│   │   ├── controller/ (PostController, FileUploadController)
│   │   ├── service/ (PostService, RedisPostService)
│   │   ├── entity/
│   │   ├── dto/
│   │   └── repository/
│   ├── comment/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── entity/
│   │   ├── dto/
│   │   └── repository/
│   └── chat/
│       ├── controller/
│       └── dto/
└── global/
    ├── config/
    │   ├── SecurityConfig.java
    │   ├── SwaggerConfig.java
    │   ├── WebSocketConfig.java
    │   ├── RedisConfig.java
    │   ├── RedisCacheConfig.java
    │   ├── AsyncConfig.java
    │   ├── WebConfig.java
    │   └── security/
    │       └── JwtFilter.java
    └── service/
        ├── ImageModerationService.java
        └── TextModerationService.java
```

### Frontend 구조

```
client/src/
├── pages/
│   ├── LoginPage.tsx
│   ├── SignupPage.tsx
│   ├── BoardPage.tsx
│   ├── PostWritePage.tsx
│   ├── PostDetailPage.tsx
│   └── ChatPage.tsx
├── components/
│   └── Navbar.tsx
├── assets/
│   └── background.jpg
├── types/
│   └── sockjs-client.d.ts
├── App.tsx
├── index.css
└── main.tsx
```

### 데이터 흐름

1. **인증 흐름**
   - 사용자 로그인 → JWT 토큰 발급 → 클라이언트 저장
   - 모든 API 요청에 Authorization 헤더로 토큰 전달

2. **게시글 조회 흐름**
   - 클라이언트 요청 → Controller → Service → Redis 캐시 확인
   - 캐시 히트: Redis에서 즉시 반환 (TTL 10분)
   - 캐시 미스: DB 조회 → Redis 캐시 저장 → 응답
   - 페이지네이션 처리 (Pageable)

3. **조회수/좋아요 기능**
   - 조회수: Redis INCR로 카운트 → @Scheduled 1분마다 DB 동기화
   - 좋아요: Redis Set으로 중복 방지 → @Scheduled 1분마다 DB 동기화

4. **이미지 업로드 흐름**
   - 이미지 업로드 → 서버 로컬 저장 → Google Vision API SafeSearch 검사
   - LIKELY 이상 부적절 이미지 → 파일 삭제 + 400 에러 반환

5. **텍스트 필터링 흐름**
   - 게시글/댓글 작성 → OpenAI Moderation API 검사
   - flagged: true → 저장 거부 + 에러 반환

6. **실시간 채팅**
   - WebSocket 연결 → STOMP 핸드셰이크
   - 메시지 발행 → 브로드캐스트 → 모든 클라이언트에 전달

---

## 트러블슈팅

### 1. Spring Security httpBasic 충돌로 JWT 인증 실패

**문제**: JWT 토큰을 Authorization 헤더에 넣어도 계속 401 에러 발생

**원인**: SecurityConfig에 httpBasic이 활성화되어 있어서 JwtFilter보다 httpBasic 인증이 먼저 막는 문제

**해결**: SecurityConfig에서 httpBasic 비활성화

```java
http.httpBasic(httpBasic -> httpBasic.disable())
```

### 2. React에서 SockJS `global is not defined` 에러

**문제**: 채팅 페이지 접속 시 흰 화면만 나오고 콘솔에 `global is not defined` 에러 발생

**원인**: SockJS가 브라우저 환경에서 Node.js의 `global` 변수를 찾아서 생기는 문제

**해결**: index.html `<head>` 태그 안에 추가

```html
<script>
  window.global = window;
</script>
```

### 3. Docker Desktop WSL 메모리 부족

**문제**: Docker Desktop 실행 시 무한 로딩 또는 WSL 에러 발생

**원인**: 시스템 메모리 부족으로 WSL 가상머신 생성 실패

**해결**: 불필요한 프로세스 종료 후 컴퓨터 재시작

### 4. Redis 컨테이너 자동 종료

**문제**: 컴퓨터 재시작 후 Redis 연결 실패

**원인**: Docker 컨테이너가 자동으로 중지됨

**해결**: Docker Desktop에서 redis 컨테이너 수동으로 재시작

```bash
docker start redis
```

### 5. React 라우트 경로 오타

**문제**: 게시글 상세 페이지 클릭 시 빈 화면 표시

**원인**: App.tsx에서 `/post/:id`로 설정했는데 BoardPage에서 `/posts/{id}`로 이동

**해결**: App.tsx 경로를 `/posts/:id`로 통일

### 6. BCryptPasswordEncoder Bean 충돌

**문제**: Spring 실행 시 BCryptPasswordEncoder Bean 중복 에러

**원인**: UserService에서 `new BCryptPasswordEncoder()`로 직접 생성하고 SecurityConfig에서도 Bean으로 등록

**해결**: SecurityConfig에서 PasswordEncoder Bean으로 등록하고 UserService에서 생성자 주입으로 변경

---

### 7. Redis 캐싱 LocalDateTime 직렬화 오류

**문제**: Redis 캐싱 적용 후 LocalDateTime 직렬화 오류로 에러율 100% 발생

**원인**: GenericJackson2JsonRedisSerializer가 LocalDateTime을 직렬화하지 못함

**해결**: RedisCacheConfig에서 ObjectMapper에 JavaTimeModule 등록 및 WRITE_DATES_AS_TIMESTAMPS 비활성화

### 8. BoardType ENUM 변경 시 DB 오류

**문제**: CERTIFICATION → CHAT으로 변경 후 서버 에러 발생

**원인**: ddl-auto: update 설정으로 테이블은 유지되는데 ENUM 값이 변경되어 불일치 발생

**해결**: HeidiSQL에서 직접 ALTER TABLE로 ENUM 값 변경

```sql
ALTER TABLE posts MODIFY COLUMN board_type ENUM('JOGGING', 'DIET', 'CHAT') NOT NULL;
```

## 부하 테스트 결과 (JMeter, 동시 1000명)

### 게시글 목록 조회

- 평균 응답시간: 48ms
- 에러율: 0%
- Throughput: 101.3/sec

### 게시글 상세 조회 - 캐싱 전

- 평균 응답시간: 190ms
- 에러율: 29.6%

### 게시글 상세 조회 - 캐싱 후 (Redis Cache 적용)

- 평균 응답시간: 19ms
- 에러율: 1.3%
- Throughput: 98.7/sec
- Redis 캐싱으로 응답시간 10배 개선, 에러율 22배 감소

## 실행 방법

### 사전 요구사항

- Java 17 이상
- Node.js 18 이상
- MariaDB 10.5 이상
- Redis 6 이상

### Backend 실행

1. **데이터베이스 및 Redis 실행**

   ```bash
   # MariaDB 시작
   mysql -u root -p

   # Redis 시작 (Docker)
    docker run -d --name redis -p 6379:6379 redis
   ```

2. **application.yaml 설정**

   ```yaml
   spring:
     datasource:
       url: jdbc:mariadb://localhost:3306/slowfit
       username: root
       password: your-password
       driver-class-name: org.mariadb.jdbc.Driver

     redis:
       host: localhost
       port: 6379
   ```

3. **애플리케이션 실행**

   ```bash
   cd slowfit
   ./gradlew bootRun
   ```

4. **Swagger UI 접근**
   - http://localhost:9090/swagger-ui/index.html

### Frontend 실행

1. **의존성 설치**

   ```bash
   cd client
   npm install
   ```

2. **개발 서버 시작**

   ```bash
   npm run dev
   ```

3. **브라우저 접근**
   - http://localhost:5173

---

## API 엔드포인트

### 사용자 API

- `POST /api/users/signup` - 회원가입
- `POST /api/users/login` - 로그인

### 게시물 API

- `GET /api/posts` - 게시물 목록 조회 (페이지네이션)
- `GET /api/posts?boardType=JOGGING` - 카테고리별 조회
- `GET /api/posts/{id}` - 게시물 상세 조회
- `POST /api/posts` - 게시물 작성
- `PUT /api/posts/{id}` - 게시물 수정
- `DELETE /api/posts/{id}` - 게시물 삭제
- `POST /api/posts/{id}/like` - 좋아요 토글

### 댓글 API

- `GET /api/posts/{postId}/comments` - 댓글 목록 조회
- `POST /api/posts/{postId}/comments` - 댓글 작성
- `DELETE /api/comments/{commentId}` - 댓글 삭제

### 채팅 API

- `WS /ws` - WebSocket 연결
- `/app/chat/{roomId}` - 메시지 발행
- `/topic/chat/{roomId}` - 메시지 구독

### 파일 API

- `POST /api/upload` - 이미지 업로드 (Google Vision API 필터링 포함)

### 기타

- `GET /api/posts/health` - 헬스체크

---

## 라이선스

MIT License

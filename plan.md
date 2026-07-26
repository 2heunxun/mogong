# 모공 (모아봐요 공부팀!) — 개발 계획서

> Claude Code 참고용 프로젝트 명세. `project_stack_plan.md` 기반. 실시간 채팅은 **카카오 오픈채팅 연동**으로 대체(자체 WebSocket 미사용).

## 1. 프로젝트 개요

**모공**은 공부 파티(스터디)를 모집하고, 파티별 소통은 카카오 오픈채팅으로 이어주는 동적 웹 서비스다.

핵심 기능 3가지:

1. **카카오 OAuth 로그인** — 카카오 계정으로 간편 가입/로그인
2. **공부파티 모집 CRUD** — 스터디 파티 생성·조회·수정·삭제 + 참여/탈퇴
3. **카카오 오픈채팅 연동** — 파티마다 오픈채팅 링크를 등록, 파티원이 링크로 입장

> 설계 결정: 인앱 실시간 채팅(WebSocket/STOMP)은 만들지 않는다. Render 무료 티어 spin-down으로 WebSocket 연결이 끊기는 문제를 피하고, 카카오톡의 검증된 채팅 UX를 그대로 활용하기 위함.

## 2. 기술 스택

기본 스택은 `project_stack_plan.md`를 따르되, 채팅을 오픈채팅으로 대체하며 WebSocket/STOMP/Redis를 뺐다.

### Frontend
- React + React Router
- Axios (REST 통신)
- Tailwind CSS
- 배포: **Vercel**

### Backend
- Spring Boot
- Spring Security + **OAuth2 Client** (카카오 로그인)
- JWT (자체 토큰 발급: Access + Refresh)
- Spring Data JPA
- Validation
- Flyway (DB 마이그레이션)
- 배포: **Render**

### Database / Infra
- PostgreSQL — **Supabase** (무료)
- CI/CD: GitHub Actions
- ~~Redis~~ — 채팅을 오픈채팅으로 대체하면서 초기 단계에선 불필요. 추후 캐싱 필요 시 재검토

## 3. 시스템 아키텍처

```text
                사용자
                  │
              HTTPS(REST)
                  │
      Vercel ───────────► Render
      (React)          (Spring Boot)
                            │
              ┌─────────────┼─────────────┐
             JPA                        OAuth2
              │                       (카카오 API)
          Supabase
         (PostgreSQL)

   채팅: 파티 상세에서 오픈채팅 링크 클릭 → 카카오톡 앱으로 이동(외부)
```

- 백엔드는 순수 REST API만 담당 (WebSocket 없음 → 구조가 단순)
- 프론트/백 도메인 분리 → **CORS 설정 필수**

## 4. 데이터 모델

### User
| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long (PK) | 내부 식별자 |
| kakaoId | Long (unique) | 카카오 회원번호 |
| nickname | String | 닉네임 |
| profileImageUrl | String (nullable) | 프로필 이미지 |
| role | Enum(USER, ADMIN) | 권한 |
| createdAt / updatedAt | Timestamp | |

### StudyParty (공부파티)
| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long (PK) | |
| owner | User (FK) | 파티장 |
| title | String | 제목 |
| description | Text | 소개 |
| category | String / Enum | 과목·분야 (예: 코딩테스트, 어학, 자격증) |
| capacity | Integer | 최대 인원 |
| status | Enum(RECRUITING, CLOSED) | 모집 상태 |
| **openChatUrl** | String | **카카오 오픈채팅 초대 링크** |
| createdAt / updatedAt | Timestamp | |
- `openChatUrl`은 `https://open.kakao.com/...` 형식 **검증** 권장

### PartyMember (파티 참여)
| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long (PK) | |
| party | StudyParty (FK) | |
| user | User (FK) | |
| joinedAt | Timestamp | |
- `(party_id, user_id)` **복합 unique 제약** — 중복 참여 방지

> `ChatMessage` 엔티티 없음 — 채팅은 카카오톡이 담당.

## 5. API 설계

### 인증 (Auth)
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/auth/kakao/login-url` | 카카오 인가 URL 반환 (또는 프론트에서 직접 구성) |
| POST | `/api/auth/kakao` | 인가 코드 → 자체 JWT 발급 |
| POST | `/api/auth/refresh` | Refresh 토큰으로 Access 재발급 |
| POST | `/api/auth/logout` | 로그아웃 (Refresh 무효화) |
| GET | `/api/users/me` | 내 정보 조회 |

### 공부파티 (Study Party)
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/parties` | 목록 (페이징·검색·카테고리 필터) |
| GET | `/api/parties/{id}` | 상세 |
| POST | `/api/parties` | 생성 (파티장 자동 참여, openChatUrl 포함) |
| PUT | `/api/parties/{id}` | 수정 (파티장만) |
| DELETE | `/api/parties/{id}` | 삭제 (파티장만) |
| POST | `/api/parties/{id}/join` | 참여 (정원·중복·상태 검증) |
| DELETE | `/api/parties/{id}/leave` | 탈퇴 |
| GET | `/api/parties/{id}/members` | 파티원 목록 |

> 채팅 관련 REST/WebSocket 엔드포인트 없음. `openChatUrl`은 파티 상세 응답에 포함되며, **파티원에게만 노출**하는 정책 권장(아래 6-3 참고).

## 6. 기능별 구현 상세

### 6-1. 카카오 OAuth 로그인
프론트/백 도메인이 분리되어 있으므로 **인가 코드 릴레이(Authorization Code Relay)** 방식을 권장한다.

1. React가 카카오 인가 URL로 리다이렉트
2. 카카오가 프론트 `redirect_uri`로 `code` 전달
3. React가 `code`를 백엔드 `POST /api/auth/kakao`로 전송
4. 백엔드가 `code` → 카카오 토큰 교환 → 카카오 사용자 정보 조회
5. `kakaoId`로 회원 **upsert** 후 **자체 JWT(Access + Refresh) 발급**
6. React는 Access 토큰을 메모리/상태에, Refresh 토큰을 관리 (아래 참고)

**토큰 저장 전략**
- Access: 짧은 만료(예: 30분), 프론트 메모리 보관
- Refresh: **httpOnly + Secure + SameSite=None 쿠키** 권장 (XSS 방어). 초기엔 body 응답 후 저장소 보관도 가능하나 XSS 리스크 인지할 것
- 카카오 앱 등록 시 **Redirect URI에 로컬/Vercel 도메인 모두 등록** 필요

### 6-2. 공부파티 CRUD
- 생성 시 파티장을 `PartyMember`로 자동 등록, `openChatUrl` 함께 입력받음
- 참여 시 검증: ① 정원 초과 여부 ② 중복 참여 여부 ③ `status == RECRUITING`
- 수정/삭제는 **파티장 권한 체크** (`owner == 현재 사용자`)
- 목록은 페이징 + 카테고리/키워드 필터 + 정렬(최신순)

### 6-3. 카카오 오픈채팅 연동
- 파티장이 **카카오톡 앱에서 오픈채팅방을 직접 생성** → 초대 링크 복사 → 파티 생성/수정 시 `openChatUrl`에 등록
- (참고: 오픈채팅방을 서버에서 자동 생성하는 공개 API는 없음. 링크 수동 등록이 현실적인 방식)
- 파티 상세 화면에서 **"오픈채팅 입장하기"** 버튼 → 새 창으로 `openChatUrl` 오픈
- **노출 정책**: `openChatUrl`은 해당 파티에 **참여한 사용자에게만** 응답/노출 (미참여자에겐 숨김) → 무단 링크 유출 방지
- URL 형식 검증(`open.kakao.com`)으로 잘못된 링크 등록 방지

## 7. 개발 순서 (권장)

1. **프로젝트 셋업** — Spring Boot 초기화, React(Vite) 초기화, Flyway 기본 마이그레이션
2. **엔티티 & DB 연동** — User/StudyParty/PartyMember + Supabase 연결
3. **카카오 OAuth + JWT 인증** — 로그인 플로우 완성, `/api/users/me`까지
4. **공부파티 CRUD** — API + React 화면 (목록/상세/생성/참여)
5. **오픈채팅 연동** — `openChatUrl` 입력/검증 + 파티원 전용 노출 + 입장 버튼
6. **CORS/보안 정리 & 배포** — Vercel + Render 배포, 환경변수 세팅
7. **CI/CD** — GitHub Actions로 빌드·배포 자동화
8. **운영 & 기능 확장** — 이미지 업로드(Cloudinary), 모니터링(Actuator) 등

## 8. 배포 & 주의사항

### Render 무료 티어
- **무료 웹 서비스는 15분 무활동 시 spin down** → 다음 요청 때 **30~60초 콜드 스타트** 발생.
- WebSocket을 쓰지 않으므로 **연결 끊김 문제는 없음.** 첫 요청이 잠깐 느린 것만 감수하면 됨.
- 실사용 트래픽이 붙으면 백엔드만 **Render 유료(Starter, 약 $7/월)**로 올려 spin-down 제거 가능.

### DB
- **Supabase PostgreSQL 사용**. Render 자체 무료 Postgres는 만료 이슈가 있으니 쓰지 않는다.

### CORS
- Vercel(프론트) ↔ Render(백) 도메인 분리 → allowed origins 설정 필요.

## 9. 환경변수 (예시)

**Backend**
```
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=
KAKAO_REDIRECT_URI=
JWT_SECRET=
JWT_ACCESS_EXPIRATION=
JWT_REFRESH_EXPIRATION=
SPRING_DATASOURCE_URL=        # Supabase 연결 문자열
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
CORS_ALLOWED_ORIGINS=         # Vercel 도메인
```

**Frontend**
```
VITE_API_BASE_URL=            # Render 백엔드 URL
VITE_KAKAO_CLIENT_ID=
VITE_KAKAO_REDIRECT_URI=
```

## 10. 디렉토리 구조 (제안)

```text
moa-study/
├── backend/                  # Spring Boot
│   └── src/main/java/.../
│       ├── auth/             # 카카오 OAuth, JWT
│       ├── user/
│       ├── party/            # 공부파티 CRUD, openChatUrl
│       ├── global/           # config, exception, security, cors
│       └── ...
│   └── src/main/resources/db/migration/   # Flyway
└── frontend/                 # React (Vite)
    └── src/
        ├── api/              # axios 인스턴스, API 함수
        ├── pages/
        ├── components/
        ├── hooks/            # useAuth 등
        └── ...
```

---

### 요약
`카카오 OAuth + JWT` 인증 위에 `공부파티 CRUD`를 얹고, 채팅은 **카카오 오픈채팅 링크 연동**으로 처리한다.
WebSocket을 걷어내면서 백엔드가 순수 REST로 단순해졌고, Render 무료 spin-down도 콜드 스타트만 감수하면 되는 수준으로 완화됐다. 전부 무료(Vercel + Render + Supabase)로 시작 가능.

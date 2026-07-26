# 모공 (모아봐요 공부팀!)

카카오 오픈채팅으로 소통하는 스터디 파티 모집 서비스. 상세 기획은 [`plan.md`](./plan.md) 참고.

- **Backend**: Spring Boot 4 / Java 21 / Spring Security (JWT) / Spring Data JPA / Flyway — `backend/`
- **Frontend**: React 19 (Vite) / React Router / Axios / Tailwind CSS 4 — `frontend/`

## 빠른 시작 (로컬 실행)

별도 설정 없이 바로 실행할 수 있도록 로컬 기본값이 구성되어 있습니다. (DB: 파일 기반 H2, 카카오 로그인 대신 "개발용 로그인" 사용 가능)

### 1) 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

- 첫 실행 시 Gradle Wrapper가 Gradle 배포판을 내려받습니다.
- `local` 프로파일이 기본으로 활성화되며, `backend/data/moa_study.mv.db`에 파일 기반 H2 DB가 생성되고 Flyway 마이그레이션이 자동 적용됩니다.
- 서버: http://localhost:8080 , H2 콘솔: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/moa_study`, 사용자 `sa`, 비밀번호 없음)
- DB를 초기화하려면 `backend/data/` 폴더를 삭제 후 재실행하세요.

### 2) 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

- http://localhost:5173 에서 확인.
- `frontend/.env`의 `VITE_API_BASE_URL`이 백엔드 주소(기본 `http://localhost:8080`)를 가리켜야 합니다.

### 3) 로그인해서 기능 확인하기

카카오 앱을 아직 등록하지 않았어도 전체 기능(파티 생성/참여/오픈채팅 링크 노출/수정/삭제)을 바로 확인할 수 있도록 **개발용 로그인**을 제공합니다.

- 로그인 화면(`/login`)에서 닉네임만 입력하고 "개발용 로그인" 클릭 → 즉시 로그인됩니다.
- 이 기능은 백엔드가 `local` 프로파일(`app.dev-login-enabled=true`)일 때만 동작하며, `prod` 프로파일에서는 자동으로 비활성화됩니다(`POST /api/auth/dev-login` → 403).

## 카카오 로그인 실제 연동하기

1. [Kakao Developers](https://developers.kakao.com)에서 앱 생성 후 **REST API 키** 발급.
2. "카카오 로그인" 활성화, **Redirect URI**에 프론트 콜백 주소 등록:
   - 로컬: `http://localhost:5173/auth/kakao/callback`
   - 배포: `https://<vercel-domain>/auth/kakao/callback`
3. 동의항목에서 닉네임 / 프로필 사진 등을 설정.
4. 백엔드 환경변수에 반영:
   ```
   KAKAO_CLIENT_ID=<REST API 키>
   KAKAO_CLIENT_SECRET=<필요 시>
   KAKAO_REDIRECT_URI=<위에서 등록한 Redirect URI와 동일해야 함>
   ```
5. 로그인 화면의 "카카오로 시작하기" 버튼으로 정식 플로우 테스트.

인증 플로우는 인가 코드 릴레이 방식입니다: 프론트가 카카오 인가 URL로 리다이렉트 → 카카오가 프론트 콜백으로 `code` 전달 → 프론트가 `POST /api/auth/kakao`로 코드 전송 → 백엔드가 카카오 토큰 교환/사용자 조회 후 자체 JWT(Access+Refresh) 발급.

## 환경변수

- `backend/.env.example`, `frontend/.env.example` 참고.
- Spring Boot는 `.env` 파일을 자동으로 읽지 않습니다. 로컬은 `application.yml`의 기본값으로 동작하고, 실제 배포(Render 등)에서는 플랫폼의 환경변수 설정 화면에 직접 입력하세요.

## 배포

- **Frontend → Vercel**, **Backend → Render**, **DB → Supabase(PostgreSQL)**. 자세한 배경은 `plan.md` 8절 참고.
- 배포 시 백엔드 프로파일을 `prod`로 설정(`SPRING_PROFILES_ACTIVE=prod`)하고 `SPRING_DATASOURCE_*`(Supabase 접속 정보), `CORS_ALLOWED_ORIGINS`(Vercel 도메인), `KAKAO_*`, `JWT_SECRET`을 실제 값으로 채워주세요.
- Render 무료 티어는 15분 미사용 시 슬립 → 첫 요청이 30~60초 느릴 수 있습니다(WebSocket을 쓰지 않으므로 연결 끊김은 없음).

## 데이터 모델 요약

- **User**: 카카오 계정 기반 회원 (`kakaoId` unique)
- **StudyParty**: 공부파티. `openChatUrl`은 **파티에 참여한 사용자에게만** 응답에 포함됩니다(비참여자에게는 `null`).
- **PartyMember**: 파티 참여 이력. `(party_id, user_id)` unique.

전체 API 목록은 `plan.md` 5절 참고 (구현 완료: 인증 5종, 공부파티 CRUD + 참여/탈퇴/멤버목록 8종).

## 디렉토리 구조

```text
backend/    Spring Boot (auth, user, party, global)
frontend/   React + Vite (api, hooks, components, pages)
plan.md     기획/설계 문서
```

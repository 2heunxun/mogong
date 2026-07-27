# 카카오 로그인 연동 체크리스트

이 문서는 "카카오 로그인 정상 구동"을 목표로, **사용자(운영자)가 카카오 개발자 콘솔에서 해야 할 일**과
**로컬(그리고 나중에 배포 시) 환경변수 설정**을 정리한 것입니다. 코드는 이미 구현되어 있고, 아래 값들을
채워 넣기만 하면 됩니다.

## 0. 현재 코드가 기대하는 흐름 (참고용)

1. 프론트 로그인 페이지 → 백엔드 `GET /api/auth/kakao/login-url` 호출 → 카카오 인가 페이지로 리다이렉트
2. 사용자가 카카오에서 로그인/동의 → 카카오가 `KAKAO_REDIRECT_URI`(프론트 콜백 주소)로 `code`를 실어서 리다이렉트
3. 프론트 `/auth/kakao/callback` 페이지가 `code`를 백엔드 `POST /api/auth/kakao`로 전달
4. 백엔드가 카카오 서버와 통신해 `access_token` 발급 → 사용자 정보(닉네임, 프로필 사진) 조회 → 우리 서비스의 JWT 발급

즉, **프론트는 카카오 키를 전혀 알 필요가 없고**, 백엔드만 카카오 키를 갖고 있으면 됩니다.

## 1. 카카오 개발자 콘솔에서 해야 할 일

https://developers.kakao.com → 내 애플리케이션 → (없다면) 애플리케이션 추가

### 1) 앱 키 확인
- **REST API 키**를 복사해둡니다 → 이게 `KAKAO_CLIENT_ID`로 들어갑니다.
  - "네이티브 앱 키" / "JavaScript 키" / "Admin 키"는 이 프로젝트에서 쓰지 않습니다. 헷갈리지 않게 REST API 키만 챙기세요.

### 2) 카카오 로그인 활성화
- 좌측 메뉴 `제품 설정 > 카카오 로그인` → **활성화 설정 ON**

### 3) Redirect URI 등록
- 같은 화면(`카카오 로그인`)의 **Redirect URI**에 아래를 정확히 등록:
  - 로컬 개발: `http://localhost:5173/auth/kakao/callback`
  - (나중에 배포하면) 실제 프론트 도메인의 동일 경로도 추가로 등록. 예: `https://mogong.example.com/auth/kakao/callback`
- 트레일링 슬래시, http/https, 포트번호까지 **한 글자도 다르면 안 됩니다.** (`redirect_uri mismatch` 에러의 90%가 여기서 남)

### 4) 플랫폼(Web) 도메인 등록 (권장)
- `앱 설정 > 플랫폼 > Web 플랫폼 등록`에 `http://localhost:5173` 추가
  - REST API 방식(지금 코드 방식)에서는 필수는 아니지만, 카카오 쪽 정책/검증에 걸리는 경우가 있어 등록해두는 걸 권장합니다.

### 5) 동의항목(스코프) 설정
- `제품 설정 > 카카오 로그인 > 동의항목`에서 아래 두 개를 **필수 동의**로 설정:
  - `닉네임 (profile_nickname)`
  - `프로필 사진 (profile_image)`
- 코드가 이 두 값만 사용합니다(이메일 등은 요청하지 않음). 닉네임에 동의하지 않으면 서버가 `모공유저{id}` 같은 임시 닉네임으로 대체하니, **필수 동의**로 걸어두는 걸 권장.

### 6) Client Secret (선택이지만 강력 권장)
- `제품 설정 > 카카오 로그인 > 보안`에서 **Client Secret 생성**
- 코드 활성화 상태 = 반드시 "사용함(ON)"으로 설정해야 함
- 생성된 값을 `KAKAO_CLIENT_SECRET`에 넣습니다.
- **주의**: 콘솔에서 "사용함"으로 켜놓고 서버에 `KAKAO_CLIENT_SECRET`을 안 넣으면 토큰 발급이 실패합니다. 반대로 콘솔에서 "사용 안 함"이면 서버 쪽 값은 비워둬도 됩니다(코드가 값이 있을 때만 요청에 포함시킴). **둘 중 하나로 일치시키세요.**

### 7) 테스트 계정 등록 (제일 많이 놓치는 부분)
- 앱이 아직 "검수 완료 전(개발 중)" 상태라면, **카카오 개발자 콘솔에 등록된 팀 멤버 계정만 로그인이 가능**합니다.
- `내 애플리케이션 > 팀 관리` (또는 앱 설정 내 "사용자 관리/테스터") 에서 로그인 테스트에 쓸 카카오 계정을 추가해두세요.
- 등록 안 된 계정으로 로그인 시도하면 `KOE006` 등의 에러가 납니다. "코드가 안 됨"이 아니라 "테스터 등록 안 됨"인 경우가 매우 흔하니 제일 먼저 의심하세요.
- 실제 서비스로 일반 사용자에게 열려면 카카오의 "비즈니스 앱 전환" 및 검수 절차가 필요합니다(지금 로컬 개발 단계에서는 불필요).

## 2. 내가(운영자가) 채워야 하는 값 정리

| 값 | 어디서 얻나 | 어디에 넣나 |
|---|---|---|
| `KAKAO_CLIENT_ID` | 카카오 콘솔 앱 요약 정보 → REST API 키 | `backend/.env` |
| `KAKAO_CLIENT_SECRET` | 카카오 로그인 > 보안 > Client Secret (활성화 시) | `backend/.env` (Client Secret 비활성화라면 비워둠) |
| `KAKAO_REDIRECT_URI` | 직접 정하는 값, 콘솔의 Redirect URI와 100% 동일해야 함 | `backend/.env` |

## 3. 로컬 환경변수 설정 방법

`backend/.env.example`을 복사해서 `backend/.env`를 만드세요 (이미 `.gitignore`에 `.env`가 포함되어 있어 커밋되지 않습니다).

```bash
cd backend
cp .env.example .env
```

`.env` 파일을 열어 아래 3개만 채웁니다 (나머지는 로컬 기본값 그대로 둬도 됩니다):

```
KAKAO_CLIENT_ID=발급받은_REST_API_키
KAKAO_CLIENT_SECRET=발급받은_Client_Secret   # 비활성화했다면 비워둠
KAKAO_REDIRECT_URI=http://localhost:5173/auth/kakao/callback
```

### ⚠️ 중요: Spring Boot는 `.env` 파일을 자동으로 읽지 않습니다

`.env` 파일을 만들어두는 것만으로는 적용되지 않습니다. 실행 전에 **실제 셸 환경변수로 export** 해줘야 합니다.

**방법 A — 터미널에서 실행할 때 (가장 간단)**

```bash
cd backend
set -a
source .env
set +a
./gradlew bootRun
```

**방법 B — 한 줄로**

```bash
cd backend
env $(grep -v '^#' .env | xargs) ./gradlew bootRun
```

**방법 C — IntelliJ로 실행할 때**

Run/Debug Configuration → `Environment variables` 필드에 `KAKAO_CLIENT_ID=...;KAKAO_CLIENT_SECRET=...;KAKAO_REDIRECT_URI=...`
직접 입력하거나, "EnvFile" 플러그인을 설치해 `backend/.env`를 자동으로 물려주는 방식도 가능합니다.

프론트엔드는 카카오 관련 값이 **필요 없습니다.** 평소처럼 `npm run dev`로 켜면 됩니다.

## 4. 다 설정한 뒤 확인하는 방법

1. 백엔드 기동 후, 로그인 URL이 제대로 만들어지는지 확인:
   ```bash
   curl http://localhost:8080/api/auth/kakao/login-url
   ```
   응답의 `loginUrl`에 본인이 넣은 `client_id`와 `redirect_uri`가 그대로 보이는지 확인하세요.

2. 프론트(`npm run dev`)와 백엔드를 같이 띄운 뒤, `http://localhost:5173/login`에서 **"카카오로 시작하기"** 클릭
3. 카카오 로그인/동의 화면 → 동의 → `http://localhost:5173/auth/kakao/callback?code=...`로 리다이렉트되며 자동 로그인
4. 최초 로그인이면 온보딩 페이지(반/조/이름 입력)로 이동하는지까지 확인

## 5. 자주 나는 에러 체크리스트

| 증상 | 원인 |
|---|---|
| `KOE101` / `redirect_uri mismatch` | 콘솔에 등록한 Redirect URI와 `KAKAO_REDIRECT_URI` 값이 한 글자라도 다름 |
| `KOE006` / 로그인 화면에서 바로 튕김 | 아직 검수 전 앱인데 로그인 시도 계정이 콘솔 "팀 관리"에 테스터로 등록 안 됨 |
| 카카오 인증은 되는데 500 에러 (`KAKAO_AUTH_FAILED`) | Client Secret을 콘솔에서 켰는데 서버 쪽 `KAKAO_CLIENT_SECRET`이 비어있음 (또는 그 반대) |
| 닉네임이 자꾸 "모공유저123"으로 나옴 | 동의항목에서 "닉네임"이 필수 동의로 안 걸려있음 |
| `invalid_client` | `KAKAO_CLIENT_ID`에 REST API 키가 아닌 다른 키(네이티브 앱 키 등)를 넣음 |
| 환경변수를 `.env`에 넣었는데도 안 먹음 | export/source 안 하고 그냥 `./gradlew bootRun`만 실행함 (2번 섹션 "중요" 참고) |

## 6. 나중에 배포(prod)할 때 추가로 바꿔야 할 것

- 카카오 콘솔 Redirect URI에 **실제 배포 도메인**의 콜백 URL 추가 등록
- `KAKAO_REDIRECT_URI`를 배포 도메인 기준으로 변경 (prod 서버 환경변수)
- `CORS_ALLOWED_ORIGINS`에 배포된 프론트 도메인 추가
- 카카오 "비즈니스 앱 전환" + 검수 완료 (일반 사용자 전체 로그인 허용을 위해 필요)
- `DEV_LOGIN_ENABLED=false` 유지 (prod 프로파일 기본값이 이미 false)

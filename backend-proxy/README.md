# OliveMe Backend Proxy

OliveMe의 서버 측 API proxy skeleton입니다. 현재 Android 앱 런타임은 Gemini/Kakao를 직접 호출하고, 이 proxy는 커머스/Naver 상품 검색과 향후 Gemini/FCM 서버 이전을 검증하는 별도 경로입니다.

프로덕션에서는 Gemini/Naver/Coupang/FCM 같은 secret을 Android APK에 넣지 않고 이 proxy 또는 정식 backend에만 보관해야 합니다.

## Current Wiring

- 현재 Android 앱은 Result 화면의 선택형 커머스/Naver 상품 추천 섹션에서만 `backend-proxy`를 호출합니다.
- Android 진단: Gemini Developer API 직접 호출 + policy/mechanical fallback.
- Android 매장: Kakao Local 직접 호출 + OSM/WebView 지도 + seed fallback.
- Backend proxy: `/health`, Gemini proxy smoke, Naver Shopping Search, Result commerce recommendation, FCM test endpoint 준비.
- Backend가 꺼져 있거나 `BACKEND_BASE_URL`에 접근할 수 없으면 Android 앱은 커머스 섹션을 조용히 숨기고 계속 실행합니다.

## Run

```bash
cd backend-proxy
npm install
cp env.example .env
# .env 값을 채운 뒤. KEY=value 또는 KEY = value 모두 지원합니다.
npm start
```

`src/server.js`가 `.env`를 직접 읽으므로 shell `source`가 필요 없습니다. Windows CMD/PowerShell에서도 같은 명령으로 실행합니다.

Android emulator에서 기본 `BACKEND_BASE_URL=http://127.0.0.1:8787/`을 사용할 때는 다음 reverse tunnel을 겁니다.

```bash
adb reverse tcp:8787 tcp:8787
```

## Endpoints

- `GET /health`: Gemini/Naver/FCM 설정 여부 확인. 값은 출력하지 않고 configured boolean만 반환합니다.
- `POST /v1/personal-color/analyze`: `{ "imageBase64": "...", "mimeType": "image/jpeg" }`를 Gemini `generateContent`로 전달.
- `GET /v1/products/search`: Naver Shopping Search API가 설정되어 있으면 실제 상품 검색을 반환하고, 없으면 curated fallback을 반환.
- `POST /v1/products/recommendations`: 진단 타입, 팔레트, 검색 키워드로 Naver Shopping 상품을 가져온 뒤 Gemini가 `AI 추천` 요약을 생성합니다. 메이크업 요청은 립/아이/베이스/치크 검색 결과를 합쳐 균형 있게 정렬하고, 네일/베이스코트 같은 비메이크업 잡상품은 제외합니다. Gemini가 quota/timeout/parse 문제로 실패하면 backend가 짧은 로컬 요약과 rank 기반 picks를 반환해 Android `AI 추천` 카드가 비지 않게 합니다.
- `POST /v1/notifications/test`: FCM HTTP v1 테스트 발송. `FCM_PROJECT_ID`, `GOOGLE_APPLICATION_CREDENTIALS`, device token이 필요합니다.

## Environment

```properties
PORT=8787
GEMINI_API_KEY=...
GEMINI_MODEL=gemini-2.5-flash
NAVER_CLIENT_ID=...
NAVER_CLIENT_SECRET=...
# NAVER_SECRET도 local alias로 허용합니다.
```

Naver/Coupang/Gemini secret은 Android `local.properties`가 아니라 backend `.env`에 둡니다. Android에는 `BACKEND_BASE_URL`만 둡니다.

## Smoke Tests

```bash
curl http://127.0.0.1:8787/health
curl "http://127.0.0.1:8787/v1/products/search?query=lip&display=3"
```

2026-06-21 QA에서 `.env` 설정 후 `/health`는 `geminiConfigured=true`, `naverShoppingConfigured=true`를 반환했습니다. 메이크업 추천 5회 반복 smoke는 모두 `source=naver-shopping`, `items=8`, 립/아이/베이스/치크 포함, `추천 아이템` 0회, 네일 잡음 0회였습니다. 같은 시점 Gemini commerce 직접 smoke는 `429 RESOURCE_EXHAUSTED`였으므로 backend local fallback 요약이 사용되었고, Android Result는 실제 Naver 상품 썸네일을 계속 표시했습니다.

## Production Notes

- HTTPS, auth/session, rate limit, request logging redaction, abuse protection을 추가해야 합니다.
- 얼굴 이미지는 proxy에서 디스크에 저장하지 않습니다.
- Gemini free tier/privacy notice는 Android 설정 화면과 개인정보 고지에 유지합니다.
- Android 앱은 커머스/Naver 추천에 한해 `BACKEND_BASE_URL`, `BackendApiService`, `CommerceRepository`를 사용합니다.
- Gemini 진단, Kakao Local, auth/session, request redaction, commerce summary generation은 아직 정식 backend integration 후속입니다.

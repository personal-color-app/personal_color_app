# OliveMe

OliveMe is an Android Kotlin personal color app. The app analyzes a face or
upper-body photo, recommends personal-color palettes/products, and shows nearby
beauty stores with a crash-free demo fallback.

## 한국어 안내

OliveMe는 Android Kotlin 기반 퍼스널 컬러 앱입니다. 얼굴 또는 상반신 사진을
분석해 퍼스널 컬러 타입, 추천 팔레트, 의상/메이크업 가이드, 주변 뷰티 매장,
선택형 상품 추천 정보를 제공합니다. 외부 API나 백엔드가 꺼져 있어도 앱은
종료되지 않고 준비된 seed 데이터와 로컬 분석 결과로 자연스럽게 동작해야 합니다.

### 기준 문서와 원본 자료

- 단일 진실 명세서: `docs/TRUTH_SPEC.md`
- 퍼스널 컬러 진단 기준: `docs/PERSONAL_COLOR_DIAGNOSIS_METHOD.md`
- Android 프로젝트: `android/`
- 디자인 원본: `Personalcolor design/`
- 로컬 계획 자료: `plan/` (git 제외)

구현, QA, 문서 판단은 `docs/TRUTH_SPEC.md`를 최우선 기준으로 봅니다. 디자인
확인은 `Personalcolor design/` 원본을 참고하되 해당 폴더는 수정하지 않습니다.

### 테스트 계정

- 이메일: `test01@gmail.com`
- 비밀번호: `test`
- 손글씨 2차 인증: 숫자 `1`

화면에서는 게스트/계정 흐름으로 자연스럽게 보이도록 구성하며, 테스트 계정 정보는
개발과 QA용으로만 사용합니다.

### 로컬 비밀값

`android/local.properties.example`을 `android/local.properties`로 복사한 뒤 필요한
값을 채웁니다.

```properties
GEMINI_API_KEY=
KAKAO_NATIVE_APP_KEY=
KAKAO_REST_API_KEY=
BACKEND_BASE_URL=http://127.0.0.1:8787/
```

실제 키는 절대 커밋하지 않습니다. `local.properties`, `.env`, keystore, build
output, `.gstack/`, `plan/`은 git 추적 대상이 아닙니다. 키가 없거나 API가 실패하면
앱은 sample/seed fallback으로 계속 동작합니다.

### Android 빌드

Android 프로젝트는 `android/` 아래에 있습니다.

```bash
cd android
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Windows에서 Gradle을 실행할 때는 `android/gradlew.bat`을 사용합니다.

### 백엔드 프록시

`backend-proxy/`는 Gemini, Naver, Coupang, FCM 같은 민감 키를 APK에 직접 넣지
않기 위한 프록시 구조입니다. 현재 Android 앱은 결과 화면의 선택형 커머스/Naver
추천 섹션에서만 프록시를 사용합니다. 백엔드가 꺼져 있거나 연결되지 않으면 해당
섹션은 조용히 숨겨지고 앱의 핵심 진단/지도/마이페이지 흐름은 정상 작동합니다.

에뮬레이터에서 Windows 로컬 백엔드를 볼 때는 앱 기본값을
`http://127.0.0.1:8787/`로 두고 다음 명령을 사용합니다.

```bash
adb reverse tcp:8787 tcp:8787
```

백엔드가 켜져 있으면 결과 화면에서 `AI 추천` 요약과 Naver Shopping 상품 썸네일
카드를 표시할 수 있습니다. Naver/Coupang/Gemini 관련 secret은 백엔드 `.env`에만
두고 Android에는 넣지 않습니다.

### 손글씨 숫자 모델

`android/app/src/main/assets/digit_mnist.tflite`는 생성된 MNIST TFLite 모델입니다.
다시 생성하려면 다음 명령을 사용합니다.

```bash
python3 -m venv tools/.venv
source tools/.venv/bin/activate
pip install tensorflow
python tools/train_digit_model.py
```

모델이 없거나 손상되어도 Android 앱은 crash 없이 재시도 안내 또는 fallback으로
복구해야 합니다.

## Source of Truth

- Implementation spec: `docs/TRUTH_SPEC.md`
- Android project: `android/`
- Design reference: `Personalcolor design/`
- Local-only planning input: `plan/` (ignored by git)

## Demo Account

- Email: `test01@gmail.com`
- Password: `test`
- Optional 2FA: enabled for the demo account
- Registered handwritten digit: `1`

## Local Secrets

Copy `android/local.properties.example` to `android/local.properties` and fill:

```properties
GEMINI_API_KEY=
KAKAO_NATIVE_APP_KEY=
KAKAO_REST_API_KEY=
```

Never commit real keys. The app falls back to sample data when keys are absent.

## Android

The project is intentionally under `android/`.

```bash
cd android
./gradlew test
./gradlew assembleDebug
```

## Backend Proxy

`backend-proxy/` contains the production-shape proxy skeleton for keeping the
Gemini/Naver/Coupang/FCM secrets out of the APK and for future FCM test
notifications. The current Android app uses this proxy only for the optional
Result commerce/Naver product section. Diagnosis and store lookup still use the
Android direct API path plus local fallbacks.

For local emulator QA the app defaults to `http://127.0.0.1:8787/`; run
`adb reverse tcp:8787 tcp:8787` so the emulator can reach the Windows backend.
If the backend is off or unreachable, the commerce section is simply hidden and
the app continues normally.

When the proxy is available, Result tabs can show an `AI 추천` summary first,
then Naver Shopping product cards with real product thumbnails. Product images
come from the Naver response and are rendered in-app; secrets stay in the
backend `.env`.

## Digit ML Model

The checked-in `digit_mnist.tflite` is the generated MNIST TFLite asset. To
regenerate it:

```bash
python3 -m venv tools/.venv
source tools/.venv/bin/activate
pip install tensorflow
python tools/train_digit_model.py
```

The Android runtime handles a missing or invalid model without crashing.

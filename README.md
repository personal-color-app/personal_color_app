# OliveMe

OliveMe is an Android Kotlin personal color app. The app analyzes a face or
upper-body photo, recommends personal-color palettes/products, and shows nearby
beauty stores with a crash-free demo fallback.

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

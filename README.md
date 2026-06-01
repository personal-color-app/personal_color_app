# OliveMe

OliveMe is an Android Kotlin personal color app. The app analyzes a face photo,
recommends winter-cool style palettes/products, and shows nearby Olive Young
stores with a crash-free demo fallback.

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

If Gradle wrapper files are not present in your checkout, run the same tasks
from Android Studio or generate a wrapper with the local Gradle installation.

## Digit ML Model

The checked-in `digit_mnist.tflite` is a placeholder until the model is trained.
To generate the real asset:

```bash
python3 -m venv tools/.venv
source tools/.venv/bin/activate
pip install tensorflow
python tools/train_digit_model.py
```

The Android runtime handles a missing or invalid model without crashing.

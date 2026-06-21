# OliveMe 채점 기능 근거 문서

작성일: 2026-06-21  
기준: `docs/TRUTH_SPEC.md`, `plan/personal_color_app_spec.md`, 실제 Android emulator QA  
최신 QA artifact: `/tmp/oliveme-downloadmanager-final-20260621-144528`

이 문서는 발표/채점 때 바로 설명할 수 있도록 각 채점 항목을 기능, 구현 근거,
스크린샷, QA 결과, 실패 방어 기준으로 다시 채점한 것이다. 점수는 과장하지 않고
현재 코드와 실제 emulator 증거가 있는 항목만 `만족`으로 판정했다.

## 총괄 채점표

| 항목 | 배점 | 판정 | 앱 기능 | 핵심 근거 | 대표 스크린샷 | QA artifact | backend/API 실패 방어 |
| --- | ---: | --- | --- | --- | --- | --- | --- |
| Activity + Intent | 필수 | 만족 | Login, 2FA, Main, Diagnosis, Result, Map, MyPage, Settings 화면 전환 | `AndroidManifest.xml`, `LoginActivity`, `IntentKeys` | `img/main.png`, `img/result.png`, `img/map.png` | `ui/main-summary.txt`, `ui/result-summary.txt`, `ui/map-summary.txt` | extra 누락 시 safe user/result로 복구 |
| Coroutine | 20 | 만족 | 로그인, 2FA, 진단, 지도, DB, 커머스 비동기 처리 | `ViewModels.kt`의 `viewModelScope`, `withContext(Dispatchers.IO)`, `suspend` DAO | `img/diagnosis-analyzing.png` | `ui/diagnosis-analyzing-summary.txt` | 실패는 state/fallback으로 전환 |
| 다운로드/API 매니저 | 20 | 만족 | API 통신, 상품 이미지 캐시, 리포트 이미지 저장 | `Retrofit/OkHttp`, `Glide`, Android OS `DownloadManager` | `img/grading-downloadmanager-report-save-mypage.png`, `img/grading-downloadmanager-exported-report.png`, `img/result-products.png` | `/tmp/oliveme-downloadmanager-final-20260621-144528`, `backend/backend-smoke-summary.txt` | 비렌더 상품 제외, 이미지 placeholder/error 유지, 저장 실패 toast fallback |
| Jetpack 3개 이상 | 30 | 만족 | Compose UI, Room DB, ViewModel/Lifecycle, ActivityResult | `build.gradle.kts`, `LegacyJetpackEvidence` | `img/main.png` | `ui/main-summary.txt` | Compose state 기반 화면 복구 |
| 외부 app 연동 | 20 | 만족 | 갤러리, 카메라, 공유 chooser, Google Maps/browser | `DiagnosisActivity`, `ResultActivity`, `MapActivity` | `img/diagnosis-source-sheet.png`, `img/result-share.png`, `img/google-maps.png` | `ui/diagnosis-source-sheet-summary.txt`, `ui/result-share.xml`, `android/google-maps.png` | 앱 없음/취소 시 toast 또는 fallback |
| DB | 30 | 만족 | 내부 로컬 Room DB | `OliveMeDatabase`, `OliveMeDao` | `img/mypage-history.png` | `ui/mypage-history-summary.txt`, `ui/mypage-saved-stores-summary.txt` | history/favorite empty state와 삭제 flow 제공 |
| API 3개 이상 | 60 | 만족 | Gemini, Kakao Login, Kakao Local, Naver Shopping, backend proxy, OSM/Google Maps | `ApiServices`, `ApiClient`, `backend-proxy/src/server.js` | `img/result-ai-clothes.png`, `img/map.png` | `backend/backend-commerce-smoke.json`, `ui/map-summary.txt` | API off 시 policy/seed/local guide fallback |
| ML 모델 | 50 | 만족 | 손글씨 숫자 2차 인증 | TensorFlow Lite MNIST digit model | `img/grading-ml-tflite-2fa.png` | `ui/2fa-summary.txt` | 모델 누락/오류 시 `DigitPrediction.unavailable` |
| 안정성 | 0-10 및 crash 0점 리스크 | 만족 | 권한/API/backend/model/image/외부 앱 실패 방어 | `runCatching`, timeout, fallback state, no secret logging | `img/result-backend-off.png` | `logs/crash-final.txt` 0 lines | backend off에서도 Result local guide 유지 |

## 발표용 짧은 설명

| 기준 | 설명 |
| --- | --- |
| Coroutine | ViewModel에서 `viewModelScope`와 `Dispatchers.IO`를 사용해 DB, API, 이미지 처리, TFLite 작업을 백그라운드로 보낸다. 그래서 진단/지도/상품/리포트 저장 중에도 UI thread를 막지 않고, 실패는 화면 상태나 toast로 복구한다. |
| 다운로드/API 매니저 | Retrofit/OkHttp로 Gemini, Kakao, backend API를 호출하고 Glide로 Naver 상품 이미지를 다운로드/캐시한다. 추가로 Result/MyPage의 `리포트 이미지 저장`은 Android OS `DownloadManager`를 직접 호출해 PNG 리포트를 다운로드 항목에 등록하고 갤러리에도 저장한다. |
| Jetpack | 화면은 Jetpack Compose, 데이터는 Room, 상태는 ViewModel/Lifecycle, 사진 선택은 ActivityResult API로 구현했다. 채점표에 나온 RecyclerView/Fragment/ViewPager2/DrawerLayout은 사용자 흐름과 별도로 증빙 레이어에 포함했다. |
| 외부 앱 연동 | 갤러리 선택, 카메라 preview, 공유 chooser, Google Maps 앱, 브라우저 fallback을 실제 Intent/ActivityResult로 연결했다. 취소나 앱 없음은 crash가 아니라 toast/fallback으로 처리한다. |
| DB | 앱 내부 Room DB에 사용자, 동의, 2FA, 진단 이력, 컬러/상품 추천, 저장 매장을 보관한다. API로 DB를 대체한 것이 아니라 로컬 DB를 직접 사용하므로 DB 항목으로 설명한다. |
| API | Gemini 진단, Kakao Login, Kakao Local 매장, Naver Shopping backend, OSM 지도 타일, Google Maps 외부 Intent를 기능별로 분리해 사용한다. API가 실패해도 로컬 진단, seed 매장, 로컬 컬러 가이드로 이어진다. |
| 머신러닝 | `digit_mnist.tflite`를 TensorFlow Lite Interpreter로 실행해 손글씨 숫자 2차 인증을 처리한다. 빈 캔버스나 모델 오류는 실패 상태로 보여주고 재시도하게 한다. |
| 안정성 | 권한 거부, backend off, API timeout, 이미지 실패, 외부 앱 없음, DownloadManager 실패를 모두 no-crash fallback으로 처리한다. 최종 QA는 crash buffer 0 lines와 실제 emulator screenshot/UI tree/log로 판정한다. |

## 1. Coroutine

정확한 설명: **Kotlin Coroutine 기반 비동기 처리**를 사용한다.

구현 근거:

- `android/app/src/main/java/com/oliveme/app/ViewModels.kt`
  - `viewModelScope.launch`로 UI 생명주기에 묶인 비동기 작업 실행.
  - `withContext(Dispatchers.IO)`로 Room, Retrofit, 이미지 처리, TFLite 분류를 IO thread에서 처리.
- `android/app/src/main/java/com/oliveme/app/data/local/OliveMeDao.kt`
  - DAO 메서드가 `suspend`로 선언되어 Room DB 접근을 coroutine에서 수행.
- `android/app/src/main/java/com/oliveme/app/util/MapDataWarmup.kt`
  - `CoroutineScope(SupervisorJob() + Dispatchers.IO)`로 지도 데이터 warmup을 분리.

기능 예시:

- 로그인/회원가입 DB 조회와 2FA 설정 조회.
- 사진 품질 분석, 이미지 byte 변환, Gemini 또는 policy 진단 저장.
- Kakao Local 매장 검색과 저장 매장 조회.
- Result 화면의 backend commerce 추천 조회.

QA 판정:

- `diagnosis-analyzing` 화면까지 실제 emulator에서 진행했고, Result 자동 이동까지 확인했다.
- `logs/crash-final.txt`는 0 lines다.

## 2. 다운로드/API 매니저

정확한 설명: **Retrofit + OkHttp 기반 API manager, Glide 기반 이미지
다운로드/캐시, Android OS DownloadManager 기반 리포트 이미지 저장**이다.

채점 기준 자료에는 `Glide, Retrofit, Volley 등 다운로드 매니저 활용 시 20점`과
`다운로드 매니저만 활용해도 점수 부여`가 명시되어 있다. 현재 앱은 기존
`Retrofit + Glide` 구현에 더해 Result/MyPage의 `리포트 이미지 저장` 버튼에서
Android OS `DownloadManager`를 직접 호출한다.

구현 근거:

- `android/app/build.gradle.kts`
  - `retrofit`, `converter-gson`, `okhttp`, `glide` dependency.
- `android/app/src/main/java/com/oliveme/app/data/remote/ApiClient.kt`
  - Gemini, Kakao Local, backend proxy Retrofit client 구성.
  - logging interceptor는 `Level.NONE`으로 secret log 노출을 막음.
- `android/app/src/main/java/com/oliveme/app/ui/screens/ResultScreen.kt`
  - `Glide.with(imageView).load(product.imageUrl)`로 Naver 상품 썸네일 렌더링.
  - placeholder/error drawable로 이미지 실패 시 layout 깨짐 방지.
- `android/app/src/main/java/com/oliveme/app/util/ReportDownloadManager.kt`
  - `context.getSystemService(DownloadManager::class.java)`로 Android OS DownloadManager 사용.
  - 앱이 생성한 PNG 리포트를 `DownloadManager.addCompletedDownload(...)`로 다운로드 목록에 등록.
  - 같은 PNG를 `MediaStore.Images`의 `Pictures/OliveMe`에도 저장해 갤러리에서 확인 가능.
  - 파일명은 `.png` 확장자를 보존하고, bitmap write 실패/0 byte 파일/MediaStore stream 실패를 모두 no-crash 실패 메시지로 흡수.
  - 긴 리포트 문구와 색상명은 Canvas 출력에서 ellipsis/wrap 처리해 이미지 밖으로 밀리지 않게 방어.
- `ResultActivity`, `MyPageActivity`
  - Result 하단과 MyPage 리포트 탭의 `리포트 이미지 저장` 버튼을 `ReportDownloadManager.saveReport(...)`에 연결.
  - 이미지 생성/파일 저장은 `Dispatchers.IO`에서 실행하고, 중복 탭은 `저장 중` toast로 흡수.

QA 판정:

- backend-on smoke에서 `source=naver-shopping`, items 8, renderable 8, AI picks 3 확인.
- Android Result 화면에서 실제 Naver 상품 썸네일과 링크 가능한 상품 카드 확인.
- MyPage와 Result의 `리포트 이미지 저장`을 실제 emulator에서 각각 눌렀고, PNG가 `/sdcard/Pictures/OliveMe`와 MediaStore `Pictures/OliveMe/`에 생성되는 것을 확인.
- MyPage 저장 버튼 double tap은 파일 수가 3->4로 1개만 증가해 중복 실행 방어를 확인.
- 최신 생성 PNG를 pull해 `PNG image data, 1080 x 1600`으로 확인했고 README/`C:\Users\pjjpj\Desktop\new`에 복사.
- 저장 시 logcat에서 MediaProvider 저장 완료와 DownloadProvider notification event를 확인했고, 두 저장 flow 모두 crash buffer 0 lines.
- Result/MyPage 리포트 저장은 backend/API 없이 로컬 PNG를 생성하므로 API-off 상태에서도 동작 가능하다.

## 3. Jetpack Library

정확한 설명: **Jetpack Compose + Room + ViewModel/Lifecycle + ActivityResult API**를
실제 화면과 데이터 흐름에 사용한다.

구현 근거:

- `android/app/build.gradle.kts`
  - Compose, Room, Lifecycle ViewModel, Activity Compose, ActivityResult, RecyclerView,
    ViewPager2, Fragment, DrawerLayout dependency.
- `android/app/src/main/java/com/oliveme/app/ui/screens/*`
  - Login/Main/Diagnosis/Result/Map/MyPage/Settings는 Compose screen.
- `android/app/src/main/java/com/oliveme/app/data/local/OliveMeDatabase.kt`
  - Room database.
- `android/app/src/main/java/com/oliveme/app/ui/screens/LegacyJetpackEvidence.kt`
  - 채점 기준에 있던 RecyclerView, Fragment, ViewPager2, DrawerLayout을 숨은 보조
    evidence layer로 포함한다.

QA 판정:

- Main, Result, Map, MyPage, Settings Compose 화면 캡처 완료.
- Legacy evidence는 사용자 화면의 Result tab 동작을 ViewPager2라고 과장하지 않고,
  채점 증빙 레이어로 분리해 설명한다.

## 4. 외부 App 연동

질문 답변: **갤러리 연동은 외부 app 연동으로 보는 것이 맞다.**

구현 근거:

- `DiagnosisActivity`
  - `ActivityResultContracts.GetContent()`로 갤러리/파일 선택.
  - `ActivityResultContracts.TakePicturePreview()`로 카메라 preview.
  - 카메라 권한 거부/취소 시 안내 문구로 복구.
- `ResultActivity`, `MyPageActivity`
  - `ACTION_SEND` chooser로 결과/리포트 공유.
- `MapActivity`
  - Google Maps 앱 package 우선 실행.
  - 실패 시 Google Maps HTTPS URL을 browser/default handler로 실행.
  - 모든 handler 실패 시 링크를 clipboard에 복사.

QA 판정:

- `diagnosis-source-sheet`에서 카메라/갤러리 진입점을 확인.
- `result-share`에서 공유 chooser 진입을 확인.
- `google-maps`에서 외부 Google Maps 앱 연결을 확인.
- 최종 crash buffer는 0 lines다.

## 5. DB

질문 답변: **내부 로컬 Database를 사용하고 있으며, DB 사용 항목으로 인정 가능하다.**

정확한 설명: **Room 기반 내부 로컬 DB**다.

구현 근거:

- `OliveMeDatabase`
  - `user_profiles`, `auth_credentials`, `digit_auth_configs`, `legal_consents`,
    `diagnosis_history`, `recommended_colors`, `product_recommendations`,
    `favorite_stores`, `color_stories` entity 등록.
  - DB version 4, migration 1->2, 2->3, 3->4 존재.
- `OliveMeDao`
  - 유저, 2FA 설정, 법적 동의, 진단 이력, 추천 컬러/상품, 저장 매장 CRUD.

채점 방어:

- 외부 DB API가 아니라 앱 내부 Room DB이므로 API 점수와 중복 문제가 아니다.
- 진단 이력과 저장 매장이 MyPage에서 실제로 표시된다.

QA 판정:

- MyPage 이력: `이력 8`, 최신 진단 카드 표시.
- 저장 매장: `저장 매장 1곳`, `올리브영 부산대점`, `지도로 이동` 표시.

## 6. API

현재 구현 기준으로 API/외부 서비스는 다음처럼 설명한다.

| API/서비스 | 앱 기능 | 구현 경로 | 실패 시 동작 |
| --- | --- | --- | --- |
| Gemini Developer API | 사진 기반 퍼스널 컬러 진단 | Android `GeminiApiService.generateContent` | policy/seed 진단 결과 저장 |
| Kakao Login SDK | 카카오 로그인 | `LoginRepository.loginWithKakao` | email/guest path 유지 |
| Kakao Local REST API | 주변 Olive Young 매장 검색 | `StoreRepository.nearbyOliveYoung` | 부산대 seed stores |
| Naver Shopping Search API | Result 의상/메이크업 상품 추천 | `backend-proxy` -> `/v1/products/search`, `/recommendations` | Android commerce 섹션 숨김 |
| backend proxy API | Naver 상품과 Gemini commerce summary 중계 | `BackendApiService`, `CommerceRepository` | `CommerceRecommendationSection()` |
| OSM tile/WebView 지도 | 앱 내부 interactive map | `MapScreen` + `assets/web/oliveme_map.html` | skeleton/list 유지 |
| Google Maps external Intent | 선택 매장 지도 앱 열기 | `MapActivity.openStoreInMap` | browser, package fallback, clipboard |

QA 판정:

- backend-on:
  - `/health`: `ok=true`, `geminiConfigured=true`, `naverShoppingConfigured=true`.
  - Naver search: `source=naver-shopping`, renderable 5/5.
  - recommendations: `source=naver-shopping`, items 8, renderable 8, `aiSummary=true`, picks 3.
- Android backend-on:
  - Result 의상/메이크업 탭에서 `AI 추천`, `실시간 상품 추천`, 실제 Naver 썸네일 확인.
- Android backend-off:
  - `adb reverse --remove tcp:8787` 후 Result 의상 탭이 `로컬 컬러 가이드`만 표시.
  - 앱 crash 없음.

## 7. 머신러닝

정식 명칭: **TensorFlow Lite 기반 MNIST 손글씨 숫자 인식 모델**

앱 기능명: **손글씨 숫자 2차 인증**

구현 근거:

- `android/app/src/main/assets/digit_mnist.tflite`
  - 앱에 포함된 digit recognition model asset.
- `android/app/src/main/java/com/oliveme/app/ml/DigitRecognizer.kt`
  - `org.tensorflow.lite.Interpreter`로 TFLite model lazy load.
  - `DigitPreprocessor.toInputBuffer(bitmap)`로 canvas bitmap을 모델 입력으로 변환.
  - 빈 캔버스, 모델 load 실패, runtime 오류는 `DigitPrediction.unavailable`로 반환.

QA 판정:

- 실제 guest flow에서 2FA 화면 진입.
- 숫자 1 stroke를 canvas 안에 입력한 스크린샷 저장.
- 인증 후 Main 화면으로 이동.

## 8. 안정성

안정성은 강제종료가 있으면 전체 감점 또는 0점 리스크가 되는 항목이므로 가장
보수적으로 판정했다.

확인한 방어:

- Login/Kakao: 실패 시 `LoginUiState.Error`; email/guest path 유지.
- Legal consent: full-screen gate, 동의 기록 저장.
- 2FA: 빈 캔버스/model unavailable/runtime error를 실패 state로 흡수.
- Diagnosis: gallery/camera cancel, image decode failure, Gemini key/network/JSON failure를
  policy result로 fallback.
- Result commerce: backend null/off/source mismatch/blank product/title/link/image를 숨김.
- Map: 위치 권한 거부, 국외/오래된 last-known, Kakao Local 실패, 외부 지도 앱 없음,
  browser 없음까지 fallback.
- Room: migration 1->4 존재, local DB로 history/favorite 관리.
- Secret: Android/Backend secret 값은 artifact와 로그에 출력하지 않음.

최신 QA 결과:

- Gradle: `:app:testDebugUnitTest :app:assembleDebug :app:installDebug` 통과.
- Backend: Windows port 8787 running, health OK, Naver/Gemini configured.
- Emulator: `Small_Phone_API_36`, 720x1280.
- Crash buffer: `/tmp/oliveme-grading-final-20260621-125344/logs/crash-final.txt` = 0 lines.
- `adb reverse tcp:8787 tcp:8787` 최종 복구 완료.

남은 주의점:

- Kakao 실제 계정 취소 popup flow는 이번 최종 캡처에서는 재실행하지 않았다. 코드상
  callback guard와 error state가 있고, email/guest fallback은 실제로 통과했다.
- WebView 지도는 tile/network 순간 jank 가능성이 있다. 이번 목적은 안정성/채점
  증빙이며 crash는 없었다.

## 제출용 스크린샷 위치

README 대표 이미지는 `img/`에 저장했다. 사용자가 요청한 제출/공유용 최신본은
`C:\Users\pjjpj\Desktop\새 폴더`에 같은 파일명으로 덮어썼다.

주요 채점용 파일:

- `img/diagnosis-analyzing.png`
- `img/result-products.png`
- `img/main.png`
- `img/diagnosis-source-sheet.png`
- `img/result-share.png`
- `img/google-maps.png`
- `img/mypage-history.png`
- `img/result-ai-clothes.png`
- `img/map.png`
- `img/result-products.png`
- `img/result-backend-off.png`
- `img/grading-ml-tflite-2fa.png`
- `img/result-backend-off.png`
- `img/map-saved-filter.png`
- `img/map-marker-accuracy-zoom19.png`

# OliveMe QA Evidence

## 2026-06-02 Final QA Fix

- Branch: `feature/data-auth-seed-api-qa`
- Latest OSM unified map/result artifact: `/tmp/oliveme-osm-map-qa-1780390109`
- Legal consent and guest flow artifact: `/tmp/oliveme-legal-guest-qa-1780393011`
- Naver API smoke artifact: `/tmp/oliveme-naver-api-qa-1780393888`
- Backend proxy configured smoke: Windows Node `npm start` with `backend-proxy/.env`; `/health` returned `geminiConfigured=true`, `naverShoppingConfigured=true`, `fcmConfigured=false`.
- Backend proxy Naver smoke: `GET /v1/products/search?query=lip&display=3` returned `source=naver-shopping`, `total=1828748`, and 3 real shopping items. Secret values were not printed.
- Backend proxy Gemini smoke: `POST /v1/personal-color/analyze` returned HTTP 200 for `spring_warm.png`, `summer_cool.png`, `autumn_warm.png`, and `winter_cool.png`; returned text hints matched spring/summer/autumn/winter respectively.
- Backend off Android QA artifact: `/tmp/oliveme-backend-off-android-qa-1780395440`; backend port 8787 connection failed as expected, Android `:app:testDebugUnitTest :app:assembleDebug :app:installDebug` passed, `LoginActivity` launched, and crash buffer was 0 lines.
- Android commerce proxy on QA artifact: `/tmp/oliveme-backend-commerce-on-qa-1780396347`; backend was running on `http://127.0.0.1:8787`, emulator used `adb reverse tcp:8787 tcp:8787`, Result `의상` tab showed `실시간 상품 추천` and `Naver Shopping 기준으로 지금 볼 수 있는 상품만 보여드려요.`, crash buffer was 0 lines.
- Android commerce proxy off QA artifact: `/tmp/oliveme-backend-commerce-off-qa-1780396439`; backend was stopped and adb reverse removed, the same Result `의상` tab did not show the realtime commerce section, no user-visible error appeared, and crash buffer was 0 lines.
- Android AI commerce thumbnail QA artifact: `/tmp/oliveme-ai-commerce-thumb-final-qa-1780397517`; backend `/v1/products/recommendations` loaded `products=8 ai=true` for `의상` and `메이크업`, Result `의상` tab showed `AI 추천`, `실시간 상품 추천`, and an actual product `ImageView desc="루즈핏 라운드 반팔니트 봄 여름 홀가먼트 ob 썸네일"` instead of the old shopping-bag icon. Crash buffer was 0 lines.
- Android thumbnail/order QA artifact: `/tmp/oliveme-thumb-ai-order-qa-1780398345`; after removing the shopping-bag fallback, Result `의상` tab showed `AI 추천` first, then `실시간 상품 추천` with actual product `ImageView ... 썸네일`. Backend logs showed `products=8 ai=true` for both `의상` and `메이크업`; `logcat -b crash` was 0 lines.
- Android AI picked products QA artifact: `/tmp/oliveme-ai-picks-qa-1780398921`; backend `/v1/products/recommendations` returned `aiSummary.picks` with Naver product ranks, and Result `의상` tab showed `AI가 고른 상품` with an actual product thumbnail row and recommendation reason. `logcat -b crash` was 0 lines.
- Android color guide QA artifact: `/tmp/oliveme-color-guide-qa-1780399453`; backend reverse was removed and Result `의상` tab still showed the local `컬러 가이드` first. UI tree confirmed `추천 팔레트` swatches with labels such as `플럼`, `네이비`, `버건디`, followed by `파트별 컬러 적용` rows such as `상의 · 플럼` and `아우터 · 네이비`. Crash buffer was 0 lines.
- Android artifact directory: `/tmp/oliveme-gemini35-map-permission-qa-1780384882`
- Latest map/current-location artifact: `/tmp/oliveme-map-final-visible-1780388946`
- gstack-browse official-doc artifact: `/tmp/oliveme-plan-research-1780383031`
- Gemini sample smoke: inline, key redacted; no raw key printed.
- Previous Gemini all-success artifact retained for comparison: `/tmp/oliveme-gemini-sample-qa-alt`
- Build: Windows Gradle `:app:testDebugUnitTest :app:assembleDebug :app:installDebug` passed.
- OSM unified map regression QA: Windows Gradle `:app:testDebugUnitTest :app:assembleDebug :app:installDebug` passed after removing the Kakao Maps native dependency and SDK calls.
- Gemini default model: `gemini-2.5-flash`; fallback chain is `gemini-2.5-flash -> gemini-2.0-flash -> gemini-flash-latest`.
- Gemini parser QA: loose real-style JSON with `confidence: "High"`, five-point numeric confidence, `matchScore: 0.95`, reversed subtype labels such as `Deep Winter`, and object `signature` now parses to canonical domain values in unit tests.
- Gemini live API QA: sample faces are called with the app model chain starting at `gemini-2.5-flash` for faster response. Earlier 3.5 smoke data is retained only as historical evidence; the current app no longer uses 3.5 in its default chain. The app keeps a 120 second deadline and policy/mechanical fallback for high-demand windows.
- Kakao Local QA: API is now active and returned 15 real stores. First visible result was `올리브영 부산대점`, `164m`, address `부산 금정구 부산대학로 47`.
- Map QA: Kakao Maps Android native SDK was removed because ABI/native-library behavior is not portable enough for the grading target. The app now uses a single interactive WebView map with local Leaflet assets and OpenStreetMap HTTPS tiles on every device. OSM marker label taps open Google Maps immediately; selected bottom-sheet store cards still support favorite and directions actions.
- OSM WebView QA: `05-map-entry.png` shows real OSM tiles, WebView bounds `[0,0][720,1280]`, current-location label `현재 위치 기준 추천 매장`, and real nearby stores `올리브영 무교동점 213m`, `올리브영 덕수궁점 249m`.
- Google Maps external QA: tapping the selected store `길찾기` action opened `com.google.android.apps.maps/com.google.android.maps.MapsActivity`; Google Maps displayed the Olive Young place card with Directions/Call/Save/Share actions.
- Result hero QA: `21-result-final-polished.png` shows the HTML-style gradient result card without the old `컬러 진단 결과` pill or clipped bottom confidence line; hero text bounds remain inside the visible card.
- OSM unified map crash/security QA: `crash-buffer-final.txt` is 0 lines, `logcat-final.txt` has no `K3fAndroid`/`KakaoMapSdk`/`vectormap` references, and artifact secret scan found no Gemini/Kakao/Naver/Coupang key patterns.
- Current-location QA: stale Mountain View last-known coordinates are no longer used first. The flow now tries Fused fresh location, then Android `LocationManager` fresh location, then valid domestic last-known, then 부산대 fallback. The latest artifact crash buffer is 0 lines.
- Permission onboarding QA: first Main entry showed consent-style onboarding for location/camera/photos plus optional non-identifying usage-flow analytics. `나중에 하기` kept the app usable.
- Legal consent QA: fresh login -> `이메일로 로그인하기` -> `바로 시작` opened the full-screen `통합 이용 동의`; before scrolling the CTA was `문서 끝까지 확인해주세요`; after scrolling to the end and checking all five rows the CTA changed to `동의하고 계속`.
- Guest consent persistence QA: after accepting once, restarting `LoginActivity` and tapping `바로 시작` skipped the legal document for the same version and moved directly to 2FA.
- Guest naming QA: drawn digit `1` reached Main and showed `안녕하세요, 게스트님`; user-facing random nickname wording is removed.
- OSM marker external QA: tapping a visible OSM marker label opened `com.google.android.apps.maps/com.google.android.maps.MapsActivity` with the Olive Young place card. Crash buffer stayed 0 lines.
- Diagnosis flow QA: sample upload sheet was fixed so all four sample buttons fit on the small emulator screen; sample preview showed the selected image and `PhotoQuality` label; tapping `분석 시작` navigated directly to `진단 결과` without the old intermediate completion page.
- Seed account QA: fresh account flow reached Main as `게스트`, with Room-backed diagnosis/favorite counts and no user-visible seed-account wording.
- Map favorite QA: saving a real Kakao store and tapping `저장` filter reduced the list to one saved store.
- MyPage delete QA: history delete dialog removed one result; counts changed from `이력 4` to `이력 3` without crash.
- Settings QA: Settings screen opened from app navigation and showed account/security/privacy sections plus `2FA 다시 테스트`; XML scan found no seed-account or non-production-security wording.
- Crash buffers: all captured crash buffers in the final artifact are 0 lines.
- Security scan: no raw `AIza...` or `key=` secret leak was found in final QA artifacts. Logcat `AndroidRuntime` lines were from `uiautomator` process startup/shutdown, not app crashes.
- Superseded Naver API QA: earlier `android/local.properties` direct smoke calls returned HTTP 401 `NID AUTH Result Invalid (28)`. Current backend `.env` settings were corrected and the proxy now returns `source=naver-shopping` for product search. Android `local.properties` Naver values are no longer required for runtime.
- Performance artifact: `gfxinfo-framestats.txt` saved in the final artifact directory.

## 2026-06-02 API/Diagnosis/Hardcoding QA

- Artifact path: `/tmp/oliveme-android-qa-gW3H3K`
- gstack-browse official-doc artifact: `/tmp/oliveme-gstack-browse-1780378777`
- Build: Windows Gradle `:app:testDebugUnitTest :app:assembleDebug :app:installDebug` passed.
- Backend proxy syntax: `node --check backend-proxy\src\server.js` passed.
- BuildConfig key status: `GEMINI_API_KEY` present length 39, `KAKAO_NATIVE_APP_KEY` present length 32, `KAKAO_REST_API_KEY` present length 32. Values were not printed.
- Login QA: `/tmp/oliveme-android-qa-gW3H3K/01-login.png`, `01-login.xml`, `01-login-summary.txt`; crash buffer 0 lines.
- Demo + 2FA QA: `02-email-sheet.*`, `03-2fa.*`, `04-main-or-2fa.*`; handwritten 1 reached Main; crash buffer 0 lines.
- Main UI polish QA: `04-main-or-2fa-summary.txt` shows quick tiles with icon descriptions `근처 매장`, `마이페이지`; no one-letter `근`/`마` tile rendering.
- Diagnosis preview QA: `18-preview.png`, `18-preview.xml`, `18-preview-summary.txt`; selected sample renders as `ImageView desc="선택한 진단 사진"` and shows `촬영 가능` from `PhotoQuality`.
- Historical Gemini real call QA: `19-analysis-logcat.txt` showed an earlier high-demand model returning upstream 503 before the final `gemini-2.5-flash` model/timeout/parser fix.
- Historical Kakao Local smoke test: REST request returned HTTP 403 before Map/Local API was enabled. This is superseded by the final QA section above, where Kakao Local returned 200 and 15 stores.
- Security finding fixed: OkHttp BASIC logging initially exposed Gemini query key in logcat. Code now sets `HttpLoggingInterceptor.Level.NONE`; existing artifact logs were redacted. Actual key pattern scan is clean and only `key=REDACTED` markers remain.
- Final install/launch QA: `20-final-login.png`, `20-final-login.xml`, `20-final-login-summary.txt`; crash buffer 0 lines.
- Performance artifact: `gfxinfo.txt`, `gfxinfo-framestats.txt` saved.

Date: 2026-06-01
Branch: `feature/ui-ux-settings-simplification`

## Build

- Windows Gradle: `:app:compileDebugKotlin` passed.
- Windows Gradle: `:app:testDebugUnitTest :app:assembleDebug` passed.
- Windows Gradle: `:app:installDebug` passed on `emulator-5554`.

## HTML Reference

- Tool: `gstack-browse`
- Artifact directory: `/tmp/oliveme-html-reference-1qI9bJ`
- Captured: `html-login.png`, `html-text.txt`
- Source URL: `file:///mnt/c/Users/pjjpj/Desktop/personal_color_app/Personalcolor%20design/index.html`

## Android Emulator QA

- Skill guidance: `@test-android-apps/android-emulator-qa`
- Artifact directory: `/tmp/oliveme-android-qa-ysSBsL`
- Emulator serial: `emulator-5554`
- Package: `com.oliveme.app`
- Captured artifact types: screenshots, UI tree XML, UI summaries, logcat, crash buffer, gfxinfo framestats.
- Crash buffer: `/tmp/oliveme-android-qa-ysSBsL/crash.txt` is 0 bytes.

## Verified Flow Highlights

- Login:
  - `21-login-fresh.*`, `22-email-sheet-fresh.*`
  - `이메일로 로그인하기` opens the email/password sheet.
  - `바로 시작` displays the temporary nickname flow and moves to 2FA.
- 2FA:
  - `23-2fa-fresh.*`, `24-main-fresh.*`
  - A drawn digit `1` moves to Main without crash.
- Main and Settings:
  - `24-main-fresh.*`, `25-drawer-fresh.*`, `09-settings.*`
  - Drawer contains the reduced navigation and Settings opens as a real screen.
- Result:
  - `26-result-fresh.*`, `27-result-overflow-fresh.*`
  - Save remains visible; share is reachable from overflow.
- Map:
  - `30-map-fresh.*`
  - Filters are reduced to `전체`, `영업 중`, `저장`; selected store shows favorite and directions actions.
- MyPage:
  - `32-mypage-fresh.*`, `33-mypage-overflow-fresh.*`
  - Tabs are `리포트`, `이력`, `매장`; report save/share are reachable from overflow.
- Diagnosis:
  - `39-diagnosis-sheet-after-fix-retry.*`, `40-diagnosis-sheet-pick.*`
  - Upload card opens the action panel with `사진 선택`, `카메라`, `갤러리`, and `샘플로 체험`.

## Notes

- A first UI dump immediately after app launch returned `ERROR: null root node returned by UiTestAutomationBridge`; retrying after a short wait produced valid UI trees.
- A coordinate-heavy exploratory script briefly left the app for Google/system screens. Final accepted artifacts use app UI tree targets and app-local navigation.
- `gfxinfo-framestats.txt` was captured for a quick performance snapshot. Perfetto was not added because the smoke flow did not show a blocking jank/crash issue.

## 2026-06-02 Auth/Data/API QA

- Branch: `feature/data-auth-seed-api-qa`
- Android artifact directory: `/tmp/oliveme-android-qa-IffMAi`
- Web research artifacts: `/tmp/oliveme-web-research-1780372468`
- Build: Windows Gradle `:app:testDebugUnitTest :app:assembleDebug :app:installDebug` passed.
- Backend proxy syntax: Windows Node `node --check backend-proxy\src\server.js` passed.
- Login failure: invalid email/password shows `해당되는 정보가 없습니다.`.
- Signup sheet: nickname, email, password, password confirm, consent checkbox, and `가입하기` are visible.
- 바로 시작 flow: temporary nickname -> 2FA -> Main passed with generated nickname.
- Demo seed: Main showed 3 diagnosis records and 2 favorite stores before deletion.
- Historical Map API state: this earlier run used fallback seed stores, showing `현재 위치 대신 부산대 기준 매장을 표시합니다.` and 5 부산대 seed stores.
- History delete: MyPage `이력` overflow -> delete dialog -> confirm removed the first diagnosis.
- Post-fix MyPage stats: after reinstall, `이력 2` and `진단 횟수 2` matched.
- Crash buffers: `crash-after-2fa.txt`, `crash-final.txt`, and `crash-after-fixed-flow.txt` were 0 bytes.
- Performance artifact: `gfxinfo.txt`, `gfxinfo-framestats.txt` saved in the artifact directory.
- Kakao debug key hash for current Windows debug keystore: `Ga2fOsUWdKh7X6aHrTMAD+0JLrE=`.

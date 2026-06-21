# OliveMe QA Evidence

## 2026-06-21 Android OS DownloadManager Report Image QA

- Artifact directory: `/tmp/oliveme-downloadmanager-qa-20260621-143414`
- Scope:
  - Added visible `리포트 이미지 저장` actions to Result and MyPage.
  - The report export creates an app-generated PNG image, saves it to `Pictures/OliveMe` through `MediaStore.Images`, and registers the completed PNG with Android OS `DownloadManager`.
  - Heavy bitmap generation/file work is executed on `Dispatchers.IO`; repeated taps are absorbed with a `저장 중` toast instead of launching duplicate writes.
- Build/install:
  - `cmd.exe /C gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:installDebug --console=plain`: pass.
  - `git diff --check -- . ':!LICENSE'`: pass.
- Android E2E:
  - Login -> email -> guest -> 2FA -> Main -> MyPage completed on `emulator-5554`.
  - MyPage report tab showed the visible `리포트 이미지 저장` button without clipping. Evidence: `android/mypage-report-button.png`, `android/mypage-report.xml`.
  - Tapping MyPage `리포트 이미지 저장` created `OliveMe_겨울_딥_(Winter_Deep)_20260621_053535.png` under both `/sdcard/Pictures/OliveMe` and the app external Pictures folder. MediaStore query returned the PNG with `relative_path=Pictures/OliveMe/` and `mime_type=image/png`. Evidence: `logs/report-image-files.txt`.
  - Logcat showed MediaProvider file finalization and DownloadProvider notification events after the save, confirming the OS download provider path was exercised. Evidence: `logs/report-save-filtered-logcat.txt`.
  - MyPage history -> Result opened the Result screen with the visible bottom `리포트 이미지 저장` action. Evidence: `android/result-screen.png`, `android/result-screen.xml`.
  - Tapping Result `리포트 이미지 저장` created another PNG, `OliveMe_겨울_딥_(Winter_Deep)_20260621_053639.png`, visible in `/sdcard/Pictures/OliveMe` and MediaStore. Evidence: `logs/result-report-save-files.txt`.
  - Crash buffers after MyPage and Result report saves were both 0 lines: `logs/crash-after-report-save.txt`, `logs/crash-after-result-report-save.txt`.

## 2026-06-21 Grading Evidence + Full Screenshot QA

- Artifact directory: `/tmp/oliveme-grading-final-20260621-125344`
- Scope:
  - Created `docs/GRADING_FEATURE_EVIDENCE.md` as the final grading evidence matrix for Coroutine, Retrofit/Glide download/API handling, Jetpack, external app integration, Room DB, APIs, TFLite ML, and stability.
  - Refreshed README grading screenshots and copied the same latest PNGs to `C:\Users\pjjpj\Desktop\새 폴더` with descriptive names.
  - `plan/` was read as grading input only and was not modified.
- Build/static:
  - `cmd.exe /C gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:installDebug --console=plain`: pass.
  - `/mnt/c/nvm4w/nodejs/node.exe --check backend-proxy/src/server.js`: pass.
  - Secret files were checked as present/blank only; secret values were not printed. Android Gemini/Kakao keys were present, `BACKEND_BASE_URL` blank/defaulted to `http://127.0.0.1:8787/`; backend Gemini/Naver keys were present.
- Backend on/off:
  - Backend stayed running on Windows port `8787`; final `/health` returned `ok=true`, `geminiConfigured=true`, `naverShoppingConfigured=true`, `fcmConfigured=false`.
  - Backend commerce smoke returned Naver source with renderable products: search `items=5`, renderable `5`; recommendations `items=8`, renderable `8`, `aiSummary=true`, picks `3`. Evidence: `backend/backend-commerce-smoke.json`, `backend/backend-smoke-summary.txt`.
  - Android backend-off was tested by removing only `adb reverse tcp:8787`, not by killing the backend. Result 의상 tab showed `로컬 컬러 가이드` and hid commerce sections. Evidence: `android/result-backend-off.png`, `ui/result-backend-off-summary.txt`.
  - `adb reverse tcp:8787 tcp:8787` was restored at the end. Evidence: `backend/adb-reverse-final.txt`.
- Android E2E/screenshots:
  - Login, email sheet, legal consent, 2FA, Main, drawer, Diagnosis, sample preview/analyzing, Result, commerce on/off, share, Map, saved filter, cluster, refresh, marker zoom detail, Google Maps, MyPage, history, saved stores, Settings were captured with screenshot/UI XML/UI summary where the target app exposed an inspectable tree.
  - Key grading screenshots: `img/diagnosis-analyzing.png`, `img/result-products.png`, `img/main.png`, `img/diagnosis-source-sheet.png`, `img/result-share.png`, `img/google-maps.png`, `img/mypage-history.png`, `img/result-ai-clothes.png`, `img/map.png`, `img/result-products.png`, `img/result-backend-off.png`, `img/grading-ml-tflite-2fa.png`, `img/result-backend-off.png`.
  - Map saved filter was corrected in evidence to show `저장 매장 1곳` with `올리브영 부산대점` and `지도로 이동`. Evidence: `android/map-saved-filter.png`, `ui/map-saved-filter-summary.txt`.
  - Map zoom/cluster evidence shows selected store label, cluster button, `이 지역 재검색`, and min zoom behavior. Evidence: `android/map-cluster.png`, `android/map-marker-accuracy-zoom19.png`.
- Stability:
  - Final crash buffer is 0 lines: `logs/crash-final.txt`.
  - `gfxinfo`, `framestats`, and `meminfo` were saved under `perf/`.

## 2026-06-21 Map Zoom Limit + Marker Anchor + ANR Guard QA

- Artifact directory: `/tmp/oliveme-map-zoom-limit-marker-20260621-100105`
- Scope:
  - Map zoom-out now stops at zoom 14. This prevents an overly broad map area from forcing the app to display or search too many stores at once.
  - Android `MapViewModel` uses the same min zoom policy, so visible-region refresh cannot request the old zoom <=13 / 20km broad radius path from normal UI operation.
  - Removed `MapWebViewWarmup`; Main/Result/MyPage keep store data warmup only. MapActivity now shows the lightweight map skeleton and store list immediately, then attaches WebView after the first focus window to avoid WebView/Chromium startup ANR.
  - Marker code was audited and E2E-checked: the Leaflet `divIcon` uses `iconAnchor: [20, 50]`, matching the SVG pin tip at `M20 50`; selected marker scale uses `transform-origin: 20px 50px`, so the pin tip remains the lat/lng anchor under zoom.
- Build/install:
  - `cmd.exe /C gradlew.bat :app:compileDebugKotlin :app:assembleDebug --console=plain`: pass.
  - Direct ADB streaming install of the final APK: pass.
  - The AVD initially blocked install due 7% `/data` free. `pm uninstall-system-updates com.google.android.youtube` restored enough emulator space without touching OliveMe data.
- Android E2E:
  - Login -> email -> guest -> 2FA -> Main completed without the earlier Main WebView-prewarm ANR after removing `MapWebViewWarmup`. Evidence: `android/02-main-after-webview-prewarm-removal.png`, `logs/logcat-after-webview-prewarm-removal.txt`.
  - Map entry no longer ANRs after delayed WebView attach. At 4s the responsive skeleton/list state was visible; at 8s WebView marker accessibility nodes were present. Evidence: `android/06-map-4s-attach-delay.png`, `ui/13-map-4s-attach-delay-summary.txt`, `android/07-map-8s-attach-delay.png`, `ui/14-map-8s-attach-delay-summary.txt`; `logs/logcat-map-entry-after-attach-delay.txt` has no ANR hit after the final run.
  - Zoom-out stress stopped at the min zoom state: selected store remained an individual pin + label, nearby stores clustered, `이 지역 재검색` appeared, and `zoom-out` lost focusable state. Evidence: `android/08-zoom-out-min14-limited.png`, `ui/15-zoom-out-min14-limited-summary.txt`.
  - Zoom-in stress reached max zoom without crash/ANR; selected marker and label remained attached. Evidence: `android/09-zoom-in-max20-marker-anchor.png`, `ui/16-zoom-in-max20-marker-anchor-summary.txt`.
  - Zoom 19 detail check shows the pin tip fixed on the same 부산대학로 building/road area with label and selected card aligned. Evidence: `android/10-zoom-19-marker-anchor.png`, `ui/17-zoom-19-marker-anchor-summary.txt`.
  - Final focused crash buffer is 0 lines: `logs/crash-after-zoom-limit-marker.txt`.

## 2026-06-21 Map Draggable Sheet + Kakao Search Limit QA

- Artifact directory: `/tmp/oliveme-map-sheet-limit-20260621-093640`
- Scope:
  - The bottom sheet handle above `근처 매장 N곳` is now an actual drag control. Users can expand or collapse the store list between safe min/max heights without changing map coordinates.
  - The app-side Kakao Local cap was raised from 30 to 45. `StoreRepository` now requests `size=15` pages until `meta.is_end`, up to the official single keyword-search exposed limit.
  - Kakao Local official docs were checked with `gstack-browse`: keyword search `size` is max 15 and the response exposes `meta.total_count`, `meta.pageable_count`, and `meta.is_end`; the documented keyword-search `pageable_count` exposure limit is 45. A truly unlimited "show every nearby store" mode would require multiple region/grid searches or backend aggregation, not a single Kakao keyword search.
- Build/static:
  - `cmd.exe /C gradlew.bat :app:compileDebugKotlin :app:installDebug --console=plain`: pass after the draggable sheet and selected-list guard changes.
  - Earlier full `cmd.exe /C gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:installDebug --console=plain`: pass for the same feature set before the final selected-list guard.
  - Final `cmd.exe /C gradlew.bat :app:testDebugUnitTest :app:assembleDebug --console=plain`: pass after the selected-list guard and docs update.
  - Final Gradle `installDebug` retry hit emulator `INSTALL_FAILED_INSUFFICIENT_STORAGE` during package-manager staging. Removing the stale `/data/local/tmp/app-debug.apk` was not enough for Gradle staging, but direct ADB streaming install of the same `app-debug.apk` succeeded.
- Android E2E:
  - Login -> email -> guest -> 2FA -> Main -> Map completed on `emulator-5554`.
  - Default Map state showed `주변 뷰티 매장`, count `7`, the draggable handle semantics `매장 목록 높이 조절`, selected store `올리브영 부산대점`, and visible `지도 앱 열기`. Evidence: `android/10-map-default-liststate.png`, `ui/13-map-default-liststate-summary.txt`.
  - Dragging the handle upward expanded the sheet. The handle moved from bounds `[32,742][688,798]` to `[32,379][688,435]`, the selected store remained the first card, and more store cards became visible. Evidence: `android/11-map-expanded-liststate.png`, `ui/14-map-expanded-liststate-summary.txt`.
  - Dragging downward collapsed the sheet only to the safe minimum. The first card and `지도 앱 열기` remained fully visible, avoiding the earlier clipped-button state. Evidence: `android/12-map-collapsed-liststate.png`, `ui/15-map-collapsed-liststate-summary.txt`.
  - Tapping `저장` changed both the header and bottom-sheet title to `저장 매장 1곳`, with one saved marker/card and no search-like UI. Evidence: `android/13-map-saved-filter-sheet.png`, `ui/16-map-saved-filter-sheet-summary.txt`.
  - Final focused crash buffers are 0 lines: `logs/crash-final-liststate.txt`, `logs/crash-after-streaming-install.txt`.

## 2026-06-21 Text Clipping + 2FA Canvas Clamp QA

- Artifact directory: `/tmp/oliveme-text-clip-final-20260621-045931`
- Scope:
  - Main drawer was made scroll-safe and width-constrained so `홈`, `진단`, `결과`, `매장`, `설정`, `로그아웃` remain fully visible on the 720x1280 small emulator.
  - Common buttons/chips/top-bar titles now use bounded lines and ellipsis where needed; Main story chips, MyPage tabs, Result tabs, and Settings theme chips are horizontally scroll-safe.
  - Digit 2FA now clamps pointer coordinates and bitmap-render coordinates to the canvas bounds, and the bottom defensive sentence was removed.
- Build/static:
  - `cmd.exe /C gradlew.bat :app:compileDebugKotlin --console=plain`: pass.
  - `cmd.exe /C gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:installDebug --console=plain`: pass.
  - `git diff --check -- . ':!LICENSE'`: pass.
  - Source scan found no 2FA UI copy `오류가 나도` or old ready hint `숫자를 크게`.
- Android E2E:
  - Login -> email -> guest -> 2FA completed. 2FA UI tree shows only `캔버스 안에 숫자를 그려주세요.` and no bottom defensive sentence. Evidence: `android/01-2fa-before-draw.png`, `ui/01-2fa-before-draw-summary.txt`.
  - A drag starting inside the canvas and moving below it did not render outside the canvas. Evidence: `android/02-2fa-outside-drag-clamped.png`.
  - Drawing `1` and tapping `확인` reached Main. Evidence: `android/03-main-after-2fa.png`, `ui/03-main-after-2fa-summary.txt`.
  - Drawer shows full `홈`, `진단`, `결과`, `매장`, `설정`, `로그아웃` text with `로그아웃` above the navigation bar. Evidence: `android/04-drawer-fixed.png`, `ui/04-drawer-fixed-summary.txt`.
  - Settings account/privacy/theme sections show complete row text and all theme chips `기본`, `봄`, `여름`, `가을`, `겨울`. Evidence: `android/05-settings-tabs-fixed.png`, `android/06-settings-theme-chips-fixed.png`.
  - MyPage profile chips and tabs show complete `가을 웜 트루`, `이력 6`, `리포트`, `이력`, `저장 매장`. Evidence: `android/07-mypage-tabs-fixed.png`, `ui/07-mypage-tabs-fixed-summary.txt`.
  - Result tabs show complete `내 컬러`, `의상`, `메이크업`, `특징`. Evidence: `android/10-result-tabs-fixed.png`, `ui/10-result-tabs-fixed-summary.txt`.
  - Map filter chips and selected-card action remain complete after common chip changes. Evidence: `android/11-map-filter-regression.png`, `ui/11-map-filter-regression-summary.txt`.
  - Diagnosis top bar and upload copy remain complete after common top-bar changes. Evidence: `android/12-diagnosis-topbar-fixed.png`, `ui/12-diagnosis-topbar-fixed-summary.txt`.
  - Focused crash buffer after the run is 0 lines: `logs/crash-final-after-diagnosis.txt`.

## 2026-06-21 Map Saved Filter Label + Preload QA

- Scope:
  - Fixed saved-filter wording so the Map status pill and bottom sheet title both say `저장 매장 N곳`.
  - Added Map data warmup from Main/Result/MyPage in addition to the existing WebView shell warmup. The default 부산대 store query is cached briefly so MapActivity can render the list without waiting for a fresh API round trip.
  - Removed the artificial 300ms WebView attach delay in `MapScreen` so a warmed shell can render as soon as MapActivity opens.
- Verification:
  - `cmd.exe /C gradlew.bat :app:compileDebugKotlin --console=plain`: pass.
  - `cmd.exe /C gradlew.bat :app:assembleDebug --console=plain`: pass.
  - `cmd.exe /C gradlew.bat :app:installDebug --console=plain`: pass on `emulator-5554`.
  - `git diff --check -- . ':!LICENSE'`: pass.
  - Android E2E path: Login -> email -> guest -> 2FA -> Main -> Map -> `저장` filter.
  - Map entry capture already showed `주변 뷰티 매장`, count `7`, `근처 매장 7곳`, selected store card, and `지도 앱 열기` without transient `0곳`/empty failure copy. Evidence: `/tmp/oliveme-map-entry-after-warmup.png`, `/tmp/oliveme-ui-map-entry-summary.txt`.
  - Saved-filter UI tree shows top `저장 매장 1곳`, bottom `저장 매장 1곳`, `즐겨찾기에 담은 매장`, one saved store card, and synced WebView marker/label. Evidence: `/tmp/oliveme-map-saved-title-final.png`, `/tmp/oliveme-ui-map-saved-title-final-summary.txt`.
  - Focused crash buffer after the run is 0 lines.

## 2026-06-21 Static Map Shell First-Entry QA

- Branch: `dev`
- Artifact directory: `/tmp/oliveme-map-entry-static-shell-20260621-034323`
- Build/static:
  - `cmd.exe /C gradlew.bat :app:testDebugUnitTest :app:assembleDebug --console=plain` passed.
  - `cmd.exe /C gradlew.bat :app:installDebug --console=plain` passed after the final asset debounce restore.
  - `/mnt/c/nvm4w/nodejs/node.exe --check backend-proxy/src/server.js` passed.
  - `git diff --check -- . ':!LICENSE'` passed.
  - Source scan found no `loadDataWithBaseURL`, old inline `webMapHtml`, fake search header, or `영업 중` map UI reintroduction.
  - Backend stayed running: `/health` returned `ok=true`, `geminiConfigured=true`, `naverShoppingConfigured=true`, `fcmConfigured=false`.
- Implementation verified:
  - Map WebView now loads `file:///android_asset/web/oliveme_map.html` once and sends store/selection/center/zoom through `window.OliveMeMapRuntime.setData(...)`.
  - The old full HTML reload path was removed from `MapScreen`.
  - Main/Result schedule application-context WebView shell warmup; warmup loads only the local shell/Leaflet assets and destroys itself without loading map tiles.
  - Map first draw keeps the lightweight Compose skeleton and bottom sheet usable until WebView ready.
- Android E2E:
  - 부산대 emulator location stayed fixed at `35.2310,129.0842`.
  - Login -> guest -> 2FA -> Main -> Map completed.
  - 1s capture already showed a nonblank screen with `주변 뷰티 매장`, count `7`, `근처 매장 7곳`, `올리브영 부산대점`, and `지도 앱 열기`; no transient `0곳`/`찾지 못했습니다`.
  - 3s and 12s captures exposed WebView `resource-id="map"`, store marker accessibility labels, selected label, and zoom controls. Evidence: `android/35-final-map-1s.png`, `36-final-map-3s.png`, `38-final-map-12s.png`.
  - Zoom-out/pan still kept selected label, cluster bubble, `이 지역 재검색`, and unchanged bottom list. Evidence: `android/39-final-zoom-out.png`, `40-final-pan-refresh.png`.
- Performance/log:
  - Previous entry baseline from the caution run was `137/176` janky frames, p95 `400ms`, p99 `4950ms`.
  - Final focused entry runs: run1 p95 `350ms`, p99 `950ms`; run2 p95 `150ms`, p99 `200ms`; run3 p95 `300ms`, p99 `400ms`; run4 p95 `200ms`, p99 `300ms`.
  - Stable runs 2-4 median: p95 `200ms`, p99 `300ms`, janky ratio `52.94%`; the old `4950ms` CPU histogram bucket was gone.
  - A first cold/tile outlier remains possible on the emulator, so Map is marked improved rather than perfectly smooth.
  - Rapid zoom stress remains the main residual WebView/tile performance caution; crash buffers stayed 0 lines.
  - Final crash buffer: `logs/crash-final-all.txt` is 0 lines.
  - Final trim memory: `Activities=2`, `WebViews=1`, `TOTAL PSS 196469KB`. Evidence: `perf/meminfo-final-after-trim.txt`.
- README screenshots refreshed: `img/map.png`, `img/map-cluster.png`, `img/map-refresh.png`.

## 2026-06-21 Map WebView Caution Fix QA

- Branch: `dev`
- Artifact directory: `/tmp/oliveme-map-webview-fix-20260621-030301`
- Baseline compared: `/tmp/oliveme-map-cautions-20260621-021910`
- Build/static:
  - `cmd.exe /C gradlew.bat :app:testDebugUnitTest :app:assembleDebug` passed after the last-known patch.
  - `:app:installDebug` initially failed only because the emulator had `INSTALL_FAILED_INSUFFICIENT_STORAGE`; old `com.example.*` test apps and `/data/local/tmp` were removed, then `cmd.exe /C gradlew.bat :app:installDebug --console=plain` passed.
  - Backend stayed running on port `8787`; `/health` recorded `ok=true`, `geminiConfigured=true`, `naverShoppingConfigured=true`.
- Device/location setup:
  - Emulator serial: `emulator-5554`.
  - `gps`, `network`, and `fused` test providers were fixed to 부산대 `35.2310,129.0842`. Evidence: `logs/location-preflight.txt`.
- Fixes verified:
  - Map no longer shows transient `근처 매장 0곳` or `근처 매장을 찾지 못했습니다` during location lookup. Latest APK 5s and 12s captures both show `근처 매장 7곳`, `현재 위치 기준 추천 매장`. Evidence: `android/23-final-map-5s-after-lastknown-patch.png`, `android/24-final-map-12s-after-lastknown-patch.png`, `ui/30-final-map-5s-after-lastknown-patch-summary.txt`, `ui/31-final-map-12s-after-lastknown-patch-summary.txt`.
  - Map 왕복 2회 후 5초 시점도 바로 `근처 매장 7곳`으로 유지됐다. Evidence: `android/25-final-cycle-1-5s-after-lastknown-patch.png`, `android/25-final-cycle-2-5s-after-lastknown-patch.png`.
  - Zoom-out 5회 후 선택 핀과 `올리브영 부산대점` 라벨이 bottom sheet 위에 유지되고, 나머지 6개 매장은 cluster bubble로 묶였다. Evidence: `android/14-zoom-out-label-fixed-retry.png`, `ui/17-zoom-out-label-fixed-retry-summary.txt`.
  - Pan stress 후 목록은 `근처 매장 7곳`으로 유지되고 `이 지역 재검색`만 표시됐다. Evidence: `android/18-pan-stress-refresh-only.png`, `ui/22-pan-stress-refresh-only-summary.txt`.
  - `이 지역 재검색` 후 새 결과가 Kakao Local 단일 keyword search 노출 한계 기준 최대 45개까지 갱신되고 selected/card가 같은 매장으로 정렬됐다. Evidence: `android/15-after-region-refresh-selected-pinned.png`, `ui/18-after-region-refresh-selected-pinned-summary.txt`; latest cap update evidence: `/tmp/oliveme-map-sheet-limit-20260621-093640`.
  - `저장` 필터는 count/list/WebView marker 모두 1개 저장 매장 기준으로 동기화됐다. Evidence: `android/19-saved-filter-after-webview-fix.png`, `ui/23-saved-filter-after-webview-fix-summary.txt`.
  - `지도 앱 열기`는 Google Maps place/coordinate result를 열었고 Back으로 OliveMe `MapActivity`에 복귀했다. Evidence: `android/20-external-map-opened.png`, `android/22-after-external-map-back-fixed-loop.png`, `logs/after-external-map-back-fixed-loop-focus.txt`.
- Performance/log:
  - Zoom stress after fix: `Janky frames 18/25 (72.00%)`, `95th percentile 250ms`, `99th percentile 300ms`; previous caution baseline was `30/35 (85.71%)`, `95th percentile 450ms`.
  - Pan stress after fix: `Janky frames 92/231 (39.83%)`, `95th percentile 57ms`, `99th percentile 77ms`; previous caution baseline was `69/135 (51.11%)`, `95th percentile 85ms`.
  - Map entry aggregate still has WebView/emulator jank (`137/176`, `95th percentile 400ms`, `99th percentile 4950ms`), so this remains a performance caution rather than a crash/stability failure.
  - Final crash buffers are 0 lines: `logs/final-crash-buffer.txt`, `logs/final-crash-buffer-after-lastknown-patch.txt`.
  - Final focused memory after trim/idle returned to `Activities=2`, `WebViews=1`, `TOTAL PSS 196084KB`. Evidence: `perf/meminfo-final-after-trim.txt`.
- README screenshots refreshed from this run: `img/map.png`, `img/map-cluster.png`, `img/map-refresh.png`, `img/map-saved-filter.png`, `img/google-maps.png`.

## 2026-06-21 MyPage Layout + 부산대 Location Store QA

- Branch: `dev`
- Artifact directory: `/tmp/oliveme-mypage-location-fix-20260621-014414`
- Build/static:
  - `cmd.exe /C gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:installDebug --console=plain` passed.
  - Scoped `git diff --check -- . ':!LICENSE'` passed in the focused run; the existing unrelated `LICENSE` dirty change was not touched.
  - Backend stayed running on Windows port `8787`; `/health` returned `ok=true`, `geminiConfigured=true`, `naverShoppingConfigured=true`, `fcmConfigured=false`.
- Device/location setup:
  - Emulator serial: `emulator-5554`.
  - Android test providers and `adb emu geo fix` were set to 부산대 coordinates `35.2310,129.0842`.
  - `dumpsys location` confirmed `network`, `fused`, and `gps` last locations at `35.231000,129.084200`. Evidence: `logs/location-final-latest.txt`.
- MyPage fixes verified:
  - Long personal-color names no longer push the `이력` chip down. UI tree shows `가을 웜 트루` bounds `[210,291][494,339]` and `이력 6` bounds `[558,291][654,339]` on the same row. Evidence: `android/22-final-mypage-report.png`, `ui/22-final-mypage-report-summary.txt`.
  - The large report card title no longer overlaps. Evidence: `android/22-final-mypage-report.png`.
  - Demo seed favorites are no longer auto-inserted. Existing legacy `pnu-*` seed favorites are deleted during demo seed cleanup, leaving only user-saved stores.
  - Pulled Room DB confirmed the visible saved store is not a seed mock: `26313994|demo-test01|올리브영 부산대점|부산 금정구 부산대학로 47|164m|35.231195|129.085991|http://place.map.kakao.com/26313994`. Evidence: `logs/favorite-stores-db.txt`.
- Map/current-location verification:
  - MyPage store card opened MapActivity without crash.
  - Map loaded real Kakao Local 부산대-area results from the forced current location: `주변 뷰티 매장`, count `7`, `근처 매장 7곳`, `현재 위치 기준 추천 매장`, first store `올리브영 부산대점`, `부산 금정구 부산대학로 47`, `164m`. Evidence: `android/25-final-map-pnu-loaded.png`, `ui/25-final-map-pnu-loaded-summary.txt`.
  - Pressing the current-location button kept the same 부산대 current-location result set and did not crash. Evidence: `android/26-map-after-location-button.png`, `ui/26-map-after-location-button-summary.txt`.
  - `지도 앱 열기` opened Google Maps place/coordinate result for `올리브영 부산대점 부산 금정구 부산대학로 47`, not direct Directions, and Back returned to `com.oliveme.app/.MapActivity`. Evidence: `ui/27-map-open-external-summary.txt`, `logs/back-from-maps.txt`.
- Stability/performance:
  - Final crash buffers after MyPage/Map/external handoff are 0 lines: `logs/final-crash-buffer.txt`, `logs/final-crash-buffer-after-external.txt`.
  - `final-logcat-after-external.txt`, `final-gfxinfo-after-external.txt`, `final-meminfo-after-external.txt`, and `final-framestats.txt` saved.
  - WebView map still shows emulator jank in aggregate `gfxinfo`; this is tracked as a residual performance risk, but the current lightweight map path avoids same-payload reloads, limits marker rendering, and stayed crash-free in this focused QA.
- README screenshots refreshed: `img/mypage.png`, `img/map.png`.

## 2026-06-21 Final Dev PR Map Lightweight QA

- Branch: `dev`
- Artifact directory: `/tmp/oliveme-final-dev-pr-qa-XWNwfuYg`
- Build/static:
  - `python3 -m json.tool android/app/src/main/assets/seed/diagnosis_policy.json` passed.
  - `/mnt/c/nvm4w/nodejs/node.exe --check backend-proxy/src/server.js` passed.
  - Scoped `git diff --check` for touched files passed.
  - `cmd.exe /C gradlew.bat :app:testDebugUnitTest :app:assembleDebug --console=plain` passed.
  - `cmd.exe /C gradlew.bat :app:installDebug --console=plain` passed after each focused map patch.
- Map lightweight changes:
  - `MapScreen` no longer reloads the WebView HTML when Compose recomposes with the same map payload.
  - Leaflet disables nonessential map animations, avoids zoom-time tile updates, lowers tile buffer, and coalesces marker/cluster redraws through `requestAnimationFrame`.
  - Region refresh now preserves the current zoom context instead of snapping back to zoom 16.
  - Leaflet renders only stores near the current viewport, while the bottom-sheet list remains unchanged until explicit `이 지역 재검색`.
  - Same-place marker dedupe now preserves the currently selected store within a duplicate group, so one physical place still renders once while the selected label is not lost.
- Android E2E:
  - Login -> guest -> 2FA -> Main -> Map completed after reinstall.
  - Map entry showed non-search `주변 뷰티 매장`, `전체/저장`, `지도 앱 열기`, OSM tiles, and selected label. Evidence: `android/20-map-entry-culling.*`.
  - Zoom-out showed `이 지역 재검색`, selected label, and cluster bubbles. Evidence: `android/21-map-cluster-culling.*`.
  - Region refresh kept zoom/cluster context and changed the label to `지도 영역 기준 추천 매장`. Evidence: `android/22-map-refresh-culling.*`.
  - Saved filter showed `근처 매장 0곳` and the empty state with no stale markers. Evidence: `android/16-map-saved-filter-final.*`.
  - Current-location permission path was rerun after revoking location permission. The system permission sheet appears from the map button, denial shows the app dialog with `설정 열기`, and crash buffers are 0 lines. Evidence: `android/26-location-permission-request-after-map-entry.*`, `android/27-location-denied-fallback.*`.
  - MyPage settings gear opens Settings without crash. Evidence: `android/28a-mypage-before-settings.*`, `android/28-settings-open-final.*`.
  - MyPage saved store card opened MapActivity. Evidence: `android/18-map-from-mypage-store.*`.
  - After the selected-dedupe safety patch and reinstall, Login -> guest -> 2FA -> Main -> Map completed again. Evidence: `android/29-final-reinstall-launch.*` through `android/34-final-post-patch-map-cluster.*`; final post-patch crash buffer is 0 lines.
- Performance/log:
  - Map culling focused `gfxinfo` shows reduced view/render node footprint versus the pre-culling focused sample: `Total attached Views 36 -> 26`, `Total RenderNode 59.05KB -> 40.19KB`.
  - WebView zoom transitions are still janky on the small emulator in focused samples, so the practical mitigation is reduced DOM/reload work rather than claiming perfect frame pacing.
  - Crash buffers `logs/final-crash-buffer-after-map.txt`, `logs/map-focused-crash-after-reset.txt`, `logs/map-culling-focused-crash.txt`, `logs/final-clean-crash-after-culling.txt`, `logs/location-permission-map-entry-crash.txt`, `logs/location-denied-fallback-crash.txt`, `logs/settings-open-final-crash.txt`, and `logs/final-post-patch-map-crash.txt` are all 0 lines.
- README screenshots refreshed: `img/map.png`, `img/map-cluster.png`, `img/map-refresh.png`, `img/map-saved-filter.png`.

## 2026-06-21 Room Color Diversity Display-Safety QA

- Branch: `dev`
- Artifact directory: `/tmp/oliveme-db-color-diversity-20260621-004011`
- Build/static:
  - `python3 -m json.tool android/app/src/main/assets/seed/diagnosis_policy.json` passed.
  - `cmd.exe /C gradlew.bat :app:compileDebugKotlin --console=plain` passed.
  - `cmd.exe /C gradlew.bat :app:installDebug --console=plain` initially hit emulator `INSTALL_FAILED_INSUFFICIENT_STORAGE`; `pm trim-caches 2G` freed cache without clearing app data, then install passed.
  - `cmd.exe /C gradlew.bat :app:testDebugUnitTest --console=plain` passed.
- Display normalization behavior:
  - Opened existing guest DB without clearing app data: Login -> 2FA -> Main -> MyPage.
  - MyPage `베스트 컬러 6` now shows a more varied winter-deep palette: `딥 베리`, `블랙 네이비`, `딥 플럼`, `쿨 로즈`, `실버 그레이`, with `아이스 핑크` as the sixth off-screen swatch. Evidence: `android/04-mypage-palette-after-repair.*`.
- Safety follow-up: the first pass rewrote `recommended_colors`/`product_recommendations` during read, which was judged too risky because a read path should not delete/insert user history. The final code keeps the varied palette as display-only normalization and no longer mutates Room rows from `DiagnosisRepository.result()`.
- Safety recheck artifact: `/tmp/oliveme-db-color-safety-recheck-20260621-004658`; newly installed APK still showed the varied palette in MyPage and `android/04-crash-buffer.txt` was 0 lines.
- README screenshot refreshed: `img/mypage.png`.
- Crash evidence: `android/05-crash-buffer.txt` is 0 lines.

## 2026-06-21 Makeup Recommendation Precision + Backend-On QA

- Branch: `dev`
- Artifact directory: `/tmp/oliveme-makeup-fix-final-20260621-001734`
- Backend final state: Windows Node process stayed running on port `8787`; final PID during QA was `28576`.
- Build/static:
  - `cmd.exe /C gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:installDebug --console=plain` passed.
  - `/mnt/c/nvm4w/nodejs/node.exe --check backend-proxy/src/server.js` passed.
  - `/health` returned `ok=true`, `geminiConfigured=true`, `naverShoppingConfigured=true`, `fcmConfigured=false`; secret values were not printed.
- Backend recommendation smoke:
  - `recommend-makeup-6.json` through `recommend-makeup-10.json`: all returned `source=naver-shopping`, `items=8`, families included `lip`, `eye`, `base`, `cheek`, `추천 아이템` was absent, and nail/base-coat noise was absent.
  - `recommend-makeup-11.json` through `recommend-makeup-13.json`: local fallback summary used concise category wording: `립, 아이, 베이스, 치크가 한쪽으로 치우치지 않도록`.
  - Gemini commerce direct smoke returned `429 RESOURCE_EXHAUSTED`, so backend local fallback summary was expected for this run while Naver products still rendered.
- Android E2E:
  - Login -> guest -> 2FA -> Main -> MyPage completed. MyPage `추천 메이크업` no longer shows four generic `추천 아이템`; it shows `딥 베리 립`, `차콜 섀도`, `핑크 베이스`, `쿨 로즈 치크`. Evidence: `android/05-mypage-makeup.*`.
  - Result `메이크업` with backend on shows `기본 컬러 요약`, then `AI 추천` with concise fallback copy. Evidence: `android/11-result-makeup-top-copy-fixed.*`.
  - Result product rows show real Naver thumbnails and category-specific pick reasons for lip/eye/base candidates. Evidence: `android/13-result-makeup-product-images.*`, `android/14-result-makeup-product-diversity.*`.
  - Backend-unreachable behavior was tested without stopping the backend process by temporarily removing `adb reverse`; Result showed `로컬 컬러 가이드` and hid AI/realtime commerce. Reverse was restored afterward. Evidence: `android/15-result-makeup-backend-unreachable.*`.
- Screenshots refreshed in README: `img/mypage.png`, `img/result-ai-makeup.png`, `img/result-products.png`, `img/result-backend-off.png`.
- Crash/log evidence: `android/16-crash-buffer.txt` is 0 lines; `android/16-logcat.txt` saved.

## 2026-06-20 Map Status Header + Refresh + External Map Fallback QA

- Branch: `dev`
- Artifact directory: `/tmp/oliveme-map-refresh-final-qa-20260620-231448`
- Build/static:
  - `cmd.exe /C gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:installDebug --console=plain` passed before focused QA. Evidence: `gradle-build-install.txt`.
  - After the saved-filter map-marker sync fix, `:app:assembleDebug :app:installDebug` passed. Evidence: `gradle-reinstall-after-filter-fix.txt`.
  - Final `:app:testDebugUnitTest :app:assembleDebug` passed. Evidence: `gradle-final-test-assemble.txt`.
  - `/mnt/c/nvm4w/nodejs/node.exe --check backend-proxy/src/server.js` passed. Evidence: `backend-node-check.txt`.
  - Scoped `git diff --check` for touched map files passed; source scan found no removed map-action label, fake search header, or operating-status map UI strings in the map code.
- Map status UI:
  - Initial Map E2E `Login -> 2FA -> Main -> Map` completed. Evidence: `01-launch.*` through `04-map-entry.*`.
  - Header now shows non-clickable `주변 뷰티 매장` with count badge and no search icon/text. Evidence: `04-map-entry.summary.txt`.
  - Selected card action is `지도 앱 열기`. Evidence: `04-map-entry.summary.txt`, `04-map-entry.png`.
- Region refresh and clustering:
  - Zoom-out shows `이 지역 재검색`, keeps the selected store as individual pin + label, and groups nearby stores into accessible cluster buttons such as `2개 매장 묶음`, `3개 매장 묶음`, `5개 매장 묶음`. Evidence: `05-map-after-zoom-out.*`.
  - Tapping `이 지역 재검색` updates the location label to `지도 영역 기준 추천 매장`; count remains within the max-45 single-search exposure rule. Evidence: `06-map-after-region-refresh.*`; latest cap update evidence: `/tmp/oliveme-map-sheet-limit-20260621-093640`.
  - Cluster tap stays inside the app map, expands/moves to the grouped bounds, and keeps the bottom-sheet list count unchanged. Evidence: `08-map-after-cluster-tap.*`.
  - A focused bug was found and fixed: `저장` filter initially showed an empty list while map markers still displayed all stores. After fix, saved filter synchronizes header count, list, and WebView markers to 0 saved stores. Evidence: `18-map-saved-filter-after-fix.*`.
- External map fallback:
  - Google Maps app enabled: `지도 앱 열기` opens Google Maps place/coordinate result, not the Directions start screen; crash buffer is 0 lines. Evidence: `19-google-maps-active-open.*`, `19-google-maps-active-crash.txt`.
  - Google Maps disabled: Chrome fallback opens Google Maps web URL; crash buffer is 0 lines. Evidence: `21-browser-fallback-open.*`, `21-browser-fallback-crash.txt`; package was re-enabled in `20-google-maps-reenable.txt`.
  - Google Maps + Chrome disabled: app stays on Map, link is copied, toast says `브라우저를 열 수 없어 지도 링크를 복사했습니다.`, crash buffer is 0 lines. Evidence: `23-no-handler-clipboard-toast.*`, `23-no-handler-clipboard-crash.txt`; both packages were re-enabled in `22-google-maps-reenable.txt` and `22-chrome-reenable.txt`.
- Performance/log evidence:
  - Final crash buffer is 0 lines: `final-crash-buffer.txt`.
  - `final-logcat.txt`, `final-gfxinfo.txt`, `final-framestats.txt`, and `final-meminfo.txt` saved.

## 2026-06-20 AI-First Result UI + Final Stability QA

- Branch: `dev`
- Artifact directory: `/tmp/oliveme-ai-first-final-qa-20260620-220021`
- Build: Windows Gradle `:app:testDebugUnitTest :app:assembleDebug :app:installDebug --console=plain` passed after the AI-first Result UI and generic-name fallback fix. Evidence: `static/gradle-build-install-after-generic-name-fix.txt`.
- Backend syntax: Windows Node `--check backend-proxy/src/server.js` passed. Evidence: `static/backend-node-check.txt`.
- Result AI-first UI:
  - Clothing tab shows `의상 추천`, compact `기본 컬러 요약`, then `AI 추천` in the first viewport. Evidence: `android/30-result-clothes-top-after-name-fix.*`.
  - Clothing tab then shows `AI가 고른 상품`, `실시간 상품 추천`, and finally `최종 컬러 분석`. Evidence: `android/18-result-clothes-products-backend-on.*`, `20-result-clothes-final-analysis-backend-on.*`, `24-result-clothes-final-analysis-backend-on.*`.
  - Makeup tab shows `메이크업 추천`, compact `기본 컬러 요약`, `립 추천 · 아이 추천 · 베이스 추천`, then `AI 추천` in the first viewport. Evidence: `android/31-result-makeup-top-after-name-fix.*`.
- Real backend on:
  - `/health` returned configured Gemini/Naver status, Naver search returned renderable products, and recommendations returned `source=naver-shopping`, renderable items, AI summary, and picks. Evidence: `backend/backend-smoke-summary.txt`.
  - Android Result clothing and makeup tabs displayed AI/realtime commerce with thumbnails and prices. Focused crash buffer: `logs/crash-backend-on-after-name-fix.txt` is 0 lines.
- Backend off and fault injection:
  - `adb reverse --remove tcp:8787`: Result kept `기본 컬러 요약` + `최종 컬러 분석`, with no `AI 추천` or `실시간 상품 추천`. Evidence: `android/33-result-clothes-backend-off.*`, crash 0.
  - HTTP 500 fault: commerce hidden, local analysis remained, crash 0. Evidence: `fault/fault-500-response.txt`, `android/34-result-clothes-fault-500.*`.
  - Empty/malformed product fault: blank title/link/image products were filtered out; no broken product card, crash 0. Evidence: `fault/fault-empty-malformed-response.json`, `android/35-result-clothes-fault-empty-malformed.*`.
  - Missing `aiSummary` fault: `AI 추천` card hidden while valid `실시간 상품 추천` products rendered. Evidence: `fault/fault-no-ai-response.json`, `android/36-result-clothes-fault-no-ai.*`.
  - Timeout fault: server delayed beyond Android read timeout; commerce hidden, local analysis remained, crash 0. Evidence: `fault/fault-timeout-curl.txt`, `android/37-result-clothes-fault-timeout.*`.
- Result save/share: save icon, overflow menu, and Android share chooser opened without crash. Evidence: `android/38-result-save-overflow.*`, `android/39-result-share-chooser.*`.
- Performance evidence: `perf/gfxinfo-final.txt`, `perf/gfxinfo-framestats-final.txt`, and `perf/meminfo-final.txt` saved.
- Whitespace/static note: full `git diff --check` still reports unrelated pre-existing `LICENSE` trailing whitespace; scoped diff check for touched files passed. Refined banned-string/stale-doc/secret scans were 0 lines.

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
- Google Maps external QA: tapping the selected store map action opened `com.google.android.apps.maps/com.google.android.maps.MapsActivity`; Google Maps displayed the Olive Young place card with Directions/Call/Save/Share actions.
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
  - Filters are reduced to `전체`, `저장`; selected store shows favorite and directions actions. Opening status is not shown because Kakao Local/seed store data does not provide verified business hours.
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

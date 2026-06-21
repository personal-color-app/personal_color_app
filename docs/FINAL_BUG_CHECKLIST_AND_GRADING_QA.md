# OliveMe Final Bug Checklist and Grading QA

Date: 2026-06-20
Branch: `dev`
Primary Result artifact dir: `/tmp/oliveme-ai-first-final-qa-20260620-220021`
Primary Map artifact dir: `/tmp/oliveme-map-refresh-final-qa-20260620-231448`
Latest makeup/backend precision artifact dir: `/tmp/oliveme-makeup-fix-final-20260621-001734`
Latest final dev PR map lightweight artifact dir: `/tmp/oliveme-final-dev-pr-qa-XWNwfuYg`
Latest MyPage/location artifact dir: `/tmp/oliveme-mypage-location-fix-20260621-014414`
Latest Map WebView caution artifact dir: `/tmp/oliveme-map-webview-fix-20260621-030301`
Latest static map shell first-entry artifact dir: `/tmp/oliveme-map-entry-static-shell-20260621-034323`
Latest text clipping/2FA clamp artifact dir: `/tmp/oliveme-text-clip-final-20260621-045931`
Latest map draggable sheet/search-limit artifact dir: `/tmp/oliveme-map-sheet-limit-20260621-093640`
Latest map zoom-limit/marker-anchor artifact dir: `/tmp/oliveme-map-zoom-limit-marker-20260621-100105`
Latest grading evidence/full screenshot artifact dir: `/tmp/oliveme-grading-final-20260621-125344`

This is the final tracked checklist for the AI-first Result UI and stability QA.
It replaces local-only `plan/` checklists for final evidence. Do not record
secret values here.

## Source Baseline

- [x] `docs/TRUTH_SPEC.md` is the source of truth.
- [x] Current Android app is the final UI baseline.
- [x] `Personalcolor design/` is treated as old visual reference only.
- [x] `plan/` materials and grading photos were re-read for scoring criteria.
- [x] Existing unrelated dirty `LICENSE` change is preserved.
- [x] `docs/GRADING_FEATURE_EVIDENCE.md` records the final scoring judgment, code evidence, screenshots, QA artifact, and backend/API-off defense for every grading item requested by the user.

## Final Grading Evidence Screenshot Checklist

| Item | Expected | Status | Evidence |
| --- | --- | --- | --- |
| Grading evidence doc | New Markdown under `docs/` with item-by-item scoring judgment | pass | `docs/GRADING_FEATURE_EVIDENCE.md` |
| README screenshots | README links representative final screenshots and grading evidence screenshots | pass | `README.md`, `img/grading-*.png` |
| Desktop screenshot export | Latest screenshots copied to `C:\Users\pjjpj\Desktop\새 폴더` with descriptive names | pass | same filenames as `img/grading-*.png`; copied during `/tmp/oliveme-grading-final-20260621-125344` run |
| Coroutine | `viewModelScope`, `withContext(Dispatchers.IO)`, `suspend` evidence plus E2E screenshot | pass | `img/diagnosis-analyzing.png`, `ui/diagnosis-analyzing-summary.txt` |
| Download/API manager | Retrofit/OkHttp + Glide evidence, not falsely claiming Android OS `DownloadManager` | pass | `img/result-products.png`, `backend/backend-smoke-summary.txt` |
| Jetpack | Compose, Room, ViewModel/Lifecycle, ActivityResult, LegacyJetpackEvidence | pass | `img/main.png`, source audit |
| External app | Gallery/camera entry, share chooser, Google Maps external path | pass | `img/diagnosis-source-sheet.png`, `img/result-share.png`, `img/google-maps.png` |
| Room DB | Local Room DB history and saved stores | pass | `img/mypage-history.png`, `img/mypage-saved-stores.png` |
| API 3+ | Gemini/Kakao/Naver/backend/OSM/Google Maps paths separated and evidenced | pass | `img/result-ai-clothes.png`, `img/map.png`, `backend/backend-commerce-smoke.json` |
| ML | TensorFlow Lite MNIST handwritten digit recognition for 2FA | pass | `img/grading-ml-tflite-2fa.png` |
| Stability | backend-off local guide, crash buffer 0, reverse restored | pass | `img/result-backend-off.png`, `logs/crash-final.txt` 0 lines, `backend/adb-reverse-final.txt` |

## Text Clipping and 2FA Clamp Checklist

| Item | Expected | Status | Evidence |
| --- | --- | --- | --- |
| Main drawer small-screen layout | Drawer labels and `로그아웃` are fully visible, scroll-safe, and not clipped by nav bar | pass | `/tmp/oliveme-text-clip-final-20260621-045931/android/04-drawer-fixed.png`, `ui/04-drawer-fixed-summary.txt` |
| Common chip/button/title safety | Buttons/chips/top-bar titles use bounded line handling; long tab rows are scroll-safe | pass | Source audit: `Common.kt`, `MainScreen.kt`, `MyPageScreen.kt`, `ResultScreen.kt`, `SettingsScreen.kt`; screenshots below |
| Settings theme chips | `기본`, `봄`, `여름`, `가을`, `겨울` remain complete | pass | `/tmp/oliveme-text-clip-final-20260621-045931/android/06-settings-theme-chips-fixed.png`, `ui/06-settings-theme-chips-fixed-summary.txt` |
| MyPage profile/tabs | `가을 웜 트루`, `이력 6`, `리포트`, `이력`, `저장 매장` remain complete | pass | `/tmp/oliveme-text-clip-final-20260621-045931/android/07-mypage-tabs-fixed.png`, `ui/07-mypage-tabs-fixed-summary.txt` |
| Result tabs | `내 컬러`, `의상`, `메이크업`, `특징` remain complete | pass | `/tmp/oliveme-text-clip-final-20260621-045931/android/10-result-tabs-fixed.png`, `ui/10-result-tabs-fixed-summary.txt` |
| Map filters | `전체`, `저장`, map header, selected-card action remain complete after common chip changes | pass | `/tmp/oliveme-text-clip-final-20260621-045931/android/11-map-filter-regression.png`, `ui/11-map-filter-regression-summary.txt` |
| Diagnosis top bar | `컬러 진단` title and upload copy remain complete after common top-bar changes | pass | `/tmp/oliveme-text-clip-final-20260621-045931/android/12-diagnosis-topbar-fixed.png`, `ui/12-diagnosis-topbar-fixed-summary.txt` |
| 2FA bottom copy cleanup | Removed the bottom `오류가 나도...` copy; only concise canvas instruction remains | pass | `/tmp/oliveme-text-clip-final-20260621-045931/ui/01-2fa-before-draw-summary.txt` |
| 2FA canvas bounds | Dragging outside the canvas does not render stroke outside the canvas or leak out-of-bounds bitmap coordinates | pass | `/tmp/oliveme-text-clip-final-20260621-045931/android/02-2fa-outside-drag-clamped.png`; code clamps pointer and bitmap coordinates |
| Focused crash buffer | No app crash after new layout, 2FA, Result, Settings, MyPage, Map, and Diagnosis flows | pass | `/tmp/oliveme-text-clip-final-20260621-045931/logs/crash-final-after-diagnosis.txt` is 0 lines |

## AI-First Result UI Checklist

| Item | Expected | Status | Evidence |
| --- | --- | --- | --- |
| Clothing tab order | `기본 컬러 요약` -> `AI 추천` -> `AI가 고른 상품` -> `실시간 상품 추천` -> `최종 컬러 분석` | pass | `android/30-result-clothes-top-after-name-fix.*`, `18-result-clothes-products-backend-on.*`, `20-result-clothes-final-analysis-backend-on.*`, `24-result-clothes-final-analysis-backend-on.*` |
| Makeup tab order | Same as clothing | pass | `android/31-result-makeup-top-after-name-fix.*`; top viewport shows `메이크업 추천`, `기본 컬러 요약`, `립 추천 · 아이 추천 · 베이스 추천`, `AI 추천` |
| Backend off | AI/realtime sections hidden, local summary and final analysis remain | pass | `android/33-result-clothes-backend-off.*`, `logs/crash-backend-off.txt` is 0 lines |
| Backend 500 | No crash, commerce hidden | pass | `fault/fault-500-response.txt`, `android/34-result-clothes-fault-500.*`, `logs/crash-fault-500.txt` is 0 lines |
| Backend timeout | No crash, commerce hidden | pass | `fault/fault-timeout-curl.txt`, `android/37-result-clothes-fault-timeout.*`, `logs/crash-fault-timeout.txt` is 0 lines |
| Empty/malformed products | Blank title/link/image products not rendered | pass | `fault/fault-empty-malformed-response.json`, `android/35-result-clothes-fault-empty-malformed.*`, `logs/crash-fault-empty-malformed.txt` is 0 lines |
| Missing `aiSummary` | Products render, AI card hides safely | pass | `fault/fault-no-ai-response.json`, `android/36-result-clothes-fault-no-ai.*`; `실시간 상품 추천` and test product visible, no `AI 추천` |
| Invalid product URL | No crash; link opens only for `http/https` | pass by code audit | `ResultScreen.kt` uses `isSafeProductUrl` and `openProductUrl` with `runCatching`; non-http links disable click behavior |
| Long Naver text | Text is ellipsized without layout overflow | pass | `android/30-result-clothes-top-after-name-fix.summary.txt`, `android/31-result-makeup-top-after-name-fix.summary.txt` show long Naver titles constrained in top viewport |
| Thumbnail failure | Stable placeholder/error drawable, no layout break | pass | no-AI fault uses `https://example.com/*.jpg`; `ProductThumbnail` keeps fixed size with Glide placeholder/error; crash buffer 0 |
| MyPage makeup preview | Stored/generic rows must display as four concrete makeup roles | pass | `android/05-mypage-makeup.*` shows `딥 베리 립`, `차콜 섀도`, `핑크 베이스`, `쿨 로즈 치크`; no `추천 아이템` |
| Makeup backend precision | Naver products must not collapse into one lip-only/generic list | pass | `recommend-makeup-6..10.json`: `naver-shopping`, `items=8`, `lip/eye/base/cheek`, no nail noise, no `추천 아이템` |
| Gemini commerce quota | Gemini 429 should not break Result or paste bad product text | pass | `gemini-commerce-smoke.json` shows `429 RESOURCE_EXHAUSTED`; `recommend-makeup-11..13.json` use concise `local-fallback`; Android `11-result-makeup-top-copy-fixed.*` |

## Build and Static Checklist

| Check | Command / Method | Status | Evidence |
| --- | --- | --- | --- |
| Android unit/build/install | `cmd.exe /C gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:installDebug --console=plain` | pass | `static/gradle-build-install-after-generic-name-fix.txt` |
| Backend syntax | `/mnt/c/nvm4w/nodejs/node.exe --check backend-proxy/src/server.js` | pass | `static/backend-node-check.txt` |
| Kotlin/docs whitespace | `git diff --check` | partial | Full repo check reports unrelated pre-existing `LICENSE` trailing whitespace; scoped check for touched files passed in `static/git-diff-check-scoped-final-3.txt`; new docs trailing whitespace scan is 0 lines |
| Banned user strings | scan Android UI/docs for internal demo/fallback terms | pass | `scans/banned-user-visible-refined-final-source.txt` is 0 lines |
| Secret leak | scan tracked docs/artifacts excluding local secret files | pass | `scans/secret-pattern-refined-final.txt` is 0 lines; secret presence recorded as present/blank only |
| Stale docs | old `컬러 가이드 -> AI 추천` order and removed map operating-status wording absent | pass | stale UI copy scan is 0 lines after docs update |

## Android E2E Checklist

All taps must be derived from UI tree bounds where possible.

| Flow | Expected | Status | Evidence |
| --- | --- | --- | --- |
| Login -> 2FA/Main | App reaches demo or guest flow without crash | pass | `android/26-after-reinstall-launch.*`, `27-login-sheet.*`, `28-after-guest.*`, `29-main-after-reinstall-2fa.*` |
| Diagnosis sample -> Result | Analysis reaches Result without intermediate completion page | pass | Earlier same artifact captures `06-diagnosis.*` through `10-result-clothes-top-backend-on.*`; no intermediate completion page was observed |
| Result tabs/dots | `내 컬러`, `의상`, `메이크업`, `특징` and dot state align | pass | Result summaries consistently show all four tabs; clothing/makeup tab transitions verified |
| Result save/share | Heart state and share chooser/fallback do not crash | pass | `android/38-result-save-overflow.*`, `android/39-result-share-chooser.*`, crash buffers are 0 lines |
| Result map/mypage | Navigation remains usable after UI reorder | code-audited, not repeated in this final pass | Bottom CTA controls remained visible in Result summaries; no navigation code path changed |
| Product link | Product row opens external handler or safely stays in app | code-audited, no crash in render tests | `openProductUrl` accepts only `http/https` and catches URI handler failures |
| Final crash buffer | 0 OliveMe app crash lines | pass | backend on/off/500/timeout/malformed/no-AI crash buffers all 0 lines |

## Backend Split Checklist

| Scenario | Expected | Status | Evidence |
| --- | --- | --- | --- |
| Real backend on | `/health` configured booleans, Naver products, AI summary/picks | pass | `backend/backend-smoke-summary.txt`, `android/30-result-clothes-top-after-name-fix.*`, `31-result-makeup-top-after-name-fix.*` |
| Real backend on, makeup precision | Balanced lip/eye/base/cheek Naver results; backend remains running | pass | `/tmp/oliveme-makeup-fix-final-20260621-001734`, final backend PID `28576` |
| Backend off/reverse removed | Local Result works; commerce hidden | pass | `android/33-result-clothes-backend-off.*`, crash 0 |
| Fault server 500 | Local Result works; commerce hidden | pass | `fault/fault-500-response.txt`, `android/34-result-clothes-fault-500.*`, crash 0 |
| Fault server timeout | Local Result works; commerce hidden | pass | `fault/fault-timeout-curl.txt`, `android/37-result-clothes-fault-timeout.*`, crash 0 |
| Fault server empty/malformed | No broken product card | pass | `fault/fault-empty-malformed-response.json`, `android/35-result-clothes-fault-empty-malformed.*`, crash 0 |
| Fault server no AI summary | Product list renders without AI card | pass | `fault/fault-no-ai-response.json`, `android/36-result-clothes-fault-no-ai.*`, crash 0 |

## Map Refresh and External App Checklist

| Flow | Expected | Status | Evidence |
| --- | --- | --- | --- |
| Header cleanup | Header is not search-looking or clickable input; no search icon/text | pass | `04-map-entry.*`; source scan found no old search component/icon/header strings in map code |
| Region refresh trigger | Pan/zoom does not auto-change list; it shows `이 지역 재검색` | pass | `05-map-after-zoom-out.*` |
| Region refresh action | Tap reloads current map-center stores, max 45 exposed Kakao Local results, and updates markers/list together | pass | `06-map-after-region-refresh.*`; latest draggable sheet QA artifact updates this behavior |
| Kakao Local result cap | App follows `meta.is_end` pagination with `size=15` and displays up to the official single-search exposed limit of 45 stores | pass | `StoreRepository.kt`, official Kakao Local docs, latest QA artifact `/tmp/oliveme-map-sheet-limit-20260621-093640` |
| Map zoom-out hard limit | Map cannot zoom out below 14, preventing overly broad visible areas and broad 20km refresh requests from normal UI operation | pass | `/tmp/oliveme-map-zoom-limit-marker-20260621-100105/android/08-zoom-out-min14-limited.png`, `ui/15-zoom-out-min14-limited-summary.txt` |
| Marker anchor accuracy | Pin tip remains the lat/lng anchor under high zoom; selected label/card stay aligned | pass | `/tmp/oliveme-map-zoom-limit-marker-20260621-100105/android/10-zoom-19-marker-anchor.png`, `ui/17-zoom-19-marker-anchor-summary.txt`; source `iconAnchor: [20, 50]` |
| Map WebView ANR guard | WebView shell prewarm removed and MapActivity attaches WebView after initial focus; skeleton/list remain responsive | pass | `/tmp/oliveme-map-zoom-limit-marker-20260621-100105/ui/13-map-4s-attach-delay-summary.txt`, `ui/14-map-8s-attach-delay-summary.txt`; final ANR scan empty |
| Draggable bottom sheet | Drag handle changes the visible store-list height without changing map coordinates or crashing | pass | `/tmp/oliveme-map-sheet-limit-20260621-093640/android/11-map-expanded-liststate.png`, `12-map-collapsed-liststate.png`; final crash buffer 0 lines |
| Selected card and saved filter labels | Selected store remains the first card after sheet resize; saved filter says `저장 매장 1곳` in both header and sheet title | pass | `/tmp/oliveme-map-sheet-limit-20260621-093640/ui/14-map-expanded-liststate-summary.txt`, `16-map-saved-filter-sheet-summary.txt` |
| Region refresh zoom retention | After refresh, map keeps the current zoom/cluster context instead of snapping back to zoom 16 | pass | `/tmp/oliveme-final-dev-pr-qa-XWNwfuYg/android/22-map-refresh-culling.*` |
| Cluster behavior | Zoom 15 or lower clusters non-selected stores; selected store keeps pin + label | pass | `05-map-after-zoom-out.*`, UI tree cluster labels `2개/3개/5개 매장 묶음` |
| Same-place selected dedupe | One physical place renders once, but selected duplicate is preserved for label/card consistency | pass | Final post-patch reinstall smoke `android/33-final-post-patch-map-entry.*`, `34-final-post-patch-map-cluster.*`; crash 0 |
| Cluster tap | Stays inside the app map and expands/moves to grouped bounds | pass | `08-map-after-cluster-tap.*` |
| Saved filter sync | Header count, list, and WebView markers use the saved-store set | pass after fix | Bug found in `09/10-map-saved-filter*`; fixed and retested in `18-map-saved-filter-after-fix.*` |
| Current-location permission | Permission-less state opens the Android location sheet; denial shows app dialog with Settings path and 부산대 fallback remains usable | pass | `/tmp/oliveme-final-dev-pr-qa-XWNwfuYg/android/26-location-permission-request-after-map-entry.*`, `27-location-denied-fallback.*`; crash 0 |
| Google Maps enabled | `지도 앱 열기` opens place/coordinate result, not direct Directions | pass | `19-google-maps-active-open.*`, crash 0 |
| Google Maps disabled | Browser Google Maps URL opens | pass | `21-browser-fallback-open.*`, crash 0; Maps re-enabled |
| No external handler | Link copied and toast shown; no crash | pass | `23-no-handler-clipboard-toast.*`, crash 0; Maps/Chrome re-enabled |
| Map perf/log | Final logs and performance evidence saved | pass | `final-logcat.txt`, `final-crash-buffer.txt`, `final-gfxinfo.txt`, `final-framestats.txt`, `final-meminfo.txt` |
| Map lightweight patch | Avoid same-payload WebView reload, reduce Leaflet animation/tile work, and render viewport-near markers only | pass with residual WebView jank note | `/tmp/oliveme-final-dev-pr-qa-XWNwfuYg/perf/map-culling-focused-gfxinfo.txt`, `perf/final-post-patch-map-gfxinfo.txt`; attached views reduced `36 -> 26`, render nodes `59.05KB -> 40.19KB`; crash 0 |
| Map static shell first entry | Avoid full HTML reload by loading `web/oliveme_map.html` once and updating stores through JS runtime payload | pass with first-cold WebView/tile caution | `/tmp/oliveme-map-entry-static-shell-20260621-034323/android/35-final-map-1s.png`, `36-final-map-3s.png`, `38-final-map-12s.png`; stable entry runs 2-4 median p95 `200ms`, p99 `300ms`, janky `52.94%`; final crash 0 |
| Loading empty-state guard | During location lookup, never show `근처 매장 0곳` or `근처 매장을 찾지 못했습니다` before a real completed empty result | pass | `/tmp/oliveme-map-webview-fix-20260621-030301/android/23-final-map-5s-after-lastknown-patch.png`, `24-final-map-12s-after-lastknown-patch.png`; Map cycles 1-2 at 5s both show `근처 매장 7곳` |
| Valid domestic last-known fast path | App shows 부산대 last-known stores immediately and only refreshes stale location in the background | pass | `ui/30-final-map-5s-after-lastknown-patch-summary.txt`, `ui/33-final-cycle-1-5s-after-lastknown-patch-summary.txt`, `ui/33-final-cycle-2-5s-after-lastknown-patch-summary.txt` |
| Selected label bottom-sheet guard | At zoom-out, selected pin and label remain visible above the bottom sheet | pass | `android/14-zoom-out-label-fixed-retry.png`, `ui/17-zoom-out-label-fixed-retry-summary.txt` |
| Zoom/pan debounce | Zoom buttons and viewport bridge are throttled; pan does not reload list automatically | pass with residual WebView jank note | `perf/gfxinfo-zoom-stress-label-fixed-retry.txt` p95 `250ms`; `perf/gfxinfo-pan-stress.txt` p95 `57ms`; crash 0 |
| Static shell zoom regression guard | New static shell still keeps selected label, cluster, refresh button, and bottom list stable under zoom/pan | pass with residual stress jank | `/tmp/oliveme-map-entry-static-shell-20260621-034323/android/39-final-zoom-out.png`, `40-final-pan-refresh.png`; rapid zoom stress can still spike on emulator but crash 0 |
| Region refresh selected/card alignment | After refresh, selected result and first visible card align; if previous selected is absent, first result becomes selected | pass | `android/15-after-region-refresh-selected-pinned.png`, `ui/18-after-region-refresh-selected-pinned-summary.txt` |
| MyPage Settings gear | Gear opens Settings; Settings content renders and does not crash | pass | `/tmp/oliveme-final-dev-pr-qa-XWNwfuYg/android/28a-mypage-before-settings.*`, `28-settings-open-final.*`; crash 0 |

## MyPage Layout and Store Source Checklist

| Flow | Expected | Status | Evidence |
| --- | --- | --- | --- |
| Profile chips on small screen | Long personal-color name must not push `이력` chip down or wrap oddly | pass | `/tmp/oliveme-mypage-location-fix-20260621-014414/android/22-final-mypage-report.png`; UI tree bounds show `가을 웜 트루` and `이력 6` on the same row |
| Report hero long type | Long type text must not overlap the report card | pass | `/tmp/oliveme-mypage-location-fix-20260621-014414/android/22-final-mypage-report.png` |
| Demo favorite seed | MyPage saved stores must not be auto-filled by mock/seed favorites | pass | `DemoSeedRepository` no longer seeds favorites and prunes legacy `pnu-*`; Room DB evidence `logs/favorite-stores-db.txt` shows Kakao place id `26313994` |
| Saved store source | Visible saved store should come from saved Kakao/real map result, not `seed/stores.json` | pass | DB row: `올리브영 부산대점`, `부산 금정구 부산대학로 47`, `164m`, Kakao place URL; seed JSON uses `pnu-*` ids and different seed addresses |
| 부산대 emulator location | App-readable location providers should be set to 부산대 before Map QA | pass | `logs/location-final-latest.txt` confirms `network`, `fused`, `gps` at `35.231000,129.084200` |
| MyPage store -> Map | Store card opens MapActivity, current-location Kakao results load, no crash | pass | `android/25-final-map-pnu-loaded.png`, `ui/25-final-map-pnu-loaded-summary.txt`; crash buffer 0 |
| Current-location button | With permission granted, reloads current 부산대 location results and stays stable | pass | `android/26-map-after-location-button.png`, `ui/26-map-after-location-button-summary.txt`; crash buffer 0 |
| External map return | `지도 앱 열기` opens Google Maps result and Back returns to OliveMe MapActivity | pass | `ui/27-map-open-external-summary.txt`, `logs/back-from-maps.txt`; crash buffer 0 |

## Grading Matrix

| Criterion | Score Risk | Current Evidence Target | Status |
| --- | --- | --- | --- |
| Activity 3+ and Intent data | Required, -50 risk if missing | Login, Digit2Fa, Main, Diagnosis, Result, Map, MyPage, Settings and `IntentKeys` | pass by source/QA evidence |
| Coroutine | 20 | `viewModelScope`, `withContext(Dispatchers.IO)`, suspend repository/DAO calls | pass by source audit |
| Download/API manager | 20 | Retrofit/OkHttp services and Glide thumbnails | pass by source and UI evidence |
| Jetpack 3+ | 30 | Room, Compose, ViewModel, ActivityResult, DrawerLayout/ViewPager2/Fragment/RecyclerView evidence | pass by source audit |
| External app integration | 20 | Gallery, camera, share chooser, Google Maps/browser/clipboard/product URL intent | pass by source and map/Result artifact evidence |
| DB | 30 | Room local DB. Local DB counts separately from API score. | pass by source and MyPage/history evidence |
| API 3+ | 60 | Gemini, Kakao Login, Kakao Local, OSM tile/WebView, Google Maps external, backend commerce | pass by source and backend smoke |
| ML model | 50 | Custom MNIST TFLite digit model and guarded unavailable path | pass by source and 2FA E2E |
| Aesthetics | 0-10 | Current Android screenshots, not old HTML 1:1 | pass for Result AI-first screenshots |
| Stability | 0-10, 0 if abnormal runtime termination | Every focused crash buffer must have 0 app crash lines | pass: all focused crash buffers 0 lines |
| Completeness | 0-10 | Core demo routes and fault fallbacks work | pass for implemented/focused final flows |
| PPT/presentation | -50 risk if major required content missing | team/roles, overview, wireframe/functions, features, stability, conclusion | doc-only reminder; not app-runtime code |

## Final Exit Criteria

- [x] Android build/unit/install pass.
- [x] Backend syntax pass.
- [x] Result AI-first UI verified on clothing and makeup tabs.
- [x] Backend on/off/500/timeout/empty/no-AI cases verified.
- [x] Map status header, explicit region refresh, max-45 single-search cap, zoom-out hard limit, marker anchor, draggable bottom sheet, clustering, saved filter sync, and external map fallback verified.
- [x] Final map lightweight pass verified WebView reload guard, zoom retention, viewport marker culling, screenshots, and crash-free behavior.
- [x] Map WebView caution pass verified loading empty-state guard, selected-label visibility, zoom/pan debounce, selected-card alignment, screenshots, and crash-free behavior.
- [x] MyPage chip/report layout, saved-store source, 부산대 emulator location, current-location reload, and external map return verified with screenshots/UI trees/logs.
- [x] Android E2E captures screenshot/UI XML/UI summary/logcat/crash buffer.
- [x] Performance `gfxinfo`, `framestats`, and `meminfo` saved.
- [x] Final focused crash buffers have 0 OliveMe app crash lines.
- [x] `docs/TRUTH_SPEC.md`, `docs/QA_EVIDENCE.md`, `docs/NAVER_BACKEND_E2E_QA.md`, and this checklist reflect final facts.

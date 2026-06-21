# OliveMe Map Cluster + Naver Backend E2E QA

Date: 2026-06-20
Branch: `dev`
Artifact dir: `/tmp/oliveme-cluster-naver-e2e-20260620-211921`

## 2026-06-21 Makeup Recommendation Precision Retest

Artifact dir: `/tmp/oliveme-makeup-fix-final-20260621-001734`

- Backend process remained running after QA. Final Windows Node PID: `28576`.
- `/health`: `ok=true`, `geminiConfigured=true`, `naverShoppingConfigured=true`, `fcmConfigured=false`.
- Backend product flow:
  - `backend-proxy/src/server.js` now merges makeup queries across lip, eye shadow, makeup base/cushion/foundation/concealer, and cheek.
  - It filters non-renderable products and excludes obvious nail/base-coat noise before Android sees the item list.
  - It stops querying once enough products and lip/eye/base/cheek coverage are present, reducing transient upstream failures.
- Repeated smoke:
  - `recommend-makeup-6.json` through `recommend-makeup-10.json`: 5/5 returned `source=naver-shopping`, `items=8`, `추천 아이템=false`, nail noise `false`, and families included `lip`, `eye`, `base`, `cheek`.
  - `recommend-makeup-11.json` through `recommend-makeup-13.json`: fallback copy stayed concise and category-based.
  - Gemini commerce smoke returned `429 RESOURCE_EXHAUSTED`; therefore `aiSummary.source=local-fallback` was expected in this run. Naver products still rendered.
- Android Result:
  - `android/11-result-makeup-top-copy-fixed.*`: `메이크업 추천` -> `기본 컬러 요약` -> `AI 추천`, with fallback text that does not paste a long product title.
  - `android/14-result-makeup-product-diversity.*`: visible product rows include lip, eye, and base candidates with real thumbnail `ImageView ... 썸네일` nodes.
  - `android/15-result-makeup-backend-unreachable.*`: with only `adb reverse` removed, backend process still on, Android showed `로컬 컬러 가이드` and hid commerce.
- Crash buffer: `android/16-crash-buffer.txt` is 0 lines.

## 2026-06-20 Map Refresh/Directions Follow-Up

Artifact dir: `/tmp/oliveme-map-refresh-final-qa-20260620-231448`

- Backend/Naver endpoint or schema changed: no.
- Android commerce path remains `ResultViewModel -> CommerceRepository -> BackendApiService -> ResultScreen`.
- Map changes are client-side only:
  - The former search-looking header is now a non-clickable status header `주변 뷰티 매장`.
  - Map pan/zoom only shows `이 지역 재검색`; the bottom-sheet list is not automatically changed by zoom.
  - Pressing `이 지역 재검색` reloads Kakao Local results around the current map center, `size=15` pages until `meta.is_end`, max 45 stores exposed by the single keyword search.
  - Zoom 15 or lower clusters non-selected stores while the selected store keeps an individual pin and label.
  - `저장` filter now synchronizes header count, bottom list, and WebView markers.
  - `지도 앱 열기` opens Google Maps place/coordinate result first, then browser Google Maps URL, then clipboard fallback.
- Focused QA:
  - `05-map-after-zoom-out.*`: cluster bubbles plus selected label and `이 지역 재검색`.
  - `06-map-after-region-refresh.*`: explicit region refresh result, count within max 30.
  - `18-map-saved-filter-after-fix.*`: saved filter marker/list/count sync.
  - `19-google-maps-active-open.*`: Google Maps enabled path, crash 0.
  - `21-browser-fallback-open.*`: Google Maps disabled -> Chrome fallback, crash 0.
  - `23-no-handler-clipboard-toast.*`: Google Maps + Chrome disabled -> link copied toast, crash 0.

## 2026-06-21 Map WebView Caution Follow-Up

Artifact dir: `/tmp/oliveme-map-webview-fix-20260621-030301`

- Backend/Naver endpoint or schema changed: no.
- Backend stayed running; `/health` recorded configured booleans without printing secret values.
- Map changes are still client-side only:
  - Valid domestic last-known location is rendered immediately to avoid transient empty-state, then stale location can be refreshed in the background.
  - Loading state hides premature `0곳`/empty failure copy.
  - Selected store is pinned to the first bottom-sheet card when it remains in the visible result set.
  - Selected label is kept above the bottom sheet after zoom-out.
  - Zoom button taps and viewport bridge reports are debounced.
- Focused QA:
  - `23-final-map-5s-after-lastknown-patch.*`, `24-final-map-12s-after-lastknown-patch.*`: no transient `0곳`; current-location 부산대 results visible.
  - `14-zoom-out-label-fixed-retry.*`: selected label plus cluster bubble visible after zoom-out.
  - `18-pan-stress-refresh-only.*`: pan shows `이 지역 재검색` while list stays unchanged.
  - `15-after-region-refresh-selected-pinned.*`: explicit refresh reloads up to 30 map-center stores and keeps selected/card alignment.
  - `19-saved-filter-after-webview-fix.*`: saved filter count/list/WebView marker sync.
  - `20-external-map-opened.*`, `22-after-external-map-back-fixed-loop.*`: Google Maps open/back path, crash 0.

## Code Path

- Map: `MapScreen` renders a WebView + local Leaflet map. Store pins are anchored by the pin tip to each store `lat/lng`; selected store keeps its individual pin and label at every zoom. At zoom 15 or lower, nearby non-selected stores are grouped into custom cluster bubbles without changing the bottom-sheet list.
- Backend proxy: `backend-proxy/src/server.js`
  - `GET /health`
  - `GET /v1/products/search`
  - `POST /v1/products/recommendations`
- Android commerce path: `ResultViewModel -> CommerceRepository -> BackendApiService -> ResultScreen`.

## Secret-Safe Setup

- `backend-proxy/.env`: `PORT`, `GEMINI_API_KEY`, `GEMINI_MODEL`, `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET` were present. Values were not printed.
- `android/local.properties`: `sdk.dir`, `GEMINI_API_KEY`, `KAKAO_NATIVE_APP_KEY`, `KAKAO_REST_API_KEY` were present. Values were not printed.

## Backend Smoke

- `/health`: `ok=true`, `geminiConfigured=true`, `naverShoppingConfigured=true`, `fcmConfigured=false`.
- Naver search:
  - Clothes: `source=naver-shopping`, `items=3`, renderable title/link/image `3`.
  - Makeup: `source=naver-shopping`, `items=3`, renderable title/link/image `3`.
- Recommendations:
  - Clothes: `source=naver-shopping`, `items=5`, renderable `5`, `aiSummary.source=gemini`, picks `3`.
  - Makeup: `source=naver-shopping`, `items=5`, renderable `5`, `aiSummary.source=gemini`, picks `2`.

## Android E2E

- Build: `:app:testDebugUnitTest :app:assembleDebug` passed.
- Map:
  - Login -> 2FA -> Main -> Map completed.
  - Initial zoom showed individual anchored pins, selected label, `전체/저장` filters, and unchanged `근처 매장 14곳` list.
  - Zoom-out showed custom cluster bubbles while keeping selected store as individual pin + label.
  - A first QA pass found a cluster/selected-pin overlap that could open Google Maps; fixed by shifting clusters away from the selected pin and setting z-index offsets.
  - Retest: cluster tap stayed inside the app map and zoomed/moved to the grouped area. List count stayed unchanged.
- Backend on with `adb reverse tcp:8787 tcp:8787`:
  - Android logcat: `commerce recommendation loaded category=의상 products=8 ai=true`.
  - Android logcat: `commerce recommendation loaded category=메이크업 products=8 ai=true`.
  - Result `의상`: `AI 추천`, `AI가 고른 상품`, `실시간 상품 추천`, Naver product thumbnails and prices displayed.
  - Result `메이크업`: `AI 추천`, `AI가 고른 상품`, `실시간 상품 추천`, Naver product thumbnails and prices displayed.
- Backend off with `adb reverse --remove tcp:8787`:
  - Result kept local `기본 컬러 요약` and `최종 컬러 분석`.
  - `AI 추천`, `AI가 고른 상품`, `실시간 상품 추천`, and product thumbnails were not shown.
  - logcat showed commerce skip by `ConnectException`, no crash.

## Final Evidence

- Key screenshots:
  - `09-map-zoomed-out-fixed.png`
  - `10-map-cluster-opened-fixed.png`
  - `20-result-clothes-more-products.png`
  - `25-result-makeup-realtime.png`
  - `27-result-backend-off-clothes.png`
- Backend JSON:
  - `backend-health.json`
  - `backend-search-clothes.json`
  - `backend-search-makeup.json`
  - `backend-recommend-clothes.json`
  - `backend-recommend-makeup.json`
- Final crash buffer: `0 bytes`.

## 2026-06-20 AI-First Result Retest

Artifact dir: `/tmp/oliveme-ai-first-final-qa-20260620-220021`

### Backend Contract

- Endpoint/schema changed: no.
- Android path remains `ResultViewModel -> CommerceRepository -> BackendApiService -> ResultScreen`.
- Android filters backend products before rendering. `title`, `linkUrl`, and `imageUrl` must all be nonblank, and product links are opened only when the URL starts with `http://` or `https://`.
- `source != naver-shopping`, HTTP errors, timeout, empty/malformed products, or missing backend all degrade to local Result analysis with no commerce section.
- Missing `aiSummary` only hides the AI card; valid products still render under `실시간 상품 추천`.

### Backend On

- `/health`: Gemini and Naver Shopping were configured. Secret values were not printed.
- `/v1/products/search`: clothing and makeup returned `source=naver-shopping` with renderable title/link/image products.
- `/v1/products/recommendations`: clothing and makeup returned `source=naver-shopping`, renderable products, AI summary, and product picks.
- Android evidence:
  - Clothing top: `android/30-result-clothes-top-after-name-fix.*`
  - Makeup top: `android/31-result-makeup-top-after-name-fix.*`
  - Product/final analysis order: `android/18-result-clothes-products-backend-on.*`, `20-result-clothes-final-analysis-backend-on.*`, `24-result-clothes-final-analysis-backend-on.*`
  - Crash buffer: `logs/crash-backend-on-after-name-fix.txt` is 0 lines.

### Backend Off and Faults

| Case | Expected Result | Evidence | Crash |
| --- | --- | --- | --- |
| Reverse removed/backend unavailable | `기본 컬러 요약` + `최종 컬러 분석`; commerce hidden | `android/33-result-clothes-backend-off.*` | 0 lines |
| HTTP 500 | Local analysis remains, commerce hidden | `fault/fault-500-response.txt`, `android/34-result-clothes-fault-500.*` | 0 lines |
| Timeout | Local analysis remains after read timeout, commerce hidden | `fault/fault-timeout-curl.txt`, `android/37-result-clothes-fault-timeout.*` | 0 lines |
| Empty/malformed products | Blank product card is not rendered | `fault/fault-empty-malformed-response.json`, `android/35-result-clothes-fault-empty-malformed.*` | 0 lines |
| Missing `aiSummary` | Products render under `실시간 상품 추천`, `AI 추천` hidden | `fault/fault-no-ai-response.json`, `android/36-result-clothes-fault-no-ai.*` | 0 lines |

### UI Order

The final clothing and makeup tabs now use this order:

1. Title: `의상 추천` or `메이크업 추천`
2. Compact `기본 컬러 요약`
3. Optional `AI 추천` and `AI가 고른 상품`
4. Optional `실시간 상품 추천`
5. Local `최종 컬러 분석`

# OliveMe Fault Injection QA 요약

실행일: 2026-06-20
브랜치: `dev`
증거 폴더: `/tmp/oliveme-fault-injection-20260620-185027`

## 최종 판정

- 핵심 fault injection 결과: 통과.
- 최종 정상 빌드: `:app:testDebugUnitTest :app:assembleDebug :app:installDebug` 통과.
- 최종 crash buffer: `0 bytes`.
- 테스트 후 `android/local.properties`, assets, Google Maps/Chrome, DB, 정상 APK 설치 상태를 복원했다.

## 실제 주입한 장애

| 장애 | 결과 |
| --- | --- |
| Kakao native key blank | 최초 1회 실제 크래시 재현. `LoginRepository`에서 blank key 사전 차단과 SDK 호출 `runCatching`/`resumeOnce` 방어 추가 후 crash 0, 사용자 오류 문구 표시. |
| Google Maps 앱 비활성화 | Chrome fallback으로 열림, crash 0. |
| Google Maps + Chrome 모두 비활성화 | 앱이 Map 화면에 남고 crash 0. no-handler 경로 안전. |
| backend URL 연결 실패/500 서버 | Result commerce 영역 숨김, 로컬 `기본 컬러 요약`과 `최종 컬러 분석` 유지, crash 0. |
| TFLite `digit_mnist.tflite` 누락 | 2FA에서 실패 메시지와 재시도 버튼 유지, crash 0. |
| `seed/stores.json` 누락 + Kakao REST blank | 하드코딩 샘플 매장 5개 fallback 표시, crash 0. |
| `seed/diagnosis_policy.json` 누락 + Gemini key blank | built-in policy fallback으로 Result 도착, crash 0. |
| Room DB 파일 손상 | Login 및 guest 시작/2FA 진입까지 crash 0. 테스트 후 DB 백업 복원. |

## 2026-06-20 Result Commerce Fault Injection

증거 폴더: `/tmp/oliveme-ai-first-final-qa-20260620-220021`

| 장애 | 결과 |
| --- | --- |
| backend reverse 제거/연결 실패 | `기본 컬러 요약`과 `최종 컬러 분석`만 유지하고 `AI 추천`/`실시간 상품 추천` 숨김. crash 0. |
| backend HTTP 500 | commerce 숨김, 로컬 분석 유지. crash 0. |
| backend read timeout | 30초 read timeout 이후 commerce 숨김, 로컬 분석 유지. crash 0. |
| `source=naver-shopping`이지만 title/link/image 빈 상품 | repository/UI 필터로 빈 상품 카드 미노출. crash 0. |
| `aiSummary=null` + 유효 상품 | `AI 추천` 카드만 숨김, `실시간 상품 추천` 상품은 정상 노출. crash 0. |
| 긴 Naver 제목/AI 요약 | `maxLines`/ellipsis로 레이아웃 범위 안에 제한. crash 0. |
| 이미지 실패 가능 URL | Glide placeholder/error drawable과 고정 썸네일 크기로 레이아웃 유지. crash 0. |

추가 수정: `ResultScreen`의 상품 탭 순서를 `기본 컬러 요약 -> AI 추천/AI가 고른 상품 -> 실시간 상품 추천 -> 최종 컬러 분석`으로 조정했고, `추천 · 추천 · 추천`처럼 일반적인 메이크업 요약명이 보이는 경우 `립 추천 · 아이 추천 · 베이스 추천`처럼 카테고리 fallback을 사용하도록 방어했다.

## 2026-06-21 메이크업 추천/commerce quota 재감사

증거 폴더: `/tmp/oliveme-makeup-fix-final-20260621-001734`

| 장애/리스크 | 결과 |
| --- | --- |
| Gemini commerce summary 429 | `gemini-commerce-smoke.json`에서 `RESOURCE_EXHAUSTED` 확인. backend local fallback이 짧은 카테고리 중심 요약을 반환하고 Android `AI 추천` 카드는 깨지지 않음. |
| 메이크업 상품이 립으로만 몰림 | backend가 립/아이/베이스/치크 검색을 병합하고 coverage 확보 후 중단. 5회 반복 smoke에서 네 카테고리 모두 포함. |
| 네일/베이스코트가 베이스 메이크업으로 섞임 | backend filter로 명시 제외. 5회 반복 smoke에서 nail noise 0. |
| 저장된 generic makeup row | MyPage가 `추천 아이템`/blank/default color를 카테고리별 로컬 컬러 가이드로 표시. `android/05-mypage-makeup.*`에서 네 칸 모두 구체 라벨과 다른 색으로 확인. |
| Android에서 backend 연결 불가 | backend 프로세스는 유지한 상태에서 `adb reverse`만 제거. Result `메이크업`은 `로컬 컬러 가이드`만 표시하고 commerce 숨김, crash 0. |

최종 backend 상태: QA 종료 시 Windows Node PID `28576`이 port `8787`에서 계속 실행 중이었다.

## 수정된 버그

- `QA-KAKAO-BLANK-001`: Kakao SDK 미초기화 상태에서 `UserApiClient.instance` 접근 시 `UninitializedPropertyAccessException`으로 앱이 종료되던 문제 수정.
- 기존 `QA-KAKAO-CALLBACK-001`: Kakao SDK 중복/늦은 콜백의 coroutine 중복 resume 가능성도 함께 방어.

## 추가 지도 감사

- 지도 마커: OSM 기본 타일의 빨간 십자 POI가 매장 마커처럼 보이지 않도록 light tile로 교체하고, 앱 마커는 핀 끝이 좌표에 닿는 커스텀 핑크 핀으로 고정했다. 같은 주소/장소 중복은 표시 전에 제거한다.
- 위치 버튼: 권한 설명 다이얼로그와 시스템 권한 요청, 영구 거부 시 앱 설정 이동을 연결했다.
- 영업정보: [Kakao Local 키워드 장소 검색](https://developers.kakao.com/docs/latest/ko/local/dev-guide#search-by-keyword) 응답과 현재 seed 데이터에는 검증 가능한 영업시간/운영 상태가 없다. 별도 [Google Places Details](https://developers.google.com/maps/documentation/places/web-service/place-details)류 API를 붙이지 않는 한 정확 표시가 불가능하므로 지도 카드에서 운영 상태 문구를 제거했다.
- 추가 증거: `/tmp/oliveme-map-marker-polish-20260620-200440-nqnw`, `/tmp/oliveme-map-hours-removal-20260620-205925`

## 2026-06-20 지도 앱 열기/재검색 재감사

증거 폴더: `/tmp/oliveme-map-refresh-final-qa-20260620-231448`

| 항목 | 결과 |
| --- | --- |
| 검색처럼 보이는 상단 UI | `주변 뷰티 매장` 상태 헤더로 표시, 돋보기/검색 입력 없음. |
| 지도 zoom-out | `이 지역 재검색` 표시, 선택 매장 라벨 유지, 주변 매장 cluster 표시. |
| `이 지역 재검색` | 현재 지도 중심 기준으로 재조회, `지도 영역 기준 추천 매장` 표시, Kakao Local `meta.is_end` pagination과 공식 노출 한계 기준 최대 45개 제한 유지. |
| 저장 필터 | 초기 QA에서 지도 마커/목록 불일치 발견 후 수정. 재검증에서 count/list/WebView marker 모두 저장 필터 기준으로 동기화. |
| Google Maps 활성 | `지도 앱 열기`가 장소/좌표 결과를 열고 Directions 화면으로 직접 진입하지 않음. crash 0. |
| Google Maps 비활성 | Chrome Google Maps web URL fallback. crash 0. |
| Google Maps + Chrome 비활성 | 앱이 Map 화면에 남고 링크 복사 toast 표시. crash 0. |

## 2026-06-21 MyPage 저장 매장/위치 재감사

증거 폴더: `/tmp/oliveme-mypage-location-fix-20260621-014414`

| 항목 | 결과 |
| --- | --- |
| 긴 퍼스널컬러 이름 | `가을 웜 트루 (Autumn Warm)` 같은 긴 타입은 화면 표시에서 한국어 compact 이름으로 줄이고, profile chip과 report hero에 `maxLines`/ellipsis를 적용해 `이력` 버튼 밀림과 title overlap을 막았다. |
| Demo seed favorite | 예전 demo seed가 `pnu-*` 매장을 즐겨찾기에 자동 삽입해 mock처럼 보일 수 있었다. 현재는 즐겨찾기를 자동 seed하지 않고 legacy `pnu-*` favorite row를 정리한다. |
| 저장 매장 출처 | Room DB pull 결과 현재 MyPage 저장 매장은 Kakao place id `26313994`, `올리브영 부산대점`, `부산 금정구 부산대학로 47`, Kakao place URL을 가진 user-saved row였다. seed JSON의 `pnu-*` mock row가 아니다. |
| 부산대 위치 고정 | `adb emu geo fix`와 Android `gps/network` test provider를 함께 설정해 `fused`, `gps`, `network` last-known이 `35.231000,129.084200`으로 잡히는 것을 확인했다. |
| 현재 위치 버튼 | 위치 권한이 허용된 상태에서 버튼 tap 후에도 `현재 위치 기준 추천 매장`, 부산대 주변 7곳, 첫 매장 164m가 유지됐다. |
| 외부 지도 | `지도 앱 열기`는 Google Maps place/coordinate result를 열고, Back으로 OliveMe MapActivity에 복귀했다. crash buffer는 0 lines. |

## 2026-06-21 지도 WebView 주의점 재감사

증거 폴더: `/tmp/oliveme-map-webview-fix-20260621-030301`

| 항목 | 결과 |
| --- | --- |
| 로딩 중 empty-state | 최신 APK에서 지도 첫 진입 5초/12초와 왕복 2회 5초 모두 `근처 매장 7곳`을 표시했다. `근처 매장 0곳`, `근처 매장을 찾지 못했습니다`는 재현되지 않았다. |
| last-known 위치 방어 | 국내 last-known 좌표가 있으면 즉시 매장을 표시하고, 오래된 경우에만 fresh 위치 갱신을 병행하도록 수정했다. |
| 선택 라벨 가림 | zoom-out 5회 후 선택 핀과 `올리브영 부산대점` 라벨이 bottom sheet 위에 유지됐다. |
| zoom/pan jank | zoom p95는 기존 450ms에서 250ms, pan p95는 기존 85ms에서 57ms로 낮아졌다. WebView/OSM tile 특성상 entry aggregate jank는 남아 성능 주의점으로 기록한다. |
| 재검색 선택/카드 정렬 | `이 지역 재검색` 후 selected store와 첫 카드가 같은 결과를 가리키도록 정렬됐다. |
| 저장 필터 | count/list/WebView marker가 모두 저장 매장 1개 기준으로 동기화됐다. |
| 외부 지도 복귀 | Google Maps place/coordinate result가 열리고 Back으로 OliveMe MapActivity에 복귀했다. |
| 안정성 | 최종 crash buffers는 0 lines. idle/trim 후 `Activities=2`, `WebViews=1`로 복귀했다. |

## 남은 한계

- 실제 Kakao 계정 서버의 사용자 취소 화면은 외부 인증 상태 의존성이 있어 blank-key/SDK failure 경로로 대체 검증했다.
- 대용량 갤러리 이미지 OOM은 picker에 실제 giant image를 주입하지 못해 코드 감사로 확인했다. 현재 `ImageBytesLoader`는 `runCatching`으로 decode/compress 실패를 null fallback에 흡수하지만, 초대형 이미지는 메모리 사용량 개선 여지가 있다.
- Perfetto 장기 추적은 수행하지 않았다. 지도 entry aggregate `gfxinfo`는 WebView/tile 전환 구간에서 여전히 jank가 커서 완전 smooth 근거로 쓰지 않고, zoom/pan focused `gfxinfo`만 before/after 비교에 사용했다.

## 2026-06-21 지도 정적 Shell 첫 진입 재감사

증거 폴더: `/tmp/oliveme-map-entry-static-shell-20260621-034323`

| 항목 | 결과 |
| --- | --- |
| full HTML reload 제거 | `MapScreen`에서 `loadDataWithBaseURL`/inline `webMapHtml` 경로를 제거하고, `web/oliveme_map.html` 정적 shell + `window.OliveMeMapRuntime.setData(...)` payload 갱신으로 바꿨다. |
| 첫 진입 blank 방지 | 1초 캡처에서 `주변 뷰티 매장`, `근처 매장 7곳`, `올리브영 부산대점`, `지도 앱 열기`가 표시됐다. |
| WebView ready | 3초/12초 UI tree에서 `resource-id="map"`, 매장 marker accessibility label, selected label, zoom control이 표시됐다. |
| entry jank | 이전 baseline p95 `400ms`, p99 `4950ms` 대비 stable runs 2-4 median p95 `200ms`, p99 `300ms`, janky `52.94%`로 개선됐다. 첫 cold/tile outlier는 run1 p95 `350ms`, p99 `950ms`로 남아 완전 smooth가 아니라 개선 완료로 판정한다. |
| zoom/pan | zoom-out/pan 후 selected label, cluster, `이 지역 재검색`, bottom list가 유지됐다. rapid zoom stress는 emulator WebView/tile 특성상 여전히 spike 가능성이 있어 잔여 주의점으로 유지한다. |
| 안정성 | 최종 crash buffer는 0 lines. trim 후 `Activities=2`, `WebViews=1`, `TOTAL PSS 196469KB`로 확인됐다. |

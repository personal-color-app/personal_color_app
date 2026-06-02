# OliveMe 퍼스널 컬러 진단법

이 문서는 OliveMe 앱의 진단 프롬프트, fallback 템플릿, 결과 화면, 추천 데이터가 따르는 기준 문서다. 퍼스널 컬러는 스타일링을 돕는 참고 정보이며, 의료적 판단이나 생체 신원 추론이 아니다. 앱은 얼굴 원본 이미지를 저장하지 않고, 분석에 필요한 압축 이미지와 결과 데이터만 임시 처리한다.

## 1. 입력과 안전 원칙

- 입력 이미지는 얼굴 또는 상반신이 보이는 사진이어야 한다.
- 앱은 정면성, 얼굴 크기, 밝기, 대비, 초점, 해상도, 다중 얼굴 여부를 먼저 평가한다.
- 품질이 부족해도 앱은 종료하지 않고, 사용자에게 다시 촬영할 수 있는 안내와 안전한 템플릿 결과를 제공한다.
- Gemini 프롬프트는 보이는 색상 단서만 분석한다. 나이, 인종, 건강, 질병, 신원, 민감 속성은 추론하지 않는다.
- Gemini inline image 입력은 공식 문서 기준 전체 요청 20MB 미만으로 유지한다.

## 2. 색채 기준

OliveMe는 계절형 퍼스널 컬러를 다음 축으로 정리한다.

| 축 | 값 | 사용 방식 |
| --- | --- | --- |
| `temperature` | `warm`, `cool`, `neutral` | 피부·머리·눈 주변 색의 따뜻함/차가움 |
| `value` | `light`, `medium`, `deep` | 얼굴에 어울리는 명도 |
| `chroma` | `soft`, `clear`, `muted`, `bright` | 채도와 선명도 |
| `contrast` | `low`, `medium`, `high` | 얼굴·머리·눈 사이 대비감 |
| `season` | `spring`, `summer`, `autumn`, `winter` | 사용자에게 보여줄 대표 계절 |
| `subtype` | 12타입 | 추천 팔레트·의상·메이크업 템플릿 선택 |

## 3. 12타입 분류

| subtype | 한국어명 | 핵심 단서 | 추천 방향 |
| --- | --- | --- | --- |
| `spring-light` | 봄 라이트 | warm, light, clear | 크림, 피치, 라이트 코랄 |
| `spring-bright` | 봄 브라이트 | warm, medium, bright | 선명 코랄, 맑은 옐로, 애플 그린 |
| `spring-warm` | 봄 웜 | warm, medium, clear | 피치, 허니, 크림 아이보리 |
| `summer-light` | 여름 라이트 | cool, light, soft | 파우더 블루, 라일락, 소프트 핑크 |
| `summer-soft` | 여름 소프트 | cool, medium, soft | 더스티 로즈, 모브, 스모키 블루 |
| `summer-cool` | 여름 쿨 | cool, medium, soft | 라벤더, 로즈, 블루 그레이 |
| `autumn-soft` | 가을 소프트 | warm, medium, muted | 세이지, 토프, 소프트 카멜 |
| `autumn-warm` | 가을 웜 | warm, medium, muted | 카멜, 올리브, 테라코타 |
| `autumn-deep` | 가을 딥 | warm, deep, muted | 초콜릿, 딥 올리브, 브릭 |
| `winter-bright` | 겨울 브라이트 | cool, medium, bright | 푸시아, 코발트, 클리어 레드 |
| `winter-cool` | 겨울 쿨 | cool, medium, clear | 플럼, 아이스 핑크, 네이비 |
| `winter-deep` | 겨울 딥 | cool, deep, clear | 와인, 버건디, 블랙 네이비 |

## 4. 사진 품질 판정

`PhotoQuality`는 다음 값을 계산한다.

| 필드 | 기준 | 화면 문구 |
| --- | --- | --- |
| `faceVisible` | 얼굴 1개 이상 감지 또는 중앙 얼굴 후보 확인 | 얼굴 영역 확인 |
| `faceCoverage` | 얼굴 bbox가 이미지의 약 12-65% | 얼굴이 너무 작거나 큼 |
| `brightness` | 평균 밝기 | 조명을 조금 더 밝게 |
| `contrast` | 밝기 표준편차 | 대비가 낮아 색 구분이 어려움 |
| `blurScore` | 인접 픽셀 변화량 | 초점을 확인해주세요 |
| `resolution` | 최소 640px 권장 | 더 큰 사진 권장 |
| `multipleFaces` | 2명 이상 | 한 명만 보이는 사진 권장 |

품질이 낮아도 분석 버튼은 비활성화하지 않는다. 대신 품질 경고를 Gemini 프롬프트와 결과 설명에 함께 넘긴다.

## 5. Gemini 응답 스키마

Gemini는 JSON only로 다음 필드를 반환해야 한다.

```json
{
  "schemaVersion": "oliveme.personal_color.v2",
  "toneNameKo": "여름 쿨톤",
  "season": "summer",
  "subtype": "summer-cool",
  "temperature": "cool",
  "value": "medium",
  "chroma": "soft",
  "contrast": "medium",
  "confidence": 82,
  "quality": {
    "label": "촬영 가능",
    "warnings": ["조명에 따라 결과가 달라질 수 있습니다"]
  },
  "sourceEvidence": [
    "사진의 전체 색감은 차갑고 부드러운 계열에 가깝습니다"
  ],
  "summary": "부드러운 라벤더와 로즈 계열이 안정적인 타입입니다.",
  "signature": "라벤더, 더스티 로즈, 파우더 블루를 우선 추천합니다.",
  "palette": [{ "hex": "#C9B8E8", "name": "라벤더", "role": "best" }],
  "avoidColors": [{ "hex": "#D9A05B", "name": "머스터드", "role": "avoid" }],
  "outfit": [{ "category": "상의", "title": "라벤더 니트", "subtitle": "부드러운 쿨톤 포인트", "colorHex": "#C9B8E8" }],
  "makeup": {
    "립": [{ "category": "립", "title": "소프트 로즈 립", "subtitle": "차분한 장미빛", "colorHex": "#D7A7B5" }]
  },
  "features": ["차가운 온도감", "부드러운 대비"],
  "productKeywords": ["여름쿨톤 립", "라벤더 니트", "쿨톤 블러셔"],
  "qualityWarnings": ["필터가 강하면 결과가 달라질 수 있습니다"]
}
```

파싱 실패, 필드 누락, 네트워크 실패, quota 실패 시 앱은 bundled `assets/seed/diagnosis_policy.json`에서 subtype 템플릿을 골라 결과를 생성한다.

## 6. 추천 데이터

- 의상/메이크업/특징 데이터는 12타입 템플릿을 기본값으로 사용한다.
- 상품 검색은 프로덕션에서 backend proxy가 담당한다.
- Naver Shopping Search API는 공식 검색 API이며, 문서상 하루 호출 한도 25,000회를 가진다.
- Coupang Partners는 접근 권한이 필요하므로 credential-ready adapter와 seed fallback만 준비한다.
- Android 앱은 API 실패나 인터넷 부재를 사용자에게 오류처럼 보이지 않게 처리하고, curated JSON 템플릿을 표시한다.

## 7. 참고 출처

- Gemini image understanding: https://ai.google.dev/gemini-api/docs/image-understanding
- Gemini API key security: https://ai.google.dev/gemini-api/docs/api-key
- Kakao Local API: https://developers.kakao.com/docs/latest/ko/local/dev-guide
- Naver Shopping Search API: https://developers.naver.com/docs/serviceapi/search/shopping/shopping.md
- ML Kit Face Detection Android: https://developers.google.com/ml-kit/vision/face-detection/android
- CameraX Android: https://developer.android.com/media/camera/camerax
- CIELAB color space reference: https://www.datacolor.com/business-solutions/blog/cie-lab-color-space/

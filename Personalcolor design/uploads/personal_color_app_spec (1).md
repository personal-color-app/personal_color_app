# 퍼스널컬러 기반 뷰티 추천 앱 - 개발 명세서

## 📱 프로젝트 개요

**앱 이름**: PersonalColor (가칭)
**플랫폼**: Android (Kotlin)
**개발 환경**: Android Studio
**최소 SDK**: API 26 (Android 8.0)
**타겟 SDK**: API 34
**언어**: Kotlin
**아키텍처**: MVVM 권장 (선택사항)

### 핵심 컨셉
사용자가 자신의 얼굴 사진을 업로드하면 LLM API가 퍼스널 컬러를 분석하고, 어울리는 옷·화장품 컬러 추천과 함께 근처 올리브영 위치를 지도로 보여주는 뷰티 추천 앱.

---

## 🛠 기술 스택 (채점 기준 대응표)

| 채점 항목 | 적용 기술 | 예상 점수 |
|----------|----------|----------|
| Activity 3개 이상 + Intent | 5개 Activity, Intent 양방향 전달 | 필수 |
| Coroutine | suspend 함수 + viewModelScope/lifecycleScope | **20점** |
| 다운로드 매니저 | Retrofit + Glide | **20점** |
| Jetpack 3개 이상 | RecyclerView + Fragment + ViewPager2 + DrawerLayout | **30점** |
| 외부 APP 연동 | 갤러리/카메라 (ACTION_PICK, ACTION_IMAGE_CAPTURE) | **20점** |
| API 3개 | OpenAI(LLM) + 카카오맵 + 카카오 로그인 | **60점** |
| DB | Room (진단 이력, 추천 색상, 즐겨찾기 매장) | **30점** |
| 심미성 | Material 3 + 파스텔 톤 디자인 | 0~10점 |
| 안정성 | try-catch, null safety 철저히 | 0~10점 |
| 완성도 | 핵심 기능 모두 동작 + 예외 처리 | 0~10점 |

> 📌 **DB와 API 중복 득점 관련**: 채점 기준의 "API와 중복 득점 불가" 문구는 일반적으로 외부 DB API(Firebase 등)를 쓸 때 DB와 API 둘 다 받지 못한다는 의미. **내부 Room DB는 외부 API(OpenAI/카카오)와 별개로 30점 인정** (단, 교수님께 사전 확인 권장).

**예상 총점: 약 210점 + 정성 평가 30점 = 최대 240점**

---

## 📂 Activity 구성 (총 5개)

### 1. LoginActivity (진입)
- **역할**: 카카오 로그인 처리
- **사용 API**: Kakao SDK (Login)
- **다음 화면 전달 데이터** (Intent):
  - `userName: String`
  - `userId: Long` (카카오 회원번호)
  - `profileImageUrl: String?`

### 2. MainActivity (홈)
- **역할**:
  - DrawerLayout으로 사이드 메뉴 (마이페이지, 로그아웃 등)
  - 진단 시작 버튼
  - 최근 진단 결과 미리보기 (있다면)
- **Jetpack**: DrawerLayout, Fragment (선택적)
- **Intent 전달**:
  - → DiagnosisActivity: 사용자 정보
  - → MyPageActivity: 사용자 정보
- **Intent 수신**: LoginActivity로부터 사용자 정보

### 3. DiagnosisActivity (퍼스널컬러 진단)
- **역할**:
  - 갤러리에서 사진 선택 OR 카메라로 촬영
  - 선택한 이미지를 LLM API로 전송하여 분석
  - 진단 진행 중 ProgressBar 표시 (Coroutine 활용)
- **외부 APP 연동**:
  - `Intent(Intent.ACTION_PICK)` - 갤러리
  - `Intent(MediaStore.ACTION_IMAGE_CAPTURE)` - 카메라
- **API 호출**: OpenAI GPT-4 Vision API (또는 Claude Vision API)
  - 이미지를 Base64로 인코딩하여 전송
  - 응답: JSON 형식의 진단 결과
- **Coroutine 사용**:
  ```kotlin
  lifecycleScope.launch {
      val result = withContext(Dispatchers.IO) {
          analyzeImage(imageUri)
      }
      // UI 업데이트
  }
  ```
- **Intent 전달** → ResultActivity:
  - `personalColorType: String` (예: "봄 웜톤")
  - `description: String`
  - `recommendedClothColors: ArrayList<String>` (HEX 코드 리스트)
  - `recommendedMakeupColors: ArrayList<String>`
  - `characteristics: ArrayList<String>` (특징 설명)

### 4. ResultActivity (진단 결과)
- **역할**:
  - 진단 결과를 카드 형태로 보여줌
  - ViewPager2로 여러 페이지 구성:
    - 1페이지: 내 퍼스널 컬러 타입 + 컬러 팔레트
    - 2페이지: 어울리는 옷 색상 (RecyclerView 그리드)
    - 3페이지: 어울리는 화장품 색상 (RecyclerView)
    - 4페이지: 특징 및 설명
  - "근처 매장 찾기" 버튼 → MapActivity
  - "마이페이지에 저장" 버튼 → Room DB 저장
- **Jetpack**: ViewPager2 + Fragment + RecyclerView
- **Intent 전달** → MapActivity:
  - `keyword: String` ("올리브영" 또는 추천 브랜드)
  - `recommendedColors: ArrayList<String>`

### 5. MapActivity (지도)
- **역할**:
  - 카카오맵 SDK로 현재 위치 기반 근처 올리브영 표시
  - 마커 클릭 시 매장 정보 표시
- **사용 API**: 카카오맵 SDK + 카카오 로컬 API (장소 검색)
- **권한**: ACCESS_FINE_LOCATION
- **Coroutine**: 위치 기반 검색 API 호출

### 6. MyPageActivity (마이페이지) ⭐
- **역할**:
  - **상단**: 가장 최근 진단 결과를 레포트 형태로 크게 표시
  - **하단**: 과거 진단 이력 리스트 (RecyclerView)
    - 각 이력 클릭 시 ResultActivity로 이동 (Intent로 diagnosisId 전달)
  - 즐겨찾기 매장 탭 (Fragment 전환)
  - **다운로드 버튼**: 현재 보고 있는 레포트를 이미지(PNG)로 저장
    - View를 Bitmap으로 변환 → MediaStore에 저장
- **Room DB 활용**:
  - 진단 이력 조회 (`DiagnosisDao.getAllByUser(userId)`)
  - 추천 색상 조회 (`ColorDao.getByDiagnosisId(id)`)
  - 즐겨찾기 매장 조회 (`StoreDao.getAll()`)
- **Coroutine + Flow**:
  ```kotlin
  lifecycleScope.launch {
      diagnosisDao.getAllByUser(userId).collect { list ->
          adapter.submitList(list)
      }
  }
  ```
- **Jetpack**: RecyclerView, Fragment

---

## 🔌 API 상세 명세

### 1. OpenAI GPT-4 Vision API (퍼스널컬러 분석)
- **엔드포인트**: `https://api.openai.com/v1/chat/completions`
- **모델**: `gpt-4o` 또는 `gpt-4-vision-preview`
- **인증**: Bearer Token (API Key)
- **요청 프롬프트 예시**:
  ```
  이 사진의 인물에 대한 퍼스널 컬러를 진단해줘.
  반드시 아래 JSON 형식으로만 응답해:
  {
    "personal_color": "봄 웜톤 (Spring Warm)",
    "description": "...",
    "characteristics": ["특징1", "특징2", "특징3"],
    "recommended_cloth_colors": [
      {"name": "코랄", "hex": "#FF7F50"},
      ...
    ],
    "recommended_makeup_colors": [
      {"name": "피치 베이지", "hex": "#FFCBA4"},
      ...
    ]
  }
  ```
- **Retrofit Service 인터페이스** 활용
- **이미지 처리**: Base64 인코딩 후 `image_url` 필드에 `data:image/jpeg;base64,...` 형식으로 전달

### 2. 카카오맵 SDK
- **라이브러리**: `com.kakao.maps.open:android:2.x.x`
- **기능**:
  - 지도 표시 (현재 위치 중심)
  - 마커 표시 (검색된 매장)
- **카카오 로컬 API** (장소 검색):
  - 엔드포인트: `https://dapi.kakao.com/v2/local/search/keyword.json`
  - 파라미터: `query=올리브영`, `x=경도`, `y=위도`, `radius=2000`
  - 헤더: `Authorization: KakaoAK {REST_API_KEY}`

### 3. Kakao 로그인 SDK
- **라이브러리**: `com.kakao.sdk:v2-user:2.x.x`
- **기능**:
  - 카카오톡 / 카카오 계정 로그인
  - 사용자 정보 조회 (닉네임, 프로필 이미지, ID)

---

## 🗄️ Room DB 설계

### Entity 1: DiagnosisHistory (진단 이력)
```kotlin
@Entity(tableName = "diagnosis_history")
data class DiagnosisHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,                    // 카카오 사용자 ID
    val personalColorType: String,       // "봄 웜톤" 등
    val description: String,             // 진단 설명
    val characteristics: String,         // JSON String (특징 리스트)
    val imageUri: String?,               // 분석한 사진 경로
    val diagnosedAt: Long                // 진단 일시 (timestamp)
)
```

### Entity 2: RecommendedColor (추천 색상)
```kotlin
@Entity(
    tableName = "recommended_color",
    foreignKeys = [ForeignKey(
        entity = DiagnosisHistory::class,
        parentColumns = ["id"],
        childColumns = ["diagnosisId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class RecommendedColor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val diagnosisId: Long,               // FK
    val colorType: String,               // "cloth" or "makeup"
    val colorName: String,               // "코랄 핑크"
    val hexCode: String                  // "#FF7F50"
)
```

### Entity 3: FavoriteStore (즐겨찾기 매장)
```kotlin
@Entity(tableName = "favorite_store")
data class FavoriteStore(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val savedAt: Long
)
```

### DAO 인터페이스 예시
```kotlin
@Dao
interface DiagnosisDao {
    @Insert
    suspend fun insert(diagnosis: DiagnosisHistory): Long

    @Query("SELECT * FROM diagnosis_history WHERE userId = :userId ORDER BY diagnosedAt DESC")
    fun getAllByUser(userId: Long): Flow<List<DiagnosisHistory>>

    @Query("SELECT * FROM diagnosis_history WHERE id = :id")
    suspend fun getById(id: Long): DiagnosisHistory?

    @Delete
    suspend fun delete(diagnosis: DiagnosisHistory)
}
```

### DB 활용 시나리오
1. **진단 완료 시**: ResultActivity에서 "저장" 버튼 → DiagnosisHistory + RecommendedColor 삽입
2. **마이페이지 진입 시**: 사용자 ID로 진단 이력 조회 → RecyclerView 표시
3. **이력 클릭 시**: ID로 상세 조회 → ResultActivity로 Intent 전달하여 결과 재표시
4. **매장 즐겨찾기**: MapActivity에서 마커 클릭 → "즐겨찾기 추가" 버튼

---

## 📦 사용 라이브러리 (build.gradle)

```kotlin
dependencies {
    // Kotlin & Coroutine
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'

    // Jetpack
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.viewpager2:viewpager2:1.1.0'
    implementation 'androidx.fragment:fragment-ktx:1.6.2'
    implementation 'androidx.drawerlayout:drawerlayout:1.2.0'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'

    // Room DB (선택 - 이력 저장용)
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'

    // 네트워크 (다운로드 매니저)
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

    // 이미지 로딩 (다운로드 매니저)
    implementation 'com.github.bumptech.glide:glide:4.16.0'

    // 카카오 SDK
    implementation "com.kakao.sdk:v2-user:2.20.0"
    implementation "com.kakao.maps.open:android:2.9.5"

    // 권한 처리 (선택)
    implementation 'com.github.permissions-dispatcher:permissionsdispatcher:4.9.2'
}
```

---

## 🎨 UI / 디자인 가이드라인 (심미성 점수 확보)

### 컬러 팔레트 (앱 전체)
- **Primary**: `#FFB6C1` (라이트 핑크)
- **Secondary**: `#E6E6FA` (라벤더)
- **Accent**: `#FFD700` (골드)
- **Background**: `#FFF8F0` (아이보리)
- **Text**: `#3D3D3A`

### 디자인 원칙
1. **Material 3 디자인** 적용
2. **둥근 모서리** (cornerRadius: 16dp 이상)
3. **카드 기반 레이아웃** (CardView)
4. **부드러운 그림자** (elevation: 4dp)
5. **여백 충분히** (padding: 16dp 기본)
6. **폰트**: 시스템 폰트 (또는 Noto Sans KR)

### 핵심 화면 디자인 포인트
- **결과 화면**: 컬러 팔레트를 큰 원형 칩으로 시각화
- **마이페이지 레포트**: 매거진 스타일 (헤더 이미지 + 본문 + 컬러 그리드)

---

## 🛡 안정성 체크리스트 (필수)

1. **권한 처리**: 카메라, 갤러리, 위치 권한 런타임 요청
2. **네트워크 예외**: try-catch + 사용자에게 Toast/Snackbar로 안내
3. **이미지 null 체크**: URI가 null이거나 파일이 없을 때 처리
4. **API 응답 검증**: JSON 파싱 실패 시 기본값 또는 재시도 옵션
5. **로그인 토큰 만료**: 자동 재로그인 또는 LoginActivity로 이동
6. **Activity 생명주기**: Coroutine 취소 처리 (lifecycleScope 사용)
7. **메모리 누수**: ViewBinding 해제, Bitmap recycle

---

## 📋 역할 분담 제안 (2인 기준)

### 팀원 A (프론트엔드 / UI 담당)
- LoginActivity 디자인 및 카카오 로그인 구현
- MainActivity + DrawerLayout 구현
- ResultActivity의 ViewPager2 + Fragment 구현
- MyPageActivity 레포트 UI 및 이미지 저장 기능
- 전체 디자인 시스템 (색상, 폰트, 컴포넌트)

### 팀원 B (백엔드 / API 담당)
- DiagnosisActivity의 카메라/갤러리 연동
- OpenAI Vision API Retrofit 통신 (Coroutine)
- MapActivity의 카카오맵 + 로컬 API 연동
- Room DB 설계 및 진단 이력 저장
- 권한 처리 및 예외 처리

---

## 📅 개발 일정 (4주 기준)

| 주차 | 작업 내용 |
|------|----------|
| 1주차 | 프로젝트 셋업, 카카오 로그인, 기본 Activity 구조 |
| 2주차 | DiagnosisActivity + OpenAI API 연동, ResultActivity ViewPager |
| 3주차 | MapActivity 카카오맵 연동, MyPageActivity + 이미지 저장 |
| 4주차 | UI 다듬기, 예외 처리, 테스트, 발표 PPT 작성 |

---

## ⚠️ Claude Code 작업 시 주의사항

1. **API 키는 절대 코드에 하드코딩하지 말 것** → `local.properties` 또는 `BuildConfig`에 보관
2. **카카오 개발자 콘솔**에서 앱 등록 후 키 해시 등록 필요
3. **OpenAI API 키**는 사용자가 직접 발급받아 주입해야 함 (과금 주의)
4. **이미지 용량 압축** 필수 (Base64 전송 시 토큰 비용 증가)
5. **카카오맵은 API Level별 SDK 차이 확인** (2024년 기준 v2 사용 권장)
6. **모든 비동기 작업은 Coroutine으로 통일** (AsyncTask 사용 금지)

---

## 🎯 최종 채점 예상 점수

| 항목 | 점수 |
|------|------|
| Activity 3개 + Intent | 필수 (감점 없음) |
| Coroutine | 20 |
| 다운로드 매니저 (Retrofit + Glide) | 20 |
| Jetpack 3개 이상 (RV, ViewPager, Fragment, Drawer) | 30 |
| 외부 APP 연동 (갤러리/카메라) | 20 |
| API 3개 (OpenAI, 카카오맵, 카카오 로그인) | 60 |
| **Room DB (진단 이력, 추천 색상, 즐겨찾기)** | **30** |
| 심미성 + 안정성 + 완성도 | 최대 30 |
| **합계** | **최대 210 + 정성 30 = 240** |

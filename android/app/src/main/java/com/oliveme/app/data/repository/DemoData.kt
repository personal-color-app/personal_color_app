package com.oliveme.app.data.repository

import com.oliveme.app.util.UiText
import java.util.UUID

object DemoData {
    fun demoUser(displayName: String = UiText.DEMO_NAME) = UserProfile(
        userId = UiText.DEMO_USER_ID,
        email = UiText.DEMO_EMAIL,
        displayName = displayName,
        loginProvider = "demo",
    )

    fun safeUser() = demoUser()

    fun sampleResult(
        reason: String = "demo",
        id: String = "sample-${UUID.randomUUID()}",
        tone: String = "winter-cool",
    ): PersonalColorResult {
        val profile = toneProfile(tone)
        return PersonalColorResult(
            id = id,
            type = profile.type,
            englishLabel = profile.englishLabel,
            matchScore = profile.matchScore,
            description = profile.description,
            signature = profile.signature,
            palette = profile.palette,
            avoidColors = profile.avoidColors,
            clothes = profile.clothes,
            makeup = profile.makeup,
            traits = profile.traits,
            keywords = profile.keywords,
            isFallback = true,
        )
    }

    fun sampleResultForSource(source: String?) = when {
        source?.contains("spring-warm") == true -> sampleResult(source, tone = "spring-warm")
        source?.contains("summer-cool") == true -> sampleResult(source, tone = "summer-cool")
        source?.contains("autumn-warm") == true -> sampleResult(source, tone = "autumn-warm")
        source?.contains("winter-cool") == true -> sampleResult(source, tone = "winter-cool")
        else -> sampleResult(source ?: "demo")
    }

    fun seededResults(): List<PersonalColorResult> = listOf(
        sampleResult("demo latest", id = "demo-diagnosis-winter-001", tone = "winter-cool"),
        sampleResult("demo history", id = "demo-diagnosis-summer-001", tone = "summer-cool"),
        sampleResult("demo history", id = "demo-diagnosis-autumn-001", tone = "autumn-warm"),
    )

    fun colorStories() = listOf(
        ColorStory(
            id = "story-cool-lip",
            title = "쿨톤 립 가이드",
            subtitle = "맑은 로즈와 베리 계열을 고르는 법",
            tag = "Makeup",
            body = "푸른기가 도는 로즈, 베리, 플럼 컬러는 쿨톤 얼굴의 대비를 선명하게 살려줍니다.",
            personalColorType = "겨울 쿨톤",
        ),
        ColorStory(
            id = "story-outer-depth",
            title = "딥톤 아우터",
            subtitle = "네이비와 와인으로 만드는 안정적인 첫인상",
            tag = "Fashion",
            body = "채도가 낮게 흐려진 색보다 깊고 차가운 네이비, 버건디 계열이 얼굴선을 또렷하게 잡아줍니다.",
            personalColorType = "겨울 쿨톤",
        ),
        ColorStory(
            id = "story-store-picks",
            title = "매장 추천템",
            subtitle = "진단 결과를 제품 선택으로 이어가기",
            tag = "Store",
            body = "리포트의 추천 팔레트와 유사한 색상명을 기준으로 립, 섀도, 니트를 비교해보세요.",
            personalColorType = null,
        ),
    )

    private fun toneProfile(tone: String): PersonalColorResult = when (tone) {
        "spring-warm" -> PersonalColorResult(
            id = "tone-spring",
            type = "봄 웜톤",
            englishLabel = "SPRING · WARM · LIGHT",
            matchScore = 88,
            description = "밝고 생기 있는 복숭아빛과 아이보리 계열이 인상을 부드럽게 밝혀주는 타입입니다.",
            signature = "피치, 코랄, 크림 아이보리처럼 따뜻하고 맑은 색을 우선 추천합니다.",
            palette = listOf(
                ColorItem("#F7B7A3", "피치"),
                ColorItem("#FF8F70", "코랄"),
                ColorItem("#F6D365", "허니 옐로"),
                ColorItem("#FFF1C7", "크림"),
                ColorItem("#A8D58B", "라이트 그린"),
                ColorItem("#FADADD", "웜 핑크"),
            ),
            avoidColors = listOf(
                ColorItem("#1B2A4E", "딥 네이비", "avoid"),
                ColorItem("#4A2347", "플럼", "avoid"),
                ColorItem("#6B7280", "차콜", "avoid"),
                ColorItem("#722F37", "와인", "avoid"),
            ),
            clothes = listOf(
                ProductRecommendation("top", "크림 블라우스", "밝고 따뜻한 기본 상의", "#FFF1C7"),
                ProductRecommendation("dress", "코랄 원피스", "생기 있는 포인트", "#FF8F70"),
                ProductRecommendation("outer", "라이트 베이지 재킷", "부드러운 웜톤 외투", "#E9C8A8"),
            ),
            makeup = mapOf(
                "lip" to listOf(ProductRecommendation("lip", "피치 코랄 립", "맑은 혈색 포인트", "#FF8F70")),
                "eye" to listOf(ProductRecommendation("eye", "샴페인 섀도", "은은한 따뜻한 펄", "#F6D365")),
                "base" to listOf(ProductRecommendation("base", "아이보리 베이스", "노란기 없이 맑게", "#FFF1C7")),
                "cheek" to listOf(ProductRecommendation("cheek", "코랄 치크", "밝은 웜 혈색", "#FFB7A8")),
            ),
            traits = listOf("밝은 명도", "따뜻한 혈색", "맑은 채도"),
            keywords = listOf("Warm", "Light", "Clear", "Peach"),
        )
        "summer-cool" -> PersonalColorResult(
            id = "tone-summer",
            type = "여름 쿨톤",
            englishLabel = "SUMMER · COOL · SOFT",
            matchScore = 89,
            description = "부드러운 라벤더, 로즈, 소프트 블루가 피부의 맑은 분위기를 살려주는 타입입니다.",
            signature = "라벤더, 더스티 로즈, 파우더 블루처럼 차갑고 부드러운 색이 안정적입니다.",
            palette = listOf(
                ColorItem("#C9B8E8", "라벤더"),
                ColorItem("#D7A7B5", "더스티 로즈"),
                ColorItem("#AEC6E8", "파우더 블루"),
                ColorItem("#E8DFF5", "라일락"),
                ColorItem("#B7C7D9", "스모키 블루"),
                ColorItem("#F3D4DE", "소프트 핑크"),
            ),
            avoidColors = listOf(
                ColorItem("#D9A05B", "머스터드", "avoid"),
                ColorItem("#FF8F70", "강한 코랄", "avoid"),
                ColorItem("#5B1A1F", "딥 버건디", "avoid"),
                ColorItem("#A45A2A", "브릭", "avoid"),
            ),
            clothes = listOf(
                ProductRecommendation("top", "라벤더 니트", "부드러운 쿨톤 포인트", "#C9B8E8"),
                ProductRecommendation("outer", "스모키 블루 재킷", "차분한 인상", "#B7C7D9"),
                ProductRecommendation("dress", "로즈 원피스", "은은한 혈색", "#D7A7B5"),
            ),
            makeup = mapOf(
                "lip" to listOf(ProductRecommendation("lip", "소프트 로즈 립", "차분한 장미빛", "#D7A7B5")),
                "eye" to listOf(ProductRecommendation("eye", "모브 브라운 섀도", "탁하지 않은 음영", "#9D8497")),
                "base" to listOf(ProductRecommendation("base", "핑크 톤업 베이스", "맑은 쿨톤 보정", "#F3D4DE")),
                "cheek" to listOf(ProductRecommendation("cheek", "쿨 핑크 치크", "은은한 혈색", "#D7A7B5")),
            ),
            traits = listOf("차가운 온도감", "부드러운 대비", "은은한 채도"),
            keywords = listOf("Cool", "Soft", "Rose", "Lavender"),
        )
        "autumn-warm" -> PersonalColorResult(
            id = "tone-autumn",
            type = "가을 웜톤",
            englishLabel = "AUTUMN · WARM · DEEP",
            matchScore = 90,
            description = "브릭, 올리브, 카멜처럼 깊고 따뜻한 색이 피부 톤과 자연스럽게 어울리는 타입입니다.",
            signature = "차분한 브라운과 올리브 계열이 얼굴의 깊이를 안정적으로 만들어줍니다.",
            palette = listOf(
                ColorItem("#A45A2A", "브릭"),
                ColorItem("#7C6A35", "올리브"),
                ColorItem("#C18A4A", "카멜"),
                ColorItem("#8A4B2F", "테라코타"),
                ColorItem("#D9A05B", "머스터드"),
                ColorItem("#5E3B2E", "초콜릿"),
            ),
            avoidColors = listOf(
                ColorItem("#F2C2D1", "아이스 핑크", "avoid"),
                ColorItem("#AEC6E8", "파우더 블루", "avoid"),
                ColorItem("#C13584", "푸시아", "avoid"),
                ColorItem("#E8DFF5", "라일락", "avoid"),
            ),
            clothes = listOf(
                ProductRecommendation("top", "브릭 니트", "따뜻한 깊이감", "#A45A2A"),
                ProductRecommendation("outer", "카멜 코트", "가을 웜톤 기본", "#C18A4A"),
                ProductRecommendation("dress", "올리브 원피스", "차분한 무드", "#7C6A35"),
            ),
            makeup = mapOf(
                "lip" to listOf(ProductRecommendation("lip", "브릭 로즈 립", "따뜻한 깊이", "#A45A2A")),
                "eye" to listOf(ProductRecommendation("eye", "카멜 브라운 섀도", "고급스러운 음영", "#C18A4A")),
                "base" to listOf(ProductRecommendation("base", "웜 아이보리 베이스", "자연스러운 피부결", "#E9C8A8")),
                "cheek" to listOf(ProductRecommendation("cheek", "베이지 치크", "차분한 온도감", "#D8B58A")),
            ),
            traits = listOf("따뜻한 온도감", "깊은 명도", "차분한 채도"),
            keywords = listOf("Warm", "Deep", "Earthy", "Brick"),
        )
        else -> PersonalColorResult(
            id = "tone-winter",
            type = "겨울 쿨톤",
            englishLabel = "WINTER · COOL · DEEP",
            matchScore = 92,
            description = "선명한 대비와 차가운 딥 컬러가 얼굴 윤곽을 또렷하게 살려주는 타입입니다.",
            signature = "버건디, 네이비, 플럼처럼 깊고 차가운 색이 가장 안정적입니다.",
        palette = listOf(
            ColorItem("#722F37", "와인"),
            ColorItem("#1B2A4E", "네이비"),
            ColorItem("#4A2347", "플럼"),
            ColorItem("#C13584", "푸시아"),
            ColorItem("#B7C7D9", "아이스 블루"),
            ColorItem("#D8DEE9", "실버 그레이"),
        ),
        avoidColors = listOf(
            ColorItem("#D9A05B", "머스터드", "avoid"),
            ColorItem("#C98763", "코랄", "avoid"),
            ColorItem("#B8A070", "카키", "avoid"),
            ColorItem("#D8B58A", "베이지", "avoid"),
        ),
        clothes = listOf(
            ProductRecommendation("top", "플럼 니트", "차가운 보랏빛 포인트", "#4A2347"),
            ProductRecommendation("outer", "네이비 재킷", "대비감을 주는 기본 아우터", "#1B2A4E"),
            ProductRecommendation("dress", "버건디 원피스", "딥톤을 살리는 시그니처", "#5B1A1F"),
            ProductRecommendation("top", "아이스 핑크 셔츠", "맑은 쿨톤 밝기", "#F2C2D1"),
        ),
        makeup = mapOf(
            "lip" to listOf(
                ProductRecommendation("lip", "쿨 로즈 립", "푸시아 한 방울", "#B85C7B"),
                ProductRecommendation("lip", "딥 베리 틴트", "저녁 조명에서도 선명", "#722F37"),
            ),
            "eye" to listOf(
                ProductRecommendation("eye", "그레이 브라운 섀도", "탁하지 않은 음영", "#6B7280"),
            ),
            "base" to listOf(
                ProductRecommendation("base", "핑크 베이스", "붉은기보다 맑은기 보정", "#F2C2D1"),
            ),
            "cheek" to listOf(
                ProductRecommendation("cheek", "쿨 핑크 치크", "차가운 혈색", "#D7A7B5"),
            ),
        ),
        traits = listOf("선명한 대비", "차가운 저채도보다 딥 컬러 선호", "노란기보다 푸른기 안정"),
        keywords = listOf("Cool", "Deep", "Clear", "Wine", "Navy", "Plum"),
        )
    }

    fun sampleStores() = listOf(
        OliveStore("pnu-1", "올리브영 부산대점", "부산 금정구 부산대학로 63", "320m", 35.2315, 129.0840),
        OliveStore("pnu-2", "올리브영 장전역점", "부산 금정구 장전온천천로 48", "710m", 35.2380, 129.0880),
        OliveStore("pnu-3", "올리브영 온천장점", "부산 동래구 온천장로 91", "1.8km", 35.2211, 129.0835),
        OliveStore("pnu-4", "올리브영 구서점", "부산 금정구 구서로 10", "2.1km", 35.2476, 129.0925),
        OliveStore("pnu-5", "올리브영 동래역점", "부산 동래구 중앙대로 1325", "3.4km", 35.2053, 129.0786),
    )
}

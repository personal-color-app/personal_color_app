package com.oliveme.app.data.repository

import com.oliveme.app.util.UiText
import java.util.UUID
import kotlin.random.Random

object DemoData {
    fun demoUser(displayName: String = UiText.DEMO_NAME) = UserProfile(
        userId = UiText.DEMO_USER_ID,
        email = UiText.DEMO_EMAIL,
        displayName = displayName,
        loginProvider = "demo",
    )

    fun safeUser() = demoUser()

    fun randomDemoName(): String {
        val tones = listOf("로즈", "라벤더", "플럼", "네이비", "베리", "올리브")
        return "${tones.random()} ${Random.nextInt(100, 999)}"
    }

    fun sampleResult(reason: String = "demo fallback") = PersonalColorResult(
        id = "sample-${UUID.randomUUID()}",
        type = "겨울 쿨톤",
        englishLabel = "WINTER · COOL · DEEP",
        matchScore = 92,
        description = "선명한 대비와 차가운 딥 컬러가 얼굴 윤곽을 또렷하게 살려주는 타입입니다.",
        signature = "버건디, 네이비, 플럼처럼 깊고 차가운 색이 가장 안정적입니다. ($reason)",
        palette = listOf(
            ColorItem("#722F37", "와인"),
            ColorItem("#5B1A1F", "버건디"),
            ColorItem("#4A2347", "플럼"),
            ColorItem("#1B2A4E", "네이비"),
            ColorItem("#C13584", "푸시아"),
            ColorItem("#F2C2D1", "아이스 핑크"),
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
        ),
        traits = listOf("선명한 대비", "차가운 저채도보다 딥 컬러 선호", "노란기보다 푸른기 안정"),
        keywords = listOf("Cool", "Deep", "Clear", "Wine", "Navy", "Plum"),
        isFallback = true,
    )

    fun sampleStores() = listOf(
        OliveStore("pnu-1", "올리브영 부산대점", "부산 금정구 부산대학로 63", "320m", 35.2315, 129.0840),
        OliveStore("pnu-2", "올리브영 장전역점", "부산 금정구 장전온천천로 48", "710m", 35.2380, 129.0880),
        OliveStore("pnu-3", "올리브영 온천장점", "부산 동래구 온천장로 91", "1.8km", 35.2211, 129.0835),
        OliveStore("pnu-4", "올리브영 구서점", "부산 금정구 구서로 10", "2.1km", 35.2476, 129.0925),
        OliveStore("pnu-5", "올리브영 동래역점", "부산 동래구 중앙대로 1325", "3.4km", 35.2053, 129.0786),
    )
}

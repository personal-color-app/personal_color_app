package com.oliveme.app

import com.oliveme.app.data.remote.BackendAiRecommendationSummary
import com.oliveme.app.data.remote.BackendApiService
import com.oliveme.app.data.remote.BackendProductItem
import com.oliveme.app.data.remote.BackendProductRecommendationRequest
import com.oliveme.app.data.remote.BackendProductRecommendationResponse
import com.oliveme.app.data.remote.BackendProductSearchResponse
import com.oliveme.app.data.repository.ColorItem
import com.oliveme.app.data.repository.CommerceFallbackReason
import com.oliveme.app.data.repository.CommerceRepository
import com.oliveme.app.data.repository.PersonalColorResult
import com.oliveme.app.data.repository.ProductRecommendation
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommerceRepositoryTest {
    @Test
    fun recommendProductsAcceptsRenderableCuratedBackendItems() = runTest {
        val repository = CommerceRepository(
            backend = fakeBackend(
                BackendProductRecommendationResponse(
                    source = "curated",
                    aiSummary = BackendAiRecommendationSummary(
                        headline = "겨울 쿨톤 추천",
                        summary = "백엔드 fallback 후보입니다.",
                        picks = emptyList(),
                        source = "local-fallback",
                    ),
                    items = listOf(
                        BackendProductItem(
                            title = "쿨 로즈 립",
                            link = "https://search.shopping.naver.com/search/all?query=cool",
                            image = "https://dummyimage.com/300x300/fce2e8/3d3137.png&text=OliveMe",
                            mallName = "OliveMe curated",
                            category2 = "립",
                        ),
                    ),
                ),
            ),
        )

        val section = repository.recommendProducts(sampleResult(), "메이크업", listOf("쿨톤 립"))

        assertEquals(null, section.fallbackReason)
        assertEquals(1, section.products.size)
        assertEquals("curated", section.products.first().source)
        assertTrue(section.ai?.headline?.isNotBlank() == true)
    }

    @Test
    fun recommendProductsReturnsQuotaFallbackForProductApi429() = runTest {
        val repository = CommerceRepository(
            backend = fakeBackend(
                BackendProductRecommendationResponse(
                    source = "quota-exhausted",
                    upstreamStatus = 429,
                    fallbackReason = "quota-exhausted",
                    items = emptyList(),
                ),
            ),
        )

        val section = repository.recommendProducts(sampleResult(), "의상", listOf("네이비 재킷"))

        assertTrue(section.products.isEmpty())
        assertEquals(CommerceFallbackReason.ProductApiQuota, section.fallbackReason)
    }

    @Test
    fun recommendProductsReturnsBackendUnavailableWhenBackendThrows() = runTest {
        val repository = CommerceRepository(backend = throwingBackend())

        val section = repository.recommendProducts(sampleResult(), "의상", listOf("네이비 재킷"))

        assertTrue(section.products.isEmpty())
        assertEquals(CommerceFallbackReason.BackendUnavailable, section.fallbackReason)
    }

    @Test
    fun recommendProductsTriesNextBackendCandidateWhenFirstConnectionFails() = runTest {
        val repository = CommerceRepository(
            backends = listOf(
                throwingBackend(),
                fakeBackend(
                    BackendProductRecommendationResponse(
                        source = "naver-shopping",
                        aiSummary = BackendAiRecommendationSummary(
                            headline = "실시간 추천",
                            summary = "두 번째 backend 후보가 응답했습니다.",
                        ),
                        items = listOf(
                            BackendProductItem(
                                title = "네이비 재킷",
                                link = "https://search.shopping.naver.com/search/all?query=navy",
                                image = "https://shopping-phinf.pstatic.net/main_1.jpg",
                                mallName = "Naver",
                                category2 = "아우터",
                            ),
                        ),
                    ),
                ),
            ),
        )

        val section = repository.recommendProducts(sampleResult(), "의상", listOf("네이비 재킷"))

        assertEquals(null, section.fallbackReason)
        assertEquals(1, section.products.size)
        assertEquals("naver-shopping", section.products.first().source)
    }

    @Test
    fun recommendProductsReturnsEmptyProductsWhenBackendItemsAreNotRenderable() = runTest {
        val repository = CommerceRepository(
            backend = fakeBackend(
                BackendProductRecommendationResponse(
                    source = "curated",
                    fallbackReason = "curated-fallback",
                    items = listOf(
                        BackendProductItem(
                            title = "이미지 없는 상품",
                            link = "https://search.shopping.naver.com/search/all?query=empty",
                            image = "",
                        ),
                    ),
                ),
            ),
        )

        val section = repository.recommendProducts(sampleResult(), "의상", listOf("네이비 재킷"))

        assertTrue(section.products.isEmpty())
        assertEquals(CommerceFallbackReason.EmptyProducts, section.fallbackReason)
    }

    private fun fakeBackend(response: BackendProductRecommendationResponse): BackendApiService =
        object : BackendApiService {
            override suspend fun searchProducts(
                query: String,
                category: String?,
                display: Int,
            ): BackendProductSearchResponse = BackendProductSearchResponse()

            override suspend fun recommendProducts(
                body: BackendProductRecommendationRequest,
            ): BackendProductRecommendationResponse = response
        }

    private fun throwingBackend(): BackendApiService =
        object : BackendApiService {
            override suspend fun searchProducts(
                query: String,
                category: String?,
                display: Int,
            ): BackendProductSearchResponse = throw IOException("backend unavailable")

            override suspend fun recommendProducts(
                body: BackendProductRecommendationRequest,
            ): BackendProductRecommendationResponse = throw IOException("backend unavailable")
        }

    private fun sampleResult(): PersonalColorResult =
        PersonalColorResult(
            id = "test",
            type = "겨울 쿨톤",
            englishLabel = "WINTER · COOL",
            season = "winter",
            subtype = "winter-cool",
            matchScore = 92,
            description = "테스트 결과",
            signature = "쿨 로즈",
            palette = listOf(ColorItem("#B85C7B", "쿨 로즈", "best")),
            avoidColors = emptyList(),
            clothes = listOf(ProductRecommendation("아우터", "네이비 재킷", "쿨톤 아우터", "#1B2A4E")),
            makeup = mapOf("립" to listOf(ProductRecommendation("립", "쿨 로즈 립", "차분한 혈색", "#B85C7B"))),
            traits = emptyList(),
            keywords = listOf("쿨톤"),
            productKeywords = listOf("쿨톤 립"),
        )
}

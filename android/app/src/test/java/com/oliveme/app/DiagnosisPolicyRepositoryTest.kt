package com.oliveme.app

import com.google.gson.Gson
import com.oliveme.app.data.repository.DiagnosisPolicy
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosisPolicyRepositoryTest {
    @Test
    fun allPolicyFallbackResultsHaveConcreteMakeupParts() {
        val policy = Gson().fromJson(
            File("src/main/assets/seed/diagnosis_policy.json").readText(),
            DiagnosisPolicy::class.java,
        )

        policy.types.forEach { tone ->
            val result = tone.toResult(id = "test-${tone.subtype}", fallback = true, reason = null)
            val makeupItems = result.makeup.values.flatten()

            assertTrue(
                "${tone.subtype} must include all makeup categories",
                result.makeup.keys.containsAll(listOf("립", "아이", "베이스", "치크")),
            )
            assertEquals("${tone.subtype} should expose one item per makeup category", 4, makeupItems.size)
            assertFalse(
                "${tone.subtype} must not expose generic recommendation titles",
                makeupItems.any { it.title in setOf("추천", "추천 아이템", "아이템", "제품", "상품", "메이크업") },
            )
            assertTrue("${tone.subtype} must have valid makeup colors", makeupItems.all { it.colorHex.matches(Regex("^#[0-9A-Fa-f]{6}$")) })
        }
    }
}

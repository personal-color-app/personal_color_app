package com.oliveme.app

import com.oliveme.app.data.repository.DemoData
import com.oliveme.app.util.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoDataTest {
    @Test
    fun demoUserMatchesSeedCredentialIdentity() {
        val user = DemoData.demoUser()
        assertEquals(UiText.DEMO_EMAIL, user.email)
        assertEquals(UiText.DEMO_USER_ID, user.userId)
    }

    @Test
    fun sampleResultIsCompleteEnoughForFallback() {
        val result = DemoData.sampleResult("test")
        assertTrue(result.palette.isNotEmpty())
        assertTrue(result.clothes.isNotEmpty())
        assertTrue(result.makeup.isNotEmpty())
        assertTrue(result.isFallback)
    }
}

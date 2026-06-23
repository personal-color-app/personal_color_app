package com.oliveme.app.data.remote

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiClientTest {
    @Test
    fun backendBaseUrlCandidatesIncludeEmulatorHostForLoopbackConfig() {
        val candidates = backendBaseUrlCandidates("http://127.0.0.1:8787/")

        assertEquals(
            listOf("http://127.0.0.1:8787/", "http://10.0.2.2:8787/"),
            candidates,
        )
    }

    @Test
    fun backendBaseUrlCandidatesKeepRemoteUrlSingle() {
        val candidates = backendBaseUrlCandidates("https://api.example.com/olive")

        assertEquals(listOf("https://api.example.com/olive/"), candidates)
    }

    @Test
    fun networkSecurityAllowsEmulatorBackendHost() {
        val config = File("src/main/res/xml/network_security_config.xml").readText()

        assertTrue(config.contains(">10.0.2.2<"))
    }
}

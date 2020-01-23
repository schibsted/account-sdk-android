package com.schibsted.account.common.util

import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UtilConnectedTest {

    @Test
    fun decodedStringShouldMatchEncodedOriginal() {
        val encoded = encodeBase64("123")
        val decoded = decodeBase64(encoded)

        assertEquals("123", decoded)
    }

    @Test
    fun basicAuthHeaderMatchesExpectedPattern() {
        val header = createBasicAuthHeader("clientId", "clientSecret")

        assertTrue(header.startsWith("Basic "))

        val encodedCredentials = header.replace("Basic ", "")
        val decodedCredentials = decodeBase64(encodedCredentials)

        val (clientId, clientSecret) = decodedCredentials.split(":")

        assertEquals("clientId", clientId)
        assertEquals("clientSecret", clientSecret)
    }
}

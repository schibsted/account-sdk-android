/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.schibsted.account.model.UserId
import com.schibsted.account.model.UserToken
import com.schibsted.account.persistence.AES_KEY
import com.schibsted.account.persistence.PersistenceEncryption
import com.schibsted.account.persistence.SessionStorageDelegate
import com.schibsted.account.persistence.UserPersistence
import com.schibsted.account.common.util.Logger
import com.schibsted.account.persistence.AES_ALG
import com.schibsted.account.persistence.EncryptionKeyProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.RSAKeyGenParameterSpec
import javax.crypto.spec.SecretKeySpec

class SharedPreferencesDelegateTest {
    private val testToken = Gson().fromJson("{\n" +
            "    \"expires_in\": 604800,\n" +
            "    \"scope\": \"\",\n" +
            "    \"user_id\": \"11099464\",\n" +
            "    \"is_admin\": false,\n" +
            "    \"token_type\": \"Bearer\",\n" +
            "    \"access_token\": \"eyJ0eXAiOiJKV1MiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczpcL1wvaWRlbnRpdHktcHJlLnNjaGlic3RlZC5jb21cLyIsImNsYXNzIjoidG9rZW4uT0F1dGhVc2VyQWNjZXNzVG9rZW4iLCJleHAiOjE1MTA1ODAxNDIsImlhdCI6MTUwOTk3NTM0MiwianRpIjoiNGQ4NDAxOGYtN2JkMC00OWYwLThlOTEtODc0NmZiMWRiZjYyIiwic3ViIjoiZTA2MTYyNzAtMjA5Mi01ZTlkLTg1NmItNDhlMDY1ZDQ4OTlmIiwic2NvcGUiOiIiLCJ1c2VyX2lkIjoiMTEwOTk0NjQiLCJhenAiOiI1OGNmZjk4ZjE3ZTU5Njg2MTU4YjQ1NjciLCJjbGllbnRfaWQiOiI1OGNmZjk4ZjE3ZTU5Njg2MTU4YjQ1NjcifQ.sMeLE74jcI4Q3eTLfJ1a2r2dNC2JSAnsiOJo8ojZyEE\",\n" +
            "    \"refresh_token\": \"eyJ0eXAiOiJKV1MiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczpcL1wvaWRlbnRpdHktcHJlLnNjaGlic3RlZC5jb21cLyIsImNsYXNzIjoidG9rZW4uT0F1dGhVc2VyUmVmcmVzaFRva2VuIiwiZXhwIjoxNTExNzg5NzQyLCJpYXQiOjE1MDk5NzUzNDIsImp0aSI6ImQ3NTY0YzJlLTk4YjItNGE1NC04YWQ4LWY4NzQzNDJiNDUxMyIsInN1YiI6ImUwNjE2MjcwLTIwOTItNWU5ZC04NTZiLTQ4ZTA2NWQ0ODk5ZiIsImFqdGkiOiI0ZDg0MDE4Zi03YmQwLTQ5ZjAtOGU5MS04NzQ2ZmIxZGJmNjIiLCJhenAiOiI1OGNmZjk4ZjE3ZTU5Njg2MTU4YjQ1NjciLCJjbGllbnRfaWQiOiI1OGNmZjk4ZjE3ZTU5Njg2MTU4YjQ1NjciLCJzY29wZSI6IiIsInVzZXJfaWQiOiIxMTA5OTQ2NCJ9.e78g0fB3J4ZgbVjje58YbyFNffZ8GENOG-cqOBmWKKw\",\n" +
            "    \"server_time\": 1509975342\n" +
            "}", UserToken::class.java)

    @Test
    fun noPreviousPrefsShouldUseEmptyList() {
        Logger.loggingEnabled = false

        val mockPrefs: SharedPreferences = mock {
            on { getString(any(), eq(null)) }.thenReturn(null)
            on { edit() }.thenReturn(mock())
        }
        val emptyList: List<UserPersistence.Session> by SessionStorageDelegate(mockContextWithPrefs(mockPrefs), "PREF", PREFERENCE_KEY)

        assert(emptyList.isEmpty(), { "List should be empty" })
    }

    @Test
    fun shouldReadFromPrefsIfAvailable() {
        Logger.loggingEnabled = false
        val json = Gson().toJson(listOf(UserPersistence.Session(1L, UserId.fromTokenResponse(testToken).id, testToken)))
        val mockPrefs: SharedPreferences = mock {
            on { getString(any(), eq(null)) }.thenReturn(Base64.encodeToString(json.toByteArray(), Base64.DEFAULT))
        }

        val mockKeyProvider: EncryptionKeyProvider = mock {
            on { keyPair }.thenReturn(generateKey())
        }

        val mockEncryption: PersistenceEncryption = mock {
            on { rsaDecrypt(any(), any()) }.then { ByteArray(16) }
            on { aesEncrypt(any(), any()) }.then { it.getArgument(0) }
            on { aesDecrypt(any(), any()) }.then { json }
        }

        val nonEmptyList by SessionStorageDelegate(mockContextWithPrefs(mockPrefs), PREFERENCE_FILENAME, PREFERENCE_KEY, mockEncryption, mockKeyProvider)

        assertEquals(1, nonEmptyList.size)
        assertEquals(UserId.fromTokenResponse(testToken).id, nonEmptyList[0].userId)
        assertEquals(604800, nonEmptyList.first().token.expiresIn)
    }

    @Test
    fun shouldRefreshKeyPair() {
        Logger.loggingEnabled = false
        val json = Gson().toJson(listOf(UserPersistence.Session(1L, UserId.fromTokenResponse(testToken).id, testToken)))
        val mockEditor: SharedPreferences.Editor = mock()
        val mockPrefs: SharedPreferences = mock {
            on { getLong(eq(EncryptionKeyProvider.SHARED_PREFERENCES_KEY_PAIR_VALID_UNTIL), anyLong()) }.thenReturn(System.currentTimeMillis())
            on { getString(any(), eq(null)) }.thenReturn(Base64.encodeToString(json.toByteArray(), Base64.DEFAULT))
            on { edit() }.thenReturn(mockEditor)
        }

        val mockKeyProvider: EncryptionKeyProvider = mock {
            on { isKeyCloseToExpiration() }.thenReturn(true)
            on { refreshKeyPair() }.thenReturn(generateKey())
            on { keyPair }.thenReturn(generateKey())
        }

        val mockEncryption: PersistenceEncryption = mock {
            on { rsaDecrypt(any(), any()) }.then { ByteArray(16) }
            on { rsaEncrypt(any(), any()) }.then { ByteArray(16) }
            on { aesEncrypt(any(), any()) }.then { it.getArgument(0) }
            on { aesDecrypt(any(), any()) }.then { json }
            on { generateAesKey() }.then { SecretKeySpec(ByteArray(16), 0, 16, AES_ALG) }
        }

        val nonEmptyList by SessionStorageDelegate(mockContextWithPrefs(mockPrefs), PREFERENCE_FILENAME, PREFERENCE_KEY, mockEncryption, mockKeyProvider)

        assertEquals(1, nonEmptyList.size)
        verify(mockKeyProvider, times(1)).refreshKeyPair()
    }

    @Test
    fun shouldNotPersistWithEmptyAesKey() {
        Logger.loggingEnabled = false
        val json = Gson().toJson(listOf(UserPersistence.Session(1L, UserId.fromTokenResponse(testToken).id, testToken)))
        val mockEditor: SharedPreferences.Editor = mock()
        val mockPrefs: SharedPreferences = mock {
            on { getString(any(), eq(null)) }.thenReturn(Base64.encodeToString(json.toByteArray(), Base64.DEFAULT))
            on { edit() }.thenReturn(mockEditor)
        }

        val mockKeyProvider: EncryptionKeyProvider = mock {
            on { getStoredEncryptionKey() }.thenReturn(generateKey())
            on { keyPair }.thenReturn(generateKey())
        }

        val mockEncryption: PersistenceEncryption = mock {
            on { rsaDecrypt(any(), any()) }.then { ByteArray(0) }
            on { aesEncrypt(any(), any()) }.then { it.getArgument(0) }
            on { aesDecrypt(any(), any()) }.then { json }
        }

        val emptyList by SessionStorageDelegate(mockContextWithPrefs(mockPrefs), PREFERENCE_FILENAME, PREFERENCE_KEY, mockEncryption, mockKeyProvider)
        assert(emptyList.isEmpty())

        verify(mockEditor, times(0)).putString(any(), any())
        verify(mockEditor, times(0)).putString(any(), any())
    }

    @Test
    fun shouldNotPersistWithNullAesKey() {
        Logger.loggingEnabled = false
        val json = Gson().toJson(listOf(UserPersistence.Session(1L, UserId.fromTokenResponse(testToken).id, testToken)))
        val mockEditor: SharedPreferences.Editor = mock()
        val mockPrefs: SharedPreferences = mock {
            on { getString(any(), eq(null)) }.thenReturn(Base64.encodeToString(json.toByteArray(), Base64.DEFAULT))
            on { edit() }.thenReturn(mockEditor)
        }

        val mockKeyProvider: EncryptionKeyProvider = mock {
            on { getStoredEncryptionKey() }.thenReturn(generateKey())
            on { keyPair }.thenReturn(generateKey())
        }

        val mockEncryption: PersistenceEncryption = mock {
            on { rsaDecrypt(any(), any()) }.then { null }
            on { aesEncrypt(any(), any()) }.then { it.getArgument(0) }
            on { aesDecrypt(any(), any()) }.then { json }
        }

        val emptyList by SessionStorageDelegate(mockContextWithPrefs(mockPrefs), PREFERENCE_FILENAME, PREFERENCE_KEY, mockEncryption, mockKeyProvider)
        assert(emptyList.isEmpty())

        verify(mockEditor, times(0)).putString(any(), any())
        verify(mockEditor, times(0)).putString(any(), any())
    }

    @Test
    fun shouldNotPersistNullData() {
        Logger.loggingEnabled = false
        val mockEditor: SharedPreferences.Editor = mock()
        val mockPrefs: SharedPreferences = mock { on { edit() }.thenReturn(mockEditor) }

        val mockKeyProvider: EncryptionKeyProvider = mock {
            on { getStoredEncryptionKey() }.thenReturn(generateKey())
        }

        val mockEncryption: PersistenceEncryption = mock {
            on { aesEncrypt(any(), any()) }.then { it.getArgument(0) }
            on { aesDecrypt(any(), any()) }.then { null }
        }

        val emptyList by SessionStorageDelegate(mockContextWithPrefs(mockPrefs), PREFERENCE_FILENAME, PREFERENCE_KEY, mockEncryption, mockKeyProvider)
        assert(emptyList.isEmpty())

        verify(mockEditor, times(0)).putString(any(), any())
        verify(mockEditor, times(0)).putString(any(), any())
    }

    @Test
    fun shouldNotPersistEmptyData() {
        Logger.loggingEnabled = false
        val mockEditor: SharedPreferences.Editor = mock()
        val mockPrefs: SharedPreferences = mock { on { edit() }.thenReturn(mockEditor) }

        val mockKeyProvider: EncryptionKeyProvider = mock {
            on { getStoredEncryptionKey() }.thenReturn(generateKey())
        }

        val mockEncryption: PersistenceEncryption = mock {
            on { aesEncrypt(any(), any()) }.then { it.getArgument(0) }
            on { aesDecrypt(any(), any()) }.then { "" }
        }

        val emptyList by SessionStorageDelegate(mockContextWithPrefs(mockPrefs), PREFERENCE_FILENAME, PREFERENCE_KEY, mockEncryption, mockKeyProvider)
        assert(emptyList.isEmpty())

        verify(mockEditor, times(0)).putString(any(), any())
        verify(mockEditor, times(0)).putString(any(), any())
    }

    @Test
    fun shouldRemoveDataFromPrefIfNotReadable() {
        Logger.loggingEnabled = false
        val mockEditor: SharedPreferences.Editor = mock()
        val mockPrefs: SharedPreferences = mock { on { edit() }.thenReturn(mockEditor) }

        var sessions: List<UserPersistence.Session> by SessionStorageDelegate(mockContextWithPrefs(mockPrefs), PREFERENCE_FILENAME, PREFERENCE_KEY)
        sessions = listOf()

        verify(mockEditor, times(1)).remove(eq(PREFERENCE_KEY))
        verify(mockEditor, times(1)).remove(eq(AES_KEY))
    }

    @Test
    fun shouldWriteToSharedPrefsOnUpdate() {
        Logger.loggingEnabled = false
        val mockEditor: SharedPreferences.Editor = mock()
        val mockPrefs: SharedPreferences = mock { on { edit() }.thenReturn(mockEditor) }

        var sessions: List<UserPersistence.Session> by SessionStorageDelegate(mockContextWithPrefs(mockPrefs), PREFERENCE_FILENAME, PREFERENCE_KEY)
        sessions = listOf(UserPersistence.Session(123, "userId", mock()))

        verify(mockEditor, times(1)).putString(eq(PREFERENCE_KEY), any())
        verify(mockEditor, times(1)).putString(eq(AES_KEY), any())
    }

    @Test
    fun shouldRemoveDataFromPersistenceIfDataAreEmpty() {
        Logger.loggingEnabled = false

        val data = "[]"
        val mockEditor: SharedPreferences.Editor = mock()
        val mockPrefs: SharedPreferences = mock {
            on { getString(any(), eq(null)) }.thenReturn(Base64.encodeToString(data.toByteArray(), Base64.DEFAULT))
            on { edit() }.thenReturn(mockEditor)
        }

        val mockKeyProvider: EncryptionKeyProvider = mock {
            on { getStoredEncryptionKey() }.thenReturn(generateKey())
            on { keyPair }.thenReturn(generateKey())
        }

        val mockEncryption: PersistenceEncryption = mock {
            on { rsaDecrypt(any(), any()) }.then { ByteArray(16) }
            on { aesEncrypt(any(), any()) }.then { it.getArgument(0) }
            on { aesDecrypt(any(), any()) }.then { data }
        }

        val sessions by SessionStorageDelegate(mockContextWithPrefs(mockPrefs), PREFERENCE_FILENAME, PREFERENCE_KEY, mockEncryption, mockKeyProvider)

        verify(mockEditor, times(1)).remove(eq(PREFERENCE_KEY))
        verify(mockEditor, times(1)).remove(eq(AES_KEY))
    }

    companion object {
        val PREFERENCE_KEY = "KEY"
        val PREFERENCE_FILENAME = "PREF"
        fun mockContextWithPrefs(prefs: SharedPreferences): Context = mock {
            on { getSharedPreferences(any(), any()) }.thenReturn(prefs)
        }

        fun generateKey(): KeyPair {
            val spec = RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4)
            val keyGen = KeyPairGenerator.getInstance("RSA")
            keyGen.initialize(spec)
            return keyGen.genKeyPair()
        }
    }
}

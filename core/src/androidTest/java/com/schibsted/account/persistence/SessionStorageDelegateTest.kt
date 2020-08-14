package com.schibsted.account.persistence

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.util.Base64
import com.nhaarman.mockitokotlin2.*
import com.schibsted.account.model.UserToken
import com.schibsted.account.persistence.UserPersistence.Session
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.KeyPair
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

@RunWith(AndroidJUnit4::class)
class SessionStorageDelegateTest {

    private lateinit var appContext: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var keyProvider: EncryptionKeyProvider
    private lateinit var encryptionUtils: EncryptionUtils

    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getTargetContext()
        prefs = appContext.getSharedPreferences(prefsFileName, MODE_PRIVATE)
        val initialKeyPair = createRsaKeysFromBytes()
        keyProvider = mock {
            on { keyPair } doReturn initialKeyPair
        }
        encryptionUtils = spy(EncUtils())
    }

    @After
    fun tearDown() {
        prefs.edit().clear().commit()
    }

    private fun storage() = SessionStorageDelegate(
            appContext,
            prefsFileName,
            encryptionKeyProvider = keyProvider,
            encryptionUtils = encryptionUtils
    )

    @Test
    fun properSessionsReturned_whenTheyAreSet() {
        val storage = storage()

        var firstList: List<Session> by storage
        assertTrue(firstList.isEmpty())

        firstList = listOf(createSession("test user 1"), createSession("test user 2"))

        val secondList: List<Session> by storage
        assertEquals(firstList, secondList)
    }

    @Test
    fun emptyListOfSessionsReturned_whenPrefsEmpty() {
        val sessions: List<Session> by storage()
        assertTrue(sessions.isEmpty())
    }

    @Test
    fun notEmptyListOfSessionsReturned_whenPrefsNotEmpty() {
        populatePrefs(encodedData, encodedKey)

        val sessions: List<Session> by storage()
        assertTrue(sessions.isNotEmpty())
    }

    @Test
    fun emptyListReturned_and_prefsCleared_whenPrefsContainIncomprehensibleData() {
        populatePrefs("wrong data", encodedKey)

        val sessions: List<Session> by storage()
        assertTrue(sessions.isEmpty())
        assertEquals(null to null, readPrefs())
    }

    @Test
    fun emptyListReturned_and_prefsCleared_whenPrefsContainIncomprehensibleKey() {
        populatePrefs(encodedData, "wrong key")

        val sessions: List<Session> by storage()
        assertTrue(sessions.isEmpty())
        assertEquals(null to null, readPrefs())
    }

    @Test
    fun emptyListReturned_and_prefsCleared_whenPrefsContainIncomprehensibleDataAndKey() {
        populatePrefs("wrong data", "wrong key")

        val sessions: List<Session> by storage()
        assertTrue(sessions.isEmpty())
        assertEquals(null to null, readPrefs())
    }

    @Test
    fun emptyListReturned_and_prefsCleared_whenPrefsContainEmptyStrings() {
        populatePrefs("", "")

        val sessions: List<Session> by storage()
        assertTrue(sessions.isEmpty())
        assertEquals(null to null, readPrefs())
    }

    @Test
    fun keyShouldBeReused_whenPrefsContainValidKey() {
        populatePrefs(encodedData, encodedKey)

        var sessions: List<Session> by storage()
        assertEquals(listOf(createSession("test user 1"), createSession("test user 2")), sessions)

        sessions = listOf(createSession("test user 3"))
        assertEquals(listOf(createSession("test user 3")), sessions)

        val (_, key) = readPrefs()
        assertEquals(encodedKey.trim(), key!!.trim())
    }

    @Test
    fun rsaKeysShouldBeRotated_whenCloseToExpiration() {
        whenever(keyProvider.isKeyCloseToExpiration()) doReturn true
        populatePrefs(encodedData, encodedKey)

        val sessions: List<Session> by storage()
        assertTrue(sessions.isNotEmpty())

        verify(keyProvider).refreshKeyPair()
    }

    @Test
    fun storedDataShouldBeRewrittenAndRsaKeysRotated_whenCloseToExpiration() {
        populatePrefs(encodedData, encodedKey)

        whenever(keyProvider.isKeyCloseToExpiration()) doReturn true
        val storageInstance1 = storage()
        val firstList: List<Session> by storageInstance1
        assertTrue(firstList.isNotEmpty())

        verify(keyProvider).refreshKeyPair()

        whenever(keyProvider.isKeyCloseToExpiration()) doReturn false
        val storageInstance2 = storage()
        val secondList: List<Session> by storageInstance2

        // The actual contents - list of sessions - must stay the same:
        assertEquals("List of sessions should be the same", firstList, secondList)

        // Encoded data and key must be re-encoded and re-written:
        val (data, key) = readPrefs()
        assertNotEquals("Encoded data should not be the same", encodedData.trim(), data!!.trim())
        assertNotEquals("Encoded key should not be the same", encodedKey.trim(), key!!.trim())
    }

    @Test
    fun rsaKeyRefreshed_whenKeyIsInvalid() {
        populatePrefs(encodedData, encodedKey)

        doAnswer { throw InvalidKeyException() }
                .whenever(encryptionUtils).aesEncrypt(any(), any())
        doAnswer { throw InvalidKeyException() }
                .whenever(encryptionUtils).aesDecrypt(any(), any())

        var sessions: List<Session> by storage()
        assertTrue(sessions.isEmpty())
        verify(keyProvider).refreshKeyPair()

        sessions = listOf(createSession("user"))
        verify(keyProvider, times(2)).refreshKeyPair()
    }

    @Test
    fun storageCleared_whenReadingFailsBecauseOfInvalidKey() {
        populatePrefs(encodedData, encodedKey)

        doAnswer { throw InvalidKeyException() }
                .whenever(encryptionUtils).aesDecrypt(any(), any())

        val storedList: List<Session> by storage()
        assertTrue(storedList.isEmpty())
    }

    @Test
    fun storageWritten_withNewKey_afterSuccessfulKeyRefresh() {
        doAnswer { throw InvalidKeyException() } // First, "key is invalid"
                .doCallRealMethod() // After refresh, key is OK
                .whenever(encryptionUtils).aesEncrypt(any(), any())

        val storageInstance1 = storage()
        var storedList: List<Session> by storageInstance1
        storedList = listOf(createSession("user"))
        assertEquals(storedList, listOf(createSession("user")))

        val storageInstance2 = storage()
        val fetchedList by storageInstance2
        assertEquals(storedList, fetchedList)
    }

    @Test
    fun storageCleared_whenEncryptionRepeatedlyFails() {
        doAnswer { throw InvalidKeyException() }
                .whenever(encryptionUtils).aesEncrypt(any(), any())

        val storageInstance1 = storage()
        var storedList: List<Session> by storageInstance1
        storedList = listOf(createSession("user"))
        assertEquals(storedList, listOf(createSession("user")))

        val storageInstance2 = storage()
        val fetchedList by storageInstance2
        assertTrue(fetchedList.isEmpty())
    }

    private fun readPrefs() = with(prefs) {
        getString(PREF_KEY_DATA, null) to getString(PREF_KEY_AES, null)
    }

    private fun populatePrefs(data: String, key: String) = with(prefs.edit()) {
        putString(PREF_KEY_DATA, data)
        putString(PREF_KEY_AES, key)
        commit()
    }

    private open class EncUtils : EncryptionUtils

    companion object {

        private const val prefsFileName = "SessionStorageDelegatePrefs"
        private const val PREF_KEY_DATA = "com.schibsted.account.persistence.SessionStorageDelegate.sessions"
        private const val PREF_KEY_AES = "com.schibsted.account.persistence.SessionStorageDelegate.aeskey"

        // A hard-coded list with two sessions encoded using the key below.
        // Contains string that would be written to SharedPreferences when calling:
        // val sessions = listOf(createSession("test user 1"), createSession("test user 2"))
        private val encodedData = """
        |zXJkIeEu1AhxPe920d6qnIuwfEePrYvdqZCTNc/hgf/yyj5R2fOp+7zZxTXaxMFY7aAYQ5+pf81c
        |fnAcOsnMZtGBz+gzh9wEdAJ2sptZeCD+3cURnhwnm8vsY6aiBFTaZfVijDShMrgR07BLz5xeBhq9
        |rRlxFcSRw8NqAEl7qA4lbcRBXs3ZK5EmvnUYJzz7m43HZhldaWiA/ogz8viKhGTKr0+Ggfj86A2L
        |v1Xmhi93sDqSuLocSTwKUe77OPbXXrw/4H0V5By3nYNZbJMTxbvnD4kEEkmBfWtthR18YQ4To7MQ
        |anQvgsSv3aj5UiZSaWqyS8pCjC2VoLLvbi5wel3NhIxq89+WLSupjA/o8uPJiJLP8GSTTnkD6Ou/
        |nk0l+C3NdFMKHmoRvlPutxAGaogUgAqWEKwVs+Ic4P9EReEIPxYTn1ZMBiImFlF0RlIyeCi10FQn
        |e/2mClkj4GpTF2b8TM1JnQgMOOgSUMRJeEcd7Vur1rs42f8nV2/swBaZeQKmSlvnlPA2wU9jPAQ7
        |jtptvaesgKWfTasho0V4Xmocl491kzVozUoaFTgK82L/192iLQOU2Igvp6UJOdCG5SW+h2YV5xEA
        |cKb/fzGRvY4=
        """.trimMargin()

        // A hard-coded AES key used to encrypt the string above:
        private val encodedKey = """
        |qP7TpCy/tjn9adamec3FCLm8jnDr1TuCSM88tMD903Xm99XXPIpP7poYu+7b04HebggdXy692I64
        |Ljyt+K7i6I/wpFAVysQC/u4fvwRi1F9ggfuiVJ2K1kXqwdALLTiwlfem4bxsKJjPE3bON6q9ghXS
        |HPnU8nk6tyWFIwu14FU=
        """.trimMargin()

        private val publicRsaKey = """
        |MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC+aYlEGCzi0ODwWCXbeVE2Cdms7+QI59UoKFnj
        |Z3jYYn5onh78rD1GoJZHKSAcrjNtiXyqI4PNmFNfAF3hyHTUr47W8Wgo0yIEtIjMl+Di6/tR+d+a
        |GThLTjk66ffLfR2tSWXM63Q0a7ZJxZAK6BHpJSKKMugWxxlD2LFD4GteRwIDAQAB
        """.trimMargin()

        private val privateRsaKey = """
        |MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAL5piUQYLOLQ4PBYJdt5UTYJ2azv
        |5Ajn1SgoWeNneNhifmieHvysPUaglkcpIByuM22JfKojg82YU18AXeHIdNSvjtbxaCjTIgS0iMyX
        |4OLr+1H535oZOEtOOTrp98t9Ha1JZczrdDRrtknFkAroEeklIooy6BbHGUPYsUPga15HAgMBAAEC
        |gYA4ya3cBp/AZ45mb8uemKBtTuPCDWuDcmzihKJGTXI/iqRrnBjev5moyjT3dR70HsoMtY/eCCiD
        |dRYMQ8Y8E7YvwX/T+7OUQASR1Fg5OLgZsOpdRmkyxWvY8q12ig4MbPzymuK0vOAZ7mhCADjafX3p
        |/ak7bsGfKZrOZoT25/g6SQJBAPsrjO0DSl6dU7bFd87Kxvjjcy7XX/42QHRU7bvqax1pnWHK1q/C
        |WfeLcYG68qhhIcoK1FwD4IRA5ZEliSRLwu8CQQDCEuO0XCw6Sd8xvzKxbYR5oasIvfYAsyaDVlH3
        |gzP/oJQCfZ8bSWbzvGZtU+CL6d1zWLZy2CnGrhAVEA5P0TopAkA8TuCvqDKbNTt8jz2NMbNE35gw
        |jxZFe9FOHXZXwJmnnkxjxsh1uzzO63R9qd3KAEiUsrT6wxONredxSon7ZRWRAkB6KXPF40M/yaJB
        |6S2au+poudg0X4bLd/mlMJ/V4nPH1Cd2zeZqQbEZeZ0r8mFlEOgBpHTsI59gXc7nwzyPB4/JAkEA
        |o462y1miHu6HrZZldeyddN7cOaPkxm3cQd1Nu77VsyBshrDYQknCLGBhNcb7iEOjejgmovYNrhsM
        |qFG8GxWISg==
        """.trimMargin()

        private fun createRsaKeysFromBytes(
                bytesPublic: ByteArray = Base64.decode(publicRsaKey, Base64.DEFAULT),
                bytesPrivate: ByteArray = Base64.decode(privateRsaKey, Base64.DEFAULT)
        ): KeyPair =
                with(KeyFactory.getInstance("RSA")) {
                    KeyPair(
                            generatePublic(X509EncodedKeySpec(bytesPublic)),
                            generatePrivate(PKCS8EncodedKeySpec(bytesPrivate))
                    )
                }

        private fun createSession(userId: String) = Session(
                1L,
                userId,
                createUserToken(userId)
        )

        private fun createUserToken(userId: String) = UserToken(
                idToken = "idToken",
                userId = userId,
                serializedAccessToken = "serializedAccessToken",
                refreshToken = "refreshToken",
                scope = "scope",
                tokenType = "tokenType",
                expiresIn = 1
        )
    }
}

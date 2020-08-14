package com.schibsted.account.persistence

import android.content.Context
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.util.Base64
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyFactory
import java.security.KeyPair
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

@RunWith(AndroidJUnit4::class)
class SessionStorageLegacyTest {

    private val rsaKeys: KeyPair = createRsaKeysFromBytes()
    private lateinit var appContext: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var keyProvider: EncryptionKeyProvider
    private lateinit var encryptionUtils: EncryptionUtils
    private lateinit var legacy: SessionStorageLegacy

    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getTargetContext()
        prefs = appContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
        keyProvider = mock { on { keyPair } doReturn rsaKeys }
        encryptionUtils = spy(EncUtils())
        legacy = SessionStorageLegacy(
                appContext,
                encryptionKeyProvider = keyProvider,
                encryptionUtils = encryptionUtils
        )
    }

    @After
    fun tearDown() {
        prefs.edit().clear().commit()
    }

    private fun readPrefs() = with(prefs) {
        getString(PREF_KEY_DATA, null) to getString(PREF_KEY_AES, null)
    }

    private fun populatePrefs(data: String, key: String) = with(prefs.edit()) {
        putString(PREF_KEY_DATA, data)
        putString(PREF_KEY_AES, key)
        commit()
    }

    @Test
    fun sessionsAreNull_whenPrefsAreEmpty() {
        val sessions = legacy.retrieve()

        assertNull(sessions)
    }

    @Test
    fun sessionsAreCorrectlyRetrieved_whenPrefsArePopulated() {
        populatePrefs(encodedData, encodedKey)

        val sessions = legacy.retrieve()

        assertNotNull(sessions)
        assertTrue(sessions!!.isNotEmpty())
    }

    @Test
    fun prefsAreCleared_whenStorageIsCleared() {
        populatePrefs(encodedData, encodedKey)

        legacy.clear()

        val (data, key) = readPrefs()
        assertNull(data)
        assertNull(key)
    }

    @Test
    fun sessionsAreNull_whenStorageIsCleared() {
        populatePrefs(encodedData, encodedKey)

        legacy.clear()

        val sessions = legacy.retrieve()
        assertNull(sessions)
    }

    @Test
    fun prefsAreCleared_whenStorageIsInconsistent() {
        populatePrefs("incorrect data", encodedKey)

        val sessions = legacy.retrieve()

        assertNull(sessions)

        val (data, key) = readPrefs()
        assertNull(data)
        assertNull(key)
    }

    private open class EncUtils : EncryptionUtils

    companion object {

        private const val FILENAME = "IDENTITY_PREFERENCES"
        private const val PREF_KEY_DATA = "IDENTITY_SESSIONS"
        private const val PREF_KEY_AES = "IDENTITY_AES_PREF_KEY"

        // A hard-coded list with two sessions encoded using the key below.
        // Contains string that would be written to SharedPreferences when calling:
        // val sessions = listOf(createSession("test user 1"), createSession("test user 2"))
        private val encodedData = """
        |iexbS4JK18zgFARQ6q3CVAudIWWBq8pvARW+oudZ8jYaMRBkpRgT9fN9EMGXKrzPAJ4eu1oMBIO5
        |CR9202id9TSKLeZoo0C9pJpvh7sLlIrDTRhKqsIV/KlwuvxH2lQp4/kHqaObNW8tPuV7jfidZ2t7
        |zRs5cZHJhe6dW3r6M2d3rcCCFeN9OzRWRb8RP9NhxYLYMTfeehUftUWGzqY0XEsexLEWCviXATOe
        |o9MGT+9gsliyBHxRaTEmQPcqWC5jbwZRcyFGi2JnOjpjDKE8XJLo8egmIsSZIEao7NpkZEbZZmBD
        |7uLsy1yW4kdpoawi0MOMuNC8QG/1Ua8XBpgCbx2mE4FHCR58aJRhLAjJbpl3MYCb0WaQALfd34LS
        |SYQ0m9r8BAYQmNn0PzbUTvQW51Ghp3Qk5BWaNv9WtYTtSBCgJ/GuJqZkPfrLsn4wV/oJbtA2yYtW
        |jgdPG/SFMudTamKCB7uM7s1tpe5Cdd29Ht4SjmxF1sNTGePWKwkI9pVNbvFQryZSae/W/qucIxek
        |cOyc0qMgO+YBQ4pWK5LYTCQ2T4Og+cvrY/Ao94OAlEoR5XvqPUPaJjyjJhWriCb3xe/6iK/n203R
        |uX8Td6QqIJpYH0X6WGU6z3O/TXDvAKmeuwv0+fpuKvJqasB7+A88yGLj5HBslJSR7aGsgkpLqrxy
        |VoJ7g9Z0Ru0IJFgrJGsgH9H1Po/0yy8eG7FykePbIFO8/8or4ChxwSog3OfVJRowSLw5SiSra+nb
        |354wTnUQev5Rzp11fTCI+4DP4qTfQJOD8qv/ZjNj9YqlOdZ9Je4=
        """.trimMargin()

        // A hard-coded AES key used to encrypt the string above:
        private val encodedKey = """
        |SMeRUY871ZXkUN7GlQo0+UAhPry6VcPGvmE8mJjy9qklEFaUFZm3RQoMO1oPRYgy4DZVkZsq/JZU
        |C9H+4Qfn8iWjjxKA0+IKYc5oIU7mjO41EsesfzELHDzytmvD0OlcawmFPFYI8lhYd9pgB0AT71Zt
        |BO4CU7tN26d1Y3rvUPs=
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
    }
}

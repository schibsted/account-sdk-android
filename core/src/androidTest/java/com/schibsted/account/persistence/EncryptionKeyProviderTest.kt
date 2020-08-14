package com.schibsted.account.persistence

import android.content.Context
import android.content.SharedPreferences
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyPair
import java.security.interfaces.RSAKey
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class EncryptionKeyProviderTest {

    private lateinit var appContext: Context
    private lateinit var prefs: SharedPreferences

    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getTargetContext()
        prefs = appContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
    }

    @After
    fun tearDown() {
        prefs.edit().clear().commit()
    }

    @Test
    fun sameKeyPairReturned_whenSameInstanceInvoked_api14() {
        val provider = EncryptionKeyProvider.createApi14(appContext)

        val first = provider.keyPair
        val second = provider.keyPair

        assertEquals(first, second)
    }

    @Test
    fun sameKeyPairReturned_whenMultipleInstancesInvoked_api14() {
        // The first provider creates new KeyPair:
        val providerInstance1 = EncryptionKeyProvider.createApi14(appContext)
        val first = providerInstance1.keyPair

        // The purpose of having multiple instances of EncryptionKeyProvider is to emulate
        // app re-start. The first provider represents the first app session, the second one
        // represents a new session after restart. The same applies to other tests below.

        // The second provider retrieves KeyPair from storage:
        val providerInstance2 = EncryptionKeyProvider.createApi14(appContext)
        val second = providerInstance2.keyPair

        assertEquals(first, second)
    }

    @Test
    fun newKeyPairGeneratedAndStored_whenRefreshInvoked_api14() {
        val providerInstance1 = EncryptionKeyProvider.createApi14(appContext)

        val first = providerInstance1.keyPair

        providerInstance1.refreshKeyPair()

        val second = providerInstance1.keyPair

        assertNotEquals(first, second)

        val providerInstance2 = EncryptionKeyProvider.createApi14(appContext)
        val third = providerInstance2.keyPair

        assertEquals(second, third)
    }

    @Test
    fun sameKeyPairReturned_whenSameInstanceInvoked_api18() {
        val provider = EncryptionKeyProvider.createApi18(appContext)

        val first = provider.keyPair
        val second = provider.keyPair

        assertEquals(first, second)
    }

    @Test
    fun sameKeyPairReturned_whenMultipleInstancesInvoked_api18() {
        // The first provider creates new KeyPair:
        val providerInstance1 = EncryptionKeyProvider.createApi18(appContext)
        val first = providerInstance1.keyPair

        // The second provider retrieves KeyPair from storage:
        val providerInstance2 = EncryptionKeyProvider.createApi18(appContext)
        val second = providerInstance2.keyPair

        assertEquals(first, second)
    }

    @Test
    fun newKeyPairGeneratedAndStored_whenRefreshInvoked_api18() {
        val providerInstance1 = EncryptionKeyProvider.createApi18(appContext)

        val first = providerInstance1.keyPair

        providerInstance1.refreshKeyPair()

        val second = providerInstance1.keyPair

        assertNotEquals(first, second)

        val providerInstance2 = EncryptionKeyProvider.createApi18(appContext)
        val third = providerInstance2.keyPair

        assertEquals(second, third)

        val providerInstance3 = EncryptionKeyProvider.createApi18(appContext)
        val fourth = providerInstance3.keyPair

        assertEquals(third, fourth)
    }

    @Test
    fun sameKeyPairReturned_whenSameInstanceInvoked_api23() {
        val provider = EncryptionKeyProvider.createApi23(appContext)

        val first = provider.keyPair
        val second = provider.keyPair

        assertEquals(first, second)
    }

    @Test
    fun sameKeyPairReturned_whenMultipleInstancesInvoked_api23() {
        // The first provider creates new KeyPair:
        val providerInstance1 = EncryptionKeyProvider.createApi23(appContext)
        val first = providerInstance1.keyPair

        // The second provider retrieves KeyPair from storage:
        val providerInstance2 = EncryptionKeyProvider.createApi23(appContext)
        val second = providerInstance2.keyPair

        assertEquals(first, second)
    }

    @Test
    fun newKeyPairGeneratedAndStored_whenRefreshInvoked_api23() {
        val providerInstance1 = EncryptionKeyProvider.createApi23(appContext)

        val first = providerInstance1.keyPair

        providerInstance1.refreshKeyPair()

        val second = providerInstance1.keyPair

        assertNotEquals(first, second)

        val providerInstance2 = EncryptionKeyProvider.createApi23(appContext)
        val third = providerInstance2.keyPair

        assertEquals(second, third)
    }

    @Test
    fun sameKeyPairReturned_whenSameInstanceInvoked_api28() {
        val provider = EncryptionKeyProvider.createApi28(appContext)

        val first = provider.keyPair
        val second = provider.keyPair

        assertEquals(first, second)
    }

    @Test
    fun sameKeyPairReturned_whenMultipleInstancesInvoked_api28() {
        // The first provider creates new KeyPair:
        val providerInstance1 = EncryptionKeyProvider.createApi28(appContext)
        val first = providerInstance1.keyPair

        // The second provider retrieves KeyPair from storage:
        val providerInstance2 = EncryptionKeyProvider.createApi28(appContext)
        val second = providerInstance2.keyPair

        assertEquals(first, second)
    }

    @Test
    fun newKeyPairGeneratedAndStored_whenRefreshInvoked_api28() {
        val providerInstance1 = EncryptionKeyProvider.createApi28(appContext)

        val first = providerInstance1.keyPair

        providerInstance1.refreshKeyPair()

        val second = providerInstance1.keyPair

        assertNotEquals(first, second)

        val providerInstance2 = EncryptionKeyProvider.createApi28(appContext)
        val third = providerInstance2.keyPair

        assertEquals(second, third)
    }

    @Test
    fun keyIsNotCloseToExpiration_whenNoExpiration() {
        val provider = EncryptionKeyProvider.create(appContext)
        provider.refreshKeyPair()

        prefs.edit().putLong(KEY_EXPIRATION, -1L).commit()

        assertFalse(provider.isKeyCloseToExpiration())
    }

    @Test
    fun keyIsNotCloseToExpiration_whenMoreThan90DayLeft() {
        val provider = EncryptionKeyProvider.create(appContext)
        provider.refreshKeyPair()

        val expiration = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(91)
        prefs.edit().putLong(KEY_EXPIRATION, expiration).commit()

        assertFalse(provider.isKeyCloseToExpiration())
    }

    @Test
    fun keyIsCloseToExpiration_whenLessThan90DayLeft() {
        val provider = EncryptionKeyProvider.create(appContext)
        provider.refreshKeyPair()

        // 90 days is exactly the threshold when we consider the key too close to expiration:
        val expiration = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(90) - 100L
        prefs.edit().putLong(KEY_EXPIRATION, expiration).commit()

        assertTrue(provider.isKeyCloseToExpiration())
    }

    @Test
    fun keyIsCloseToExpiration_whenExpirationDateUnknown() {
        val provider = EncryptionKeyProvider.create(appContext)
        provider.refreshKeyPair()

        prefs.edit().remove(KEY_EXPIRATION).commit()

        assertTrue(provider.isKeyCloseToExpiration())
    }

    companion object {
        private const val FILENAME = "IDENTITY_KEYSTORE"
        private const val KEY_EXPIRATION = "IDENTITY_KEY_EXPIRATION_DATE"

        fun assertEquals(expected: KeyPair?, actual: KeyPair?) {
            if (expected == null && actual == null) return

            val privateExpected = expected?.private as RSAKey
            val privateActual = actual?.private as RSAKey
            assertEquals(privateExpected.modulus, privateActual.modulus)

            val publicExpected = expected.public as RSAKey
            val publicActual = actual.public as RSAKey
            assertEquals(publicExpected.modulus, publicActual.modulus)
        }

        fun assertNotEquals(expected: KeyPair?, actual: KeyPair?) {
            if (expected == null && actual == null) fail()

            val privateExpected = expected?.private as RSAKey
            val privateActual = actual?.private as RSAKey
            assertNotEquals(privateExpected.modulus, privateActual.modulus)

            val publicExpected = expected.public as RSAKey
            val publicActual = actual.public as RSAKey
            assertNotEquals(publicExpected.modulus, publicActual.modulus)
        }
    }
}

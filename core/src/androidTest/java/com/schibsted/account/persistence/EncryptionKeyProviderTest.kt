package com.schibsted.account.persistence

import android.content.Context
import android.content.SharedPreferences
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class EncryptionKeyProviderTest {

    private lateinit var appContext: Context
    private lateinit var prefs: SharedPreferences

    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getTargetContext()
        prefs = appContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE).also {
            it.edit().clear().commit()
        }
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

        assertEquals(first.public, second.public)
    }

    @Test
    fun sameKeyPairReturned_whenMultipleInstancesInvoked_api14() {
        // The first provider creates new KeyPair:
        val providerInstance1 = EncryptionKeyProvider.createApi14(appContext)
        val first = providerInstance1.keyPair

        // The second provider retrieves KeyPair from storage:
        val providerInstance2 = EncryptionKeyProvider.createApi14(appContext)
        val second = providerInstance2.keyPair

        assertEquals(first.public, second.public)
    }

    @Test
    fun newKeyPairGeneratedAndStored_whenRefreshInvoked_api14() {
        val providerInstance1 = EncryptionKeyProvider.createApi14(appContext)

        val first = providerInstance1.keyPair

        providerInstance1.refreshKeyPair()

        val second = providerInstance1.keyPair

        assertNotEquals(first.public, second.public)

        val providerInstance2 = EncryptionKeyProvider.createApi14(appContext)
        val third = providerInstance2.keyPair

        assertEquals(second.public, third.public)
    }

    @Test
    fun sameKeyPairReturned_whenSameInstanceInvoked_api18() {
        val provider = EncryptionKeyProvider.createApi18(appContext)

        val first = provider.keyPair
        val second = provider.keyPair

        assertEquals(first.public, second.public)
    }

    @Test
    fun sameKeyPairReturned_whenMultipleInstancesInvoked_api18() {
        // The first provider creates new KeyPair:
        val providerInstance1 = EncryptionKeyProvider.createApi18(appContext)
        val first = providerInstance1.keyPair

        // The second provider retrieves KeyPair from storage:
        val providerInstance2 = EncryptionKeyProvider.createApi18(appContext)
        val second = providerInstance2.keyPair

        assertEquals(first.public, second.public)
    }

    @Test
    fun newKeyPairGeneratedAndStored_whenRefreshInvoked_api18() {
        val providerInstance1 = EncryptionKeyProvider.createApi18(appContext)

        val first = providerInstance1.keyPair

        providerInstance1.refreshKeyPair()

        val second = providerInstance1.keyPair

        assertNotEquals(first.public, second.public)

        val providerInstance2 = EncryptionKeyProvider.createApi18(appContext)
        val third = providerInstance2.keyPair

        assertEquals(second.public, third.public)
        assertEquals(second.private, third.private)

        val providerInstance3 = EncryptionKeyProvider.createApi18(appContext)
        val fourth = providerInstance3.keyPair

        assertEquals(third.public, fourth.public)
        assertEquals(third.private, fourth.private)
    }

    @Test
    fun sameKeyPairReturned_whenSameInstanceInvoked_api23() {
        val provider = EncryptionKeyProvider.createApi23(appContext)

        val first = provider.keyPair
        val second = provider.keyPair

        assertEquals(first.public, second.public)
    }

    @Test
    fun sameKeyPairReturned_whenMultipleInstancesInvoked_api23() {
        // The first provider creates new KeyPair:
        val providerInstance1 = EncryptionKeyProvider.createApi23(appContext)
        val first = providerInstance1.keyPair

        // The second provider retrieves KeyPair from storage:
        val providerInstance2 = EncryptionKeyProvider.createApi23(appContext)
        val second = providerInstance2.keyPair

        assertEquals(first.public, second.public)
    }

    @Test
    fun newKeyPairGeneratedAndStored_whenRefreshInvoked_api23() {
        val providerInstance1 = EncryptionKeyProvider.createApi23(appContext)

        val first = providerInstance1.keyPair

        providerInstance1.refreshKeyPair()

        val second = providerInstance1.keyPair

        assertNotEquals(first.public, second.public)

        val providerInstance2 = EncryptionKeyProvider.createApi23(appContext)
        val third = providerInstance2.keyPair

        assertEquals(second.public, third.public)
    }

    @Test
    fun sameKeyPairReturned_whenSameInstanceInvoked_api28() {
        val provider = EncryptionKeyProvider.createApi28(appContext)

        val first = provider.keyPair
        val second = provider.keyPair

        assertEquals(first.public, second.public)
    }

    @Test
    fun sameKeyPairReturned_whenMultipleInstancesInvoked_api28() {
        // The first provider creates new KeyPair:
        val providerInstance1 = EncryptionKeyProvider.createApi28(appContext)
        val first = providerInstance1.keyPair

        // The second provider retrieves KeyPair from storage:
        val providerInstance2 = EncryptionKeyProvider.createApi28(appContext)
        val second = providerInstance2.keyPair

        assertEquals(first.public, second.public)
    }

    @Test
    fun newKeyPairGeneratedAndStored_whenRefreshInvoked_api28() {
        val providerInstance1 = EncryptionKeyProvider.createApi28(appContext)

        val first = providerInstance1.keyPair

        providerInstance1.refreshKeyPair()

        val second = providerInstance1.keyPair

        assertNotEquals(first.public, second.public)

        val providerInstance2 = EncryptionKeyProvider.createApi28(appContext)
        val third = providerInstance2.keyPair

        assertEquals(second.public, third.public)
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

        val expiration = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(90)
        prefs.edit().putLong(KEY_EXPIRATION, expiration).commit()

        assertTrue(provider.isKeyCloseToExpiration())
    }

    companion object {
        private const val FILENAME = "IDENTITY_KEYSTORE"
        private const val KEY_EXPIRATION = "KEY_PAIR_VALID_UNTIL"
    }
}

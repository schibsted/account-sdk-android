/*
 * Copyright (c) 2019 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.schibsted.account.common.util.Logger
import io.kotlintest.specs.WordSpec
import org.mockito.ArgumentMatchers.anyLong
import java.util.concurrent.TimeUnit

class EncryptionKeyProviderTest : WordSpec({
    Logger.loggingEnabled = false
    "checking a key more than 90 days left" should {
        "not be close to expiration" {
            val timestamp = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(91)

            val context = mockContextWithPrefs(timestamp)
            val provider = EncryptionKeyProvider(context)
            assert(!provider.isKeyCloseToExpiration())
        }
    }

    "checking a key without a stored validity" should {
        "count as close to expiry" {
            val context = mockContextWithPrefs(0L)
            val provider = EncryptionKeyProvider(context)
            assert(provider.isKeyCloseToExpiration())
        }
    }

    "checking a key with no validity limit" should {
        "not count as being close to expiry" {
            val context = mockContextWithPrefs(EncryptionKeyProvider.NO_EXPIRY)
            val provider = EncryptionKeyProvider(context)
            assert(!provider.isKeyCloseToExpiration())
        }
    }

    "checking a key with less than 90 days left" should {
        "be close to expiration" {
            val timestamp = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(89)

            val context = mockContextWithPrefs(timestamp)
            val provider = EncryptionKeyProvider(context)
            assert(provider.isKeyCloseToExpiration())
        }
    }

    "checking a key that has expired" should {
        "count as being close to expiry" {
            val timestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14)

            val context = mockContextWithPrefs(timestamp)
            val provider = EncryptionKeyProvider(context)
            assert(provider.isKeyCloseToExpiration())
        }
    }
}) {
    companion object {
        private fun mockContextWithPrefs(durationAnswer: Long): Context = mock {
            val prefs = mockSharedPrefs(durationAnswer)
            on { getSharedPreferences(any(), any()) }.thenReturn(prefs)
        }

        private fun mockSharedPrefs(durationAnswer: Long): SharedPreferences {
            val mockEditor: SharedPreferences.Editor = mock()
            return mock {
                on { getLong(any(), anyLong()) }.thenReturn(durationAnswer)
                on { edit() }.thenReturn(mockEditor)
            }
        }
    }
}

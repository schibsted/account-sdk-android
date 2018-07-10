/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.session

import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.whenever
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.never
import com.schibsted.account.ClientConfiguration
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.UserToken
import com.schibsted.account.network.Environment
import com.schibsted.account.persistence.UserPersistence
import com.schibsted.account.test.TestUtil
import com.schibsted.account.util.DateUtils
import io.kotlintest.forAll
import io.kotlintest.matchers.haveSubstring
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.StringSpec
import java.util.Date
import java.util.Calendar

class UserPersistenceTest : StringSpec({
    Logger.loggingEnabled = false
    ClientConfiguration.set(ClientConfiguration(Environment.ENVIRONMENT_PREPRODUCTION, "myClient", "mySecret"))
    val userToken = UserToken(null, "myUser", "accessToken", "refreshToken", "scope", "type", 123)

    "When the user is not persistable, it should not be persisted" {
        val mockedSharedPrefs: SharedPreferences = mock {
            val storage = mutableMapOf<String, String>()

            val mockEditor: SharedPreferences.Editor = mock {
                on { putString(any(), any()) }.then {
                    val key: String = it.getArgument(0)
                    val value: String = it.getArgument(1)
                    storage.put(key, value)
                }
            }

            on { edit() } doReturn mockEditor
            on { getString(any(), anyOrNull()) }.thenAnswer {
                val key: String = it.getArgument(0)
                val default: String? = it.getArgument(1)
                storage[key] ?: default
            }
        }

        val mockContext: Context = mock {
            on { getSharedPreferences(any(), eq(0)) }.thenReturn(mockedSharedPrefs)
        }

        val userPersistence: UserPersistence = spy(UserPersistence(mockContext))
        doReturn(null).whenever(userPersistence).readTokenCompat()
        doNothing().whenever(userPersistence).clearTokenCompat()

        val user = User(userToken, false)

        val logger = TestUtil.CaptureLogger()
        Logger.logWorker = logger
        Logger.loggingEnabled = true

        userPersistence.persist(user)
        logger.messages.size shouldBe 1
        logger.messages.first() should haveSubstring("user is not flagged as persistable")

        Logger.logWorker = Logger.DEFAULT_LOG_WORKER
        Logger.loggingEnabled = false
    }

    "Terms previously accepted should check time against the current" {
        val up: UserPersistence = mock {
            on { acceptedAgreementsCache }.thenReturn("myuserid|${DateUtils.getLaterRandomDateAsString(1,5)}")
            on { termsPreviouslyAccepted(any()) }.thenCallRealMethod()
        }

        up.termsPreviouslyAccepted("myuserid") shouldBe true
    }

    "Terms only use a matching user id" {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.MINUTE, 5)

        val up: UserPersistence = mock {
            on { acceptedAgreementsCache }.thenReturn("myuserid|${DateUtils.getLaterRandomDateAsString(1,5)}")
            on { termsPreviouslyAccepted(any()) }.thenCallRealMethod()
        }

        up.termsPreviouslyAccepted("myuseridxx") shouldBe false
    }

    "Terms cache requires the entry to be valid" {
        val up: UserPersistence = mock {
            on { acceptedAgreementsCache }.thenReturn("myuserid|")
            on { termsPreviouslyAccepted(any()) }.thenCallRealMethod()
        }

        up.termsPreviouslyAccepted("myuserid") shouldBe false
    }

    "Storing a new entry should produce a next valid entry within the defined interval" {
        val storedValues = mutableListOf<String>()

        val editor: SharedPreferences.Editor = mock {
            on { putString(eq("AGR_CACHE"), any()) }.then {
                storedValues.add(it.arguments[1] as String)
                mock<SharedPreferences.Editor>()
            }
        }

        val prefs: SharedPreferences = mock {
            on { getString(eq("AGR_CACHE"), any()) }.then {
                storedValues.last()
            }
            on { edit() }.thenReturn(editor)
        }

        val mockedContext: Context = mock {
            on { getSharedPreferences(any(), any()) }.thenReturn(prefs)
        }

        val up: UserPersistence = spy(UserPersistence(mockedContext))

        (1..10).forEach { up.putCacheResult("myuser") }

        val currentTime = Date()
        val cal = Calendar.getInstance()

        cal.time = currentTime
        cal.add(Calendar.MINUTE, UserPersistence.MAX_TERMS_CACHE_MINUTES)
        val maxDate = cal.time

        cal.time = currentTime
        cal.add(Calendar.MINUTE, UserPersistence.MIN_TERMS_CACHE_MINUTES)
        val minDate = cal.time

        forAll(storedValues.map { it.split("|").last() }) { value ->
            val date = DateUtils.fromString(value)
            date?.after(minDate) shouldBe true
            date?.before(maxDate) shouldBe true
        }

        storedValues.last() shouldEqual up.acceptedAgreementsCache
    }

    "resumeSession should use a valid cached result if available" {
        val userPersistence: UserPersistence = mock {
            on { resumeSession(any(), any()) }.thenCallRealMethod()
            on { termsPreviouslyAccepted(any()) }.thenReturn(true)
        }

        val mockCallback: ResultCallback<User> = mock()
        userPersistence.resumeSession(userToken, mockCallback)

        verify(mockCallback).onSuccess(any())
        verify(userPersistence).termsPreviouslyAccepted(any())
        verify(userPersistence, never()).putCacheResult(any())
    }
})

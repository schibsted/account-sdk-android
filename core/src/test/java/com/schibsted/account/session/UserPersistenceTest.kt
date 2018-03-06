/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.session

import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import com.schibsted.account.ClientConfiguration
import com.schibsted.account.model.UserToken
import com.schibsted.account.network.Environment
import com.schibsted.account.persistence.UserPersistence
import com.schibsted.account.test.TestUtil
import com.schibsted.account.common.util.Logger
import io.kotlintest.matchers.haveSubstring
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.BehaviorSpec

class UserPersistenceTest : BehaviorSpec({
    Logger.loggingEnabled = false
    ClientConfiguration.set(ClientConfiguration(Environment.ENVIRONMENT_PREPRODUCTION, "myClient", "mySecret"))

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

    Given("A fresh UserPersistence instance") {
        When("storing a user which is persistable") {
            val user = User(UserToken(null, "myUser", "accessToken", "refreshToken", "scope", "type", 123), true)
            userPersistence.persist(user)

            Then("it should be retrieved successfully using the resumeLast function") {
                val res = userPersistence.resumeLast()
                res shouldNotBe null
                res!!.userId.id shouldBe "myUser"
            }

            Then("it should be successfully retrieved using the user id") {
                val res = userPersistence.resume("myUser")
                res shouldNotBe null
                res!!.userId.id shouldBe "myUser"
            }
        }

        When("the user is not persistable") {
            val user = User(UserToken(null, "myUser", "accessToken", "refreshToken", "scope", "type", 123), false)

            Then("it should not be persisted") {
                val logger = TestUtil.CaptureLogger()
                Logger.logWorker = logger
                Logger.loggingEnabled = true

                userPersistence.persist(user)
                logger.messages.size shouldBe 1
                logger.messages.first() should haveSubstring("user is not flagged as persistable")

                Logger.logWorker = Logger.DEFAULT_LOG_WORKER
                Logger.loggingEnabled = false
            }
        }
    }
})

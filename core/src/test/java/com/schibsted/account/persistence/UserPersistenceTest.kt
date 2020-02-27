/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import com.nhaarman.mockitokotlin2.*
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.UserId
import com.schibsted.account.model.UserToken
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.model.error.ClientError.ErrorType.AGREEMENTS_NOT_ACCEPTED
import com.schibsted.account.model.error.ClientError.ErrorType.SESSION_NOT_FOUND
import com.schibsted.account.session.User
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class UserPersistenceTest : StringSpec() {

    override fun isInstancePerTest() = true

    init {

        Logger.loggingEnabled = false

        val sessionStorageDelegate: SessionStorageDelegate = mock()
        val resumeDelegate: ResumeDelegate = mock()
        val legacyKeyValueStore: LegacyKeyValueStore = mock()
        val userPersistence = UserPersistence(sessionStorageDelegate, resumeDelegate, legacyKeyValueStore)

        val testToken = UserToken(null, "myUser", "accessToken", "refreshToken", "scope", "type", 123)
        val user: User = mock {
            on { userId } doReturn UserId(id = "myUser", legacyId = "myUser")
            on { token } doReturn testToken
            on { isPersistable } doReturn true
        }

        val session = UserPersistence.Session(0L, "myUser", testToken)

        fun `assume storage contains session`() {
            whenever(sessionStorageDelegate.getValue(anyOrNull(), any())).thenReturn(listOf(session))
        }

        "When user is signed out, session should not be persisted" {
            whenever(user.token).thenReturn(null) // signed out

            userPersistence.persist(user)

            verify(sessionStorageDelegate, never()).setValue(anyOrNull(), any(), any())
        }

        "When the user is not persistable, session should not be persisted" {
            whenever(user.isPersistable).thenReturn(false)

            userPersistence.persist(user)

            verify(sessionStorageDelegate, never()).setValue(anyOrNull(), any(), any())
        }

        "When user is persistable, session should be persisted" {
            userPersistence.persist(user)

            argumentCaptor<List<UserPersistence.Session>>().apply {
                verify(sessionStorageDelegate).setValue(anyOrNull(), any(), capture())
                firstValue.first().userId shouldBe "myUser"
            }
        }

        "If agreement is obtained, session should be resumed" {
            `assume storage contains session`()
            whenever(resumeDelegate.proceed(any(), any(), any())).thenAnswer {
                val success = it.getArgument(1) as ((user: User) -> Unit)
                success(user)
            }

            val callback: ResultCallback<User> = mock()
            userPersistence.resume("myUser", callback)

            verify(callback).onSuccess(any())
        }

        "If agreement is not obtained, session should not be resumed" {
            `assume storage contains session`()

            val error = ClientError(AGREEMENTS_NOT_ACCEPTED,
                    "User has not accepted agreements, please log in again.")
            whenever(resumeDelegate.proceed(any(), any(), any())).thenAnswer {
                val failure = it.getArgument(2) as ((error: ClientError) -> Unit)
                failure(error)
            }

            val callback: ResultCallback<User> = mock()
            userPersistence.resume("myUser", callback)

            verify(callback).onError(eq(error))
        }

        "When cache doesn't contain session for user, session should not be resumed" {
            val callback: ResultCallback<User> = mock()
            userPersistence.resume("myUser", callback)

            argumentCaptor<ClientError> {
                verify(callback).onError(capture())
                firstValue.errorType shouldBe SESSION_NOT_FOUND
            }
        }

        "When cache is empty, session should not be resumed" {
            val callback: ResultCallback<User> = mock()
            userPersistence.resumeLast(callback)

            argumentCaptor<ClientError> {
                verify(callback).onError(capture())
                firstValue.errorType shouldBe SESSION_NOT_FOUND
            }
        }

        "When session is persisted, legacy KeyValueStore should be cleared" {
            userPersistence.persist(user)

            verify(legacyKeyValueStore).clearToken()
        }

        "When last session is resumed, legacy KeyValueStore should be queried" {
            userPersistence.resumeLast(mock())

            verify(legacyKeyValueStore).readToken()
        }

        "When session for user is resumed, invalid tokens should cleared" {
            val validToken: UserToken = mock { on { isValidToken() } doReturn true }
            val invalidToken: UserToken = mock { on { isValidToken() } doReturn false }
            whenever(sessionStorageDelegate.getValue(anyOrNull(), any())).thenReturn(listOf(
                    UserPersistence.Session(2L, "myUser", validToken),
                    UserPersistence.Session(1L, "myUser", invalidToken)
            ))

            userPersistence.resume("userId", mock())

            verify(sessionStorageDelegate).setValue(anyOrNull(), any(), eq(listOf(
                    UserPersistence.Session(2L, "myUser", validToken)
            )))
        }

        "When last session is resumed, invalid tokens should cleared" {
            val validToken: UserToken = mock { on { isValidToken() } doReturn true }
            val invalidToken: UserToken = mock { on { isValidToken() } doReturn false }
            whenever(sessionStorageDelegate.getValue(anyOrNull(), any())).thenReturn(listOf(
                    UserPersistence.Session(2L, "myUser", validToken),
                    UserPersistence.Session(1L, "myUser", invalidToken)
            ))

            userPersistence.resumeLast(mock())

            verify(sessionStorageDelegate).setValue(anyOrNull(), any(), eq(listOf(
                    UserPersistence.Session(2L, "myUser", validToken)
            )))
        }

        "When sessions for user are removed, they are removed from storage too" {
            whenever(sessionStorageDelegate.getValue(anyOrNull(), any())).thenReturn(listOf(
                    UserPersistence.Session(3L, "otherUser", testToken),
                    UserPersistence.Session(2L, "myUser", testToken),
                    UserPersistence.Session(1L, "myUser", testToken)
            ))

            userPersistence.remove("myUser")

            verify(sessionStorageDelegate).setValue(anyOrNull(), any(), eq(listOf(
                    UserPersistence.Session(3L, "otherUser", testToken)
            )))
        }

        "When the oldest session is removed, it is removed from storage too" {
            whenever(sessionStorageDelegate.getValue(anyOrNull(), any())).thenReturn(listOf(
                    UserPersistence.Session(3L, "otherUser", testToken),
                    UserPersistence.Session(2L, "myUser", testToken),
                    UserPersistence.Session(1L, "myUser", testToken)
            ))

            userPersistence.removeLast()

            verify(sessionStorageDelegate).setValue(anyOrNull(), any(), eq(listOf(
                    UserPersistence.Session(3L, "otherUser", testToken),
                    UserPersistence.Session(2L, "myUser", testToken)
            )))
        }

        "When all sessions are removed, they are removed from storage too" {
            whenever(sessionStorageDelegate.getValue(anyOrNull(), any())).thenReturn(listOf(
                    UserPersistence.Session(3L, "otherUser", testToken),
                    UserPersistence.Session(2L, "myUser", testToken),
                    UserPersistence.Session(1L, "myUser", testToken)
            ))

            userPersistence.removeAll()

            verify(sessionStorageDelegate).setValue(anyOrNull(), any(), eq(emptyList()))
        }
    }
}

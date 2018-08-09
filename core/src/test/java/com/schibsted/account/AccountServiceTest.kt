/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account

import android.test.mock.MockContext
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.schibsted.account.common.util.Logger
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

class AccountServiceTest : WordSpec({
    Logger.loggingEnabled = false

    "unbind" should {
        "not be called when bind fails" {
            val mockContext: MockContext = mock {
                on { bindService(any(), any(), any()) } doReturn false
                on { packageName } doReturn "com.example.app"
            }
            val srv = AccountService(mockContext, mock())

            srv.bind()
            srv.isPersistenceServiceBound shouldBe false

            srv.unbind()

            verify(mockContext, never()).unbindService(any())
        }

        "be called when bind succeeds" {
            val mockContext: MockContext = mock {
                on { bindService(any(), any(), any()) } doReturn true
                on { packageName } doReturn "com.example.app"
            }
            val srv = AccountService(mockContext, mock())

            srv.bind()
            srv.isPersistenceServiceBound shouldBe true
            srv.unbind()

            verify(mockContext).unbindService(any())
        }
    }
})

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.test.mock.MockContext
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.schibsted.account.common.util.Logger
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import org.mockito.ArgumentMatchers.anyString

class AccountServiceTest : WordSpec({
    Logger.loggingEnabled = false

    fun mockAppContext(bindReturnValue: Boolean): MockContext {
        val packageManageMock: PackageManager = mock {
            on { getPackageInfo(anyString(), any()) } doReturn run {
                val packageInfo = PackageInfo()
                packageInfo.versionCode = 1234
                packageInfo
            }
        }
        val mockContext: MockContext = mock {
            on { bindService(any(), any(), any()) } doReturn bindReturnValue
            on { packageName } doReturn "com.example.app"
            on { packageManager } doReturn packageManageMock
        }
        return mockContext
    }

    "unbind" should {
        "not be called when bind fails" {
            val mockContext = mockAppContext(false)
            val srv = AccountService(mockContext, mock())

            srv.bind()
            srv.isPersistenceServiceBound shouldBe false

            srv.unbind()

            verify(mockContext, never()).unbindService(any())
        }

        "be called when bind succeeds" {
            val mockContext = mockAppContext(true)
            val srv = AccountService(mockContext, mock())

            srv.bind()
            srv.isPersistenceServiceBound shouldBe true
            srv.unbind()

            verify(mockContext).unbindService(any())
        }
    }
})

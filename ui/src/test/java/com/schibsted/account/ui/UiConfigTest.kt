/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.test.mock.MockContext
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.kotlintest.matchers.beOfType
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import java.util.Locale

class UiConfigTest : WordSpec({

    "fromManifest" should {
        "resolve it's fields from the manifest" {
            val locale = Locale("fr", "FR")
            val mockBundle: Bundle = mock {
                on { getString(any()) } doReturn listOf(
                        "fr_FR",
                        "my disabled message"
                )

                on { get(any()) }.thenReturn(
                        false,
                        true,
                        555
                )
            }
            val mockPackageManager: PackageManager = mock { on { getApplicationInfo(any(), any()) } doReturn ApplicationInfo().apply { metaData = mockBundle } }
            val mockContext: MockContext = mock {
                on { packageManager } doReturn mockPackageManager
                on { packageName } doReturn "MYPACKAGENAME"
                on { getString(any()) } doReturn "AAA"
            }

            val conf = OptionalConfiguration.fromManifest(mockContext)

            conf.locale shouldBe locale
            conf.signUpMode should beOfType<SignUpMode.Disabled>()
            (conf.signUpMode as SignUpMode.Disabled).disabledMessage shouldBe "my disabled message"
            conf.isCancellable shouldBe true
            conf.clientLogo shouldBe 555
        }
    }
})

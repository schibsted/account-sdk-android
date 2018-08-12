/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui.smartlock

import android.app.Activity
import android.os.Parcelable
import io.kotlintest.matchers.shouldBe
import io.kotlintest.mock.mock
import io.kotlintest.specs.WordSpec

class SmartlockTaskTest : WordSpec({
    "initialize smartlock" should {
        "without smartlock available" should {
            val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
            "return false if smartlock is running" {
                smartlockTask.initializeSmartlock(true, false).value shouldBe false
            }

            "return false if smartlock is not running" {
                smartlockTask.initializeSmartlock(false, false).value shouldBe false
            }

            "return false if mode id enabled" {
                smartlockTask.initializeSmartlock(true, false).value shouldBe false
            }
        }
        "with disabled mode" should {
            val smartlockTask = SmartlockTask(SmartlockMode.DISABLED)
            "return false if smartlock is running" {
                smartlockTask.initializeSmartlock(true).value shouldBe false
            }

            "return false if smartlock is not running" {
                smartlockTask.initializeSmartlock(false).value shouldBe false
            }
        }
        "with enabled mode" should {
            val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
            "return false if smartlock is running" {
                smartlockTask.initializeSmartlock(true, true).value shouldBe false
            }

            "return true if smartlock is not running" {
                smartlockTask.initializeSmartlock(false, true).value shouldBe true
            }
        }

        "with forced mode" should {
            val smartlockTask = SmartlockTask(SmartlockMode.FORCED)
            "return false if smartlock is running" {
                smartlockTask.initializeSmartlock(true, true).value shouldBe false
            }

            "return true if smartlock is not running" {
                smartlockTask.initializeSmartlock(false, true).value shouldBe true
            }
        }

        "with failed mode" should {
            val smartlockTask = SmartlockTask(SmartlockMode.FAILED)
            "return false all the time" {
                smartlockTask.initializeSmartlock(false).value shouldBe false
                smartlockTask.initializeSmartlock(true).value shouldBe false
            }
        }
    }

    "get credentials from intent" should {
        "with a invalid result code" should {
            "fail" {
                val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
                val result = smartlockTask.credentialsFromParcelable(SmartlockController.RC_CHOOSE_ACCOUNT, Activity.RESULT_CANCELED, mock()).value
                (result as SmartlockTask.SmartLockResult.Failure).resultCode shouldBe Activity.RESULT_CANCELED
            }
        }

        "with a correct result code" should {
            "fail if the parcelable value is null" {
                val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
                val result = smartlockTask.credentialsFromParcelable(SmartlockController.RC_CHOOSE_ACCOUNT, Activity.RESULT_OK, null).value
                (result as SmartlockTask.SmartLockResult.Failure).resultCode shouldBe Activity.RESULT_OK
            }

            "fail if the request code is identify but the mode is forced" {
                val smartlockTask = SmartlockTask(SmartlockMode.FORCED)
                val result = smartlockTask.credentialsFromParcelable(SmartlockController.RC_IDENTIFIER_ONLY, Activity.RESULT_OK, mock()).value
                (result as SmartlockTask.SmartLockResult.Failure).resultCode shouldBe Activity.RESULT_OK
            }

            "succeed if the request code is unknown" {
                val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
                val cred = mock<Parcelable>()
                val result = smartlockTask.credentialsFromParcelable(234234, Activity.RESULT_OK, cred).value
                (result as SmartlockTask.SmartLockResult.Failure).resultCode shouldBe Activity.RESULT_OK
            }

            "succeed if the request code is choose account" {
                val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
                val cred = mock<Parcelable>()
                val result = smartlockTask.credentialsFromParcelable(SmartlockController.RC_CHOOSE_ACCOUNT, Activity.RESULT_OK, cred).value
                (result as SmartlockTask.SmartLockResult.Success).requestCode shouldBe SmartlockController.RC_CHOOSE_ACCOUNT
                result.credentials shouldBe cred
            }

            "succeed if the request code is identify and mode enabled" {
                val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
                val cred = mock<Parcelable>()
                val result = smartlockTask.credentialsFromParcelable(SmartlockController.RC_IDENTIFIER_ONLY, Activity.RESULT_OK, cred).value
                (result as SmartlockTask.SmartLockResult.Success).requestCode shouldBe SmartlockController.RC_IDENTIFIER_ONLY
                result.credentials shouldBe cred
            }
        }
    }
})
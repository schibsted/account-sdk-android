/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui.smartlock

import android.app.Activity
import android.os.Parcelable
import com.nhaarman.mockito_kotlin.mock
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

class SmartlockTaskTest : WordSpec({
    "initialize smartlock" should {
        "return false if smartlock is running without smartlock available" {
            val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
            smartlockTask.initializeSmartlock(true, false).value shouldBe false
        }

        "return false if smartlock is not running without smartlock available should" {
            val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
            smartlockTask.initializeSmartlock(false, false).value shouldBe false
        }

        "return false if mode id enabled without smartlock available" {
            val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
            smartlockTask.initializeSmartlock(true, false).value shouldBe false
        }

        "return false if smartlock is running with disabled mode" {
            val smartlockTask = SmartlockTask(SmartlockMode.DISABLED)
            smartlockTask.initializeSmartlock(true).value shouldBe false
        }

        "return false if smartlock is not running with disabled mode should" {
            val smartlockTask = SmartlockTask(SmartlockMode.DISABLED)
            smartlockTask.initializeSmartlock(false).value shouldBe false
        }

        "return false if smartlock is not running with enabled mode" {
            val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
            smartlockTask.initializeSmartlock(true, true).value shouldBe false
        }

        "return true if smartlock is not running with enabled mode" {
            val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
            smartlockTask.initializeSmartlock(false, true).value shouldBe true
        }

        "return false if smartlock is running with forced mode" {
            val smartlockTask = SmartlockTask(SmartlockMode.FORCED)
            smartlockTask.initializeSmartlock(true, true).value shouldBe false
        }

        "return true if smartlock is not running with forced mode" {
            val smartlockTask = SmartlockTask(SmartlockMode.FORCED)
            smartlockTask.initializeSmartlock(false, true).value shouldBe true
        }

        "return false all the time with failed mode" {
            val smartlockTask = SmartlockTask(SmartlockMode.FAILED)
            smartlockTask.initializeSmartlock(false).value shouldBe false
            smartlockTask.initializeSmartlock(true).value shouldBe false
        }
    }

    "get credentials from intent" should {
        "fail with a invalid result code " {
            val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
            val result = smartlockTask.credentialsFromParcelable(SmartlockController.RC_CHOOSE_ACCOUNT, Activity.RESULT_CANCELED, mock()).value
            (result as SmartlockTask.SmartLockResult.Failure).resultCode shouldBe Activity.RESULT_CANCELED
        }

        "fail if the parcelable value is null with a correct result code" {
            val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
            val result = smartlockTask.credentialsFromParcelable(SmartlockController.RC_CHOOSE_ACCOUNT, Activity.RESULT_OK, null).value
            (result as SmartlockTask.SmartLockResult.Failure).resultCode shouldBe Activity.RESULT_OK
        }

        "fail if the request code is identify but the mode is forced with a correct result code" {
            val smartlockTask = SmartlockTask(SmartlockMode.FORCED)
            val result = smartlockTask.credentialsFromParcelable(SmartlockController.RC_IDENTIFIER_ONLY, Activity.RESULT_OK, mock()).value
            (result as SmartlockTask.SmartLockResult.Failure).resultCode shouldBe Activity.RESULT_OK
        }

        "succeed if the request code is unknown with a correct result code " {
            val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
            val cred = mock<Parcelable>()
            val result = smartlockTask.credentialsFromParcelable(234234, Activity.RESULT_OK, cred).value
            (result as SmartlockTask.SmartLockResult.Failure).resultCode shouldBe Activity.RESULT_OK
        }

        "succeed if the request code is choose account with a correct result code" {
            val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
            val cred = mock<Parcelable>()
            val result = smartlockTask.credentialsFromParcelable(SmartlockController.RC_CHOOSE_ACCOUNT, Activity.RESULT_OK, cred).value
            (result as SmartlockTask.SmartLockResult.Success).requestCode shouldBe SmartlockController.RC_CHOOSE_ACCOUNT
            result.credentials shouldBe cred
        }

        "succeed if the request code is identify and mode enabled with a correct result code" {
            val smartlockTask = SmartlockTask(SmartlockMode.ENABLED)
            val cred = mock<Parcelable>()
            val result = smartlockTask.credentialsFromParcelable(SmartlockController.RC_IDENTIFIER_ONLY, Activity.RESULT_OK, cred).value
            (result as SmartlockTask.SmartLockResult.Success).requestCode shouldBe SmartlockController.RC_IDENTIFIER_ONLY
            result.credentials shouldBe cred
        }
    }
})
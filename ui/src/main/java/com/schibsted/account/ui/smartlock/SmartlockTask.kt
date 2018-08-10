/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui.smartlock

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import com.schibsted.account.common.lib.ObservableField

class SmartlockTask(private val smartlockMode: SmartlockMode) {

    sealed class SmartLockResult {
        data class Success(val requestCode: Int, val credentials: Parcelable) : SmartLockResult()
        data class Failure(val resultCode: Int) : SmartLockResult()
    }

    fun initializeSmartlock(isSmartlockRunning: Boolean, isSmartlockAvailable : Boolean = SmartlockController.isSmartlockAvailable()): ObservableField<Boolean> {
        return if (isSmartlockAvailable && smartlockMode != SmartlockMode.DISABLED) {
            ObservableField(!(isSmartlockRunning || smartlockMode == SmartlockMode.FAILED))
        } else {
            ObservableField(false)
        }
    }

    fun credentialsFromParcelable(requestCode: Int, resultCode: Int, smartlockCredentials : Parcelable?): ObservableField<SmartLockResult> {
        if (resultCode == Activity.RESULT_OK) {
            smartlockCredentials?.let {
                when (requestCode) {
                    SmartlockController.RC_CHOOSE_ACCOUNT ->
                        return ObservableField(SmartLockResult.Success(requestCode, smartlockCredentials))
                    SmartlockController.RC_IDENTIFIER_ONLY -> {
                        return if (smartlockMode == SmartlockMode.FORCED) {
                            ObservableField(SmartLockResult.Failure(resultCode))
                        } else {
                            ObservableField(SmartLockResult.Success(requestCode, smartlockCredentials))
                        }
                    }
                }
                return ObservableField(SmartLockResult.Failure(resultCode))
            } ?: return ObservableField(SmartLockResult.Failure(resultCode))
        } else {
            return ObservableField(SmartLockResult.Failure(resultCode))
        }
    }
}
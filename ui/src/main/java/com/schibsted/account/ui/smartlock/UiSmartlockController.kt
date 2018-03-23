/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.smartlock

import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import com.schibsted.account.common.smartlock.SmartLockCallback
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.smartlock.SmartlockController

class UiSmartlockController(activity: AppCompatActivity, smartLockCallback: SmartLockCallback) {
    private val smartlockController = SmartlockController(activity, smartLockCallback)

    fun requestCredentials() {
        smartlockController.requestCredentials()
    }

    fun saveCredential(identifier: String, password: String) {
        smartlockController.saveCredentials(identifier, password)
    }

    fun deleteCredential() {
        smartlockController.deleteCredentials()
    }

    fun convertToIdentityCredential(parcelable: Parcelable): Credentials? {
        return smartlockController.mapToIdentityCredentials(parcelable)
    }

    fun extractCredentialData(parcelable: Parcelable): Pair<String?, String?>? {
        return smartlockController.extractCredentialData(parcelable)
    }
}

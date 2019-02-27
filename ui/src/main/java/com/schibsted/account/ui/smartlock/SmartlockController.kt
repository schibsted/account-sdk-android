/*
 * `Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.`
 */

package com.schibsted.account.ui.smartlock

import android.os.Parcelable
import com.schibsted.account.common.util.existsOnClasspath
import com.schibsted.account.ui.login.BaseLoginActivity

class SmartlockController(loginActivity: BaseLoginActivity, private val smartlockReceiver: SmartlockReceiver) {
    private var uiSmartlockController: UiSmartlockController = UiSmartlockController(loginActivity, smartlockReceiver)

    fun start() {
        if (!smartlockReceiver.isSmartlockResolving.value) {
            uiSmartlockController.requestCredentials()
            smartlockReceiver.isSmartlockResolving.value = true
        }
    }

    fun deleteCredential() {
        uiSmartlockController.deleteCredential()
    }

    fun saveCredential(identifier: String, password: String) {
        uiSmartlockController.saveCredential(identifier, password)
    }

    fun provideCredential(parcelable: Parcelable) {
        uiSmartlockController.convertToIdentityCredential(parcelable)?.let {
            smartlockReceiver.onCredentialRetrieved(it.identifier.identifier, it.password, it.keepLoggedIn)
        } ?: smartlockReceiver.onFailure()
    }

    fun provideHint(parcelable: Parcelable) {
        uiSmartlockController.extractCredentialData(parcelable)?.first?.let {
            smartlockReceiver.onHintRetrieved(it)
        } ?: smartlockReceiver.onFailure()
    }

    companion object {
        /**
         * Request code sent by the smartlock controller when the user has to choose between multiple account the one to login with.
         * This must be checked in [BaseLoginActivity.onActivityResult]
         */
        const val RC_CHOOSE_ACCOUNT = 3
        /**
         * Request code sent by the smartlock controller when the user has to choose which account must be used to pre-fill the identifier field.
         * This must be checked in [BaseLoginActivity.onActivityResult]
         */
        const val RC_IDENTIFIER_ONLY = 4
        /**
         * Key used to extract the Parcelable credential object from an Intent
         * Note that you should not directly cast the object as Credential [com.google.android.gms.credentials.Credential], that would bring a dependency
         * which might not be resolvable if the smartlock dependency isn't added by the client.
         * This must be checked in [BaseLoginActivity.onActivityResult]
         */
        const val EXTRA_SMARTLOCK_CREDENTIALS = "com.google.android.gms.credentials.Credential"

        fun isSmartlockAvailable(): Boolean = existsOnClasspath("com.schibsted.account.smartlock.SmartlockController")
        fun hasSmartlockResult(requestCode: Int): Boolean {
            return when (requestCode) {
                RC_CHOOSE_ACCOUNT, RC_IDENTIFIER_ONLY -> true
                else -> false
            }
        }
    }
}

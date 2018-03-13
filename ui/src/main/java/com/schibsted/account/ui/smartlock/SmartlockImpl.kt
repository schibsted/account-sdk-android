/*
 * `Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.`
 */

package com.schibsted.account.ui.smartlock

import android.app.Activity.RESULT_FIRST_USER
import android.os.Parcelable
import com.schibsted.account.common.util.existsOnClasspath
import com.schibsted.account.engine.controller.LoginController
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.integration.contract.LoginContract
import com.schibsted.account.smartlock.SmartLockCallback
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.BaseLoginActivity.Companion.KEY_UI_CONFIGURATION

class SmartlockImpl(private val loginActivity: BaseLoginActivity, private val loginController: LoginController, private val loginContract: LoginContract) : SmartLockCallback {
    private var uiSmartlockController: UiSmartlockController? = null
    var isSmartlockResolving: Boolean = false

    init {
        uiSmartlockController = uiSmartlockController ?: UiSmartlockController(loginActivity, this).apply {
            if (!isSmartlockResolving) {
                requestCredentials()
                isSmartlockResolving = true
            }
        }
    }

    fun deleteCredential() {
        uiSmartlockController?.deleteCredential()
    }

    fun saveCredential(identifier: String, password: String) {
        uiSmartlockController?.saveCredential(identifier, password)
    }

    fun provideCredential(parcelable: Parcelable) {
        uiSmartlockController?.convertToIdentityCredential(parcelable)?.let {
            onCredentialRetrieved(it)
        } ?: onFailure()
    }

    fun provideHint(parcelable: Parcelable) {
        uiSmartlockController?.extractCredentialData(parcelable)?.first?.let {
            onHintRetrieved(it)
        } ?: onFailure()
    }

    override fun onCredentialRetrieved(credential: Credentials) {
        isSmartlockResolving = false
        loginActivity.smartlockCredentials = credential
        loginController.start(loginContract)
    }

    override fun onHintRetrieved(id: String) {
        loginActivity.uiConfiguration = loginActivity.uiConfiguration.newBuilder().identifier(id).build()
        isSmartlockResolving = false
    }

    override fun onCredentialDeleted() {
        loginActivity.smartlockCredentials = null
    }

    override fun onFailure() {
        val intent = loginActivity.intent.putExtra(KEY_UI_CONFIGURATION, loginActivity.uiConfiguration.newBuilder().disableSmartlock().build())
        loginActivity.setResult(SMARTLOCK_FAILED, intent)
        loginActivity.navigationController.finishNavigation()
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
        /**
         * Result code sent through [BaseLoginActivity.onActivityResult] to notify the client application that the smartlock login
         * failed
         */
        const val SMARTLOCK_FAILED = RESULT_FIRST_USER + 1

        fun isSmartlockAvailable(): Boolean = existsOnClasspath("com.schibsted.account.smartlock.SmartlockController")
    }
}

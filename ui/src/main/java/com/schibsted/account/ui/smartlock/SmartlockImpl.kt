/*
 * `Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.`
 */

package com.schibsted.account.ui.smartlock

import android.os.Parcelable
import com.schibsted.account.common.smartlock.SmartLockCallback
import com.schibsted.account.common.util.Logger
import com.schibsted.account.common.util.existsOnClasspath
import com.schibsted.account.engine.controller.LoginController
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.integration.contract.LoginContract
import com.schibsted.account.ui.AccountUi
import com.schibsted.account.ui.login.BaseLoginActivity

class SmartlockImpl(private val loginActivity: BaseLoginActivity, private val loginController: LoginController, private val loginContract: LoginContract) : SmartLockCallback {
    private var uiSmartlockController: UiSmartlockController = UiSmartlockController(loginActivity, this)
    var isSmartlockResolving: Boolean = false

    fun start() {
        if (!isSmartlockResolving) {
            uiSmartlockController.requestCredentials()
            isSmartlockResolving = true
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
            onCredentialRetrieved(it.identifier.identifier, it.password, it.keepLoggedIn)
        } ?: onFailure()
    }

    fun provideHint(parcelable: Parcelable) {
        uiSmartlockController.extractCredentialData(parcelable)?.first?.let {
            onHintRetrieved(it)
        } ?: onFailure()
    }

    override fun onCredentialRetrieved(id: String, password: String, keepMeLoggedIn: Boolean) {
        isSmartlockResolving = false
        loginActivity.smartlockCredentials = Credentials(Identifier(Identifier.IdentifierType.EMAIL, id), password, keepMeLoggedIn)
        loginController.start(loginContract)
    }

    override fun onHintRetrieved(id: String) {
        loginActivity.uiConfiguration = loginActivity.uiConfiguration.copy(identifier = id)
        isSmartlockResolving = false
    }

    override fun onCredentialDeleted() {
        loginActivity.smartlockCredentials = null
    }

    override fun onFailure() {
        Logger.info(TAG, "Smartlock login failed - smartlock mode ${loginActivity.uiConfiguration.smartlockMode.name}")
        loginActivity.setResult(AccountUi.SMARTLOCK_FAILED, loginActivity.intent)
        loginActivity.finish()
    }

    companion object {
        private val TAG = SmartlockImpl::class.java.simpleName
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
    }
}

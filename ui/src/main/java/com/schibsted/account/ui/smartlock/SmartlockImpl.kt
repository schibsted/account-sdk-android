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

class SmartlockImpl(private val loginActivity: BaseLoginActivity, val loginController: LoginController, val loginContract: LoginContract) : SmartLockCallback {
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
        loginActivity.credentials = credential
        loginController.start(loginContract)
    }

    override fun onHintRetrieved(id: String) {
        loginActivity.uiConfiguration = loginActivity.uiConfiguration.newBuilder().identifier(id).build()
        isSmartlockResolving = false
    }

    override fun onCredentialDeleted() {
        loginActivity.credentials = null
    }

    override fun onFailure() {
        val intent = loginActivity.intent.putExtra(KEY_UI_CONFIGURATION, loginActivity.uiConfiguration.newBuilder().disableSmartlock().build())
        loginActivity.setResult(SMARTLOCK_FAILED, intent)
        loginActivity.navigationController.finishNavigation()
    }

    companion object {
        const val RC_READ = 3
        const val RC_HINT = 4
        const val EXTRA_SMARTLOCK_CREDENTIALS = "com.google.android.gms.credentials.Credential"
        const val SMARTLOCK_FAILED = RESULT_FIRST_USER + 1

        fun isSmartlockAvailable(): Boolean = existsOnClasspath("com.schibsted.account.smartlock.SmartlockController")
    }
}

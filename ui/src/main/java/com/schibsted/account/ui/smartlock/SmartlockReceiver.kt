/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.smartlock

import android.app.Activity
import com.schibsted.account.common.lib.ObservableField
import com.schibsted.account.common.smartlock.SmartLockCallback
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.ui.login.LoginActivityViewModel
import com.schibsted.account.ui.login.flow.password.FlowSelectionListener

class SmartlockReceiver(private val loginActivityViewModel: LoginActivityViewModel) : SmartLockCallback {

    var isSmartlockResolving: ObservableField<Boolean> = ObservableField(false)
    override fun onCredentialRetrieved(id: String, password: String, keepMeLoggedIn: Boolean) {
        isSmartlockResolving.value = false
        loginActivityViewModel.smartlockCredentials.value = Credentials(Identifier(Identifier.IdentifierType.EMAIL, id), password, keepMeLoggedIn)
        loginActivityViewModel.userFlowType = FlowSelectionListener.FlowType.LOGIN
    }

    override fun onHintRetrieved(id: String) {
        isSmartlockResolving.value = false
        loginActivityViewModel.uiConfiguration.value = loginActivityViewModel.uiConfiguration.value?.copy(identifier = id)
    }

    override fun onCredentialDeleted() {
        isSmartlockResolving.value = false
        loginActivityViewModel.smartlockCredentials.value = null
    }

    override fun onFailure() {
        isSmartlockResolving.value = false
        loginActivityViewModel.smartlockResult.value = SmartlockTask.SmartLockResult.Failure(Activity.RESULT_CANCELED)
    }

    override fun onNoValue() {
        isSmartlockResolving.value = false
        loginActivityViewModel.smartlockCredentials.value = null
    }
}
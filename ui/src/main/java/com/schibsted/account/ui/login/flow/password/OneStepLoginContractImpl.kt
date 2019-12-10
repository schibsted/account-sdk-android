/*
 * Copyright (c) 2019 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.flow.password

import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.LoginActivityViewModel

class OneStepLoginContractImpl(private val loginActivity: BaseLoginActivity, private val loginActivityViewModel: LoginActivityViewModel) : LoginContractImpl(loginActivity, loginActivityViewModel) {
    override fun onCredentialsRequested(provider: InputProvider<Credentials>) {
        val credentials = loginActivityViewModel.smartlockCredentials.value
        if (credentials != null) {
            provider.provide(credentials, object : ResultCallback<NoValue> {
                override fun onSuccess(result: NoValue) {
                }

                override fun onError(error: ClientError) {
                    loginActivity.smartlockController?.deleteCredential()
                    loginActivityViewModel.smartlockReceiver.onFailure()
                }
            })
        } else {
            loginActivityViewModel.credentialsProvider.value = provider
        }
    }
}

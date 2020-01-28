/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.flow.password

import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.engine.input.Agreements
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.input.RequiredFields
import com.schibsted.account.engine.integration.CallbackProvider
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.integration.contract.LoginContract
import com.schibsted.account.model.LoginResult
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.AgreementLinksResponse
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.LoginActivityViewModel
import com.schibsted.account.ui.ui.FlowFragment

open class LoginContractImpl(private val loginActivity: BaseLoginActivity, private val loginActivityViewModel: LoginActivityViewModel) : LoginContract {
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
            loginActivityViewModel.userIdentifier?.let { identifier ->
                val fragment = loginActivity.fragmentProvider.getOrCreatePasswordFragment(
                        provider,
                        identifier,
                        loginActivityViewModel.isUserAvailable(),
                        loginActivity.smartlockController)
                loginActivity.navigationController.navigateToFragment(fragment)
            }
                    ?: loginActivity.loadRequiredInformation()
        }
    }

    override fun onAccountVerificationRequested(identifier: Identifier) {
        val fragment = loginActivity.fragmentProvider.getOrCreateInboxFragment(identifier)
        loginActivity.navigationController.navigateToFragment(fragment)
    }

    override fun onAgreementsRequested(agreementsProvider: InputProvider<Agreements>, agreementLinks: AgreementLinksResponse) {
        BaseLoginActivity.tracker?.userId = this.loginActivity.loginController?.currentUserId?.legacyId
        val fragment = loginActivity.fragmentProvider.getOrCreateTermsFragment(
                agreementsProvider,
                loginActivityViewModel.isUserAvailable(),
                agreementLinks)
        loginActivity.navigationController.navigateToFragment(fragment)
    }

    override fun onRequiredFieldsRequested(requiredFieldsProvider: InputProvider<RequiredFields>, fields: Set<String>) {
        BaseLoginActivity.tracker?.userId = this.loginActivity.loginController?.currentUserId?.legacyId

        val fragment = loginActivity.fragmentProvider.getOrCreateRequiredFieldsFragment(
                requiredFieldsProvider,
                fields)
        loginActivity.navigationController.navigateToFragment(fragment)
    }

    override fun onFlowReady(callbackProvider: CallbackProvider<LoginResult>) {
        callbackProvider.provide(object : ResultCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                BaseLoginActivity.tracker?.userId = this@LoginContractImpl.loginActivity.loginController?.currentUserId?.legacyId

                loginActivity.navigationController.finishFlow(result.user)
            }

            override fun onError(error: ClientError) {
                val fragment = loginActivity.navigationController.currentFragment
                fragment?.displayErrorDialog(error)
                if (fragment is FlowFragment<*>) {
                    fragment.hideProgress()
                }

                if (error.errorType == ClientError.ErrorType.NETWORK_ERROR) {
                    BaseLoginActivity.tracker?.eventError(TrackingData.UIError.NetworkError, null)
                }
            }
        })
    }
    companion object {
        const val TAG = "LoginContractImpl"
    }
}

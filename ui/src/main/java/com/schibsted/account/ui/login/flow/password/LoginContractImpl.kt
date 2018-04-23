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
import com.schibsted.account.ui.ui.FlowFragment

class LoginContractImpl(private val loginActivity: BaseLoginActivity) : LoginContract {
    override fun onCredentialsRequested(provider: InputProvider<Credentials>) {
        val credentials = loginActivity.smartlockCredentials
        if (credentials != null) {
            provider.provide(credentials, object : ResultCallback<NoValue> {
                override fun onSuccess(result: NoValue) {
                }

                override fun onError(error: ClientError) {
                    loginActivity.smartlock?.deleteCredential()
                    loginActivity.smartlock?.onFailure()
                }
            })
        } else {
            loginActivity.currentIdentifier?.let { identifier ->
                val fragment = loginActivity.fragmentProvider.getOrCreatePasswordFragment(
                        loginActivity.navigationController.currentFragment,
                        provider,
                        identifier,
                        loginActivity.isUserAvailable(),
                        loginActivity.smartlock)
                loginActivity.navigationController.navigateToFragment(fragment)
            }
                    ?: loginActivity.startIdentificationFragment(if (loginActivity is FlowSelectionListener) loginActivity else null)
        }
    }

    override fun onAccountVerificationRequested(identifier: Identifier) {
        val fragment = loginActivity.fragmentProvider.getOrCreateInboxFragment(loginActivity.navigationController.currentFragment, identifier)
        loginActivity.navigationController.navigateToFragment(fragment)
    }

    override fun onAgreementsRequested(agreementsProvider: InputProvider<Agreements>, agreementLinks: AgreementLinksResponse) {
        BaseLoginActivity.tracker?.userId = this.loginActivity.loginController?.currentUserId?.legacyId
        val fragment = loginActivity.fragmentProvider.getOrCreateTermsFragment(loginActivity.navigationController.currentFragment,
                agreementsProvider,
                loginActivity.isUserAvailable(),
                agreementLinks)
        loginActivity.navigationController.navigateToFragment(fragment)
    }

    override fun onRequiredFieldsRequested(requiredFieldsProvider: InputProvider<RequiredFields>, fields: Set<String>) {
        BaseLoginActivity.tracker?.userId = this.loginActivity.loginController?.currentUserId?.legacyId

        val fragment = loginActivity.fragmentProvider.getOrCreateRequiredFieldsFragment(
                loginActivity.navigationController.currentFragment,
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
}

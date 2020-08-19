/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.flow.password

import androidx.lifecycle.Observer
import android.os.Bundle
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.engine.controller.SignUpController
import com.schibsted.account.engine.input.Agreements
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.input.RequiredFields
import com.schibsted.account.engine.integration.CallbackProvider
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.integration.contract.SignUpContract
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.AgreementLinksResponse
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.screen.LoginScreen
import com.schibsted.account.ui.ui.FlowFragment

class PasswordActivity : BaseLoginActivity(), SignUpContract {

    private var signUpController: SignUpController? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if (viewModel.isFlowReady()) {
            viewModel.startLoginController(this.loginContract)
            viewModel.startSignUpController(this)
        } else {
            loadRequiredInformation()
        }

        viewModel.smartlockCredentials.observe(this, Observer { credentials ->
            credentials?.let {
                viewModel.startLoginController(this.loginContract)
                BaseLoginActivity.tracker?.intent = TrackingData.UserIntent.LOGIN
            }
        })

        viewModel.signUpController.observe(this, Observer {
            if (!viewModel.isSmartlockResolving()) {
                signUpController = it?.peek()
                viewModel.startSignUpController(this)
                BaseLoginActivity.tracker?.intent = TrackingData.UserIntent.CREATE
            }
        })

        viewModel.loginController.observe(this, Observer {
            if (!viewModel.isSmartlockResolving()) {
                viewModel.startLoginController(this.loginContract)
                BaseLoginActivity.tracker?.intent = TrackingData.UserIntent.LOGIN
            }
        })
    }

    override fun onCredentialsRequested(provider: InputProvider<Credentials>) {
        if (viewModel.activityTitle.value == LoginScreen.ONE_STEP_SIGNUP_SCREEN) {
            viewModel.credentialsProvider.value = provider
        } else {
            viewModel.userIdentifier?.let { identifier ->
            val fragment = fragmentProvider.getOrCreatePasswordFragment(
                    provider = provider,
                    currentIdentifier = identifier,
                    userAvailable = viewModel.isUserAvailable(),
                    smartlockController = null)
            navigationController.navigateToFragment(fragment)
        } ?: loadRequiredInformation()
        }
    }

    override fun onAgreementsRequested(agreementsProvider: InputProvider<Agreements>, agreementLinks: AgreementLinksResponse) {
        val fragment = fragmentProvider.getOrCreateTermsFragment(
                provider = agreementsProvider,
                userAvailable = viewModel.isUserAvailable(),
                agreementLinks = agreementLinks)
        navigationController.navigateToFragment(fragment)
    }

    override fun onRequiredFieldsRequested(requiredFieldsProvider: InputProvider<RequiredFields>, fields: Set<String>) {
        val fragment = fragmentProvider.getOrCreateRequiredFieldsFragment(
                requiredFieldsProvider,
                fields)
        navigationController.navigateToFragment(fragment)
    }

    override fun onFlowReady(callbackProvider: CallbackProvider<Identifier>) {
        callbackProvider.provide(object : ResultCallback<Identifier> {
            override fun onSuccess(result: Identifier) {
                val fragment = fragmentProvider.getOrCreateInboxFragment(result)
                navigationController.navigateToFragment(fragment)
            }

            override fun onError(error: ClientError) {
                val currentFragment = navigationController.currentFragment
                currentFragment?.displayErrorDialog(error)

                if (currentFragment is FlowFragment<*>) {
                    currentFragment.hideProgress()
                }
                if (error.errorType == ClientError.ErrorType.NETWORK_ERROR) {
                    BaseLoginActivity.tracker?.eventError(TrackingData.UIError.NetworkError, null)
                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (viewModel.isUserAvailable()) {
            navigationController.handleBackPressed(signUpController, this)
        } else {
            navigationController.handleBackPressed(loginController, loginContract)
        }
    }

    override fun onNavigateBackRequested() {
        onBackPressed()
    }
}

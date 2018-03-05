/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.flow.passwordless

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.engine.controller.PasswordlessController
import com.schibsted.account.engine.input.Agreements
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.input.RequiredFields
import com.schibsted.account.engine.input.VerificationCode
import com.schibsted.account.engine.integration.CallbackProvider
import com.schibsted.account.engine.integration.ResultCallbackData
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.contract.PasswordlessContract
import com.schibsted.account.model.LoginResult
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.AgreementLinksResponse
import com.schibsted.account.ui.UiConfiguration
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.screen.identification.ui.AbstractIdentificationFragment
import com.schibsted.account.ui.login.screen.identification.ui.MobileIdentificationFragment
import com.schibsted.account.ui.navigation.Navigation
import com.schibsted.account.ui.ui.FlowFragment

/**
 * an Activity use as navigation controller for the UI login flow.
 * This activity manage the keyboard visibility.
 *
 * @see MobileIdentificationFragment
 */
class PasswordlessActivity : BaseLoginActivity(), PasswordlessContract {

    private lateinit var passwordlessController: PasswordlessController
    private lateinit var identifierType: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        passwordlessController = if (savedInstanceState?.getParcelable<Parcelable>(KEY_CONTROLLER) != null) {
            savedInstanceState.getParcelable(KEY_CONTROLLER)
        } else {
            PasswordlessController(true)
        }
        identifierType = super.uiConfiguration.identifierType.value
        navigationController = Navigation(this, this)
        passwordlessController.start(this)
    }

    override fun onIdentifierRequested(provider: InputProvider<Identifier>) {
        val fragment = fragmentProvider.getOrCreateIdentificationFragment(
                navigationController.currentFragment,
                provider,
                identifierType = identifierType)
        navigationController.navigateToFragment(fragment as AbstractIdentificationFragment)
    }

    override fun onVerificationCodeRequested(verificationCodeProvider: InputProvider<VerificationCode>, identifier: Identifier) {
        val fragment = fragmentProvider.getOrCreateVerificationScreen(navigationController.currentFragment, verificationCodeProvider, identifier, passwordlessController)
        navigationController.navigateToFragment(fragment)
    }

    override fun onAgreementsRequested(agreementsProvider: InputProvider<Agreements>, agreementLinks: AgreementLinksResponse) {
        val fragment = fragmentProvider.getOrCreateTermsFragment(navigationController.currentFragment, agreementsProvider, isUserAvailable(), agreementLinks)
        navigationController.navigateToFragment(fragment)
    }

    override fun onRequiredFieldsRequested(requiredFieldsProvider: InputProvider<RequiredFields>, fields: Set<String>) {
        val fragment = fragmentProvider.getOrCreateRequiredFieldsFragment(navigationController.currentFragment, requiredFieldsProvider, fields)
        navigationController.navigateToFragment(fragment)
    }

    override fun onFlowReady(callbackProvider: CallbackProvider<LoginResult>) {
        callbackProvider.provide(object : ResultCallbackData<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                navigationController.finishFlow(result.user)

                val event = if (result.isNewUser) TrackingData.SpidAction.ACCOUNT_CREATED else TrackingData.SpidAction.LOGIN_COMPLETED
                BaseLoginActivity.tracker?.eventActionSuccessful(event, result.user.userId.legacyId)
            }

            override fun onError(error: ClientError) {
                val fragment = navigationController.currentFragment
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

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_CONTROLLER, passwordlessController)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigationController.handleBackPressed(passwordlessController, this)
    }

    companion object {
        const val KEY_CONTROLLER = "CONTROLLER"
        /**
         * Provides an [Intent] that can be used to launch the visual authentication flow.
         *
         * @param context The context.
         * @param identityUiOptions The identityUiOptions for this [Activity].
         * @return An [Intent] that can be used to launch the visual authentication flow.
         */
        @JvmStatic
        fun getCallingIntent(context: Context, uiConfiguration: UiConfiguration): Intent =
                Intent(context, PasswordlessActivity::class.java).apply {
                    putExtra(KEY_UI_CONFIGURATION, uiConfiguration)
                }
    }
}

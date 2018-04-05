/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.identification

import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.input.Identifier.IdentifierType
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.AccountStatusResponse
import com.schibsted.account.ui.ErrorUtil
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.flow.password.FlowSelectionListener
import com.schibsted.account.ui.ui.InputField
import com.schibsted.account.ui.ui.component.PhoneInputView

/**
 * Following the MVP design pattern this interface represent the implementation of the [IdentificationContract.Presenter].
 * this class executes the mobile identification business logic and ask for UI updates depending on results.
 */
class IdentificationPresenter(
        private val identificationView: IdentificationContract.View,
        private val provider: InputProvider<Identifier>?,
        private val flowSelectionListener: FlowSelectionListener) : IdentificationContract.Presenter {

    init {
        identificationView.setPresenter(this)
    }

    private fun identifyUser(identifierType: Identifier.IdentifierType, input: String?, identifier: InputField) {
        provider?.provide(Identifier(identifierType, input!!), object : ResultCallback<NoValue> {
            override fun onSuccess(result: NoValue) {
                identificationView.clearField()
            }

            override fun onError(error: ClientError) {
                if (identificationView.isActive) {
                    if (ErrorUtil.isServerError(error.errorType)) {
                        identificationView.showErrorDialog(error, null)
                    } else {
                        identificationView.showError(identifier)
                    }
                    identificationView.hideProgress()
                    trackError(error)
                }
            }
        })
    }

    private fun getAccountStatus(identifierType: IdentifierType, input: String?, identifier: InputField, allowSignUp: Boolean, signUpErrorMessage: String) {
        val id = Identifier(identifierType, input!!)
        id.getAccountStatus(object : ResultCallback<AccountStatusResponse> {
            override fun onSuccess(result: AccountStatusResponse) {
                BaseLoginActivity.tracker?.let {
                    it.intent = if (result.isAvailable) TrackingData.UserIntent.CREATE else TrackingData.UserIntent.LOGIN
                }

                if (result.isAvailable && !allowSignUp) {
                    onError(ClientError(ClientError.ErrorType.SIGNUP_FORBIDDEN, "Signup is not allowed"))
                    return
                }

                if (provider == null) { // Having no provider means we have a password flow
                    identificationView.clearField()
                    val flowType = if (result.isAvailable) FlowSelectionListener.FlowType.SIGN_UP else FlowSelectionListener.FlowType.LOGIN
                    flowSelectionListener.onFlowSelected(flowType, id)
                } else { // Otherwise, passwordless
                    identifyUser(identifierType, input, identifier)
                }
            }

            override fun onError(error: ClientError) {
                if (identificationView.isActive) {
                    val isSignUpForbidden = error.errorType === ClientError.ErrorType.SIGNUP_FORBIDDEN
                    val showDialog = isSignUpForbidden || ErrorUtil.isServerError(error.errorType)
                    if (showDialog) {
                        if (isSignUpForbidden) {
                            identificationView.showErrorDialog(error, signUpErrorMessage)
                        } else {
                            identificationView.showErrorDialog(error, null)
                        }
                    } else {
                        identificationView.showError(identifier)
                    }
                    identificationView.hideProgress()
                }
            }
        })
    }

    /**
     * Verify the input of the user, the input could be a phone number or an email address.
     *
     *
     * This method request a navigation to the next screen if the call was successful or show an error
     * otherwise.
     *
     * @param identifier [InputField] representing the input
     * @see PhoneInputView.getInput
     */
    override fun verifyInput(identifier: InputField, identifierType: Identifier.IdentifierType, allowSignup: Boolean, signUpErrorMessage: String) {
        if (identificationView.isActive) {
            identificationView.hideError(identifier)
            if (identifier.isInputValid) {
                identificationView.showProgress()
                val input = identifier.input
                getAccountStatus(identifierType, input, identifier, allowSignup, signUpErrorMessage)

            } else {
                BaseLoginActivity.tracker?.let {
                    val event = if (identifierType === Identifier.IdentifierType.SMS) {
                        TrackingData.UIError.InvalidPhone
                    } else {
                        TrackingData.UIError.InvalidEmail
                    }
                    it.eventError(event, TrackingData.Screen.IDENTIFICATION)
                }
                identificationView.showError(identifier)
            }
        }
    }
    
    private fun trackError(error: ClientError) {
        BaseLoginActivity.tracker?.let {
            when {
                error.errorType === ClientError.ErrorType.NETWORK_ERROR -> it.eventError(TrackingData.UIError.NetworkError, TrackingData.Screen.IDENTIFICATION)
                error.errorType === ClientError.ErrorType.INVALID_EMAIL -> it.eventError(TrackingData.UIError.InvalidEmail, TrackingData.Screen.IDENTIFICATION)
                error.errorType === ClientError.ErrorType.INVALID_PHONE_NUMBER -> it.eventError(TrackingData.UIError.InvalidPhone, TrackingData.Screen.IDENTIFICATION)
                else -> { }
            }
        }
    }

}

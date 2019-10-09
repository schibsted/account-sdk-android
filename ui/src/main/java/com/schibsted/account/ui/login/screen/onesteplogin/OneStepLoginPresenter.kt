/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.onesteplogin

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.AccountStatusResponse
import com.schibsted.account.ui.ErrorUtil
import com.schibsted.account.ui.R
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.flow.password.FlowSelectionListener
import com.schibsted.account.ui.smartlock.SmartlockController
import com.schibsted.account.ui.ui.InputField
import com.schibsted.account.ui.ui.component.PhoneInputView

class OneStepLoginPresenter(
        private val view: OneStepLoginContract.View,
        private var credProvider: MutableLiveData<InputProvider<Credentials>>,
        private val idProvider: InputProvider<Identifier>?,
        private val smartlockController: SmartlockController?,
        private val flowSelectionListener: FlowSelectionListener?
) : OneStepLoginContract.Presenter {

    init {
        view.setPresenter(this)
    }

    internal lateinit var id: Identifier

    private fun identifyUser(identifier: InputField) {
        idProvider?.provide(id, object : ResultCallback<NoValue> {
            override fun onSuccess(result: NoValue) { }

            override fun onError(error: ClientError) {
                if (view.isActive) {
                    if (ErrorUtil.isServerError(error.errorType)) {
                        view.showErrorDialog(error, null)
                    } else {
                        view.showError(identifier)
                    }
                    view.hideProgress()
                    trackError(error)
                }
            }
        })
    }

    override fun getAccountStatus(input: InputField, allowSignUp: Boolean, signUpErrorMessage: String?) {
        id.getAccountStatus(object : ResultCallback<AccountStatusResponse> {
            override fun onSuccess(result: AccountStatusResponse) {
                BaseLoginActivity.tracker?.let {
                    it.intent = if (result.isAvailable) TrackingData.UserIntent.CREATE else TrackingData.UserIntent.LOGIN
                }

                if (result.isAvailable && !allowSignUp) {
                    onError(ClientError(ClientError.ErrorType.SIGNUP_FORBIDDEN, "Signup is not allowed"))
                    return
                }

                if (idProvider == null) { // Having no provider means we have a password flow
                    val flowType = if (result.isAvailable) FlowSelectionListener.FlowType.SIGN_UP else FlowSelectionListener.FlowType.LOGIN
                    flowSelectionListener?.onFlowSelected(flowType, id)
                } else { // Otherwise, passwordless
                    identifyUser(input)
                }
            }

            override fun onError(error: ClientError) {
                if (view.isActive) {
                    val isSignUpForbidden = error.errorType == ClientError.ErrorType.SIGNUP_FORBIDDEN
                    val showDialog = isSignUpForbidden || ErrorUtil.isServerError(error.errorType)
                    if (showDialog) {
                        if (isSignUpForbidden) {
                            view.showErrorDialog(error, signUpErrorMessage)
                        } else {
                            view.showErrorDialog(error, null)
                        }
                    } else {
                        view.showError(input)
                    }
                    // view.hideProgress()
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
    override fun verifyInput(identifier: InputField, identifierType: Identifier.IdentifierType, allowSignup: Boolean, signUpErrorMessage: String?) {
        if (view.isActive) {
            view.hideError(identifier)
            if (identifier.isInputValid) {
                //view.showProgress()
                identifier.input?.let {
                    id = Identifier(identifierType, it)
                    getAccountStatus(identifier, allowSignup, signUpErrorMessage)
                }
            } else {
                BaseLoginActivity.tracker?.let {
                    val event = if (identifierType == Identifier.IdentifierType.SMS) {
                        TrackingData.UIError.InvalidPhone
                    } else {
                        TrackingData.UIError.InvalidEmail
                    }
                    it.eventError(event, TrackingData.Screen.IDENTIFICATION)
                }
                view.showError(identifier)
            }
        }
    }

    private fun trackError(error: ClientError) {
        BaseLoginActivity.tracker?.let {
            when {
                error.errorType == ClientError.ErrorType.NETWORK_ERROR -> it.eventError(TrackingData.UIError.NetworkError, TrackingData.Screen.IDENTIFICATION)
                error.errorType == ClientError.ErrorType.INVALID_EMAIL -> it.eventError(TrackingData.UIError.InvalidEmail, TrackingData.Screen.IDENTIFICATION)
                error.errorType == ClientError.ErrorType.INVALID_PHONE_NUMBER -> it.eventError(TrackingData.UIError.InvalidPhone, TrackingData.Screen.IDENTIFICATION)
                else -> {
                }
            }
        }
    }

    override fun sign(inputField: InputField, keepUserLoggedIn: Boolean, lifecycleOwner: LifecycleOwner) {
        view.hideError(inputField)
        view.showProgress()
        requireNotNull(id) { "Identifier can't be null at this stage" }
        if (inputField.isInputValid) {
            credProvider.observe(lifecycleOwner, Observer { provider ->
                credProvider.value?.provide(Credentials(id!!, inputField.input!!, keepUserLoggedIn), object : ResultCallback<NoValue> {
                    override fun onSuccess(result: NoValue) {
                        smartlockController?.saveCredential(id.identifier, inputField.input!!)
                    }

                    override fun onError(error: ClientError) {
                        when {
                            ErrorUtil.isServerError(error.errorType) -> view.showErrorDialog(error)
                            error.errorType == ClientError.ErrorType.INVALID_USER_CREDENTIALS -> {
                                view.showError(inputField, R.string.schacc_password_error_incorrect)
                                BaseLoginActivity.tracker?.eventError(TrackingData.UIError.InvalidCredentials, TrackingData.Screen.PASSWORD)
                            }
                            else -> {
                                showPasswordLengthError(inputField)
                            }
                        }
                        view.hideProgress()
                    }
                })
            })

        } else {
            showPasswordLengthError(inputField)
            view.hideProgress()
        }
    }

    private fun showPasswordLengthError(inputField: InputField) {
        view.showError(inputField, R.string.schacc_password_error_length)
        BaseLoginActivity.tracker?.eventError(TrackingData.UIError.InvalidPassword, TrackingData.Screen.PASSWORD)
    }
}

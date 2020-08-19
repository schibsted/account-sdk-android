/*
 * Copyright (c) 2019 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.onesteplogin

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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
import com.schibsted.account.util.KeyValueStore

class OneStepLoginPresenter(
    private val view: OneStepLoginContract.View,
    private var credProvider: MutableLiveData<InputProvider<Credentials>>,
    private val smartlockController: SmartlockController?,
    private val flowSelectionListener: FlowSelectionListener?
) : OneStepLoginContract.Presenter {

    init {
        view.setPresenter(this)
    }

    internal var id: Identifier? = null

    private var isOneStepSignUp: Boolean = false

    private val trackingScreen: TrackingData.Screen
        get() = if (isOneStepSignUp) TrackingData.Screen.ONE_STEP_SIGNUP else TrackingData.Screen.ONE_STEP_LOGIN

    override fun getAccountStatus(
        input: InputField,
        allowSignUp: Boolean,
        signUpErrorMessage: String?,
        callback: () -> Unit
    ) {
        id?.getAccountStatus(object : ResultCallback<AccountStatusResponse> {
            override fun onSuccess(result: AccountStatusResponse) {
                BaseLoginActivity.tracker?.let {
                    it.intent = if (result.isAvailable) TrackingData.UserIntent.CREATE else TrackingData.UserIntent.LOGIN
                }

                if (result.isAvailable && !allowSignUp) {
                    onError(ClientError(ClientError.ErrorType.SIGNUP_FORBIDDEN, "Signup is not allowed"))
                    return
                }

                val flowType = when {
                    result.isAvailable && !isOneStepSignUp -> FlowSelectionListener.FlowType.SIGN_UP
                    result.isAvailable && isOneStepSignUp -> FlowSelectionListener.FlowType.ONE_STEP_SIGNUP
                    !result.isAvailable && isOneStepSignUp -> {
                        showEmailExistsError(input)
                        view.hideProgress()
                        return
                    }
                    else -> FlowSelectionListener.FlowType.ONE_STEP_LOGIN
                }
                flowSelectionListener?.onFlowSelected(flowType, id!!)
                when (flowType) {
                    FlowSelectionListener.FlowType.ONE_STEP_SIGNUP,
                    FlowSelectionListener.FlowType.ONE_STEP_LOGIN
                    -> callback()
                    else -> {}
                }
            }

            override fun onError(error: ClientError) {
                if (view.isActive) {
                    val isSignUpForbidden = error.errorType == ClientError.ErrorType.SIGNUP_FORBIDDEN
                    val showDialog = isSignUpForbidden || ErrorUtil.isServerError(error.errorType)
                    if (showDialog) {
                        view.showErrorDialog(error, signUpErrorMessage)
                    } else {
                        view.showError(input)
                    }
                }
            }
        })
    }

    /**
     * Verify the input of the user, the input could be an email address.
     *
     *
     * This method request a navigation to the next screen if the call was successful or show an error
     * otherwise.
     *
     * @param identifier [InputField] representing the input
     */
    override fun verifyInput(
        identifier: InputField,
        identifierType: Identifier.IdentifierType,
        allowSignup: Boolean,
        signUpErrorMessage: String?,
        callback: () -> Unit
    ) {
        if (view.isActive) {
            view.hideError(identifier)
            if (identifier.isInputValid) {
                identifier.input?.let {
                    id = Identifier(identifierType, it)
                    getAccountStatus(identifier, allowSignup, signUpErrorMessage, callback)
                }
            } else {
                BaseLoginActivity.tracker?.eventError(TrackingData.UIError.InvalidEmail, trackingScreen)
                view.showError(identifier)
            }
        }
    }

    override fun signIn(
        identifier: InputField,
        credentials: InputField,
        keepUserLoggedIn: Boolean,
        lifecycleOwner: LifecycleOwner,
        keyValueStore: KeyValueStore?
    ) {

        view.hideError(credentials)
        view.showProgress()
        if (id == null) {
            view.showError(identifier)
            view.hideProgress()
            return
        }

        if (credentials.isInputValid) {
            credProvider.observe(lifecycleOwner, Observer {
                credProvider.value?.provide(Credentials(id!!, credentials.input!!, keepUserLoggedIn), object : ResultCallback<NoValue> {
                    override fun onSuccess(result: NoValue) {
                        smartlockController?.saveCredential(id!!.identifier, credentials.input!!)
                        if (keepUserLoggedIn) {
                            keyValueStore?.writeEmailPrefillValue(id!!.identifier)
                        } else {
                            keyValueStore?.clearEmailPrefillValue()
                        }
                    }

                    override fun onError(error: ClientError) {
                        when {
                            ErrorUtil.isServerError(error.errorType) -> view.showErrorDialog(error)
                            error.errorType == ClientError.ErrorType.INVALID_USER_CREDENTIALS -> {
                                view.showError(credentials, R.string.schacc_password_error_incorrect)
                                BaseLoginActivity.tracker?.eventError(TrackingData.UIError.InvalidCredentials, trackingScreen)
                            }
                            else -> {
                                showPasswordLengthError(credentials)
                            }
                        }
                        view.hideProgress()
                    }
                })
            })
        } else {
            showPasswordLengthError(credentials)
            view.hideProgress()
        }
    }

    override fun signup(identifier: InputField, credInputField: InputField, keepUserLoggedIn: Boolean, lifecycleOwner: LifecycleOwner) {
        view.hideError(credInputField)
        view.showProgress()

        if (id == null) {
            view.showError(identifier)
            view.hideProgress()
        } else {
            if (credInputField.isInputValid) {
                credProvider.observe(lifecycleOwner, Observer {
                    credProvider.value?.provide(Credentials(id!!, credInputField.input!!, keepUserLoggedIn), object : ResultCallback<NoValue> {
                        override fun onSuccess(result: NoValue) {}

                        override fun onError(error: ClientError) {
                            when {
                                ErrorUtil.isServerError(error.errorType) -> view.showErrorDialog(error)
                                else -> {
                                    showPasswordLengthError(credInputField)
                                }
                            }
                            view.hideProgress()
                        }
                    })
                })
            } else {
                showPasswordLengthError(credInputField)
                view.hideProgress()
            }
        }
    }

    override fun startSignin() {
        view.hideProgress()
        isOneStepSignUp = false
        flowSelectionListener?.onFlowSelected(FlowSelectionListener.FlowType.ONE_STEP_LOGIN)
    }

    override fun startSignup() {
        view.hideProgress()
        isOneStepSignUp = true
        flowSelectionListener?.onFlowSelected(FlowSelectionListener.FlowType.ONE_STEP_SIGNUP)
    }

    private fun showPasswordLengthError(inputField: InputField) {
        view.showError(inputField, R.string.schacc_password_error_length)
        BaseLoginActivity.tracker?.eventError(TrackingData.UIError.InvalidPassword, TrackingData.Screen.ONE_STEP_LOGIN)
    }

    private fun showEmailExistsError(inputField: InputField) {
        view.showError(inputField, R.string.schacc_email_already_in_use_error)
        BaseLoginActivity.tracker?.eventError(TrackingData.UIError.AlreadyInUseEmail, TrackingData.Screen.ONE_STEP_SIGNUP)
    }
}

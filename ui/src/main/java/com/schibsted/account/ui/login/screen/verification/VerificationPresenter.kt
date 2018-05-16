/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.verification

import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.engine.controller.PasswordlessController
import com.schibsted.account.engine.input.VerificationCode
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.ErrorUtil
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.ui.ErrorField
import com.schibsted.account.ui.ui.component.CodeInputView

/**
 * Following the MVP design pattern this interface represent the implementation of the [VerificationContract.Presenter].
 * this class executes the code verification business logic and ask for UI updates depending on results.
 */
class VerificationPresenter(
    private val verificationView: VerificationContract.View,
    provider: InputProvider<VerificationCode>
) : VerificationContract.Presenter {

    private val provider: InputProvider<VerificationCode>

    init {
        verificationView.setPresenter(this)
        this.provider = provider
    }

    /**
     * Used to resend a code verification to the identifier.
     * It will ask the view to show an error if any failure or to
     * show a pop-up in case of success
     */
    override fun resendCode(passwordlessController: PasswordlessController) {
        passwordlessController.resendCode(object : ResultCallback<NoValue> {
            override fun onSuccess(result: NoValue) {
                if (verificationView.isActive) {
                    verificationView.showResendCodeView()
                }
            }

            override fun onError(error: ClientError) {
                if (verificationView.isActive) {
                    verificationView.showErrorDialog(error, null)

                    val tracker = BaseLoginActivity.tracker
                    if (tracker != null && error.errorType == ClientError.ErrorType.NETWORK_ERROR) {
                        tracker.eventError(TrackingData.UIError.NetworkError, TrackingData.Screen.VERIFICATION_CODE)
                    }
                }
            }
        })
    }

    /**
     * Used to verify if the code provided by the user is valid
     * If there is an agreement update a navigation to [com.schibsted.account.ui.login.screen.term.TermsFragment]
     * will be asked.
     * If the verification is successful a navigation to [com.schibsted.account.ui.login.screen.information.RequiredFieldsFragment]
     * will be asked.
     * If there is a failure  the view will be requested to show an error
     *
     * @param codeInputView the provided code
     */
    override fun verifyCode(codeInputView: CodeInputView, keepMeLoggedIn: Boolean) {
        if (verificationView.isActive) {
            verificationView.hideError(codeInputView)
            if (codeInputView.isInputValid) {
                verificationView.showProgress()

                provider.provide(VerificationCode(codeInputView.input!!, keepMeLoggedIn), object : ResultCallback<NoValue> {
                    override fun onSuccess(result: NoValue) {}

                    override fun onError(error: ClientError) {
                        showError(error, codeInputView)

                        BaseLoginActivity.tracker?.eventError(TrackingData.UIError.InvalidVerificationCode, TrackingData.Screen.VERIFICATION_CODE)
                    }
                })
            } else {
                verificationView.showError(codeInputView)
            }
        }
    }

    private fun showError(error: ClientError, errorField: ErrorField) {
        if (verificationView.isActive) {
            if (ErrorUtil.isServerError(error.errorType)) {
                verificationView.showErrorDialog(error, null)
            } else {
                verificationView.showError(errorField)
            }
            verificationView.hideProgress()
        }
    }
}

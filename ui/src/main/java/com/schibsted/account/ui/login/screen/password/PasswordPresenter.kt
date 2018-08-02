/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.password

import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.ErrorUtil
import com.schibsted.account.ui.R
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.smartlock.SmartlockImpl
import com.schibsted.account.ui.ui.InputField

class PasswordPresenter(private val view: PasswordContract.View, private var provider: InputProvider<Credentials>, private val smartlockImpl: SmartlockImpl?) : PasswordContract.Presenter {

    init {
        view.setPresenter(this)
    }

    override fun sign(inputField: InputField, identifier: Identifier?, keepUserLoggedIn: Boolean) {
        view.hideError(inputField)
        view.showProgress()
        requireNotNull(identifier) { "Identifier can't be null at this stage" }
        if (inputField.isInputValid) {
            provider.provide(Credentials(identifier!!, inputField.input!!, keepUserLoggedIn), object : ResultCallback<NoValue> {
                override fun onSuccess(result: NoValue) {
                    smartlockImpl?.saveCredential(identifier.identifier, inputField.input!!)
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

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.information

import com.schibsted.account.engine.input.RequiredFields
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.ErrorUtil
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.ui.ui.InputField

class RequiredFieldsPresenter(val view: RequiredFieldsContract.View, private val provider: InputProvider<RequiredFields>) : RequiredFieldsContract.Presenter {

    init {
        view.setPresenter(this)
    }

    override fun updateMissingFields(fields: Map<String, InputField>) {
        if (view.isActive) {
            if (fields.all { it.value.isInputValid }) {
                view.hideErrors()
                view.showProgress()
                provider.provide(RequiredFields(fields.mapValues { it.value.input!! }), object : ResultCallback {
                    override fun onSuccess() {
                        BaseLoginActivity.tracker?.eventActionSuccessful(TrackingData.SpidAction.REQUIRED_FIELDS_PROVIDED)
                    }

                    override fun onError(error: ClientError) {
                        if (view.isActive) {
                            if (ErrorUtil.isServerError(error.errorType)) {
                                view.showErrorDialog(error)
                                BaseLoginActivity.tracker?.eventError(TrackingData.UIError.NetworkError, TrackingData.Screen.REQUIRED_FIELDS)
                            }
                            view.hideProgress()
                        }

                        if (error.errorType == ClientError.ErrorType.MISSING_FIELDS) {
                            BaseLoginActivity.tracker?.eventError(TrackingData.UIError.MissingRequiredField(error.message), TrackingData.Screen.REQUIRED_FIELDS)
                        }
                    }
                })
            } else {
                fields.filterNot { it.value.isInputValid }.forEach {
                    view.showError(it.value)
                    BaseLoginActivity.tracker?.eventError(TrackingData.UIError.InvalidRequiredField(it.key), TrackingData.Screen.REQUIRED_FIELDS)
                }
                view.hideProgress()
            }
        }
    }
}

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.term

import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.engine.input.Agreements
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.ui.component.CheckBoxView

/**
 * Following the MVP design pattern this interface represent the implementation of the [TermsContract.Presenter].
 * this class executes the terms and condition business logic and ask for UI updates depending on results.
 */
class TermsPresenter(private val termsView: TermsContract.View, private val provider: InputProvider<Agreements>) : TermsContract.Presenter {

    init {
        termsView.setPresenter(this)
    }

    /**
     * Verify if given checkboxes are all checked.
     * Calls [.acceptAgreements]  if `true`
     * Shows errors if `false`
     *
     * @param privacyBox [CheckBoxView]  the privacy checkbox
     * @param termsBox [CheckBoxView]  the terms checkbox
     */
    override fun verifyBoxes(privacyBox: CheckBoxView, termsBox: CheckBoxView) {
        if (termsView.isActive) {
            if (privacyBox.isChecked && termsBox.isChecked) {
                acceptAgreements()
            } else {
                val tracker = BaseLoginActivity.tracker
                tracker?.eventError(TrackingData.UIError.AgreementsNotAccepted, TrackingData.Screen.AGREEMENTS)

                if (!privacyBox.isChecked) {
                    termsView.showError(privacyBox)
                }

                if (!termsBox.isChecked) {
                    termsView.showError(termsBox)
                }
            }
        }
    }

    /**
     * Accepts TC on backend side.
     * Order a navigation to an other screen if request succeeded, show an error otherwise
     */
    private fun acceptAgreements() {
        termsView.showProgress()
        provider.provide(Agreements(true), object : ResultCallback<NoValue> {
            override fun onSuccess(result: NoValue) {
                val tracker = BaseLoginActivity.tracker
                tracker?.eventActionSuccessful(TrackingData.SpidAction.AGREEMENTS_ACCEPTED)
            }

            override fun onError(error: ClientError) {
                if (termsView.isActive) {
                    termsView.hideProgress()

                    val tracker = BaseLoginActivity.tracker
                    if (tracker != null && error.errorType === ClientError.ErrorType.NETWORK_ERROR) {
                        tracker.eventError(TrackingData.UIError.NetworkError, TrackingData.Screen.AGREEMENTS)
                    }
                }
            }
        })
    }
}

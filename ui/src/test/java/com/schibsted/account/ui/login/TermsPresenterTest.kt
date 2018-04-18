package com.schibsted.account.ui.login

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.input.Agreements
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.login.screen.term.TermsContract
import com.schibsted.account.ui.login.screen.term.TermsPresenter
import com.schibsted.account.ui.ui.component.CheckBoxView
import io.kotlintest.specs.WordSpec

class TermsPresenterTest : WordSpec() {

    init {
        Logger.loggingEnabled = false
        BaseLoginActivity.tracker = mock()
        val provider: InputProvider<Agreements> = mock()
        val view: TermsContract.View = mock { on { isActive } doReturn true }
        val presenter = TermsPresenter(view, provider)

        "initialization" should {
            " assign the presenter to the view" {
                verify(view).setPresenter(presenter)
            }
        }

        "verify checkboxes state" should {
            var privacyBox: CheckBoxView = mock { on { isChecked } doReturn false }
            var termsBox: CheckBoxView = mock { on { isChecked } doReturn false }
            "show error if boxes aren't checked" {
                presenter.verifyBoxes(privacyBox, termsBox)
                verify(view).showError(privacyBox)
                verify(view).showError(termsBox)
            }

            "show terms error if terms box isn't checked" {
                privacyBox = mock { on { isChecked } doReturn true }
                presenter.verifyBoxes(privacyBox, termsBox)
                verify(view, never()).showError(privacyBox)
                verify(view).showError(termsBox)
            }

            "show privacy error if privacy box isn't checked" {
                termsBox = mock { on { isChecked } doReturn true }
                presenter.verifyBoxes(privacyBox, termsBox)
                verify(view, never()).showError(termsBox)
                verify(view).showError(privacyBox)
            }
            "track error if boxes aren't checked" {
                presenter.verifyBoxes(privacyBox, termsBox)
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.AgreementsNotAccepted, TrackingData.Screen.AGREEMENTS)
            }

            "show progress if boxes are checked" {
                termsBox = mock { on { isChecked } doReturn true }
                privacyBox = mock { on { isChecked } doReturn true }
                presenter.verifyBoxes(privacyBox, termsBox)
                verify(view).showProgress()
            }

            "track event if terms was accepted" {
                termsBox = mock { on { isChecked } doReturn true }
                privacyBox = mock { on { isChecked } doReturn true }
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onSuccess(mock())
                }

                presenter.verifyBoxes(privacyBox, termsBox)
                verify(BaseLoginActivity.tracker)?.eventActionSuccessful(TrackingData.SpidAction.AGREEMENTS_ACCEPTED)
            }

            "hide progress if terms acceptance failed and track failure" {
                termsBox = mock { on { isChecked } doReturn true }
                privacyBox = mock { on { isChecked } doReturn true }

                val error = ClientError(ClientError.ErrorType.NETWORK_ERROR, "")
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }

                presenter.verifyBoxes(privacyBox, termsBox)
                verify(view).hideProgress()
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.NetworkError, TrackingData.Screen.AGREEMENTS)
            }
        }
    }
}

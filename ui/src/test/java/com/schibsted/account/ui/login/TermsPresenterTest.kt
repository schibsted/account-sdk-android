package com.schibsted.account.ui.login

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.schibsted.account.ClientConfiguration
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
        val testConfig = ClientConfiguration("https://dev-example.com/", "myId", "mySecret")
        ClientConfiguration.set(testConfig)
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
            var termsBox: CheckBoxView = mock { on { isChecked } doReturn false }

            "show terms error if terms box isn't checked" {
                presenter.acceptTerms(termsBox)
                verify(view).showError(termsBox)
            }

            "track error if boxes aren't checked" {
                presenter.acceptTerms(termsBox)
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.AgreementsNotAccepted, TrackingData.Screen.AGREEMENTS)
            }

            "show progress if boxes are checked" {
                termsBox = mock { on { isChecked } doReturn true }
                presenter.acceptTerms(termsBox)
                verify(view).showProgress()
            }

            "hide progress if terms acceptance failed and track failure" {
                termsBox = mock { on { isChecked } doReturn true }

                val error = ClientError(ClientError.ErrorType.NETWORK_ERROR, "")
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }

                presenter.acceptTerms(termsBox)
                verify(view).hideProgress()
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.NetworkError, TrackingData.Screen.AGREEMENTS)
            }
        }
    }
}

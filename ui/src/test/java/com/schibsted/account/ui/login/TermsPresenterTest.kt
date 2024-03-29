package com.schibsted.account.ui.login

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
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

    private fun viewMock(): TermsContract.View {
        return mock { on { isActive } doReturn true }
    }

    private fun termsPresenter(
        view: TermsContract.View = viewMock(),
        provider: InputProvider<Agreements> = mock()
    ): TermsPresenter {
        return TermsPresenter(view, provider)
    }

    init {
        val testConfig = ClientConfiguration("https://dev-example.com/", "myId", "mySecret")
        ClientConfiguration.set(testConfig)
        Logger.loggingEnabled = false

        "initialization" should {
            "assign the presenter to the view" {
                val view = viewMock()
                val presenter = termsPresenter(view)
                verify(view).setPresenter(presenter)
            }
        }

        "verify checkboxes state" should {
            var termsBox: CheckBoxView = mock { on { isChecked } doReturn false }

            "show terms error if terms box isn't checked" {
                val view = viewMock()
                val presenter = termsPresenter(view)
                presenter.acceptTerms(termsBox)
                verify(view).showError(termsBox)
            }

            "track error if boxes aren't checked" {
                BaseLoginActivity.tracker = mock()
                val presenter = termsPresenter()
                presenter.acceptTerms(termsBox)
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.AgreementsNotAccepted, TrackingData.Screen.AGREEMENTS)
            }

            "show progress if boxes are checked" {
                val view = viewMock()
                val presenter = termsPresenter(view)
                termsBox = mock { on { isChecked } doReturn true }
                presenter.acceptTerms(termsBox)
                verify(view).showProgress()
            }

            "hide progress if terms acceptance failed and track failure" {
                BaseLoginActivity.tracker = mock()
                termsBox = mock { on { isChecked } doReturn true }

                val provider: InputProvider<Agreements> = mock()
                val error = ClientError(ClientError.ErrorType.NETWORK_ERROR, "")
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }

                val view = viewMock()
                val presenter = termsPresenter(view, provider)
                presenter.acceptTerms(termsBox)
                verify(view).hideProgress()
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.NetworkError, TrackingData.Screen.AGREEMENTS)
            }
        }
    }
}

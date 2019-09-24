package com.schibsted.account.ui.login

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.schibsted.account.ClientConfiguration
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.controller.PasswordlessController
import com.schibsted.account.engine.input.VerificationCode
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.login.screen.verification.VerificationContract
import com.schibsted.account.ui.login.screen.verification.VerificationPresenter
import com.schibsted.account.ui.ui.component.CodeInputView
import io.kotlintest.specs.WordSpec

class VerificationPresenterTest : WordSpec() {
    private fun viewMock(): VerificationContract.View {
        return mock { on { isActive } doReturn true }
    }

    private fun verificationPresenter(
            view: VerificationContract.View = viewMock(),
            provider: InputProvider<VerificationCode> = mock()
    ): VerificationPresenter {
        return VerificationPresenter(view, provider)
    }

    init {

        Logger.loggingEnabled = false
        val testConfig = ClientConfiguration("https://dev-example.com/", "myId", "mySecret")
        ClientConfiguration.set(testConfig)
        "initialization" should {
            "assign the presenter to the view" {
                val view = viewMock()
                val presenter = verificationPresenter(view)
                verify(view).setPresenter(presenter)
            }
        }

        "success to resend code" should {
            val controller: PasswordlessController = mock()
            whenever(controller.resendCode(any())).thenAnswer {
                (it.getArgument(0) as ResultCallback<NoValue>).onSuccess(mock())
            }
            "show the resend code view" {
                val view = viewMock()
                val presenter = verificationPresenter(view)
                presenter.resendCode(controller)
                verify(view).showResendCodeView()
            }
        }

        "failed to resend code" should {
            val controller: PasswordlessController = mock()
            var error: ClientError = mock()
            whenever(controller.resendCode(any())).thenAnswer {
                (it.getArgument(0) as ResultCallback<NoValue>).onError(error)
            }

            "show the resend code view" {
                val view = viewMock()
                val presenter = verificationPresenter(view)
                presenter.resendCode(controller)
                verify(view).showErrorDialog(error, null)
            }

            "track the error only if it's a server error" {
                error = ClientError(ClientError.ErrorType.NETWORK_ERROR, "")
                BaseLoginActivity.tracker = mock()

                val presenter = verificationPresenter()
                presenter.resendCode(controller)

                error = ClientError(ClientError.ErrorType.INVALID_EMAIL, "")

                presenter.resendCode(controller)

                verify(BaseLoginActivity.tracker, times(1))?.eventError(TrackingData.UIError.NetworkError, TrackingData.Screen.VERIFICATION_CODE)
            }
        }

        "verify code" should {

            "hide previously shown error" {
                val view = viewMock()
                val presenter = verificationPresenter(view)
                presenter.verifyCode(mock(), true)
                verify(view).hideError(any())
            }

            "show error if input isn't valid" {
                val view = viewMock()
                val presenter = verificationPresenter(view)
                val code: CodeInputView = mock { on { isInputValid } doReturn false }
                presenter.verifyCode(code, true)
                verify(view).showError(any())
            }

            "show progress if input is valid" {
                val view = viewMock()
                val presenter = verificationPresenter(view)
                val code: CodeInputView = mock {
                    on { isInputValid } doReturn true
                    on { input } doReturn "12345"
                }
                presenter.verifyCode(code, true)
                verify(view).showProgress()
            }

            "track the error if any" {
                val code: CodeInputView = mock {
                    on { isInputValid } doReturn true
                    on { input } doReturn "12345"
                }
                BaseLoginActivity.tracker = mock()

                val provider: InputProvider<VerificationCode> = mock()
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(mock { on { errorType } doReturn ClientError.ErrorType.NETWORK_ERROR })
                }

                val presenter = verificationPresenter(provider = provider)
                presenter.verifyCode(code, true)
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.InvalidVerificationCode, TrackingData.Screen.VERIFICATION_CODE)
            }

            "show error if providing code failed" {
                val code: CodeInputView = mock {
                    on { isInputValid } doReturn true
                    on { input } doReturn "12345"
                }
                BaseLoginActivity.tracker = mock()
                var error: ClientError = mock { on { errorType } doReturn ClientError.ErrorType.NETWORK_ERROR }
                val provider: InputProvider<VerificationCode> = mock()
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }

                val view = viewMock()
                val presenter = verificationPresenter(view, provider)
                presenter.verifyCode(code, true)
                verify(view).showErrorDialog(error)

                error = mock { on { errorType } doReturn ClientError.ErrorType.INVALID_CODE }

                presenter.verifyCode(code, true)
                verify(view).showError(any())
            }

            "hide progress if there's an error" {
                val code: CodeInputView = mock {
                    on { isInputValid } doReturn true
                    on { input } doReturn "12345"
                }
                BaseLoginActivity.tracker = mock()
                val error: ClientError = mock { on { errorType } doReturn ClientError.ErrorType.NETWORK_ERROR }
                val provider: InputProvider<VerificationCode> = mock()
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }

                val view = viewMock()
                val presenter = verificationPresenter(view, provider)
                presenter.verifyCode(code, true)
                verify(view).hideProgress()
            }
        }
    }
}

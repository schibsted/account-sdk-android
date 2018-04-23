package com.schibsted.account.ui.login

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
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
    init {

        Logger.loggingEnabled = false

        "initialization" should {
            val view: VerificationContract.View = mock()
            val presenter = VerificationPresenter(view, mock())
            " assign the presenter to the view" {
                verify(view).setPresenter(presenter)
            }
        }

        "success to resend code" should {
            val view: VerificationContract.View = mock()
            val presenter = VerificationPresenter(view, mock())
            val controller: PasswordlessController = mock()
            whenever(view.isActive).thenReturn(true)
            whenever(controller.resendCode(any())).thenAnswer {
                (it.getArgument(0) as ResultCallback<NoValue>).onSuccess(mock())
            }
            "show the resend code view" {
                presenter.resendCode(controller)
                verify(view).showResendCodeView()
            }

            "track the action" {
                BaseLoginActivity.tracker = mock()
                presenter.resendCode(controller)
                verify(BaseLoginActivity.tracker)?.eventActionSuccessful(TrackingData.SpidAction.VERIFICATION_CODE_SENT)
            }
        }

        "failed to resend code" should {
            val view: VerificationContract.View = mock()
            val presenter = VerificationPresenter(view, mock())
            val controller: PasswordlessController = mock()
            var error: ClientError = mock()
            whenever(view.isActive).thenReturn(true)
            whenever(controller.resendCode(any())).thenAnswer {
                (it.getArgument(0) as ResultCallback<NoValue>).onError(error)
            }

            "show the resend code view" {
                presenter.resendCode(controller)
                verify(view).showErrorDialog(error, null)
            }

            "track the error only if it's a server error" {
                error = ClientError(ClientError.ErrorType.NETWORK_ERROR, "")
                BaseLoginActivity.tracker = mock()
                presenter.resendCode(controller)

                error = ClientError(ClientError.ErrorType.INVALID_EMAIL, "")

                presenter.resendCode(controller)

                verify(BaseLoginActivity.tracker, times(1))?.eventError(TrackingData.UIError.NetworkError, TrackingData.Screen.VERIFICATION_CODE)
            }
        }

        "verify code" should {
            val view: VerificationContract.View = mock()
            whenever(view.isActive).thenReturn(true)
            val provider: InputProvider<VerificationCode> = mock()
            val presenter = VerificationPresenter(view, provider)

            "hide previously shown error" {
                presenter.verifyCode(mock(), true)
                verify(view).hideError(any())
            }

            "show error if input isn't valid" {
                val code: CodeInputView = mock { on { isInputValid } doReturn false }
                presenter.verifyCode(code, true)
                verify(view).showError(any())
            }

            "show progress if input is valid" {
                val code: CodeInputView = mock {
                    on { isInputValid } doReturn true
                    on { input } doReturn "12345"
                }
                presenter.verifyCode(code, true)
                verify(view).showProgress()
            }

            "track the result" {
                val code: CodeInputView = mock {
                    on { isInputValid } doReturn true
                    on { input } doReturn "12345"
                }
                BaseLoginActivity.tracker = mock()
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onSuccess(mock())
                }
                presenter.verifyCode(code, true)
                verify(BaseLoginActivity.tracker)?.eventActionSuccessful(TrackingData.SpidAction.VERIFICATION_CODE_SENT)

                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(mock { on { errorType } doReturn ClientError.ErrorType.NETWORK_ERROR })
                }

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
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }

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
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }

                presenter.verifyCode(code, true)
                verify(view).hideProgress()
            }
        }
    }
}

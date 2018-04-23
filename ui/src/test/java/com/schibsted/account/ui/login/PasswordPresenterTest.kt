package com.schibsted.account.ui.login

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.R
import com.schibsted.account.ui.login.screen.password.PasswordContract
import com.schibsted.account.ui.login.screen.password.PasswordPresenter
import com.schibsted.account.ui.smartlock.SmartlockImpl
import com.schibsted.account.ui.ui.InputField
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.WordSpec

class PasswordPresenterTest : WordSpec() {

    init {
        Logger.loggingEnabled = false
        BaseLoginActivity.tracker = mock()

        "sign" should {
            val provider: InputProvider<Credentials> = mock()
            val smartlockImpl: SmartlockImpl = mock()
            val view: PasswordContract.View = mock { on { isActive } doReturn true }
            val presenter = PasswordPresenter(view, provider, smartlockImpl)
            val identifier = Identifier(Identifier.IdentifierType.EMAIL, "id")
            val input: InputField = mock {
                on { isInputValid } doReturn true
                on { input } doReturn "password"
            }

            "hide previously shown error" {
                presenter.sign(mock(), mock(), true)
                verify(view).hideError(any())
            }

            "show progress" {
                presenter.sign(mock(), mock(), true)
                verify(view).showProgress()
            }

            "throw an exception if identifier is null" {
                shouldThrow<IllegalArgumentException> {
                    presenter.sign(mock(), null, true)
                }
            }

            "save credentials with smartlock if password was successfully provided" {
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onSuccess(mock())
                }

                presenter.sign(input, identifier, true)
                verify(smartlockImpl).saveCredential("id", "password")
            }

            "show dialog error if there's a network error" {
                val error = ClientError(ClientError.ErrorType.NETWORK_ERROR, "")

                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }
                presenter.sign(input, identifier, true)
                verify(view).showErrorDialog(eq(error), anyOrNull())
            }

            "show credentials error if credentials are wrong" {
                val error = ClientError(ClientError.ErrorType.INVALID_USER_CREDENTIALS, "")

                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }
                presenter.sign(input, identifier, true)
                verify(view).showError(eq(input), eq(R.string.schacc_password_error_incorrect))
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.InvalidCredentials, TrackingData.Screen.PASSWORD)
            }

            "show password error if length is wrong" {
                val error = ClientError(ClientError.ErrorType.INVALID_INPUT, "")

                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }
                presenter.sign(input, identifier, true)
                verify(view).showError(eq(input), eq(R.string.schacc_password_error_length))
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.InvalidPassword, TrackingData.Screen.PASSWORD)
            }

            "hide progress if there's an error" {
                val error = ClientError(ClientError.ErrorType.INVALID_INPUT, "")

                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }
                presenter.sign(input, identifier, true)
                verify(view).hideProgress()
            }

            "show password error if input is not valid and track it" {
                whenever(input.isInputValid).thenReturn(false)
                presenter.sign(input, identifier, true)
                verify(view).showError(eq(input), eq(R.string.schacc_password_error_length))
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.InvalidPassword, TrackingData.Screen.PASSWORD)
            }

            "hide progress if input isn't valid" {
                whenever(input.isInputValid).thenReturn(false)
                presenter.sign(input, identifier, true)
                verify(view).hideProgress()
            }
        }
    }
}

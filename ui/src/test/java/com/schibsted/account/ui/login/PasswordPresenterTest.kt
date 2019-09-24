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
import com.schibsted.account.ui.smartlock.SmartlockController
import com.schibsted.account.ui.ui.InputField
import io.kotlintest.shouldThrow
import io.kotlintest.specs.WordSpec

class PasswordPresenterTest : WordSpec() {
    private fun viewMock(isActive: Boolean = true): PasswordContract.View {
        return mock { on { it.isActive } doReturn isActive }
    }

    private fun passwordPresenter(view: PasswordContract.View = viewMock(),
                                  provider: InputProvider<Credentials> = mock())
            : PasswordPresenter {
        return PasswordPresenter(view, provider, null)
    }

    init {
        Logger.loggingEnabled = false

        "sign" should {
            val identifier = Identifier(Identifier.IdentifierType.EMAIL, "id")
            val input: InputField = mock {
                on { isInputValid } doReturn true
                on { input } doReturn "password"
            }

            "hide previously shown error" {
                val view = viewMock()
                val presenter = passwordPresenter(view)
                presenter.sign(mock(), mock(), true)
                verify(view).hideError(any())
            }

            "show progress" {
                val view = viewMock()
                val presenter = passwordPresenter(view)
                presenter.sign(mock(), mock(), true)
                verify(view).showProgress()
            }

            "throw an exception if identifier is null" {
                val presenter = passwordPresenter()
                shouldThrow<IllegalArgumentException> {
                    presenter.sign(mock(), null, true)
                }
            }

            "save credentials with smartlockController if password was successfully provided" {
                val provider: InputProvider<Credentials> = mock()
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onSuccess(mock())
                }
                val view = viewMock()
                val smartlockController: SmartlockController = mock()

                val presenter = PasswordPresenter(view, provider, smartlockController)
                presenter.sign(input, identifier, true)
                verify(smartlockController).saveCredential("id", "password")
            }

            "show dialog error if there's a network error" {
                val error = ClientError(ClientError.ErrorType.NETWORK_ERROR, "")
                val provider: InputProvider<Credentials> = mock()
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }

                val view = viewMock()
                val presenter = passwordPresenter(view, provider)
                presenter.sign(input, identifier, true)
                verify(view).showErrorDialog(eq(error), anyOrNull())
            }

            "show credentials error if credentials are wrong" {
                BaseLoginActivity.tracker = mock()

                val error = ClientError(ClientError.ErrorType.INVALID_USER_CREDENTIALS, "")
                val provider: InputProvider<Credentials> = mock()
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }
                val view = viewMock()
                val presenter = passwordPresenter(view, provider)
                presenter.sign(input, identifier, true)
                verify(view).showError(eq(input), eq(R.string.schacc_password_error_incorrect))
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.InvalidCredentials, TrackingData.Screen.PASSWORD)
            }

            "show password error if length is wrong" {
                BaseLoginActivity.tracker = mock()
                val error = ClientError(ClientError.ErrorType.INVALID_INPUT, "")
                val provider: InputProvider<Credentials> = mock()
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }

                val view = viewMock()
                val presenter = passwordPresenter(view, provider)
                presenter.sign(input, identifier, true)
                verify(view).showError(eq(input), eq(R.string.schacc_password_error_length))
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.InvalidPassword, TrackingData.Screen.PASSWORD)
            }

            "hide progress if there's an error" {
                val error = ClientError(ClientError.ErrorType.INVALID_INPUT, "")
                val provider: InputProvider<Credentials> = mock()
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }
                val view = viewMock()
                val presenter = passwordPresenter(view, provider)
                presenter.sign(input, identifier, true)
                verify(view).hideProgress()
            }

            "show password error if input is not valid and track it" {
                BaseLoginActivity.tracker = mock()
                whenever(input.isInputValid).thenReturn(false)
                val view = viewMock()
                val presenter = passwordPresenter(view)
                presenter.sign(input, identifier, true)
                verify(view).showError(eq(input), eq(R.string.schacc_password_error_length))
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.InvalidPassword, TrackingData.Screen.PASSWORD)
            }

            "hide progress if input isn't valid" {
                whenever(input.isInputValid).thenReturn(false)
                val view = viewMock()
                val presenter = passwordPresenter(view)
                presenter.sign(input, identifier, true)
                verify(view).hideProgress()
            }
        }
    }
}

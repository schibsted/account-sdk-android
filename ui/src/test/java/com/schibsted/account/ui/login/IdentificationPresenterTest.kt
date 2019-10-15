package com.schibsted.account.ui.login

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.schibsted.account.ClientConfiguration
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.AccountStatusResponse
import com.schibsted.account.ui.login.flow.password.FlowSelectionListener
import com.schibsted.account.ui.login.screen.identification.IdentificationContract
import com.schibsted.account.ui.login.screen.identification.IdentificationPresenter
import com.schibsted.account.ui.ui.InputField
import io.kotlintest.specs.WordSpec

class IdentificationPresenterTest : WordSpec() {
    private fun viewMock(isActive: Boolean = true): IdentificationContract.View {
        return mock { on { it.isActive } doReturn isActive }
    }

    private fun identitificationPresenter(
        view: IdentificationContract.View = viewMock(),
        flowSelectionListener: FlowSelectionListener? = null
    ): IdentificationPresenter {
        val presenter = IdentificationPresenter(view, null, flowSelectionListener)
        presenter.id = mock()
        return presenter
    }

    private fun setAccountStatusResponse(presenter: IdentificationPresenter, isAvailable: Boolean) {
        val result: AccountStatusResponse = mock { on { it.isAvailable } doReturn isAvailable }
        whenever(presenter.id.getAccountStatus(any())).thenAnswer {
            (it.getArgument(0) as ResultCallback<AccountStatusResponse>).onSuccess(result)
        }
    }

    init {
        Logger.loggingEnabled = false
        "initialization" should {
            "assign the presenter to the view" {
                val view = viewMock()
                val presenter = identitificationPresenter(view)
                verify(view).setPresenter(presenter)
            }
        }

        "verify input" should {
            "do nothing is the view isn't active" {
                val view = viewMock(false)
                val presenter = identitificationPresenter(view)
                presenter.verifyInput(mock(), mock(), false, "message")
                verify(view, never())
            }

            "hide previously shown errors" {
                val view = viewMock()
                val presenter = identitificationPresenter(view)
                presenter.verifyInput(mock(), mock(), false, "message")
                verify(view).hideError(any())
            }
            "show error if the input isn't valid" {
                val view = viewMock()
                val presenter = identitificationPresenter(view)
                val input: InputField = mock()
                whenever(input.isInputValid).thenReturn(false)
                presenter.verifyInput(input, mock(), false, "message")
                verify(view).showError(any())
            }

            "Track error if the input isn't valid" {
                val view = viewMock()
                val presenter = identitificationPresenter(view)
                val input: InputField = mock()
                BaseLoginActivity.tracker = mock()
                whenever(input.isInputValid).thenReturn(false)

                presenter.verifyInput(input, Identifier.IdentifierType.SMS, false, "message")
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.InvalidPhone, TrackingData.Screen.IDENTIFICATION)

                presenter.verifyInput(input, Identifier.IdentifierType.EMAIL, false, "message")
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.InvalidEmail, TrackingData.Screen.IDENTIFICATION)
            }

            "show progress if the input is valid" {
                val view = viewMock()
                val presenter = identitificationPresenter(view)
                ClientConfiguration.set(ClientConfiguration("https://example.com", "id", "secret"))
                val input: InputField = mock()
                whenever(input.isInputValid).thenReturn(true)
                whenever(input.input).thenReturn("test@test.com")
                presenter.verifyInput(input, Identifier.IdentifierType.SMS, false, "message")
                verify(view).showProgress()
            }
        }

        "success to get account status" should {
            BaseLoginActivity.tracker = mock()

            "set tracking intent to CREATE if user is available" {
                val presenter = identitificationPresenter()
                setAccountStatusResponse(presenter, true)
                presenter.getAccountStatus(mock(), false, "sdf")
                verify(BaseLoginActivity.tracker)?.intent = TrackingData.UserIntent.CREATE
            }

            "set tracking intent to LOGIN if user is not available" {
                val presenter = identitificationPresenter()
                setAccountStatusResponse(presenter, false)
                presenter.getAccountStatus(mock(), false, "sdf")
                verify(BaseLoginActivity.tracker)?.intent = TrackingData.UserIntent.LOGIN
            }

            "show error if user is available is signup not allowed" {
                val view = viewMock()
                val presenter = identitificationPresenter(view)
                setAccountStatusResponse(presenter, true)
                presenter.getAccountStatus(mock(), false, "sdf")
                verify(view).showErrorDialog(any(), anyOrNull())
                verify(view).hideProgress()
            }

            "clear field" {
                val presenter = identitificationPresenter()
                setAccountStatusResponse(presenter, true)
                presenter.getAccountStatus(mock(), true, "sdf")
            }

            "call the flow listener with right values" {
                val flowListener: FlowSelectionListener = mock()
                val presenter = identitificationPresenter(flowSelectionListener = flowListener)
                setAccountStatusResponse(presenter, true)
                presenter.getAccountStatus(mock(), true, "sdf")
                verify(flowListener).onFlowSelected(eq(FlowSelectionListener.FlowType.SIGN_UP), any())

                setAccountStatusResponse(presenter, false)
                presenter.getAccountStatus(mock(), true, "sdf")
                verify(flowListener).onFlowSelected(eq(FlowSelectionListener.FlowType.LOGIN), any())
            }
            "clear field when providing identifer is successful" {
                val view = viewMock()
                val provider: InputProvider<Identifier>? = mock()
                val flowListener: FlowSelectionListener = mock()
                val presenter = IdentificationPresenter(view, provider, flowListener)
                presenter.id = mock()
                setAccountStatusResponse(presenter, true)

                presenter.getAccountStatus(mock(), true, "sdf")
            }

            "show error track it and hide progress" {
                val view = viewMock()
                val provider: InputProvider<Identifier>? = mock()
                val flowListener: FlowSelectionListener = mock()
                val presenter = IdentificationPresenter(view, provider, flowListener)
                presenter.id = mock()
                var error = ClientError(ClientError.ErrorType.NETWORK_ERROR, "")
                setAccountStatusResponse(presenter, true)
                whenever(provider!!.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }
                presenter.getAccountStatus(mock(), true, "sdf")

                verify(view).showErrorDialog(error)

                whenever(provider.provide(any(), any())).thenAnswer {
                    error = ClientError(ClientError.ErrorType.INVALID_EMAIL, "")
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }
                presenter.getAccountStatus(mock(), true, "sdf")

                verify(view).showError(any())
                verify(view, times(2)).hideProgress()
                verify(BaseLoginActivity.tracker, times(2))?.eventError(any(), any())
            }
        }

        "fail to get account status" should {
            "show error dialog if it's a server error" {
                val view = viewMock()
                val presenter = identitificationPresenter(view)

                val error = ClientError(ClientError.ErrorType.NETWORK_ERROR, "")
                whenever(presenter.id.getAccountStatus(any())).thenAnswer {
                    (it.getArgument(0) as ResultCallback<AccountStatusResponse>).onError(error)
                }
                presenter.getAccountStatus(mock(), true, "sdf")
                verify(view).showErrorDialog(error)
                verify(view).hideProgress()
            }

            "show error dialog if it's a signup not allowed error" {
                val view = viewMock()
                val presenter = identitificationPresenter(view)

                val error = ClientError(ClientError.ErrorType.SIGNUP_FORBIDDEN, "")
                whenever(presenter.id.getAccountStatus(any())).thenAnswer {
                    (it.getArgument(0) as ResultCallback<AccountStatusResponse>).onError(error)
                }
                presenter.getAccountStatus(mock(), false, "sdf")
                verify(view).showErrorDialog(error, "sdf")
                verify(view).hideProgress()
            }

            "show error field if it's a client error and signup allowed" {
                val view = viewMock()
                val presenter = identitificationPresenter(view)

                val error = ClientError(ClientError.ErrorType.INVALID_EMAIL, "")
                whenever(presenter.id.getAccountStatus(any())).thenAnswer {
                    (it.getArgument(0) as ResultCallback<AccountStatusResponse>).onError(error)
                }
                presenter.getAccountStatus(mock(), true, "sdf")
                verify(view).showError(any())
                verify(view).hideProgress()
            }
        }
    }
}

package com.schibsted.account.ui.login

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
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

    init {
        Logger.loggingEnabled = false

        "initialization" should {
            val view: IdentificationContract.View = mock()
            val presenter = IdentificationPresenter(view, mock(), mock())
            " assign the presenter to the view" {
                verify(view).setPresenter(presenter)
            }
        }

        "verify input" should {
            val view: IdentificationContract.View = mock()
            val presenter = IdentificationPresenter(view, mock(), mock())

            "do nothing is the view isn't active" {
                whenever(view.isActive).thenReturn(false)
                presenter.verifyInput(mock(), mock(), false, "message")
                verify(view, never())
            }

            "hide previously shown errors" {
                whenever(view.isActive).thenReturn(true)
                presenter.verifyInput(mock(), mock(), false, "message")
                verify(view).hideError(any())
            }
            "show error if the input isn't valid" {
                val input: InputField = mock()
                whenever(input.isInputValid).thenReturn(false)
                whenever(view.isActive).thenReturn(true)
                presenter.verifyInput(input, mock(), false, "message")
                verify(view).showError(any())
            }

            "Track error if the input isn't valid" {
                val input: InputField = mock()
                BaseLoginActivity.tracker = mock()
                whenever(input.isInputValid).thenReturn(false)
                whenever(view.isActive).thenReturn(true)

                presenter.verifyInput(input, Identifier.IdentifierType.SMS, false, "message")
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.InvalidPhone, TrackingData.Screen.IDENTIFICATION)

                presenter.verifyInput(input, Identifier.IdentifierType.EMAIL, false, "message")
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.InvalidEmail, TrackingData.Screen.IDENTIFICATION)
            }

            "show progress if the input is valid" {
                ClientConfiguration.set(ClientConfiguration("https://example.com", "id", "secret"))
                val input: InputField = mock()
                whenever(view.isActive).thenReturn(true)
                whenever(input.isInputValid).thenReturn(true)
                whenever(input.input).thenReturn("test@test.com")
                presenter.verifyInput(input, Identifier.IdentifierType.SMS, false, "message")
                verify(view).showProgress()
            }
        }

        "success to get account status" should {
            val view: IdentificationContract.View = mock()
            val flowListener: FlowSelectionListener = mock()
            val presenter = IdentificationPresenter(view, null, flowListener)
            presenter.id = mock()
            val result: AccountStatusResponse = mock()
            BaseLoginActivity.tracker = mock()
            whenever(presenter.id.getAccountStatus(any())).thenAnswer {
                (it.getArgument(0) as ResultCallback<AccountStatusResponse>).onSuccess(result)
            }

            "set tracking intent to CREATE if user is available" {
                whenever(result.isAvailable).thenReturn(true)
                presenter.getAccountStatus(mock(), false, "sdf")
                verify(BaseLoginActivity.tracker)?.intent = TrackingData.UserIntent.CREATE
            }

            "set tracking intent to LOGIN if user is not available" {
                whenever(result.isAvailable).thenReturn(false)
                presenter.getAccountStatus(mock(), false, "sdf")
                verify(BaseLoginActivity.tracker)?.intent = TrackingData.UserIntent.LOGIN
            }

            "show  error if user is available is signup not allowed" {
                whenever(view.isActive).thenReturn(true)
                whenever(result.isAvailable).thenReturn(true)
                presenter.getAccountStatus(mock(), false, "sdf")
                verify(view).showErrorDialog(any(), anyOrNull())
                verify(view).hideProgress()
            }

            "clear field" {
                whenever(result.isAvailable).thenReturn(true)
                presenter.getAccountStatus(mock(), true, "sdf")
            }

            "call the flow listener with right values" {
                whenever(result.isAvailable).thenReturn(true)
                presenter.getAccountStatus(mock(), true, "sdf")
                verify(flowListener).onFlowSelected(eq(FlowSelectionListener.FlowType.SIGN_UP), any())

                whenever(result.isAvailable).thenReturn(false)
                presenter.getAccountStatus(mock(), true, "sdf")
                verify(flowListener).onFlowSelected(eq(FlowSelectionListener.FlowType.LOGIN), any())
            }
            "clear field when providing identifer is successfull" {
                val provider: InputProvider<Identifier>? = mock()
                val pres = IdentificationPresenter(view, provider, flowListener)
                whenever(result.isAvailable).thenReturn(true)
                whenever(view.isActive).thenReturn(true)
                pres.id = mock()

                whenever(pres.id.getAccountStatus(any())).thenAnswer {
                    (it.getArgument(0) as ResultCallback<AccountStatusResponse>).onSuccess(result)
                }
                whenever(provider!!.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onSuccess(mock())
                }

                pres.getAccountStatus(mock(), true, "sdf")
            }

            "show error track it and hide progress" {
                val provider: InputProvider<Identifier>? = mock()
                val pres = IdentificationPresenter(view, provider, flowListener)
                pres.id = mock()
                whenever(view.isActive).thenReturn(true)
                var error = ClientError(ClientError.ErrorType.NETWORK_ERROR, "")
                whenever(result.isAvailable).thenReturn(true)
                whenever(pres.id.getAccountStatus(any())).thenAnswer {
                    (it.getArgument(0) as ResultCallback<AccountStatusResponse>).onSuccess(result)
                }
                whenever(provider!!.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }
                pres.getAccountStatus(mock(), true, "sdf")

                verify(view).showErrorDialog(error)

                whenever(provider.provide(any(), any())).thenAnswer {
                    error = ClientError(ClientError.ErrorType.INVALID_EMAIL, "")
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }
                pres.getAccountStatus(mock(), true, "sdf")

                verify(view).showError(any())
                verify(view, times(2)).hideProgress()
                verify(BaseLoginActivity.tracker, times(2))?.eventError(any(), any())
            }
        }

        "fail to get account status" should {
            val view: IdentificationContract.View = mock()
            val flowListener: FlowSelectionListener = mock()
            val presenter = IdentificationPresenter(view, null, flowListener)
            presenter.id = mock()
            BaseLoginActivity.tracker = mock()
            whenever(view.isActive).thenReturn(true)

            "show error dialog if it's a server error" {
                val error = ClientError(ClientError.ErrorType.NETWORK_ERROR, "")
                whenever(presenter.id.getAccountStatus(any())).thenAnswer {
                    (it.getArgument(0) as ResultCallback<AccountStatusResponse>).onError(error)
                }
                presenter.getAccountStatus(mock(), true, "sdf")
                verify(view).showErrorDialog(error)
                verify(view).hideProgress()
            }

            "show error dialog if it's a signup not allowed error" {
                val error = ClientError(ClientError.ErrorType.SIGNUP_FORBIDDEN, "")
                whenever(presenter.id.getAccountStatus(any())).thenAnswer {
                    (it.getArgument(0) as ResultCallback<AccountStatusResponse>).onError(error)
                }
                presenter.getAccountStatus(mock(), false, "sdf")
                verify(view).showErrorDialog(error, "sdf")
                verify(view).hideProgress()
            }

            "show error field if it's a client error and signup allowed" {
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

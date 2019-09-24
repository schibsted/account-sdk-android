package com.schibsted.account.ui.login

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.schibsted.account.ClientConfiguration
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.input.RequiredFields
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.login.screen.information.RequiredFieldsContract
import com.schibsted.account.ui.login.screen.information.RequiredFieldsPresenter
import com.schibsted.account.ui.ui.InputField
import io.kotlintest.specs.WordSpec

class RequiredFieldsPresenterTest : WordSpec() {

    private fun viewMock(): RequiredFieldsContract.View {
        return mock { on { isActive } doReturn true }
    }

    private fun requiredFieldsPresenter(
            view: RequiredFieldsContract.View = viewMock(),
            provider: InputProvider<RequiredFields> = mock()
    ): RequiredFieldsPresenter {
        return RequiredFieldsPresenter(view, provider)
    }

    init {
        val testConfig = ClientConfiguration("https://dev-example.com/", "myId", "mySecret")
        ClientConfiguration.set(testConfig)
        Logger.loggingEnabled = false

        "initialization" should {
            "assign the presenter to the view" {
                val view = viewMock()
                val presenter = requiredFieldsPresenter(view)
                verify(view).setPresenter(presenter)
            }
        }

        "update missing fields" should {
            val fields: MutableMap<String, InputField> = mutableMapOf()
            fields["KEY"] = mock {
                on { isInputValid } doReturn true
                on { input } doReturn "input"
            }
            "hide errors if all inputs are valid" {
                val view = viewMock()
                val presenter = requiredFieldsPresenter(view)
                presenter.updateMissingFields(fields)
                verify(view).hideErrors()
            }
            "show progress if all inputs are valid" {
                val view = viewMock()
                val presenter = requiredFieldsPresenter(view)
                presenter.updateMissingFields(fields)
                verify(view).showProgress()
            }

            "provide missing fields if all inputs are valid" {
                val provider: InputProvider<RequiredFields> = mock()
                val presenter = requiredFieldsPresenter(provider = provider)
                presenter.updateMissingFields(fields)
                verify(provider).provide(any(), any())
            }
        }

        "provide missing fields" should {
            val fields: MutableMap<String, InputField> = mutableMapOf()
            fields["KEY"] = mock {
                on { isInputValid } doReturn true
                on { input } doReturn "input"
            }

            "hide progress if there's an error" {
                val error = ClientError(ClientError.ErrorType.UNKNOWN_ERROR, "")
                val provider: InputProvider<RequiredFields> = mock()
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }

                val view = viewMock()
                val presenter = requiredFieldsPresenter(view, provider)
                presenter.updateMissingFields(fields)
                verify(view).hideProgress()
            }

            "show dialog error if there's a server error and track it" {
                BaseLoginActivity.tracker = mock()
                val error = ClientError(ClientError.ErrorType.UNKNOWN_ERROR, "")
                val provider: InputProvider<RequiredFields> = mock()
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }
                val view = viewMock()
                val presenter = requiredFieldsPresenter(view, provider)
                presenter.updateMissingFields(fields)
                verify(view).showErrorDialog(any(), anyOrNull())
                verify(BaseLoginActivity.tracker)?.eventError(TrackingData.UIError.NetworkError, TrackingData.Screen.REQUIRED_FIELDS)
            }

            "track error if it's a MISSING_FIELDS" {
                BaseLoginActivity.tracker = mock()
                val error = ClientError(ClientError.ErrorType.MISSING_FIELDS, "fields are missing")
                val provider: InputProvider<RequiredFields> = mock()
                whenever(provider.provide(any(), any())).thenAnswer {
                    (it.getArgument(1) as ResultCallback<NoValue>).onError(error)
                }
                val view = viewMock()
                val presenter = requiredFieldsPresenter(view, provider)
                presenter.updateMissingFields(fields)
                verify(BaseLoginActivity.tracker)?.eventError(any(), eq(TrackingData.Screen.REQUIRED_FIELDS))
            }

            "show error if some fields are not valid and track it" {
                BaseLoginActivity.tracker = mock()
                fields["KEY2"] = mock {
                    on { isInputValid } doReturn false
                    on { input } doReturn "nonValidInput"
                }
                val view = viewMock()
                val presenter = requiredFieldsPresenter(view)
                presenter.updateMissingFields(fields)
                verify(view).showError(any())
                verify(BaseLoginActivity.tracker)?.eventError(any(), eq(TrackingData.Screen.REQUIRED_FIELDS))
                verify(view).hideProgress()
            }
        }
    }
}

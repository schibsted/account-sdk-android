package com.schibsted.account.ui.login

import android.app.Activity
import android.arch.core.executor.ArchTaskExecutor
import android.arch.core.executor.TaskExecutor
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.schibsted.account.common.lib.ObservableField
import com.schibsted.account.engine.controller.LoginController
import com.schibsted.account.network.response.ClientInfo
import com.schibsted.account.ui.AccountUi
import com.schibsted.account.ui.smartlock.SmartlockTask
import io.kotlintest.matchers.instanceOf
import io.kotlintest.matchers.shouldBe
import io.kotlintest.mock.mock
import io.kotlintest.specs.WordSpec
import org.mockito.internal.verification.Times
import java.net.URI

class LoginActivityViewModelTest : WordSpec({

    ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
        override fun executeOnDiskIO(runnable: Runnable) {
            runnable.run()
        }

        override fun postToMainThread(runnable: Runnable) {
            runnable.run()
        }

        override fun isMainThread(): Boolean = true
    })

    val smartlockTask: SmartlockTask = mock()
    val params: AccountUi.Params = AccountUi.Params()
    val loginActivityViewModel = LoginActivityViewModel(smartlockTask, URI.create("http://redirectUri"), params)

    "view model initialization" should {
        "add a listener to the smartlock resolving state" {
            loginActivityViewModel.smartlockReceiver.isSmartlockResolving.hasObservers() shouldBe true
        }
    }

    "get smartlock credentials from intent" should {
        val failure: ObservableField<SmartlockTask.SmartLockResult> = ObservableField(SmartlockTask.SmartLockResult.Failure(-1))
        val success: ObservableField<SmartlockTask.SmartLockResult> = ObservableField(SmartlockTask.SmartLockResult.Success(-1, mock()))

        "with not valid credentials" should {
            whenever(smartlockTask.credentialsFromParcelable(any(), any(), any())) doReturn failure
            "assign a failure value to the smartlock result observer" {
                loginActivityViewModel.updateSmartlockCredentials(1, Activity.RESULT_OK, mock())
                verify(smartlockTask).credentialsFromParcelable(eq(1), eq(Activity.RESULT_OK), any())
                loginActivityViewModel.smartlockResult.value = failure.value
            }
        }

        "with a valid credentials" should {
            whenever(smartlockTask.credentialsFromParcelable(any(), any(), any())) doReturn success
            "assign a success value to the smartlock result observer" {
                loginActivityViewModel.updateSmartlockCredentials(1, Activity.RESULT_OK, mock())
                verify(smartlockTask).credentialsFromParcelable(eq(1), eq(Activity.RESULT_OK), any())
                loginActivityViewModel.smartlockResult.value = success.value
            }
        }
    }

    "initialize smartlock" should {
        "initialize the login controller" {
            whenever(smartlockTask.initializeSmartlock(any(), any())) doReturn ObservableField(false)
            loginActivityViewModel.loginController.value shouldBe null
            loginActivityViewModel.initializeSmartlock()
            verify(smartlockTask).initializeSmartlock(loginActivityViewModel.smartlockReceiver.isSmartlockResolving.value)
            loginActivityViewModel.loginController.value shouldBe instanceOf(LoginController::class)
        }
        "update the smartlock flow observable with the returned value" {
            whenever(smartlockTask.initializeSmartlock(any(), any())) doReturn ObservableField(false)
            loginActivityViewModel.startSmartLockFlow.value shouldBe null
            loginActivityViewModel.initializeSmartlock()
            loginActivityViewModel.startSmartLockFlow.value shouldBe false

            whenever(smartlockTask.initializeSmartlock(any(), any())) doReturn ObservableField(true)
            loginActivityViewModel.startSmartLockFlow.value shouldBe false
            loginActivityViewModel.initializeSmartlock()
            loginActivityViewModel.startSmartLockFlow.value shouldBe true

            verify(smartlockTask, Times(2)).initializeSmartlock(loginActivityViewModel.smartlockReceiver.isSmartlockResolving.value)
        }
    }

    "is smartlock resolving" should {
        "return the value of contained in the receiver" {
            loginActivityViewModel.smartlockReceiver.isSmartlockResolving.value = false
            loginActivityViewModel.isSmartlockResolving() shouldBe false
            loginActivityViewModel.smartlockReceiver.isSmartlockResolving.value = true
            loginActivityViewModel.isSmartlockResolving() shouldBe true
        }
    }

    "get client info" should {
        val viewModel: LoginActivityViewModel = mock {}
        "with a null intent info" should {
            "fetch info from the network" {
                whenever(viewModel.getClientInfo(null)).thenCallRealMethod()
                viewModel.getClientInfo(null)
                verify(viewModel).fetchClientInfo()
            }
        }
        "with a valid intent info" should {
            "assign the intent data to the clienResult value" {
                val clientInfo = ClientInfo("1", "client", "alias", mapOf(), "domain", 3, mapOf(), mapOf(), mock())
                loginActivityViewModel.getClientInfo(clientInfo)
                loginActivityViewModel.clientResult.value shouldBe instanceOf(LoginActivityViewModel.ClientResult.Success::class)
                (loginActivityViewModel.clientResult.value as LoginActivityViewModel.ClientResult.Success).clientInfo shouldBe clientInfo
            }
        }
    }
})
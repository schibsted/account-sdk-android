/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui.login

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.Parcelable
import com.schibsted.account.common.lib.Try
import com.schibsted.account.common.lib.getOrNull
import com.schibsted.account.common.util.Logger
import com.schibsted.account.common.util.getQueryParam
import com.schibsted.account.engine.controller.LoginController
import com.schibsted.account.engine.controller.SignUpController
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.integration.contract.LoginContract
import com.schibsted.account.engine.integration.contract.SignUpContract
import com.schibsted.account.engine.operation.ClientInfoOperation
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.ClientInfo
import com.schibsted.account.session.User
import com.schibsted.account.ui.AccountUi
import com.schibsted.account.ui.Event
import com.schibsted.account.ui.InternalUiConfiguration
import com.schibsted.account.ui.login.flow.password.FlowSelectionListener
import com.schibsted.account.ui.login.screen.LoginScreen
import com.schibsted.account.ui.smartlock.SmartlockReceiver
import com.schibsted.account.ui.smartlock.SmartlockTask
import com.schibsted.account.util.DeepLink
import java.net.URI

class LoginActivityViewModel(
    private val smartlockTask: SmartlockTask,
    uiConfiguration: InternalUiConfiguration,
    private val params: AccountUi.Params
) : ViewModel(), FlowSelectionListener {
    private val redirectUri = uiConfiguration.redirectUri

    val loginController = MutableLiveData<Event<LoginController>>()
    val signUpController = MutableLiveData<Event<SignUpController>>()
    val activityTitle = MutableLiveData<LoginScreen>()
    val user = MutableLiveData<User>()
    var userFlowType: FlowSelectionListener.FlowType? = null
    var userIdentifier: Identifier? = null

    val clientResult = MutableLiveData<Event<ClientResult>>()
    val clientResolvingState = MutableLiveData<Boolean>()

    val uiConfiguration = MutableLiveData<InternalUiConfiguration>()

    var smartlockCredentials = MutableLiveData<Credentials>()
    var credentialsProvider = MutableLiveData<InputProvider<Credentials>>()
    val smartlockReceiver: SmartlockReceiver = SmartlockReceiver(this)
    val startSmartLockFlow = MutableLiveData<Boolean>()
    val smartlockResolvingState = MutableLiveData<Boolean>()
    val smartlockResult = MutableLiveData<SmartlockTask.SmartLockResult>()

    init {
        smartlockReceiver.isSmartlockResolving.addListener(false, true) {
            smartlockResolvingState.value = smartlockReceiver.isSmartlockResolving.value
        }
        this.uiConfiguration.value = uiConfiguration
    }

    override fun onFlowSelected(flowType: FlowSelectionListener.FlowType, identifier: Identifier) {
        userIdentifier = identifier
        userFlowType = flowType
        when (userFlowType) {
            FlowSelectionListener.FlowType.LOGIN -> {
                activityTitle.value = LoginScreen.IDENTIFICATION_SCREEN
                loginController.value = Event(LoginController(true, params.scopes))
            }

            FlowSelectionListener.FlowType.ONE_STEP_LOGIN -> {
                activityTitle.value = LoginScreen.ONE_STEP_LOGIN_SCREEN
                loginController.value = Event(LoginController(true, params.scopes))
            }

            FlowSelectionListener.FlowType.SIGN_UP -> {
                activityTitle.value = LoginScreen.PASSWORD_SCREEN
                signUpController.value = Event(SignUpController(redirectUri, params.scopes))
            }

            FlowSelectionListener.FlowType.ONE_STEP_SIGNUP -> {
                activityTitle.value = LoginScreen.ONE_STEP_SIGNUP_SCREEN
                signUpController.value = Event(SignUpController(redirectUri, params.scopes))
            }
        }
    }

    override fun onFlowSelected(flowType: FlowSelectionListener.FlowType) {
        when (flowType) {
            FlowSelectionListener.FlowType.ONE_STEP_SIGNUP -> {
                activityTitle.value = LoginScreen.ONE_STEP_SIGNUP_SCREEN
            }

            FlowSelectionListener.FlowType.ONE_STEP_LOGIN -> {
                activityTitle.value = LoginScreen.ONE_STEP_LOGIN_SCREEN
            }

            FlowSelectionListener.FlowType.SIGN_UP -> {
                activityTitle.value = LoginScreen.PASSWORD_SCREEN
            }
        }
    }

    fun isUserAvailable() = userFlowType == FlowSelectionListener.FlowType.SIGN_UP || userFlowType == FlowSelectionListener.FlowType.ONE_STEP_SIGNUP
    fun isSmartlockResolving() = smartlockReceiver.isSmartlockResolving.value

    fun initializeSmartlock() {
        smartlockTask.initializeSmartlock(smartlockReceiver.isSmartlockResolving.value).addListener(true, true) { shouldStartSmartlock ->
            if (shouldStartSmartlock) {
                loginController.value = Event(LoginController(true, params.scopes))
            }
            startSmartLockFlow.value = shouldStartSmartlock
        }
    }

    fun loginFromDeepLink(state: DeepLink.ValidateAccount) {
        Logger.info(TAG, "Attempting login from deep link, extracting code")
        User.fromSessionCode(state.code, redirectUri.toString(), state.isPersistable,
                ResultCallback.fromLambda(
                        { error ->
                            Logger.info(TAG, "Automatic login after account validation failed: ${error.message}")
                            user.value = null
                        },
                        { user ->
                            Logger.info(TAG, "Automatic login after account validation was successful")
                            this.user.value = user
                        }
                ), null) // sign-up doesn't support scope yet
    }

    fun isDeepLinkRequestNewPassword(dataString: String?): Boolean {
        return dataString?.let { Try { URI.create(it) } }?.getOrNull()?.getQueryParam("spid_page")?.equals("request+new+password") == true
    }

    fun getClientInfo(intentClientInfo: ClientInfo?) {
        if (intentClientInfo == null) {
            fetchClientInfo()
        } else {
            clientResult.value = Event(ClientResult.Success(intentClientInfo))
        }
    }

    internal fun fetchClientInfo() {
        clientResolvingState.value = true
        ClientInfoOperation({ error ->
            clientResolvingState.value = false
            clientResult.value = Event(ClientResult.Failure(error.toClientError()))
        }, { info ->
            clientResolvingState.value = false
            clientResult.value = (Event(ClientResult.Success(info)))
        })
    }

    fun startLoginController(contract: LoginContract) {
        loginController.value?.peek()?.start(contract)
    }

    fun startSignUpController(contract: SignUpContract) {
        signUpController.value?.peek()?.start(contract)
    }

    fun updateSmartlockCredentials(requestCode: Int, resultCode: Int, smartlockCredentials: Parcelable?) {
        smartlockTask.credentialsFromParcelable(requestCode, resultCode, smartlockCredentials).addListener(true, true) {
            smartlockResult.value = it
        }
    }

    fun isFlowReady(): Boolean = smartlockCredentials.value == null && !smartlockReceiver.isSmartlockResolving.value && userFlowType != null

    sealed class ClientResult {
        data class Success(val clientInfo: ClientInfo) : ClientResult()
        data class Failure(val error: ClientError) : ClientResult()
    }

    companion object {
        private const val TAG = "LoginActivityViewModel"
    }
}
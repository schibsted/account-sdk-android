/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.flow.password

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.engine.controller.LoginController
import com.schibsted.account.engine.controller.SignUpController
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.ui.UiConfiguration
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.screen.identification.ui.AbstractIdentificationFragment

class PasswordActivity : BaseLoginActivity(), FlowSelectionListener {

    private var loginController: LoginController? = null
    private var signUpController: SignUpController? = null

    private val loginContract = LoginContractImpl(this, ::startIdentificationFragment)
    private val signUpContract = SignupContractImpl(this, ::startIdentificationFragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savedLoginController = savedInstanceState?.getParcelable<Parcelable>(KEY_LOGIN_CONTROLLER)
        val savedSignUpController = savedInstanceState?.getParcelable<Parcelable>(KEY_SIGN_UP_CONTROLLER)

        if (savedLoginController != null) {
            loginController = savedLoginController as LoginController
        } else if (savedSignUpController != null) {
            signUpController = savedSignUpController as SignUpController
        }

        if (activeFlowType == null) {
            startIdentificationFragment()
        } else {
            signUpController?.start(this.signUpContract)
            loginController?.start(this.loginContract)
        }
    }

    override fun onFlowSelected(flowType: FlowSelectionListener.FlowType, identifier: Identifier) {
        this.currentIdentifier = identifier
        this.activeFlowType = flowType

        when (flowType) {
            FlowSelectionListener.FlowType.LOGIN -> {
                BaseLoginActivity.tracker?.intent = TrackingData.UserIntent.LOGIN
                loginController = LoginController(true).apply { start(this@PasswordActivity.loginContract) }
            }

            FlowSelectionListener.FlowType.SIGN_UP -> {
                BaseLoginActivity.tracker?.intent = TrackingData.UserIntent.CREATE
                this.signUpController = SignUpController(uiConfiguration.redirectUri)
                        .apply { start(this@PasswordActivity.signUpContract) }
            }
        }
    }

    private fun startIdentificationFragment() {
        val fragment = fragmentProvider.getOrCreateIdentificationFragment(
                navigationController.currentFragment,
                identifierType = Identifier.IdentifierType.EMAIL.value,
                flowSelectionListener = this)
        navigationController.navigateToFragment(fragment as AbstractIdentificationFragment)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_SIGN_UP_CONTROLLER, signUpController)
        outState.putParcelable(KEY_LOGIN_CONTROLLER, loginController)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isUserAvailable()) {
            navigationController.handleBackPressed(signUpController, this.signUpContract)
        } else {
            navigationController.handleBackPressed(loginController, this.loginContract)
        }
    }

    override fun onNavigateBackRequested() {
        onBackPressed()
    }

    companion object {
        const val KEY_LOGIN_CONTROLLER = "LOGIN_CONTROLLER"
        const val KEY_SIGN_UP_CONTROLLER = "SIGN_UP_CONTROLLER"
        /**
         * Provides an [Intent] that can be used to launch the visual authentication flow.
         *
         * @param context The context.
         * @param identityUiOptions The identityUiOptions for this [Activity].
         * @return An [Intent] that can be used to launch the visual authentication flow.
         */
        @JvmStatic
        fun getCallingIntent(context: Context, uiConfiguration: UiConfiguration): Intent {
            val intent = Intent(context, PasswordActivity::class.java)
            intent.putExtra(KEY_UI_CONFIGURATION, uiConfiguration)
            return intent
        }
    }
}

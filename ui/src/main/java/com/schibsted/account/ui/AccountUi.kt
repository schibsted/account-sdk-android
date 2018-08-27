/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.DrawableRes
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.operation.ClientInfoOperation
import com.schibsted.account.network.OIDCScope
import com.schibsted.account.network.response.ClientInfo
import com.schibsted.account.ui.login.flow.password.PasswordActivity
import com.schibsted.account.ui.login.flow.passwordless.PasswordlessActivity
import com.schibsted.account.ui.smartlock.SmartlockController
import com.schibsted.account.ui.smartlock.SmartlockMode
import kotlinx.android.parcel.Parcelize
import java.util.Locale

object AccountUi {
    const val KEY_PARAMS = "SCHACC_PARAMS"
    const val KEY_FLOW_TYPE = "SCHACC_FLOW_TYPE"
    const val KEY_CLIENT_INFO = "SCHACC_CLIENT_INFO"
    const val EXTRA_ERROR = "EXTRA_ERROR"

    /**
     * Result code sent through [BaseLoginActivity.onActivityResult] to notify the client application that an error happened
     */
    const val RESULT_ERROR = Activity.RESULT_FIRST_USER + 0

    /**
     * Result code sent through [BaseLoginActivity.onActivityResult] to notify the client application that the smartlock login
     * failed
     */
    const val SMARTLOCK_FAILED = Activity.RESULT_FIRST_USER + 1

    /**
     * @param teaserText A teaser text to show on the initial screen
     * @param preFilledIdentifier If the user ID is known, the identifier can be pre-filled
     *
     */
    @Parcelize
    data class Params internal constructor(
        val teaserText: String? = null,
        val preFilledIdentifier: String? = null,
        val smartLockMode: SmartlockMode = SmartlockMode.DISABLED,
        val locale: Locale = Locale.getDefault(),
        val signUpMode: SignUpMode = SignUpMode.Enabled,
        val isCancellable: Boolean = true,
        @DrawableRes val clientLogo: Int = 0,
        @OIDCScope val scopes: Array<String> = arrayOf(OIDCScope.SCOPE_OPENID)

    ) : Parcelable {

        class Builder {
            private var params = Params()
            fun teaserText(teaserText: String?) = apply { params = params.copy(teaserText = teaserText) }
            fun preFilledIdentifier(preFilledIdentifier: String?) = apply { params = params.copy(preFilledIdentifier = preFilledIdentifier) }
            fun smartLockMode(smartLockMode: SmartlockMode) = apply { params = params.copy(smartLockMode = smartLockMode) }
            fun scopes(@OIDCScope scopes: Array<String>) = apply { params = params.copy(scopes = scopes) }
            fun locale(locale: Locale) = apply { params = params.copy(locale = locale) }
            fun signUpMode(mode: SignUpMode) = apply { params = params.copy(signUpMode = mode) }
            fun isCancellable(isCancellable: Boolean) = apply { params = params.copy(isCancellable = isCancellable) }
            fun build() = params
        }

        companion object {
            operator fun invoke(bundle: Bundle): Params {
                // when app is launched via deeplink the parcelable value is null
                return bundle.getParcelable(KEY_PARAMS) as? Params ?: Params()
            }
        }
    }

    @JvmStatic
    private var clientInfo: ClientInfo? = null

    enum class FlowType {
        PASSWORD, PASSWORDLESS_EMAIL, PASSWORDLESS_SMS;
    }

    @JvmStatic
    fun preInitialize(onUiReady: ResultCallback<Void?>) {
        ClientInfoOperation({ onUiReady.onError(it.toClientError()) }, {
            clientInfo = it
            onUiReady.onSuccess(null)
        })
    }

    /** @param context The application context
     * @param flowType Which UI flow to initialize
     * @param params Additional [Params] for the UIs
     */
    @JvmStatic
    fun getCallingIntent(context: Context, flowType: FlowType, params: Params = Params()): Intent {
        if (params.smartLockMode != SmartlockMode.DISABLED && !SmartlockController.isSmartlockAvailable()) {
            throw IllegalStateException("SmartLock is enabled, but not found on the classpath. Please verify that the smartlock module is included in your build")
        }

        var intent = when (flowType) {
            FlowType.PASSWORD -> Intent(context, PasswordActivity::class.java)
            FlowType.PASSWORDLESS_EMAIL,
            FlowType.PASSWORDLESS_SMS -> Intent(context, PasswordlessActivity::class.java)
        }
                .putExtra(KEY_PARAMS, params)
                .putExtra(KEY_FLOW_TYPE, flowType.name)

        clientInfo?.let { intent = intent.putExtra(KEY_CLIENT_INFO, it) }

        return intent
    }
}

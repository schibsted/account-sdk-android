/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.operation.ClientInfoOperation
import com.schibsted.account.network.response.ClientInfo
import com.schibsted.account.ui.login.flow.password.PasswordActivity
import com.schibsted.account.ui.login.flow.passwordless.PasswordlessActivity
import com.schibsted.account.ui.smartlock.SmartlockImpl
import com.schibsted.account.ui.smartlock.SmartlockMode
import kotlinx.android.parcel.Parcelize

object AccountUi {
    const val KEY_PARAMS = "SCHACC_PARAMS"
    const val KEY_FLOW_TYPE = "SCHACC_FLOW_TYPE"
    const val KEY_CLIENT_INFO = "SCHACC_CLIENT_INFO"

    @Parcelize
    /**
     * @param teaserText A teaser text to show on the initial screen
     * @param preFilledIdentifier If the user ID is known, the identifier can be pre-filled
     *
     */
    data class Params(val teaserText: String? = null, val preFilledIdentifier: String? = null, val smartLockMode: SmartlockMode = SmartlockMode.DISABLED) : Parcelable {

        companion object {
            operator fun invoke(bundle: Bundle): Params {
                return bundle.getParcelable(KEY_PARAMS) as Params
            }
        }
    }

    @JvmStatic
    private var clientInfo: ClientInfo? = null

    enum class FlowType {
        PASSWORD, PASSWORDLESS_EMAIL, PASSWORDLESS_PHONE;
    }

    @JvmStatic
    fun preInitialize(onUiReady: ResultCallback<Void?>) {
        ClientInfoOperation({ onUiReady.onError(it.toClientError()) }, {
            clientInfo = it
            onUiReady.onSuccess(null)
        })
    }

    @JvmStatic
            /**
             * @param context The application context
             * @param flowType Which UI flow to initialize
             * @param params Additional [Params] for the UIs
             */
    fun getCallingIntent(context: Context, flowType: FlowType, params: Params = Params()): Intent {
        if (params.smartLockMode != SmartlockMode.DISABLED && !SmartlockImpl.isSmartlockAvailable()) {
            throw IllegalStateException("SmartLock is enabled, but not found on the classpath. Please verify that the smartlock module is included in your build")
        }

        var intent = when (flowType) {
            FlowType.PASSWORD -> Intent(context, PasswordActivity::class.java).putExtra(KEY_FLOW_TYPE, flowType.name)
            FlowType.PASSWORDLESS_EMAIL -> Intent(context, PasswordlessActivity::class.java).putExtra(KEY_FLOW_TYPE, flowType.name)
            FlowType.PASSWORDLESS_PHONE -> Intent(context, PasswordlessActivity::class.java).putExtra(KEY_FLOW_TYPE, flowType.name)
        }.putExtra(KEY_PARAMS, params)

        clientInfo?.let { intent = intent.putExtra(KEY_CLIENT_INFO, it) }

        return intent
    }
}

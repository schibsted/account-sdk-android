/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.schibsted.account.common.lib.ObservableField
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

    @Parcelize
    data class Params(val teaserText: String? = null, val preFilledIdentifier: String? = null, val smartLockMode: SmartlockMode = SmartlockMode.DISABLED) : Parcelable {

        companion object {
            operator fun invoke(bundle: Bundle): Params {
                return bundle.getParcelable(KEY_PARAMS) as Params
            }
        }
    }

    // TODO: Make this observable and listen in BaseLoginActivity
    @JvmStatic
    internal var clientInfo = ObservableField<ClientInfo?>(null)
        private set

    enum class FlowType {
        PASSWORD, PASSWORDLESS_EMAIL, PASSWORDLESS_PHONE;
    }

    @JvmStatic
    fun preInitialize(onUiReady: ResultCallback<Void?>) {
        ClientInfoOperation({ onUiReady.onError(it.toClientError()) }, {
            clientInfo.value = it
            onUiReady.onSuccess(null)
        })
    }

    @JvmStatic
    fun getCallingIntent(context: Context, flowType: FlowType, params: Params = Params()): Intent {
        if (params.smartLockMode != SmartlockMode.DISABLED && !SmartlockImpl.isSmartlockAvailable()) {
            throw IllegalStateException("SmartLock is enabled, but not found on the classpath. Please verify that the smartlock module is included in your build")
        }

        val intent = when (flowType) {
            FlowType.PASSWORD -> Intent(context, PasswordActivity::class.java).putExtra(KEY_FLOW_TYPE, flowType.name)
            FlowType.PASSWORDLESS_EMAIL -> Intent(context, PasswordlessActivity::class.java).putExtra(KEY_FLOW_TYPE, flowType.name)
            FlowType.PASSWORDLESS_PHONE -> Intent(context, PasswordlessActivity::class.java).putExtra(KEY_FLOW_TYPE, flowType.name)
        }

        return intent.putExtra(KEY_PARAMS, params)
    }
}

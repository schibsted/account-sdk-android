package com.schibsted.account.session


import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import com.schibsted.account.ClientConfiguration
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.response.DeviceFingerprint
import com.schibsted.account.network.service.user.UserService

class Device(private val applicationName: String, private val applicationVersion: String, private val androidId: String, private val user: User, private val userService: UserService = UserService(ClientConfiguration.get().environment, user.authClient)) {
    fun createFingerprint(callback: ResultCallback<DeviceFingerprint>? = null) {
        val token = user.token
        if (token == null) {
            callback?.onError(ClientError.USER_LOGGED_OUT_ERROR)
            return
        }


        val deviceData = hashMapOf<String, Any>()
        deviceData["deviceId"] = androidId
        deviceData["platfom"] = PLATFORM_NAME
        deviceData["applicationName"] = applicationName
        deviceData["applicationVersion"] = applicationVersion

        userService.createDeviceFingerprint(token, deviceData).enqueue(NetworkCallback.lambda("Creating a new device fingerprint",
                { callback?.onError(it.toClientError()) },
                { callback?.onSuccess(it.data) })
        )
    }

    companion object {
        const val PLATFORM_NAME = "Android"
    }
}
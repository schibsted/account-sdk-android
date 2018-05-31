/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.smartlock

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.CredentialsOptions
import com.schibsted.account.Events

/**
 * A [BroadcastReceiver] which disables auto login after a user has been logged out
 */
class SmartlockReceiver(activity: Activity) : BroadcastReceiver() {
    private val client = Credentials.getClient(activity, CredentialsOptions.DEFAULT)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Events.ACTION_USER_LOGOUT) {
            this.client.disableAutoSignIn()
        }
    }
}

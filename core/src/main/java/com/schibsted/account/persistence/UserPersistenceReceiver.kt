/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import com.schibsted.account.Events
import com.schibsted.account.common.util.Logger
import com.schibsted.account.model.UserId
import com.schibsted.account.session.User

class UserPersistenceReceiver(appContext: Context) : BroadcastReceiver() {
    private val TAG = "UserPersistenceReceiver"

    private val userPersistence = UserPersistence(appContext)
    private val localBroadcastManager = LocalBroadcastManager.getInstance(appContext)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Events.ACTION_USER_LOGIN -> {
                Logger.verbose(TAG, "Received event: User logged in")
                val user = intent.extras.getParcelable<User>(Events.EXTRA_USER)
                user.takeIf { it.isPersistable }?.let { userPersistence.persist(it) }
            }

            Events.ACTION_USER_LOGOUT -> {
                Logger.verbose(TAG, "Received event: User logged out")
                val userId = intent.extras.getParcelable<UserId>(Events.EXTRA_USER_ID)
                userPersistence.remove(userId.id)
            }

            Events.ACTION_USER_TOKEN_REFRESH -> {
                Logger.verbose(TAG, "Received event: Token refreshing")
                val user = intent.extras.getParcelable<User>(Events.EXTRA_USER)
                user.takeIf { it.isPersistable }?.let { userPersistence.persist(it) }
            }
        }
    }

    fun register() {
        IntentFilter(Events.ACTION_USER_LOGIN).apply {
            addAction(Events.ACTION_USER_LOGOUT)
            addAction(Events.ACTION_USER_TOKEN_REFRESH)
        }.also { localBroadcastManager.registerReceiver(this, it) }
    }

    fun unregister() {
        localBroadcastManager.unregisterReceiver(this)
    }
}

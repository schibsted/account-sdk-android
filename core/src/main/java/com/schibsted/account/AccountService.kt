/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.schibsted.account.common.util.Logger
import com.schibsted.account.persistence.UserPersistenceService

class AccountService(private val appContext: Context) : LifecycleObserver {
    private val TAG = Logger.DEFAULT_TAG + "-ASRV"
    private val upConnection = UserPersistenceService.Connection()

    init {
        localBroadcastManager = LocalBroadcastManager.getInstance(appContext)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun bind() {
        Logger.verbose(TAG, { "Binding ${AccountService::class.simpleName}" })
        appContext.bindService(Intent(appContext, UserPersistenceService::class.java), upConnection, Context.BIND_AUTO_CREATE)
                .also {
                    when (it) {
                        true -> Logger.verbose(TAG, { "${UserPersistenceService::class.simpleName} was bound successfully" })
                        false -> Logger.error(TAG, { "Failed to bind ${UserPersistenceService::class.simpleName}. Is it added to the manifest?" })
                    }
                }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun unbind() {
        Logger.verbose(TAG, { "Un-binding ${AccountService::class.simpleName}" })
        appContext.unbindService(upConnection)
    }

    companion object {
        internal var localBroadcastManager: LocalBroadcastManager? = null
    }
}

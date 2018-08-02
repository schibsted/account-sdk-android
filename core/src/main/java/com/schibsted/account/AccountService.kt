/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import android.support.annotation.VisibleForTesting
import android.support.v4.content.LocalBroadcastManager
import com.schibsted.account.common.util.Logger
import com.schibsted.account.persistence.UserPersistenceService

class AccountService @JvmOverloads constructor(
    private val appContext: Context,
    localBroadcastManager: LocalBroadcastManager = LocalBroadcastManager.getInstance(appContext)
) : LifecycleObserver {

    private val upConnection = UserPersistenceService.Connection()
    var isPersistenceServiceBound = false
        private set

    init {
        AccountService.localBroadcastManager = localBroadcastManager
        AccountService.packageName = appContext.packageName
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun bind() {
        Logger.verbose(TAG, { "Binding ${AccountService::class.simpleName}" })
        this.isPersistenceServiceBound = appContext.bindService(Intent(appContext, UserPersistenceService::class.java), upConnection, Context.BIND_AUTO_CREATE)
                .also {
                    when (it) {
                        true -> Logger.verbose(TAG, { "${UserPersistenceService::class.simpleName} was bound successfully" })
                        false -> Logger.error(TAG, { "Failed to bind ${UserPersistenceService::class.simpleName}. Is it added to the manifest?" })
                    }
                }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun unbind() {
        if (this.isPersistenceServiceBound) {
            Logger.verbose(TAG, { "Un-binding ${AccountService::class.simpleName}" })
            appContext.unbindService(upConnection)
        } else {
            Logger.warn(TAG, { "Cannot un-bind service, as it is currently not bound" })
        }
    }

    companion object {
        private const val TAG = Logger.DEFAULT_TAG + "-ASRV"
        internal var localBroadcastManager: LocalBroadcastManager? = null
            @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
            set
        internal var packageName: String = "unknown"
            private set
    }
}

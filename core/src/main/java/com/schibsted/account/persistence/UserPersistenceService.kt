/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import com.schibsted.account.common.util.Logger

class UserPersistenceService : Service() {
    class Connection : ServiceConnection {
        private val TAG = "UserPersistenceService"

        var service: UserPersistenceService? = null
            private set

        override fun onServiceDisconnected(componentName: ComponentName) {
            Logger.verbose(TAG, "UserPersistenceService disconnected")
            service = null
        }

        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            Logger.verbose(TAG, "UserPersistenceService connected")
            service = (binder as ServiceBinder).getService()
        }
    }

    inner class ServiceBinder : Binder() {
        fun getService(): UserPersistenceService {
            return this@UserPersistenceService
        }
    }

    override fun onBind(intent: Intent) = ServiceBinder()

    private lateinit var automaticUserPersistence: UserPersistenceReceiver

    override fun onCreate() {
        super.onCreate()
        automaticUserPersistence = UserPersistenceReceiver(applicationContext)
        automaticUserPersistence.register()
    }

    override fun onDestroy() {
        super.onDestroy()
        automaticUserPersistence.unregister()
    }
}

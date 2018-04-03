/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account

object Events {
    const val ACTION_USER_LOGIN = "AccountSdkActionUserLogin"
    const val ACTION_USER_LOGOUT = "AccountSdkActionUserLogout"
    const val ACTION_USER_TOKEN_REFRESH = "AccountSdkActionUserTokenRefresh"

    const val EXTRA_USER = "AccountSdkExtraUser"
    const val EXTRA_USER_ID = "AccountSdkExtraUserId"
}

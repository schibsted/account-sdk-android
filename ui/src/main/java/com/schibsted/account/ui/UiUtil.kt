/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui

import android.content.Context
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.ui.login.screen.LoginScreen
import java.util.Locale

object UiUtil {
    @JvmStatic
    fun getTrackingScreen(loginScreen: LoginScreen): TrackingData.Screen? = when (loginScreen) {
        LoginScreen.IDENTIFICATION_SCREEN -> TrackingData.Screen.IDENTIFICATION
        LoginScreen.PASSWORD_SCREEN -> TrackingData.Screen.PASSWORD
        LoginScreen.VERIFICATION_SCREEN -> TrackingData.Screen.VERIFICATION_CODE
        LoginScreen.TC_SCREEN -> TrackingData.Screen.AGREEMENTS
        LoginScreen.REQUIRED_FIELDS_SCREEN -> TrackingData.Screen.REQUIRED_FIELDS
        LoginScreen.CHECK_INBOX_SCREEN -> TrackingData.Screen.ACCOUNT_VERIFICATION
        else -> null
    }

    @JvmStatic
    fun setLanguage(context: Context, locale: Locale) {
        val resources = context.resources
        val conf = resources.configuration
        conf.locale = locale
        resources.updateConfiguration(conf, resources.displayMetrics)
    }
}

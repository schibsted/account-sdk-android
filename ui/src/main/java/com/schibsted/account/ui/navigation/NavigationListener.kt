/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.navigation

import android.support.v4.app.DialogFragment
import com.schibsted.account.ui.login.screen.LoginScreen
import com.schibsted.account.ui.ui.FlowFragment
import com.schibsted.account.ui.ui.WebFragment
import com.schibsted.account.ui.ui.dialog.InformationDialogFragment

/**
 * a actionListener of navigation events
 */
interface NavigationListener {

    /**
     * Use this method to listen to navigation event involving a [FlowFragment].
     *
     * @param where the screen to go to.
     */
    fun onWebViewNavigationRequested(where: WebFragment, loginScreen: LoginScreen)

    /**
     * Use this method to listen to navigation event involving a [InformationDialogFragment].
     *
     * @param where the given dialog fragment to display
     */
    fun onDialogNavigationRequested(where: DialogFragment)

    /**
     * Use this method to navigate back to the previous defined screen
     *
     */
    fun onNavigateBackRequested()

    fun onNavigationDone(screen: LoginScreen)
}

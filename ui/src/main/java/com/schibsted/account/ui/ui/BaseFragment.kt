/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.ErrorUtil
import com.schibsted.account.ui.KeyboardManager
import com.schibsted.account.ui.R
import com.schibsted.account.ui.login.screen.LoginScreen
import com.schibsted.account.ui.navigation.NavigationListener
import com.schibsted.account.ui.ui.dialog.InformationDialogFragment

abstract class BaseFragment : Fragment(), Animation.AnimationListener {

    /**
     * the navigation controller used to request a navigation
     */
    @JvmField
    protected var navigationListener: NavigationListener? = null
    /**
     * The keyboard manager allowing to interact with the state of the soft keyboard
     */
    protected lateinit var keyboardManager: KeyboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = activity
        if (activity is KeyboardManager) {
            keyboardManager = activity
        }
        requireNotNull(keyboardManager) { "the Keyboard manager can't be null" }
    }

    /**
     * this method is used to register the navigation controller
     *
     * @param navigationListener the navigation controller to use
     */
    fun registerNavigationController(navigationListener: NavigationListener) {
        this.navigationListener = navigationListener
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation {
        super.onCreateAnimation(transit, enter, nextAnim)
        val animRes = if (nextAnim == 0) R.anim.schacc_none else nextAnim
        val anim = AnimationUtils.loadAnimation(context, animRes)
        anim.setAnimationListener(this)
        return anim
    }

    override fun onAnimationEnd(animation: Animation?) {
    }

    override fun onAnimationRepeat(animation: Animation?) {
    }

    override fun onAnimationStart(animation: Animation?) {
    }

    /**
     * free the reference to the navigation controller
     *
     */
    fun unregisterNavigationController() {
        navigationListener = null
    }

    fun displayErrorDialog(error: ClientError, errorMessage: String? = null) {
        val message = errorMessage
                ?: getString(ErrorUtil.getErrorMessageRes(error.errorType, LoginScreen.valueOf(tag!!)))
        val dialog = InformationDialogFragment.newInstance(
                getString(R.string.schacc_generic_exception_error_message_title),
                message,
                R.drawable.schacc_ic_error, null)
        navigationListener?.onDialogNavigationRequested(dialog)
    }
}

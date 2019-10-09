/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.navigation

import android.app.Activity
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.controller.Controller
import com.schibsted.account.engine.integration.contract.Contract
import com.schibsted.account.session.User
import com.schibsted.account.ui.AccountUiHook
import com.schibsted.account.ui.R
import com.schibsted.account.ui.UiUtil
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.BaseLoginActivity.Companion.EXTRA_USER
import com.schibsted.account.ui.login.screen.LoginScreen
import com.schibsted.account.ui.login.screen.identification.ui.AbstractIdentificationFragment
import com.schibsted.account.ui.login.screen.inbox.InboxFragment
import com.schibsted.account.ui.login.screen.information.RequiredFieldsFragment
import com.schibsted.account.ui.login.screen.onesteplogin.OneStepLoginFragment
import com.schibsted.account.ui.login.screen.password.PasswordFragment
import com.schibsted.account.ui.login.screen.term.TermsFragment
import com.schibsted.account.ui.login.screen.verification.VerificationFragment
import com.schibsted.account.ui.ui.BaseFragment
import com.schibsted.account.ui.ui.WebFragment

const val DIALOG_SCREEN = "DIALOG_SCREEN"

/**
 * Base navigation controller defining common method to navigate through screens
 * every and each controller should extend it.
 */
class Navigation(
    private var activity: BaseLoginActivity,
    private val navigationListener: NavigationListener
) : FragmentManager.OnBackStackChangedListener {
    private val fragmentManager: FragmentManager = activity.supportFragmentManager

    init {
        fragmentManager.addOnBackStackChangedListener(this)
    }

    /**
     * the currently displayed fragment
     */
    var currentFragment: BaseFragment? = fragmentManager.findFragmentById(R.id.fragment_container) as BaseFragment?
        private set

    /**
     * used to finish the current flow
     */
    fun finishNavigation() {
        currentFragment?.tag?.let {
            val screen = LoginScreen.valueOf(it)
            UiUtil.getTrackingScreen(screen)?.let {
                BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.CLOSE, it)
            }
        }

        val hooks = (this.activity.application as? AccountUiHook)
                .also { Logger.info(TAG, "Resolving UI hooks: $it") }
                ?: AccountUiHook.DEFAULT

        hooks.onLoginAborted(AccountUiHook.OnProceedListener {
            activity.finish()
        })
    }

    /**
     * used to navigate to the given information dialog fragment
     *
     * @param dialogFragment the [DialogFragment] to display
     */
    fun navigationToDialog(dialogFragment: DialogFragment) {
        if (fragmentManager.findFragmentByTag(DIALOG_SCREEN) as? DialogFragment == null) {
            dialogFragment.showNow(fragmentManager, DIALOG_SCREEN)
        } else {
            Logger.warn(TAG, "a dialog is already shown")
        }
    }

    fun dismissDialog(allowStateLoss: Boolean = false) {
        (fragmentManager.findFragmentByTag(DIALOG_SCREEN) as? DialogFragment)?.let {
            if (allowStateLoss) {
                it.dismissAllowingStateLoss()
            } else {
                it.dismiss()
            }
        }
    }

    /**
     * called when a navigation back was performed in order to update the current fragment reference
     *
     * @see .currentFragment
     */
    override fun onBackStackChanged() {
        currentFragment = fragmentManager.findFragmentById(R.id.fragment_container) as BaseFragment
        currentFragment?.tag?.let {
            navigationListener.onNavigationDone(LoginScreen.valueOf(it))
        }
    }

    /**
     * used to perform the navigation: begin a transaction, replace the current fragment and commit the transaction
     * @param fragment the fragment to display
     */
    private fun navigateTo(fragment: BaseFragment, loginScreen: LoginScreen) {
        if (!LoginScreen.isWebView(currentFragment?.tag)) {
            val transaction = fragmentManager.beginTransaction()
            currentFragment = fragment
            currentFragment?.registerNavigationController(navigationListener)

            currentFragment?.let {
                transaction
                        .setCustomAnimations(R.anim.schacc_right_in, R.anim.schacc_left_out, R.anim.schacc_left_in, R.anim.schacc_right_out)
                        .replace(R.id.fragment_container, it, loginScreen.value)
                transaction.addToBackStack(loginScreen.value)
                transaction.commitAllowingStateLoss()
            }

        }
    }

    fun finishFlow(user: User) {
        val hooks = (this.activity.application as? AccountUiHook)
                .also { Logger.info(TAG, "Resolving UI hooks: $it") }
                ?: AccountUiHook.DEFAULT

        hooks.onLoginCompleted(user, AccountUiHook.OnProceedListener {
            if (activity.callingActivity == null) {
                val intent = activity.packageManager.getLaunchIntentForPackage(activity.application.packageName)
                intent?.putExtra(EXTRA_USER, user)
                activity.startActivity(intent)
            } else {
                activity.setResult(Activity.RESULT_OK, activity.intent.putExtra(EXTRA_USER, user))
                activity.finish()
            }
        })
    }

    fun <T : Contract<*>, C : Controller<T>> handleBackPressed(controller: C?, contract: T) {
        when (currentFragment?.tag) {
            LoginScreen.TC_SCREEN.value,
            LoginScreen.REQUIRED_FIELDS_SCREEN.value -> {
                fragmentManager.popBackStack()
                controller?.back()
                controller?.evaluate(contract)
            }

            LoginScreen.CHECK_INBOX_SCREEN.value,
            LoginScreen.PASSWORD_SCREEN.value,
            LoginScreen.VERIFICATION_SCREEN.value -> {
                controller?.back(fragmentManager.backStackEntryCount)
                fragmentManager.popBackStack(LoginScreen.IDENTIFICATION_SCREEN.value, 0)
                controller?.evaluate(contract)
            }

            LoginScreen.WEB_TC_SCREEN.value,
            LoginScreen.WEB_FORGOT_PASSWORD_SCREEN.value,
            LoginScreen.WEB_NEED_HELP_SCREEN.value -> {
                fragmentManager.popBackStack()
            }
            else -> finishNavigation()
        }
    }

    fun unregister() = currentFragment?.unregisterNavigationController()

    fun register(listener: NavigationListener) = currentFragment?.registerNavigationController(listener)

    fun navigateToWebView(what: WebFragment, loginScreen: LoginScreen) {
        navigateTo(what, loginScreen)
    }

    fun navigateBackTo(screen: LoginScreen) {
        fragmentManager.popBackStack(screen.value, 0)
    }

    fun <F : BaseFragment> navigateToFragment(fragment: F) {
        when (fragment) {
            is AbstractIdentificationFragment -> {
                // How to introduce the single flow screen?
                if (fragment.tag != LoginScreen.IDENTIFICATION_SCREEN.value) {
                    navigateTo(fragment, LoginScreen.IDENTIFICATION_SCREEN)
                }
            }
            is OneStepLoginFragment -> {
                if (fragment.tag != LoginScreen.ONE_STEP_LOGIN_SCREEN.value) {
                    navigateTo(fragment, LoginScreen.ONE_STEP_LOGIN_SCREEN)
                }
            }
            is PasswordFragment ->
                if (fragment.tag != LoginScreen.PASSWORD_SCREEN.value) {
                    navigateTo(fragment, LoginScreen.PASSWORD_SCREEN)
                }
            is TermsFragment ->
                if (fragment.tag != LoginScreen.TC_SCREEN.value) {
                    navigateTo(fragment, LoginScreen.TC_SCREEN)
                }
            is RequiredFieldsFragment ->
                if (fragment.tag != LoginScreen.REQUIRED_FIELDS_SCREEN.value) {
                    navigateTo(fragment, LoginScreen.REQUIRED_FIELDS_SCREEN)
                }
            is InboxFragment ->
                if (fragment.tag != LoginScreen.CHECK_INBOX_SCREEN.value) {
                    navigateTo(fragment, LoginScreen.CHECK_INBOX_SCREEN)
                }
            is VerificationFragment ->
                if (fragment.tag != LoginScreen.VERIFICATION_SCREEN.value) {
                    navigateTo(fragment, LoginScreen.VERIFICATION_SCREEN)
                }
        }
    }

    companion object {
        private val TAG = Navigation::class.java.simpleName
    }
}

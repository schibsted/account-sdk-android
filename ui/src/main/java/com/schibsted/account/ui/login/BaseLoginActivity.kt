/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.annotation.Nullable
import android.support.annotation.StringRes
import android.support.annotation.VisibleForTesting
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.common.tracking.UiTracking
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.session.User
import com.schibsted.account.ui.KeyboardManager
import com.schibsted.account.ui.R
import com.schibsted.account.ui.UiConfiguration
import com.schibsted.account.ui.login.flow.password.FlowSelectionListener
import com.schibsted.account.ui.login.screen.LoginScreen
import com.schibsted.account.ui.login.screen.identification.ui.EmailIdentificationFragment
import com.schibsted.account.ui.navigation.Navigation
import com.schibsted.account.ui.navigation.NavigationListener
import com.schibsted.account.ui.ui.FlowFragment
import com.schibsted.account.ui.ui.WebFragment
import com.schibsted.account.util.DeepLink
import com.schibsted.account.util.DeepLinkHandler
import kotlinx.android.synthetic.main.schacc_mobile_activity_layout.*

abstract class BaseLoginActivity : AppCompatActivity(), KeyboardManager, NavigationListener {

    companion object {
        private val TAG = "${Logger.DEFAULT_TAG}-BLA"
        private val KEY_SCREEN = "SCREEN"
        private val KEY_FLOW_TYPE = "FLOW_TYPE"
        @JvmField
        val EXTRA_USER = "USER_USER"
        @JvmField
        val KEY_CURRENT_IDENTIFIER = "CURRENT_IDENTIFIER"
        @JvmField
        val KEY_UI_CONFIGURATION = "UI_CONFIGURATION"

        @Nullable
        @JvmStatic
        var tracker: UiTracking? = null
    }

    /**
     * defines the state of the keyboard visibility
     * `true` if the keyboard is visible
     * `false` otherwise
     */
    private var keyboardIsOpen: Boolean = false
    @JvmField
    protected var menu: Menu? = null
    @JvmField
    protected var screen: LoginScreen? = null
    protected var activeFlowType: FlowSelectionListener.FlowType? = null
    var currentIdentifier: Identifier? = null

    /**
     * defines the first element of the layout, this is the main container of the activity
     */
    private lateinit var activityRoot: View

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal lateinit var uiConfiguration: UiConfiguration
    private lateinit var layoutListener: ViewTreeObserver.OnGlobalLayoutListener
    lateinit var navigationController: Navigation
        protected set
    lateinit var fragmentProvider: FragmentProvider
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uiConf: UiConfiguration? = intent.getParcelableExtra(KEY_UI_CONFIGURATION)
        this.uiConfiguration = if (uiConf != null) {
            uiConf
        } else {
            Logger.warn(Logger.DEFAULT_TAG, {
                "Configuration not found in intent, falling back to parsing the manifest. " +
                        "If the activity is created from a deep link, this is to be expected."
            })
            UiConfiguration.Builder.fromManifest(applicationContext).build()
        }

        theme.applyStyle(R.style.schacc_NoActionBar, true)
        setContentView(R.layout.schacc_mobile_activity_layout)
        setUpActionBar(this.uiConfiguration.headerResource)

        activityRoot = findViewById(R.id.activity_layout)
        navigationController = Navigation(this, this)

        if (savedInstanceState != null) {
            savedInstanceState.getString(KEY_SCREEN)?.let { screen = LoginScreen.valueOf(savedInstanceState.getString(KEY_SCREEN)) }
            savedInstanceState.getParcelable<Parcelable>(KEY_CURRENT_IDENTIFIER)?.let { currentIdentifier = savedInstanceState.getParcelable(KEY_CURRENT_IDENTIFIER) }
            if (savedInstanceState.getInt(KEY_FLOW_TYPE) == 1) {
                activeFlowType = FlowSelectionListener.FlowType.LOGIN
            } else if (savedInstanceState.getInt(KEY_FLOW_TYPE) == 2) {
                activeFlowType = FlowSelectionListener.FlowType.SIGN_UP
            }
        } else {
            BaseLoginActivity.tracker?.resetContext()
            BaseLoginActivity.tracker?.flowVariant = TrackingData.FlowVariant.PASSWORD
        }

        fragmentProvider = FragmentProvider(uiConfiguration)

        followDeepLink(intent.dataString)
    }

    private fun followDeepLink(dataString: String?) {
        val action = DeepLinkHandler.resolveDeepLink(dataString)
        when (action) {
            is DeepLink.ValidateAccount -> {
                validateAccount(action)
            }
            is DeepLink.IdentifierProvided -> {
                if (navigationController.currentFragment?.tag == LoginScreen.WEB_FORGOT_PASSWORD_SCREEN.value) {
                    navigationController.navigateBackTo(LoginScreen.PASSWORD_SCREEN)
                } else if (navigationController.currentFragment?.tag == LoginScreen.IDENTIFICATION_SCREEN.value) {
                    val frag = navigationController.currentFragment as EmailIdentificationFragment
                    frag.prefillIdentifier(action.identifier)
                }
            }
        }
    }

    private fun validateAccount(state: DeepLink.ValidateAccount) {
        Logger.info(TAG, { "Attempting login from deep link, extracting code" })
        BaseLoginActivity.tracker?.eventActionSuccessful(TrackingData.SpidAction.ACCOUNT_VERIFIED)

        User.fromSessionCode(state.code, uiConfiguration.redirectUri.toString(), state.isPersistable,
                ResultCallback.fromLambda(
                        { error ->
                            Logger.info(TAG, { "Automatic login after account validation failed: ${error.message}" })
                        },
                        { user ->
                            Logger.info(TAG, { "Automatic login after account validation was successful" })
                            BaseLoginActivity.tracker?.eventActionSuccessful(TrackingData.SpidAction.LOGIN_COMPLETED, user.userId.legacyId)
                            navigationController.finishFlow(user)
                        }
                ))
    }

    /**
     * set up the keyboard actionListener
     * [FlowFragment.onVisibilityChanged] is called when a layout change occurs due to
     * a change of the soft keyboard visibility
     */
    private fun setUpKeyboardListener() {
        val keyboardThreshold = 150f
        layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            private val usableSpace = Rect()
            private val pxKeyboardThreshold = keyboardThreshold * (resources.displayMetrics.densityDpi / 160f)
            private val visibleThreshold = Math.round(pxKeyboardThreshold)
            private var isOpenAlready = false

            override fun onGlobalLayout() {
                activityRoot.getWindowVisibleDisplayFrame(usableSpace)
                val heightDiff = activityRoot.rootView.height - usableSpace.height()

                keyboardIsOpen = heightDiff > visibleThreshold

                if (keyboardIsOpen != isOpenAlready) {
                    isOpenAlready = keyboardIsOpen
                    onKeyboardVisibilityChanged(keyboardIsOpen)
                }
            }
        }

        activityRoot.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }

    override fun onResume() {
        super.onResume()
        setUpKeyboardListener()
        navigationController.register(this)
    }

    override fun onPause() {
        super.onPause()
        activityRoot.viewTreeObserver.removeGlobalOnLayoutListener(layoutListener)
        navigationController.unregister()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SCREEN, screen?.value)
        outState.putInt(KEY_FLOW_TYPE, activeFlowType?.let { if (isUserAvailable()) 2 else 1 }
                ?: 0)
        outState.putParcelable(KEY_CURRENT_IDENTIFIER, currentIdentifier)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item == menu?.findItem(R.id.close_flow)) {
            navigationController.finishNavigation()
            return true
        }

        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.schacc_menu, menu)
        updateActionBar()
        return true
    }

    /**
     * setup the [ActionBar] depending on client configurations
     * if the [ActionBar] is provided by the Theme the UI flow will use it
     * else a [Toolbar] need to be set up.
     */
    private fun setUpActionBar(@DrawableRes headerResource: Int) {
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            headerResource.takeIf { it != 0 }?.apply {
                toolbar_logo.setImageResource(headerResource)
            }
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            toolbar_title.setTextColor(ContextCompat.getColor(this, R.color.schacc_primaryHeader))
            toolbar_back_arrow.setOnClickListener { onBackPressed() }
            supportActionBar!!.elevation = 1f
        }
    }

    override fun isKeyboardOpen(): Boolean = keyboardIsOpen

    /**
     * Closes down the keyboard
     * On some devices keyboard may still be showing up, even on screen without field to fill in
     * Because the keyboard is not part of the application a different behavior might occurs depending
     * on the system implementation.
     */
    override fun closeKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun updateActionBar() {
        updateTitle(screen)
        menu?.findItem(R.id.close_flow)?.isVisible = uiConfiguration.isClosingAllowed
        if (screen == LoginScreen.IDENTIFICATION_SCREEN) {
            toolbar_back_arrow.visibility = View.GONE
        } else {
            toolbar_back_arrow.visibility = View.VISIBLE
        }
    }

    /**
     * Update the title of the activity depending on the screen displayed
     *
     * @param screen the screen currently displayed
     * @see LoginScreen
     */
    private fun updateTitle(screen: LoginScreen?) {
        @StringRes
        val title: Int = when (screen) {
            LoginScreen.IDENTIFICATION_SCREEN -> if (this.uiConfiguration.signUpEnabled) R.string.schacc_identification_title else R.string.schacc_identification_login_only_title
            LoginScreen.PASSWORD_SCREEN -> if (isUserAvailable()) R.string.schacc_register_title else R.string.schacc_welcome_back_title
            LoginScreen.TC_SCREEN -> R.string.schacc_terms_title
            LoginScreen.REQUIRED_FIELDS_SCREEN -> R.string.schacc_required_fields_title
            LoginScreen.CHECK_INBOX_SCREEN -> R.string.schacc_inbox_check_inbox_title
            LoginScreen.WEB_FORGOT_PASSWORD_SCREEN -> R.string.schacc_web_forgot_password_title
            LoginScreen.WEB_NEED_HELP_SCREEN -> R.string.schacc_web_need_help_title
            LoginScreen.VERIFICATION_SCREEN -> R.string.schacc_verification_title
            LoginScreen.WEB_TC_SCREEN -> return
            else -> throw Resources.NotFoundException("Resource not found")
        }
        toolbar_title.setText(title)
    }

    fun onKeyboardVisibilityChanged(keyboardOpen: Boolean) {
        if (navigationController.currentFragment is FlowFragment<*>) {
            (navigationController.currentFragment as FlowFragment<*>).onVisibilityChanged(keyboardOpen)
        }
    }

    override fun onBackPressed() {
        when (screen) {
            LoginScreen.VERIFICATION_SCREEN,
            LoginScreen.PASSWORD_SCREEN,
            LoginScreen.CHECK_INBOX_SCREEN -> {
                currentIdentifier = null
            }
            else -> {
            }
        }
    }

    fun isUserAvailable() = activeFlowType == FlowSelectionListener.FlowType.SIGN_UP

    override fun onWebViewNavigationRequested(where: WebFragment, loginScreen: LoginScreen) {
        navigationController.navigateToWebView(where, loginScreen)
    }

    override fun onDialogNavigationRequested(where: DialogFragment) {
        navigationController.navigationToDialog(where)
    }

    override fun onNavigateBackRequested() {
        onBackPressed()
    }

    override fun onNavigationDone(screen: LoginScreen) {
        this.screen = screen
        updateActionBar()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        followDeepLink(intent.dataString)
    }
}

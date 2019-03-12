/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.annotation.VisibleForTesting
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import com.google.gson.Gson
import com.schibsted.account.AccountService
import com.schibsted.account.ClientConfiguration
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.common.tracking.UiTracking
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.controller.LoginController
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.network.Environment
import com.schibsted.account.network.response.ClientInfo
import com.schibsted.account.persistence.LocalSecretsProvider
import com.schibsted.account.ui.AccountUi
import com.schibsted.account.ui.InternalUiConfiguration
import com.schibsted.account.ui.KeyboardController
import com.schibsted.account.ui.OptionalConfiguration
import com.schibsted.account.ui.R
import com.schibsted.account.ui.UiUtil
import com.schibsted.account.ui.login.flow.password.FlowSelectionListener
import com.schibsted.account.ui.login.flow.password.LoginContractImpl
import com.schibsted.account.ui.login.screen.LoginScreen
import com.schibsted.account.ui.login.screen.identification.ui.AbstractIdentificationFragment
import com.schibsted.account.ui.login.screen.identification.ui.EmailIdentificationFragment
import com.schibsted.account.ui.login.screen.password.PasswordFragment
import com.schibsted.account.ui.login.screen.verification.VerificationFragment
import com.schibsted.account.ui.navigation.Navigation
import com.schibsted.account.ui.navigation.NavigationListener
import com.schibsted.account.ui.smartlock.SmartlockController
import com.schibsted.account.ui.smartlock.SmartlockTask
import com.schibsted.account.ui.ui.FlowFragment
import com.schibsted.account.ui.ui.WebFragment
import com.schibsted.account.ui.ui.dialog.LoadingDialogFragment
import com.schibsted.account.util.DeepLink
import com.schibsted.account.util.DeepLinkHandler
import kotlinx.android.synthetic.main.schacc_mobile_activity_layout.*
import kotlin.properties.Delegates

abstract class BaseLoginActivity : AppCompatActivity(), NavigationListener {

    companion object {
        private val TAG = BaseLoginActivity::class.java.simpleName
        private const val KEY_SCREEN = "SCREEN"
        const val EXTRA_USER = "USER_USER"
        const val KEY_SMARTLOCK_CREDENTIALS = "CREDENTIALS"
        @JvmStatic
        var tracker by Delegates.observable<UiTracking?>(null) { _, _, newValue ->
            val conf = ClientConfiguration.get()
            newValue?.clientId = conf.clientId
            newValue?.loginRealm = when (conf.environment) {
                Environment.ENVIRONMENT_PRODUCTION_NORWAY -> "spid.no"
                else -> "schibsted.com"
            }
        }
        var isLanguageOverridden = false
    }

    private var idProvider: InputProvider<Identifier>? = null

    protected lateinit var loginContract: LoginContractImpl
    @JvmField
    protected var menu: Menu? = null
    @JvmField
    protected var screen: LoginScreen? = null

    lateinit var viewModel: LoginActivityViewModel
    lateinit var accountService: AccountService
    lateinit var navigationController: Navigation
        protected set
    lateinit var fragmentProvider: FragmentProvider
        protected set

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal lateinit var uiConfiguration: InternalUiConfiguration
    internal var loginController: LoginController? = null
    internal var smartlockController: SmartlockController? = null

    private lateinit var params: AccountUi.Params
    private lateinit var flowType: AccountUi.FlowType
    private lateinit var keyboardController: KeyboardController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        params = intent.extras?.let { AccountUi.Params(it) } ?: AccountUi.Params()
        flowType = intent.getStringExtra(AccountUi.KEY_FLOW_TYPE)
                ?.let { AccountUi.FlowType.valueOf(it) }
                ?: AccountUi.FlowType.PASSWORD

        initializeUi()

        params.locale?.let {
            if (!isLanguageOverridden) {
                UiUtil.updateContextLocale(this, it)
                isLanguageOverridden = true
                recreate()
            }
        }

        accountService = AccountService(applicationContext)
        lifecycle.addObserver(accountService)
        navigationController = Navigation(this, this)
        keyboardController = KeyboardController(this)
        uiConfiguration = initializeConfiguration()
        fragmentProvider = FragmentProvider(uiConfiguration, navigationController)

        val smartlockTask = SmartlockTask(params.smartLockMode)
        viewModel = ViewModelProviders.of(this, LoginActivityViewModelFactory(smartlockTask, uiConfiguration.redirectUri, params)).get(LoginActivityViewModel::class.java)

        viewModel.smartlockCredentials.value = intent.getParcelableExtra(KEY_SMARTLOCK_CREDENTIALS)
        initializePropertiesFromBundle(savedInstanceState)

        loginContract = LoginContractImpl(this, viewModel)

        if (SmartlockController.isSmartlockAvailable()) {
            smartlockController = SmartlockController(this, viewModel.smartlockReceiver)
        }

        val action = DeepLinkHandler.resolveDeepLink(intent.dataString)
        if (action is DeepLink.ValidateAccount) {
            followDeepLink(intent.dataString, action, navigationController.currentFragment?.tag)
        } else {
            viewModel.initializeSmartlock()
        }

        viewModel.startSmartLockFlow.observe(this, Observer { launchSmartlock ->
            launchSmartlock?.let {
                if (launchSmartlock) {
                    smartlockController?.start()
                }
            }
        })

        viewModel.smartlockResolvingState.observe(this, Observer { isResolving ->
            isResolving?.let {
                progressBar.visibility = if (it) View.VISIBLE else View.GONE
            }
        })

        viewModel.smartlockResult.observe(this, Observer { result ->
            when (result) {
                is SmartlockTask.SmartLockResult.Success -> {
                    if (result.requestCode == SmartlockController.RC_CHOOSE_ACCOUNT) {
                        smartlockController?.provideCredential(result.credentials)
                    } else {
                        smartlockController?.provideHint(result.credentials)
                        fragmentProvider = FragmentProvider(uiConfiguration, navigationController)
                    }
                }
                is SmartlockTask.SmartLockResult.Failure -> {
                    if (result.resultCode != Activity.RESULT_OK) {
                        Logger.info(TAG, "Smartlock login failed - smartlockController mode ${params.smartLockMode.name}")
                        setResult(AccountUi.SMARTLOCK_FAILED, intent)
                        progressBar.visibility = View.GONE
                        finish()
                    }
                }
            }
        })

        viewModel.user.observe(this, Observer { user ->
            if (user == null) {
                loadRequiredInformation(idProvider)
            } else {
                navigationController.finishFlow(user)
            }
        })

        viewModel.clientResult.observe(this, Observer {
            val result = it?.get()
            when (result) {
                is LoginActivityViewModel.ClientResult.Success -> {
                    BaseLoginActivity.tracker?.merchantId = result.clientInfo.merchantId
                    navigateToIdentificationFragment(result.clientInfo, viewModel, idProvider)
                }
                is LoginActivityViewModel.ClientResult.Failure -> {
                    setResult(AccountUi.RESULT_ERROR, Intent().putExtra(AccountUi.EXTRA_ERROR, result.error))
                    finish()
                }
            }
        })

        viewModel.clientResolvingState.observe(this, Observer {
            if (it == true) {
                navigationController.navigationToDialog(LoadingDialogFragment())
            } else {
                navigationController.dismissDialog()
            }
        })

        viewModel.uiConfiguration.observe(this, Observer { configuration ->
            configuration?.let { uiConfiguration = it }
        })

        keyboardController.keyboardVisibility.observe(this, Observer {
            (navigationController.currentFragment as? FlowFragment<*>)?.onVisibilityChanged(it == true)
        })
    }

    override fun attachBaseContext(base: Context) {
        val locale = OptionalConfiguration.fromManifest(base.applicationContext).locale
        if (locale == null || isLanguageOverridden) {
            super.attachBaseContext(base)
        } else {
            super.attachBaseContext(UiUtil.updateContextLocale(base, locale))
        }
    }

    private fun initializeUi() {
        theme.applyStyle(R.style.schacc_NoActionBar, true)
        setContentView(R.layout.schacc_mobile_activity_layout)
        setUpActionBar()
    }

    private fun initializeConfiguration(): InternalUiConfiguration {
        val idType = if (flowType == AccountUi.FlowType.PASSWORDLESS_SMS) Identifier.IdentifierType.SMS else Identifier.IdentifierType.EMAIL
        return InternalUiConfiguration.resolve(application, params, idType)
    }

    private fun initializePropertiesFromBundle(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            savedInstanceState.getString(KEY_SCREEN)?.let { screen = LoginScreen.valueOf(savedInstanceState.getString(KEY_SCREEN)) }
        } else {
            BaseLoginActivity.tracker?.resetContext()
            BaseLoginActivity.tracker?.flowVariant = TrackingData.FlowVariant.PASSWORD
        }
    }

    fun loadRequiredInformation(provider: InputProvider<Identifier>? = null) {
        idProvider = provider
        viewModel.getClientInfo(intent.getParcelableExtra(AccountUi.KEY_CLIENT_INFO))
    }

    private fun followDeepLink(dataString: String?, deepLink: DeepLink?, fragmentTag: String?) {
        when (deepLink) {
            is DeepLink.ValidateAccount -> {
                viewModel.loginFromDeepLink(deepLink)
            }
            is DeepLink.IdentifierProvided -> {
                if (fragmentTag == LoginScreen.WEB_FORGOT_PASSWORD_SCREEN.value) {
                    navigationController.navigateBackTo(LoginScreen.PASSWORD_SCREEN)
                } else if (fragmentTag == LoginScreen.IDENTIFICATION_SCREEN.value) {
                    LocalSecretsProvider(application.applicationContext).get(deepLink.identifier)?.let {
                        val frag = (navigationController.currentFragment as EmailIdentificationFragment)
                        val id = Gson().fromJson(it, Identifier::class.java)
                        id.identifier
                                .takeIf { Patterns.EMAIL_ADDRESS.matcher(it).matches() }
                                ?.let { frag.prefillIdentifier(id.identifier) }
                    }
                }
            }
            else -> {
                if (viewModel.isDeepLinkRequestNewPassword(dataString) && fragmentTag == LoginScreen.PASSWORD_SCREEN.value) {
                    navigationController.navigateBackTo(LoginScreen.PASSWORD_SCREEN)
                }
            }
        }
    }

    private fun navigateToIdentificationFragment(clientInfo: ClientInfo, flowSelectionListener: FlowSelectionListener?, provider: InputProvider<Identifier>?) {
        val fragment = fragmentProvider.getOrCreateIdentificationFragment(
                provider = provider,
                flowType = flowType,
                flowSelectionListener = flowSelectionListener,
                clientInfo = clientInfo)
        navigationController.navigateToFragment(fragment as AbstractIdentificationFragment)
    }

    override fun onResume() {
        super.onResume()
        keyboardController.register(navigationController.currentFragment)
        navigationController.register(this)
    }

    override fun onPause() {
        super.onPause()
        keyboardController.unregister(navigationController.currentFragment)
        navigationController.unregister()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SCREEN, screen?.value)
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
        val closeItem = menu.findItem(R.id.close_flow)
        closeItem.icon = UiUtil.getTintDrawable(this, closeItem.icon, R.color.schacc_toolbarIconsColor)
        navigationController.currentFragment?.let { updateActionBar() }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        loadRequiredInformation()
        viewModel.updateSmartlockCredentials(requestCode, resultCode, data?.getParcelableExtra(SmartlockController.EXTRA_SMARTLOCK_CREDENTIALS))
    }

    /**
     * setup the [Toolbar] depending on client configurations
     * if the [Toolbar] is provided by the Theme the UI flow will use it
     * else a [Toolbar] need to be set up.
     */
    private fun setUpActionBar() {
        setSupportActionBar(toolbar)
        toolbar_title.setTextColor(ContextCompat.getColor(this, R.color.schacc_primaryHeader))
        toolbar_title.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                val rect = Rect()
                toolbar_title.getGlobalVisibleRect(rect)
                // if we click on the drawable attached to the editText
                if (motionEvent.rawX <= toolbar_title.totalPaddingLeft + (rect.left / 2)) {
                    onBackPressed()
                }
            }
            false
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.elevation = 1f
    }

    private fun updateActionBar() {
        updateTitle(screen)
        menu?.findItem(R.id.close_flow)?.isVisible = uiConfiguration.isClosingAllowed
        if (screen == LoginScreen.IDENTIFICATION_SCREEN) {
            toolbar_title.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        } else {
            val drawable = ContextCompat.getDrawable(this, R.drawable.schacc_ic_arrow_back)
            drawable?.setColorFilter(ContextCompat.getColor(this, R.color.schacc_toolbarIconsColor), PorterDuff.Mode.SRC_IN)
            toolbar_title.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
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
            LoginScreen.PASSWORD_SCREEN -> if (viewModel.isUserAvailable()) R.string.schacc_register_title else R.string.schacc_welcome_back_title
            LoginScreen.TC_SCREEN -> R.string.schacc_terms_title
            LoginScreen.REQUIRED_FIELDS_SCREEN -> R.string.schacc_required_fields_title
            LoginScreen.CHECK_INBOX_SCREEN -> R.string.schacc_inbox_check_inbox_title
            LoginScreen.WEB_FORGOT_PASSWORD_SCREEN -> R.string.schacc_web_forgot_password_title
            LoginScreen.WEB_NEED_HELP_SCREEN -> R.string.schacc_web_need_help_title
            LoginScreen.VERIFICATION_SCREEN -> R.string.schacc_verification_title
            LoginScreen.WEB_TC_SCREEN -> return
            else -> if (SmartlockController.isSmartlockAvailable()) R.string.schacc_identification_login_only_title else throw Resources.NotFoundException("Resource not found")
        }
        toolbar_title.text = getString(title)
    }

    override fun onBackPressed() {
        when (screen) {
            LoginScreen.VERIFICATION_SCREEN,
            LoginScreen.PASSWORD_SCREEN,
            LoginScreen.CHECK_INBOX_SCREEN -> {
                viewModel.userIdentifier = null
            }
            else -> {
            }
        }
    }

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
        keyboardController.register(navigationController.currentFragment)
        if (!LoginScreen.isWebView(screen.value)) {
            val customFields = mutableMapOf<String, Any>()
            val fragment = navigationController.currentFragment

            when (fragment) {
                is AbstractIdentificationFragment -> fragment.isTeaserEnabled().let { customFields["teaser"] to it }
                is PasswordFragment -> fragment.isRememberMeEnabled().let { customFields["keepLoggedIn"] to it }
                is VerificationFragment -> fragment.isRememberMeEnabled.let { customFields["keepLoggedIn"] to it }
                else -> {
                }
            }
            BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.VIEW, UiUtil.getTrackingScreen(screen)!!, customFields)
        }
        updateActionBar()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val action = DeepLinkHandler.resolveDeepLink(intent.dataString)
        followDeepLink(intent.dataString, action, navigationController.currentFragment?.tag)
    }
}

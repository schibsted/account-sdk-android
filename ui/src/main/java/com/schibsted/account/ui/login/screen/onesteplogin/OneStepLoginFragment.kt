/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.onesteplogin

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.schibsted.account.Routes
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.ClientInfo
import com.schibsted.account.persistence.LocalSecretsProvider
import com.schibsted.account.ui.InternalUiConfiguration
import com.schibsted.account.ui.KeyboardController
import com.schibsted.account.ui.R
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.screen.LoginScreen
import com.schibsted.account.ui.login.screen.identification.ui.MobileIdentificationFragment
import com.schibsted.account.ui.ui.FlowFragment
import com.schibsted.account.ui.ui.InputField
import com.schibsted.account.ui.ui.WebFragment
import com.schibsted.account.ui.ui.component.CheckBoxView
import com.schibsted.account.ui.ui.component.PasswordView

import com.schibsted.account.ui.ui.component.SingleFieldView
import com.schibsted.account.ui.ui.dialog.InformationDialogFragment
import com.schibsted.account.ui.ui.rule.EmailValidationRule
import com.schibsted.account.util.DeepLink
import com.schibsted.account.util.KeyValueStore


private const val KEY_IDENTIFIER = "IDENTIFIER"
private const val KEY_USER_AVAILABLE = "USER_EXISTING"
private const val KEY_UI_CONF = "UI_CONF"
/**
 * a [Fragment] displaying the one step login screen
 */
class OneStepLoginFragment : FlowFragment<OneStepLoginContract.Presenter>(), OneStepLoginContract.View{
    /**
     * The presenter tied with this [com.schibsted.account.ui.login.screen.onesteplogin.OneStepLoginContract.View]
     */
    private lateinit var loginPresenter: OneStepLoginContract.Presenter

    private var args: Bundle? = null
    private var identifier: Identifier? = null
    private lateinit var clientInfo: ClientInfo
    override val isActive: Boolean
        get() = isAdded

    private lateinit var inputFieldView: SingleFieldView
    private lateinit var credInputFieldView: PasswordView
    private lateinit var rememberMe: CheckBoxView
    private lateinit var rememberMeInfo: TextView
    private lateinit var teaserText: TextView
    private lateinit var forgotPasswordLink: TextView

    /**
     * ties a presenter to this view
     *
     * @param presenter the presenter to tie with this view
     */
    override fun setPresenter(presenter: OneStepLoginContract.Presenter) {
        loginPresenter = presenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = savedInstanceState ?: arguments

        clientInfo = args!!.getParcelable(KEY_CLIENT_INFO)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        BaseLoginActivity.tracker?.resetContext()

        val view = inflater.inflate(R.layout.schacc_one_step_login_fragment_layout, container, false)

        primaryActionView = view.findViewById(R.id.onestep_identification_button_continue)
        secondaryActionView = view.findViewById(R.id.onestep_identification_button_signup)

        inputFieldView = view.findViewById(R.id.onestep_login_input_view)
        credInputFieldView = view.findViewById(R.id.onestep_password_input_view)

        rememberMe = view.findViewById(R.id.onestep_login_remember_me)
        rememberMeInfo = view.findViewById(R.id.onestep_login_remember_me_info)
        teaserText = view.findViewById(R.id.schacc_teaser_text)

        val schibstedLogo = view.findViewById<ImageView>(R.id.schibsted_logo)
        val clientLogo = view.findViewById<ImageView>(R.id.client_logo)
        forgotPasswordLink = view.findViewById(R.id.onestep_login_forgot_password)

        if (uiConf.teaserText?.isNotEmpty() == true) {
            this.teaserText.text = uiConf.teaserText
            this.teaserText.visibility = View.VISIBLE
        }

        if (uiConf.clientLogo != 0) {
            clientLogo.visibility = View.VISIBLE
            clientLogo.setImageResource(uiConf.clientLogo)
        } else {
            clientLogo.visibility = View.GONE
            schibstedLogo.layoutParams = clientLogo.layoutParams
        }

        inputFieldView.isCancelable = true
        inputFieldView.inputField.hint = getString(R.string.schacc_required_field_email)

        credInputFieldView.setTitle(R.string.schacc_password_sign_in_label)

        prefillIdentifier(uiConf.identifier)
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rememberMe.isChecked = true

        if (uiConf.showRememberMeEnabled) {
            rememberMe.labelView.text = getString(R.string.schacc_remember_me)
            rememberMeInfo.setOnClickListener {
                navigationListener?.onDialogNavigationRequested(
                        InformationDialogFragment.newInstance(
                                getString(R.string.schacc_dialog_remember_me_title),
                                getString(R.string.schacc_dialog_remember_me_description),
                                R.drawable.schacc_ic_remember_me,
                                null
                        ))
            }
        } else {
            rememberMe.visibility = View.GONE
            rememberMeInfo.visibility = View.GONE
        }

        forgotPasswordLink.setOnClickListener {
            BaseLoginActivity.tracker?.eventEngagement(TrackingData.Engagement.CLICK, TrackingData.UIElement.FORGOT_PASSWORD, TrackingData.Screen.ONE_STEP_LOGIN)

            val idKey = identifier?.let {
                LocalSecretsProvider(requireContext()).put(GSON.toJson(it))
            }

            val redirectUri = DeepLink.IdentifierProvided.createDeepLinkUri(uiConf.redirectUri, idKey
                    ?: "")

            navigationListener?.onWebViewNavigationRequested(
                    WebFragment.newInstance(Routes.forgotPasswordUrl(redirectUri, uiConf.locale).toString(), uiConf.redirectUri), LoginScreen.WEB_FORGOT_PASSWORD_SCREEN)
        }

        inputFieldView.inputField.requestFocus()
        if (inputFieldView.inputField.text.isNullOrBlank()) {
            Logger.debug(TAG, "Showing keyboard")
            activity?.let { KeyboardController.showKeyboard(it) }
        }

        secondaryActionView?.visibility = if (uiConf.signUpEnabled) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        registerLoginListeners()
    }

    internal fun prefillIdentifier(identifier: String?) {
        Logger.info(TAG, "Attempting to prefill  email")
        if (identifier.isNullOrEmpty()) {
            Logger.info(TAG, "email wasn't found in config")
            val storedEmailPrefillValue: String? = this.context?.let { KeyValueStore(it).readEmailPrefillValue() }
            storedEmailPrefillValue?.let {
                Logger.info(TAG, "email has been prefilled from stored value")
                inputFieldView.inputField.setText(it)
            }
        } else {
            if (EmailValidationRule.isValid(identifier)) {
                inputFieldView.inputField.setText(identifier)
                Logger.info(TAG, "email has been prefilled")
            } else {
                Logger.warn(TAG, "Failed to prefill the email - Wrong format")
            }
        }
    }
    /**
     * setup listeners for the sign in views of this class
     */
    private fun registerLoginListeners() {
        inputFieldView.setImeAction(EditorInfo.IME_ACTION_NEXT) { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                identifyUser(inputFieldView, TrackingData.Screen.ONE_STEP_LOGIN)
            }
            false
        }

        credInputFieldView.setImeAction(EditorInfo.IME_ACTION_DONE) { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                identifyUser(inputFieldView, TrackingData.Screen.ONE_STEP_LOGIN)
                signInUser()
            }
            false
        }


        primaryActionView.setOnClickListener {
            identifyUser(inputFieldView, TrackingData.Screen.ONE_STEP_LOGIN)
            signInUser()
        }

        secondaryActionView?.setOnClickListener {
            showSignup()
        }
    }

    /**
     * setup listeners for the sign up views of this class
     */
    private fun registerSignUpListeners() {
        inputFieldView.setImeAction(EditorInfo.IME_ACTION_NEXT) { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                identifyUser(inputFieldView, TrackingData.Screen.ONE_STEP_SIGNUP)
            }
            false
        }

        credInputFieldView.setImeAction(EditorInfo.IME_ACTION_DONE) { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                identifyUser(inputFieldView, TrackingData.Screen.ONE_STEP_SIGNUP)
                signUpUser()
            }
            false
        }


        primaryActionView.setOnClickListener {
            identifyUser(inputFieldView, TrackingData.Screen.ONE_STEP_SIGNUP)
            signUpUser()
        }

        secondaryActionView?.setOnClickListener {
            showSignIn()
        }
    }

    private fun signInUser() {
        BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.ONE_STEP_LOGIN)
        identifier = args?.getParcelable(KEY_IDENTIFIER)

        loginPresenter.signIn(inputFieldView, credInputFieldView, rememberMe.isChecked, viewLifecycleOwner, this.context?.let { KeyValueStore(it) })
    }

    private fun signUpUser() {
        BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.ONE_STEP_SIGNUP)
        identifier = args?.getParcelable(KEY_IDENTIFIER)
        loginPresenter.signup(inputFieldView, credInputFieldView, rememberMe.isChecked, viewLifecycleOwner)
    }

    private fun showSignIn() {
        BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.ONE_STEP_LOGIN)
        loginPresenter.startSignin()
        credInputFieldView.setTitle(R.string.schacc_password_sign_in_label)
        credInputFieldView.setInformationMessage("")
        credInputFieldView.hideErrorView()
        inputFieldView.hideErrorView()
        forgotPasswordLink.visibility = View.VISIBLE
        secondaryActionView?.setText(R.string.schacc_register_title)
        registerLoginListeners()

    }

    private fun showSignup() {
        BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.ONE_STEP_SIGNUP)
        credInputFieldView.hideErrorView()
        inputFieldView.hideErrorView()
        loginPresenter.startSignup()
        credInputFieldView.setTitle(R.string.schacc_password_sign_up_label)
        credInputFieldView.setInformationMessage(getString(R.string.schacc_password_extra_info))
        forgotPasswordLink.visibility = View.GONE
        secondaryActionView?.setText(R.string.schacc_password_sign_up_have_account_button_label)
        registerSignUpListeners()
    }

    protected fun identifyUser(inputField: InputField, trackingScreen: TrackingData.Screen) {
        BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.SEND, trackingScreen)
        loginPresenter.verifyInput(inputField, uiConf.identifierType, uiConf.signUpEnabled, uiConf.signUpNotAllowedErrorMessage)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_UI_CONF, uiConf)
        outState.putParcelable(KEY_CLIENT_INFO, clientInfo)
    }

    override fun showErrorDialog(error: ClientError, errorMessage: String?) {
        displayErrorDialog(error, errorMessage)
    }


    companion object {
        private val TAG = OneStepLoginFragment::class.java.simpleName
        const val KEY_CLIENT_INFO = "KEY_CLIENT_INFO"
        /**
         * provide a new instance of this [Fragment]
         *
         * @param uiConfiguration
         * @return a parametrized instance of [MobileIdentificationFragment]
         */
        fun newInstance(uiConfiguration: InternalUiConfiguration, clientInfo: ClientInfo): OneStepLoginFragment {
            val args = Bundle()
            val fragment = OneStepLoginFragment()
            args.putParcelable(KEY_UI_CONF, uiConfiguration)
            args.putParcelable(KEY_CLIENT_INFO, clientInfo)
            fragment.arguments = args
            return fragment
        }

        private val GSON = Gson()
    }
}


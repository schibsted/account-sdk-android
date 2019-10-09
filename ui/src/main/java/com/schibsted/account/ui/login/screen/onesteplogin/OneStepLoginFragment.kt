/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.onesteplogin

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.schibsted.account.Routes
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.ClientInfo
import com.schibsted.account.network.response.Merchant
import com.schibsted.account.persistence.LocalSecretsProvider
import com.schibsted.account.ui.InternalUiConfiguration
import com.schibsted.account.ui.KeyboardController
import com.schibsted.account.ui.R
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.screen.LoginScreen
import com.schibsted.account.ui.login.screen.identification.ui.EmailIdentificationFragment
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
import kotlinx.android.synthetic.main.schacc_one_step_login_fragment_layout.*


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
    private lateinit var teaserText: TextView

    private var args: Bundle? = null
    private var identifier: Identifier? = null


    /**
     * this reference is used to add a child view in extended class
     *
     * @see MobileIdentificationFragment.onCreateView
     * @see EmailIdentificationFragment.onCreateView
     */
    protected lateinit var idInputViewContainer: FrameLayout
    private lateinit var inputFieldView: SingleFieldView
    private lateinit var credInputFieldView: PasswordView
    private lateinit var remember_me: CheckBoxView
    private lateinit var remember_me_info: TextView


    /**
     * Field used to display the policy of Schibsted account and the clientAccepted.
     */
    //private lateinit var identificationPolicy: TextView

    private lateinit var forgotPasswordLink: TextView
    private lateinit var clientInfo: ClientInfo
    override val isActive: Boolean
        get() = isAdded

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
        idInputViewContainer = view.findViewById(R.id.onestep_identification_input_view)

        credInputFieldView = view.findViewById(R.id.onestep_password_input_view)
        remember_me = view.findViewById(R.id.onestep_login_remember_me)
        remember_me_info = view.findViewById(R.id.onestep_login_remember_me_info)

        //identificationPolicy = view.findViewById(R.id.identification_share_policy)
        teaserText = view.findViewById(R.id.schacc_teaser_text)

        val schibstedLogo = view.findViewById<ImageView>(R.id.schibsted_logo)
        val clientLogo = view.findViewById<ImageView>(R.id.client_logo)

        forgotPasswordLink = view.findViewById(R.id.onestep_login_forgot_password)
        //linkView.setOnClickListener {
         //   navigationListener?.let {
         //       navigationListener?.onWebViewNavigationRequested(WebFragment.newInstance(getString(R.string.schacc_identification_help_link), uiConf.redirectUri), LoginScreen.WEB_NEED_HELP_SCREEN)
         //       BaseLoginActivity.tracker?.eventEngagement(TrackingData.Engagement.CLICK, TrackingData.UIElement.ABOUT_SCH_ACCOUNT, TrackingData.Screen.ONE_STEP_LOGIN)
        //    }
       // }
        @StringRes val msgRes = if (clientInfo.merchant.type == Merchant.EXTERNAL) {
            R.string.schacc_identification_external_information
        } else {
            R.string.schacc_identification_internal_information
        }

        //identificationPolicy.text = getString(msgRes, clientInfo.merchant.name)

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

        inputFieldView = SingleFieldView.create(context!!) {
            isCancelable { true }
            inputType { InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS }
            ime { EditorInfo.IME_ACTION_DONE }
            error { getString(R.string.schacc_email_identification_error) }
            title { getString(R.string.schacc_email_label) }
            hint { getString(R.string.schacc_required_field_email) }
        }

        /*

        credInputFieldView = SingleFieldView.create(context!!) {
            isCancelable { true }
            inputType { InputType.TYPE_TEXT_VARIATION_PASSWORD }
            ime { EditorInfo.IME_ACTION_DONE }
            error { getString(R.string.schacc_password_error_incorrect) }
            title { getString(R.string.schacc_password_sign_up_label) }
            hint { getString(R.string.schacc_password_extra_info) }
        }


         */
        credInputFieldView.setTitle(R.string.schacc_password_sign_in_label)
        credInputFieldView.setInformationMessage(getString(R.string.schacc_password_extra_info))

        idInputViewContainer.addView(inputFieldView)


        // credInputViewContainer.addView(credInputFieldView)
        prefillIdentifier(uiConf.identifier)
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        remember_me.isChecked = true

        if (uiConf.showRememberMeEnabled) {
            remember_me.labelView.text = getString(R.string.schacc_remember_me)
            remember_me_info.setOnClickListener {
                navigationListener?.onDialogNavigationRequested(
                        InformationDialogFragment.newInstance(
                                getString(R.string.schacc_dialog_remember_me_title),
                                getString(R.string.schacc_dialog_remember_me_description),
                                R.drawable.schacc_ic_remember_me,
                                null
                        ))
            }
        } else {
            remember_me.visibility = View.GONE
            remember_me_info.visibility = View.GONE
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
    }

    fun prefillIdentifier(identifier: String?) {
        Logger.info(TAG, "Attempting to prefill  email")
        if (identifier.isNullOrEmpty()) {
            Logger.info(TAG, "email wasn't found")
        } else {
            if (EmailValidationRule.isValid(identifier)) {
                inputFieldView.inputField.setText(identifier)
                Logger.info(TAG, "email has been prefilled")
            } else {
                Logger.warn(TAG, "Failed to prefill the email - Wrong format")
            }
        }
    }


    override fun onResume() {
        super.onResume()
        registerListeners()
    }

    /**
     * setup listeners for the views of this class
     */
    private fun registerListeners() {
        // needs to be changed.

        /*
        primaryActionView.setOnClickListener { identifyUser(inputFieldView) }
*/
        inputFieldView.setImeAction(EditorInfo.IME_ACTION_NEXT) { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                identifyUser(inputFieldView)
            }
            false
        }

        credInputFieldView.setImeAction(EditorInfo.IME_ACTION_DONE) { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                identifyUser(inputFieldView)
                signUser()
            }
            false
        }


        primaryActionView.setOnClickListener {
            identifyUser(inputFieldView)
            signUser()
        }
    }

    private fun signUser() {
        BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.ONE_STEP_LOGIN)
        identifier = args?.getParcelable(KEY_IDENTIFIER)

        loginPresenter.sign(onestep_password_input_view, remember_me.isChecked, viewLifecycleOwner)
    }

    protected fun identifyUser(inputField: InputField) {
        BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.ONE_STEP_LOGIN)
        loginPresenter.verifyInput(inputField, uiConf.identifierType, uiConf.signUpEnabled, uiConf.signUpNotAllowedErrorMessage)
    }

    fun isTeaserEnabled() = !uiConf.teaserText.isNullOrEmpty()

    /**
     * ties a presenter to this view
     *
     * @param presenter the presenter to tie with this view
     */
    override fun setPresenter(presenter: OneStepLoginContract.Presenter) {
        loginPresenter = presenter
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


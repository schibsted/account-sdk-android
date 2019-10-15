/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.google.gson.Gson
import com.schibsted.account.Routes
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.persistence.LocalSecretsProvider
import com.schibsted.account.ui.InternalUiConfiguration
import com.schibsted.account.ui.KeyboardController
import com.schibsted.account.ui.R
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.screen.LoginScreen
import com.schibsted.account.ui.ui.FlowFragment
import com.schibsted.account.ui.ui.WebFragment
import com.schibsted.account.ui.ui.component.AccountSelectorView
import com.schibsted.account.ui.ui.dialog.InformationDialogFragment
import com.schibsted.account.ui.ui.dialog.SelectorDialog
import com.schibsted.account.ui.ui.rule.BasicValidationRule
import com.schibsted.account.ui.ui.rule.PasswordValidationRule
import com.schibsted.account.util.DeepLink
import com.schibsted.account.util.KeyValueStore
import kotlinx.android.synthetic.main.schacc_password_fragment_layout.*

private const val KEY_IDENTIFIER = "IDENTIFIER"
private const val KEY_USER_AVAILABLE = "USER_EXISTING"
private const val KEY_UI_CONF = "UI_CONF"

class PasswordFragment : FlowFragment<PasswordContract.Presenter>(), PasswordContract.View, AccountSelectorView.Listener {
    override val isActive: Boolean get() = isAdded
    private lateinit var presenter: PasswordContract.Presenter
    private var isUserAvailable: Boolean = false
    private var identifier: Identifier? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arg = savedInstanceState ?: arguments
        isUserAvailable = arg?.getBoolean(KEY_USER_AVAILABLE) ?: false
        identifier = arg?.getParcelable(KEY_IDENTIFIER)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.schacc_password_fragment_layout, container, false)
        primaryActionView = view.findViewById(R.id.password_button_continue)
        secondaryActionView = if (isUserAvailable) null else view.findViewById(R.id.mobile_password_button_forgot)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addUserOptions(isUserAvailable)

        info_bar_message.setCompoundDrawablesWithIntrinsicBounds(R.drawable.schacc_ic_info, 0, 0, 0)

        mobile_password_button_forgot.setOnClickListener {
            BaseLoginActivity.tracker?.eventEngagement(TrackingData.Engagement.CLICK, TrackingData.UIElement.FORGOT_PASSWORD, TrackingData.Screen.PASSWORD)

            val idKey = identifier?.let {
                LocalSecretsProvider(requireContext()).put(GSON.toJson(it))
            }

            val redirectUri = DeepLink.IdentifierProvided.createDeepLinkUri(uiConf.redirectUri, idKey
                    ?: "")

            navigationListener?.onWebViewNavigationRequested(
                    WebFragment.newInstance(Routes.forgotPasswordUrl(redirectUri, uiConf.locale).toString(), uiConf.redirectUri), LoginScreen.WEB_FORGOT_PASSWORD_SCREEN)
        }

        account_selector_view.setAccountIdentifier(arrayListOf(identifier))
        account_selector_view.actionListener = this

        val accSelectDesc = if (isUserAvailable) R.string.schacc_accessibility_signup_id else R.string.schacc_accessibility_login_id
        account_selector_view.contentDescription = getString(accSelectDesc, identifier?.identifier)

        password_input_view.validationRule = (if (isUserAvailable) PasswordValidationRule else BasicValidationRule)
        primaryActionView.setOnClickListener { signUser() }

        password_input_view.setImeAction(EditorInfo.IME_ACTION_NEXT) { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                signUser()
            }
            return@setImeAction false
        }

        password_input_view.requestFocus()
        activity?.let { KeyboardController.showKeyboard(it) }

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
    }

    fun isRememberMeEnabled() = remember_me.isChecked

    private fun signUser() {
        BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.PASSWORD)
        presenter.sign(password_input_view, identifier, remember_me.isChecked, this.context?.let { KeyValueStore(it) })
    }

    private fun addUserOptions(isUserAvailable: Boolean) {
        if (isUserAvailable) {
            mobile_password_button_forgot.visibility = View.GONE
            age_limit_info.visibility = View.VISIBLE
            info_bar_message.visibility = View.VISIBLE
            password_input_view.setTitle(R.string.schacc_password_sign_up_label)
            password_input_view.setInformationMessage(getString(R.string.schacc_password_extra_info))
            password_button_continue.setText(R.string.schacc_password_sign_up_button_label)
        } else {
            password_input_view.setTitle(R.string.schacc_password_sign_in_label)
            mobile_password_button_forgot.visibility = View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_USER_AVAILABLE, isUserAvailable)
        outState.putParcelable(KEY_IDENTIFIER, identifier)
        outState.putParcelable(KEY_UI_CONF, uiConf)
    }

    override fun onDialogRequested(selectorDialog: SelectorDialog) {
        selectorDialog.actionListener = View.OnClickListener {
            BaseLoginActivity.tracker?.eventEngagement(TrackingData.Engagement.CLICK, TrackingData.UIElement.CHANGE_IDENTIFIER, TrackingData.Screen.PASSWORD)
            navigationListener?.onNavigateBackRequested()
            selectorDialog.dismiss()
        }
        navigationListener?.onDialogNavigationRequested(selectorDialog)
    }

    override fun setPresenter(presenter: PasswordContract.Presenter) {
        this.presenter = presenter
    }

    override fun showErrorDialog(error: ClientError, errorMessage: String?) {
        displayErrorDialog(error, errorMessage)
    }

    companion object {
        @JvmStatic
        fun newInstance(identifier: Identifier, isUserAvailable: Boolean, uiConfiguration: InternalUiConfiguration): PasswordFragment {
            val fragment = PasswordFragment()
            val arg = Bundle()
            arg.putParcelable(KEY_IDENTIFIER, identifier)
            arg.putBoolean(KEY_USER_AVAILABLE, isUserAvailable)
            arg.putParcelable(KEY_UI_CONF, uiConfiguration)
            fragment.arguments = arg
            return fragment
        }

        private val GSON = Gson()
    }
}

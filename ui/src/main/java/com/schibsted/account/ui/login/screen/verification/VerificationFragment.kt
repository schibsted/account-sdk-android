/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.verification

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.engine.controller.PasswordlessController
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.R
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.ui.FlowFragment
import com.schibsted.account.ui.ui.component.AccountSelectorView
import com.schibsted.account.ui.ui.component.CheckBoxView
import com.schibsted.account.ui.ui.component.CodeInputView
import com.schibsted.account.ui.ui.dialog.InformationDialogFragment
import com.schibsted.account.ui.ui.dialog.SelectorDialog
import java.util.ArrayList
import java.util.Locale

/**
 * a [Fragment] displaying the code verification screen
 */
class VerificationFragment : FlowFragment<VerificationContract.Presenter>(), VerificationContract.View, AccountSelectorView.Listener {

    /**
     * the presenter of this view
     *
     * @see VerificationPresenter
     */
    private lateinit var mobileVerificationPresenter: VerificationContract.Presenter

    /**
     * the view allowing the user to enter the verification code
     */
    private lateinit var codeInputView: CodeInputView

    private lateinit var rememberMeView: CheckBoxView

    /**
     * the user phone number
     */
    private lateinit var identifier: Identifier
    private lateinit var passwordlessController: PasswordlessController

    val isRememberMeEnabled: Boolean
        get() = this::rememberMeView.isInitialized && rememberMeView.isChecked

    /**
     * This method provide the right action label to build the [InformationDialogFragment]
     *
     * @return the label of the action button contained by the [InformationDialogFragment]
     * @see .showResendCodeView
     */
    private val actionLabel: String
        get() = if (identifier.identifierType == Identifier.IdentifierType.EMAIL) {
            getString(R.string.schacc_verification_edit_email_address)
        } else {
            getString(R.string.schacc_verification_edit_phone_number)
        }

    override val isActive: Boolean
        get() = isAdded

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = savedInstanceState ?: arguments
        arguments?.let {
            identifier = it.getParcelable(KEY_IDENTIFIER)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.schacc_verification_fragment_layout, container, false)
        primaryActionView = view.findViewById(R.id.mobile_verification_button_continue)
        secondaryActionView = view.findViewById(R.id.mobile_verification_button_resend)
        codeInputView = view.findViewById(R.id.verification_code_input_view)
        rememberMeView = view.findViewById(R.id.remember_me)

        val accountSelectorView = view.findViewById<AccountSelectorView>(R.id.identifier_modifier)
        val identifiers = ArrayList<Identifier?>()
        identifiers.add(identifier)
        accountSelectorView.setAccountIdentifier(identifiers)
        accountSelectorView.actionListener = this
        accountSelectorView.contentDescription = getString(R.string.schacc_accessibility_login_id, identifier.identifier)

        rememberMeView.isChecked = true
        rememberMeView.textView.text = getString(R.string.schacc_remember_me)

        val rememberMeInfoView = view.findViewById<TextView>(R.id.remember_me_info)
        rememberMeInfoView.setOnClickListener {
            navigationListener?.onDialogNavigationRequested(
                    InformationDialogFragment.newInstance(
                            getString(R.string.schacc_dialog_remember_me_title),
                            getString(R.string.schacc_dialog_remember_me_description),
                            R.drawable.schacc_ic_remember_me, null
                    ))
        }

        registerListeners()
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_IDENTIFIER, identifier)
    }

    /**
     * register listeners for the views of this class
     */
    private fun registerListeners() {
        primaryActionView.setOnClickListener { verifyCode() }
        secondaryActionView?.let {
            it.setOnClickListener {
                BaseLoginActivity.tracker?.eventEngagement(TrackingData.Engagement.CLICK, TrackingData.UIElement.RESEND_VERIFICATION_CODE, TrackingData.Screen.VERIFICATION_CODE)
                mobileVerificationPresenter.resendCode(passwordlessController)
            }
        }

        codeInputView.setImeAction(EditorInfo.IME_ACTION_NEXT) { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                verifyCode()
            }
            false
        }
    }

    private fun verifyCode() {
        BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.VERIFICATION_CODE)
        mobileVerificationPresenter.verifyCode(codeInputView, rememberMeView.isChecked)
    }

    /**
     * set the presenter of this view
     *
     * @param presenter
     */
    override fun setPresenter(presenter: VerificationContract.Presenter) {
        mobileVerificationPresenter = presenter
    }

    fun setPasswordlessController(passwordlessController: PasswordlessController) {
        this.passwordlessController = passwordlessController
    }

    /**
     * Builds an [InformationDialogFragment] an ask the [.navigationListener]
     * to show it.
     * The [InformationDialogFragment] inform the user that the code was successfully resent
     */
    override fun showResendCodeView() {
        navigationListener?.let { nav ->
            val dialog = InformationDialogFragment.newInstance(
                    getString(R.string.schacc_verification_dialog_title),
                    String.format(Locale.ENGLISH, getString(R.string.schacc_verification_dialog_information), identifier.identifier),
                    R.drawable.schacc_ic_email, actionLabel)
            nav.onDialogNavigationRequested(dialog)
            dialog.setActionListener {
                dialog.dismiss()
                nav.onNavigateBackRequested()
            }
        }
    }

    override fun showErrorDialog(error: ClientError, errorMessage: String?) = displayErrorDialog(error, errorMessage)

    override fun onDialogRequested(selectorDialog: SelectorDialog) {
        navigationListener?.let { nav ->
            selectorDialog.actionListener = View.OnClickListener {
                BaseLoginActivity.tracker?.eventEngagement(TrackingData.Engagement.CLICK, TrackingData.UIElement.CHANGE_IDENTIFIER, TrackingData.Screen.VERIFICATION_CODE)
                nav.onNavigateBackRequested()
                selectorDialog.dismiss()
            }
            nav.onDialogNavigationRequested(selectorDialog)
        }
    }

    companion object {

        private const val KEY_IDENTIFIER = "IDENTIFIER"

        /**
         * provide a new instance of this [Fragment]
         *
         * @param identifier the user identifier
         */
        fun newInstance(identifier: Identifier): VerificationFragment {
            val args = Bundle()
            val fragment = VerificationFragment()
            args.putParcelable(KEY_IDENTIFIER, identifier)
            fragment.arguments = args
            return fragment
        }
    }
}

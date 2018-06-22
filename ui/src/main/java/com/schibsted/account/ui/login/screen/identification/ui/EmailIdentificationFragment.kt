/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.identification.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.schibsted.account.common.util.Logger
import com.schibsted.account.network.response.ClientInfo
import com.schibsted.account.ui.R
import com.schibsted.account.ui.InternalUiConfiguration
import com.schibsted.account.ui.login.screen.identification.IdentificationContract
import com.schibsted.account.ui.ui.component.InputFieldView
import com.schibsted.account.ui.ui.rule.EmailValidationRule

/**
 * a [Fragment] displaying the email identification screen
 */
class EmailIdentificationFragment : AbstractIdentificationFragment(), IdentificationContract.View {

    /**
     * Provides a way to the user to enter his identifier.
     */
    private lateinit var inputFieldView: InputFieldView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        inputFieldView = InputFieldView.Builder(context, EmailValidationRule)
                .setCancelable(true)
                .setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS)
                .setImeOption(EditorInfo.IME_ACTION_DONE)
                .setError(R.string.schacc_email_identification_error)
                .setTitle(R.string.schacc_email_label)
                .setHint(R.string.schacc_required_field_email)
                .build()
        inputViewContainer.addView(inputFieldView)
        prefillIdentifier(uiConf.identifier)
        return view
    }

    public override fun prefillIdentifier(identifier: String?) {
        val tag = Logger.DEFAULT_TAG + "-" + this.javaClass.simpleName
        Logger.info(tag, "Attempting to prefill  email")
        if (identifier.isNullOrEmpty()) {
            Logger.info(tag, "email wasn't found")
        } else {
            if (EmailValidationRule.isValid(identifier)) {
                inputFieldView.inputView.setText(uiConf.identifier)
                Logger.info(tag, "email has been prefilled")
            } else {
                Logger.warn(tag, "Failed to prefill the email - Wrong format")
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
        primaryActionView.setOnClickListener { identifyUser(inputFieldView) }

        inputFieldView.setImeAction(EditorInfo.IME_ACTION_NEXT) { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                identifyUser(inputFieldView)
            }
            false
        }
    }

    companion object {

        /**
         * provide a new instance of this [Fragment]
         *
         * @param uiConfiguration
         * @return a parametrized instance of [MobileIdentificationFragment]
         */
        fun newInstance(uiConfiguration: InternalUiConfiguration, clientInfo: ClientInfo): EmailIdentificationFragment {
            val args = Bundle()
            val fragment = EmailIdentificationFragment()
            args.putParcelable(KEY_UI_CONF, uiConfiguration)
            args.putParcelable(AbstractIdentificationFragment.KEY_CLIENT_INFO, clientInfo)
            fragment.arguments = args
            return fragment
        }
    }
}

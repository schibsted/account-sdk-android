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
import com.schibsted.account.ui.InternalUiConfiguration
import com.schibsted.account.ui.KeyboardController
import com.schibsted.account.ui.R
import com.schibsted.account.ui.login.screen.identification.IdentificationContract
import com.schibsted.account.ui.ui.component.SingleFieldView
import com.schibsted.account.ui.ui.rule.EmailValidationRule

/**
 * a [Fragment] displaying the email identification screen
 */
class EmailIdentificationFragment : AbstractIdentificationFragment(), IdentificationContract.View {

    /**
     * Provides a way to the user to enter his identifier.
     */
    private lateinit var inputFieldView: SingleFieldView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        inputFieldView = SingleFieldView.create(context!!) {
            isCancelable { true }
            inputType { InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS }
            ime { EditorInfo.IME_ACTION_DONE }
            error { getString(R.string.schacc_email_identification_error) }
            title { getString(R.string.schacc_email_label) }
            hint { getString(R.string.schacc_required_field_email) }
        }
        inputViewContainer.addView(inputFieldView)
        prefillIdentifier(uiConf.identifier)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inputFieldView.inputField.requestFocus()
        if (inputFieldView.inputField.text.isNullOrBlank()) {
            Logger.debug(TAG, "Showing keyboard")
            activity?.let { KeyboardController.showKeyboard(it) }
        }
    }

    public override fun prefillIdentifier(identifier: String?) {
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
        primaryActionView.setOnClickListener { identifyUser(inputFieldView) }

        inputFieldView.setImeAction(EditorInfo.IME_ACTION_NEXT) { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                identifyUser(inputFieldView)
            }
            false
        }
    }

    companion object {
        private val TAG = EmailIdentificationFragment::class.java.simpleName
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

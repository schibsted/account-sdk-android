/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.identification.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.schibsted.account.common.util.Logger
import com.schibsted.account.ui.R
import com.schibsted.account.ui.UiConfiguration
import com.schibsted.account.ui.ui.component.PhoneInputView

/**
 * a [Fragment] displaying the phone number identification screen
 */
class MobileIdentificationFragment : AbstractIdentificationFragment() {

    /**
     * Provides a way to the user to enter his identifier.
     */
    lateinit var inputFieldView: PhoneInputView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        inputFieldView = PhoneInputView(context)
        inputFieldView.setPhonePrefixHint(uiConf.defaultPhonePrefix)
        inputViewContainer.addView(inputFieldView)
        identificationPolicy.text = getString(R.string.schacc_mobile_privacy_information)

        prefillIdentifier(uiConf.identifier)
        return view
    }

    override fun prefillIdentifier(phoneNumber: String?) {
        val tag = Logger.DEFAULT_TAG + "-" + this.javaClass.simpleName
        Logger.info(tag, "Attempting to prefill the phone number")
        if (phoneNumber.isNullOrEmpty()) {
            Logger.info(tag, "The phone number wasn't found")
        } else {
            if (TextUtils.isDigitsOnly(phoneNumber)) {
                inputFieldView.setPhonePrefix(uiConf.defaultPhonePrefix)
                inputFieldView.setPhoneNumber(uiConf.identifier)
                Logger.info(tag, "The phone number has been prefilled")
            } else {
                Logger.warn(tag, "Failed to prefill the phone number - Wrong format")
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

    override fun onDestroyView() {
        inputFieldView.reset()
        super.onDestroyView()
    }

    override fun clearField() {
        inputFieldView.reset()
    }

    companion object {

        /**
         * provide a new instance of this [Fragment]
         *
         * @param uiConfiguration
         * @return a parametrized instance of [MobileIdentificationFragment]
         */
        fun newInstance(uiConfiguration: UiConfiguration): MobileIdentificationFragment {
            val args = Bundle()
            val fragment = MobileIdentificationFragment()
            args.putParcelable(AbstractIdentificationFragment.KEY_UI_CONF, uiConfiguration)
            fragment.arguments = args
            return fragment
        }
    }
}

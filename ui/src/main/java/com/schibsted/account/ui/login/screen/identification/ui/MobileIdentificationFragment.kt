/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.identification.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.schibsted.account.common.util.Logger
import com.schibsted.account.network.response.ClientInfo
import com.schibsted.account.ui.InternalUiConfiguration
import com.schibsted.account.ui.ui.component.PhoneInputView

/**
 * a [Fragment] displaying the phone number identification screen
 */
class MobileIdentificationFragment : AbstractIdentificationFragment() {

    /**
     * Provides a way to the user to enter his identifier.
     */
    private lateinit var inputFieldView: PhoneInputView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        inputFieldView = PhoneInputView(context)

        inputViewContainer.addView(inputFieldView)

        prefillIdentifier(uiConf.identifier)
        return view
    }

    override fun prefillIdentifier(identifier: String?) {
        val tag = Logger.DEFAULT_TAG + "-" + this.javaClass.simpleName
        Logger.info(tag, "Attempting to prefill the phone number")
        if (identifier.isNullOrEmpty()) {
            Logger.info(tag, "The phone number wasn't found")
        } else {
            if (TextUtils.isDigitsOnly(identifier)) {
                inputFieldView.setPhoneNumber(uiConf.identifier!!)
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

    companion object {

        /**
         * provide a new instance of this [Fragment]
         *
         * @param uiConfiguration
         * @return a parametrized instance of [MobileIdentificationFragment]
         */
        fun newInstance(uiConfiguration: InternalUiConfiguration, clientInfo: ClientInfo): MobileIdentificationFragment {
            val args = Bundle()
            val fragment = MobileIdentificationFragment()
            args.putParcelable(KEY_UI_CONF, uiConfiguration)
            args.putParcelable(AbstractIdentificationFragment.KEY_CLIENT_INFO, clientInfo)
            fragment.arguments = args
            return fragment
        }
    }
}

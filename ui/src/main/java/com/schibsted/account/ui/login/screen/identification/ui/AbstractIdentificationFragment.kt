/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.identification.ui

import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.common.util.Logger
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.ClientInfo
import com.schibsted.account.network.response.Merchant
import com.schibsted.account.ui.R
import com.schibsted.account.ui.UiConfiguration
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.screen.LoginScreen
import com.schibsted.account.ui.login.screen.identification.IdentificationContract
import com.schibsted.account.ui.ui.FlowFragment
import com.schibsted.account.ui.ui.InputField
import com.schibsted.account.ui.ui.WebFragment

/**
 * Abstract class containing the common logic and ui for the identification process
 *
 * @see MobileIdentificationFragment
 *
 * @see EmailIdentificationFragment
 */
abstract class AbstractIdentificationFragment : FlowFragment<IdentificationContract.Presenter>(), IdentificationContract.View {
    /**
     * The presenter tied with this [com.schibsted.account.ui.login.screen.identification.IdentificationContract.View]
     */
    private lateinit var identificationPresenter: IdentificationContract.Presenter
    private lateinit var teaserText: TextView

    /**
     * this reference is used to add a child view in extended class
     *
     * @see MobileIdentificationFragment.onCreateView
     * @see EmailIdentificationFragment.onCreateView
     */
    protected lateinit var inputViewContainer: FrameLayout

    /**
     * Field used to display the policy of SPiD and the clientAccepted.
     */
    private lateinit var identificationPolicy: TextView

    private lateinit var linkView: TextView
    protected lateinit var uiConf: UiConfiguration
    private lateinit var clientInfo: ClientInfo

    override val isActive: Boolean
        get() = isAdded

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = savedInstanceState ?: arguments
        args?.let {
            if (args.getParcelable<Parcelable>(KEY_UI_CONF) != null) {
                uiConf = args.get(KEY_UI_CONF) as UiConfiguration
            }
            clientInfo = args.getParcelable(KEY_CLIENT_INFO)
        }

        if (!this::uiConf.isInitialized && context != null) {
            this.uiConf = UiConfiguration.Builder.fromManifest(context!!.applicationContext).build()
            Logger.warn(Logger.DEFAULT_TAG, "AbstractIdentificationFragment: Falling back to UiConfiguration from manifest")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        BaseLoginActivity.tracker?.resetContext()

        val view = inflater.inflate(R.layout.schacc_abstract_identification_fragment_layout, container, false)
        primaryActionView = view.findViewById(R.id.identification_button_continue)
        inputViewContainer = view.findViewById(R.id.identification_input_view)
        identificationPolicy = view.findViewById(R.id.identification_share_policy)
        teaserText = view.findViewById(R.id.schacc_teaser_text)

        val schibstedLogo = view.findViewById<ImageView>(R.id.schibsted_logo)
        val clientLogo = view.findViewById<ImageView>(R.id.client_logo)
        linkView = view.findViewById(R.id.help_link)
        linkView.setOnClickListener {
            navigationListener?.let {
                navigationListener?.onWebViewNavigationRequested(WebFragment.newInstance(getString(R.string.schacc_identification_help_link), uiConf.redirectUri), LoginScreen.WEB_NEED_HELP_SCREEN)
                BaseLoginActivity.tracker?.eventEngagement(TrackingData.Engagement.CLICK, TrackingData.UIElement.HELP, TrackingData.Screen.IDENTIFICATION)
            }
        }
        @StringRes val msgRes = if (clientInfo.merchant.type == Merchant.EXTERNAL) {
            R.string.schacc_identification_external_information
        } else {
            R.string.schacc_identification_internal_information
        }

        identificationPolicy.text = getString(msgRes, clientInfo.merchant.name)

        if (uiConf.teaserText?.isNotEmpty() == true) {
            this.teaserText.text = uiConf.teaserText
            this.teaserText.visibility = View.VISIBLE
        }
        if (uiConf.clientLogo != 0) {
            clientLogo.visibility = View.VISIBLE
            clientLogo.setImageDrawable(ContextCompat.getDrawable(context!!, uiConf.clientLogo))
        } else {
            clientLogo.visibility = View.GONE
            schibstedLogo.layoutParams = clientLogo.layoutParams
        }

        return view
    }

    protected fun identifyUser(inputField: InputField) {
        BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.IDENTIFICATION)
        identificationPresenter.verifyInput(inputField, uiConf.identifierType, uiConf.signUpEnabled, uiConf.signUpNotAllowedErrorMessage)
    }

    fun isTeaserEnabled() = !uiConf.teaserText.isNullOrEmpty()

    protected abstract fun prefillIdentifier(identifier: String?)

    /**
     * ties a presenter to this view
     *
     * @param presenter the presenter to tie with this view
     */
    override fun setPresenter(presenter: IdentificationContract.Presenter) {
        identificationPresenter = presenter
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
        const val KEY_UI_CONF = "UI_CONF"
        const val KEY_CLIENT_INFO = "KEY_CLIENT_INFO"
    }
}

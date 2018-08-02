/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.term

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.ColorInt
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.AgreementLinksResponse
import com.schibsted.account.ui.InternalUiConfiguration
import com.schibsted.account.ui.R
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.screen.LoginScreen
import com.schibsted.account.ui.setPartAsClickableLink
import com.schibsted.account.ui.ui.FlowFragment
import com.schibsted.account.ui.ui.WebFragment
import com.schibsted.account.ui.ui.component.CheckBoxView

/**
 * a [Fragment] displaying the terms and conditions screen
 */
class TermsFragment : FlowFragment<TermsContract.Presenter>(), TermsContract.View {
    /**
     * the presenter of this view
     *
     * @see TermsPresenter
     */
    private lateinit var presenter: TermsContract.Presenter

    /**
     * [CheckBox] allowing the use to accept terms policy
     */
    private lateinit var termsCheckView: CheckBoxView

    private lateinit var agreements: AgreementLinksResponse
    private var isUserAvailable: Boolean = false

    override val isActive: Boolean
        get() = isAdded

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = savedInstanceState ?: arguments
        args?.getParcelable<Parcelable>(KEY_LINKS).let { agreements = it as AgreementLinksResponse }
        isUserAvailable = args?.getBoolean(KEY_USER_AVAILABLE) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.schacc_terms_fragment_layout, container, false)
        initViews(view)
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_LINKS, agreements)
        outState.putBoolean(KEY_USER_AVAILABLE, isUserAvailable)
    }

    /**
     * initialize texts and behaviors of views
     *
     * @param view the inflated view
     * @see .onCreateView
     */

    private fun initViews(view: View) {
        // find view
        primaryActionView = view.findViewById(R.id.terms_button_continue)
        termsCheckView = view.findViewById(R.id.terms_box)

        val linkMovementMethod = LinkMovementMethod.getInstance()
        termsCheckView.labelView.movementMethod = linkMovementMethod
        termsCheckView.setError(R.string.schacc_terms_terms_error)

        val termsDescription = view.findViewById<TextView>(R.id.terms_description)
        termsDescription.setText(if (isUserAvailable) R.string.schacc_terms_sign_up_description else R.string.schacc_terms_sign_in_description)

        primaryActionView.setOnClickListener {
            BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.AGREEMENTS)
            presenter.acceptTerms(termsCheckView)
        }
    }

    /**
     * Set up text views to look like a web link and redirect to the associated agreement url when
     * the user click on the [TextView]
     *
     * @param agreements contains all agreements urls.
     */
    private fun setAgreementLinks(agreements: AgreementLinksResponse) {

        // get data to build texts
        val spidLabel = getString(R.string.schacc_spid_label)
        val clientLabel = uiConf.clientName
        val termsText: SpannableString

        termsText = if (TextUtils.isEmpty(agreements.clientTermsUrl)) {
            SpannableString(getString(R.string.schacc_terms_policy_spid_only, spidLabel))
        } else {
            SpannableString(getString(R.string.schacc_terms_policy, spidLabel, clientLabel))
        }

        // build texts
        @ColorInt val color = ContextCompat.getColor(context!!, R.color.schacc_primaryEnabled)

        termsText.setPartAsClickableLink(color, spidLabel, getLinkAction(agreements.spidTermsUrl))
        termsText.setPartAsClickableLink(color, clientLabel, getLinkAction(agreements.clientTermsUrl))

        termsCheckView.labelView.text = termsText
        termsCheckView.contentDescription = termsText

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            termsCheckView.labelView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        setAgreementLinks(agreements)
    }

    private fun getLinkAction(link: String): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(widget: View) {
                BaseLoginActivity.tracker?.let {
                    val element = when (link) {
                        agreements.spidTermsUrl -> TrackingData.UIElement.AGREEMENTS_SPID
                        else -> TrackingData.UIElement.AGREEMENTS_CLIENT
                    }
                    it.eventEngagement(TrackingData.Engagement.CLICK, element, TrackingData.Screen.AGREEMENTS)
                }
                requestNavigationToWebView(link)
            }

            override fun updateDrawState(ds: TextPaint) {
            }
        }
    }

    /**
     * ties a presenter to this view
     *
     * @param presenter the presenter to tie with this view
     */
    override fun setPresenter(presenter: TermsContract.Presenter) {
        this.presenter = presenter
    }

    /**
     * Request a navigation to a [WebFragment] to shows terms and policies web page
     *
     * @param link the client link to go to.
     */
    override fun requestNavigationToWebView(link: String) {
        navigationListener?.onWebViewNavigationRequested(WebFragment.newInstance(link, uiConf.redirectUri), LoginScreen.WEB_TC_SCREEN)
    }

    override fun showErrorDialog(error: ClientError, errorMessage: String?) {
        displayErrorDialog(error, errorMessage)
    }

    companion object {

        private const val KEY_LINKS = "LINKS"
        private const val KEY_UI_CONF = "UI_CONF"
        private const val KEY_USER_AVAILABLE = "USER_AVAILABLE"

        fun newInstance(uiConfiguration: InternalUiConfiguration, isUserAvailable: Boolean, agreementLinks: AgreementLinksResponse): TermsFragment {
            val fragment = TermsFragment()
            val args = Bundle()
            args.putParcelable(KEY_UI_CONF, uiConfiguration)
            args.putBoolean(KEY_USER_AVAILABLE, isUserAvailable)
            args.putParcelable(KEY_LINKS, agreementLinks)

            fragment.arguments = args
            return fragment
        }
    }
}

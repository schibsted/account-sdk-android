/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.term

import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.ColorRes
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.AgreementLinksResponse
import com.schibsted.account.ui.R
import com.schibsted.account.ui.UiConfiguration
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.screen.LoginScreen
import com.schibsted.account.ui.ui.FlowFragment
import com.schibsted.account.ui.ui.WebFragment
import com.schibsted.account.ui.ui.component.CheckBoxView
import com.schibsted.account.ui.ui.component.TermsUpdateDialog
import java.util.regex.Pattern

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

    /**
     * [CheckBox] allowing the use to accept privacy policy
     */
    private lateinit var privacyCheckView: CheckBoxView

    private lateinit var agreements: AgreementLinksResponse
    private lateinit var uiConf: UiConfiguration
    private var isUserAvailable: Boolean = false

    override val isActive: Boolean
        get() = isAdded

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = savedInstanceState ?: arguments
        args?.get(KEY_UI_CONF)?.let { uiConf = it as UiConfiguration }
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
        outState.putParcelable(KEY_UI_CONF, uiConf)
        outState.putBoolean(KEY_USER_AVAILABLE, isUserAvailable)
    }

    /**
     * initialize texts and behaviors of views
     *
     * @param view the inflated view
     * @see .onCreateView
     */

    private fun initViews(view: View) {
        //find view
        primaryActionView = view.findViewById(R.id.terms_button_continue)
        termsCheckView = view.findViewById(R.id.terms_box)
        privacyCheckView = view.findViewById(R.id.privacy_box)
        val updateLinkView = view.findViewById<TextView>(R.id.terms_update_link)

        val linkMovementMethod = LinkMovementMethod.getInstance()
        termsCheckView.textView.movementMethod = linkMovementMethod
        privacyCheckView.textView.movementMethod = linkMovementMethod
        termsCheckView.setError(R.string.schacc_terms_terms_error)
        privacyCheckView.setError(R.string.schacc_terms_privacy_error)

        updateLinkView.visibility = if (isUserAvailable) View.GONE else View.VISIBLE
        updateLinkView.setOnClickListener {
            navigationListener?.let {
                BaseLoginActivity.tracker?.eventEngagement(TrackingData.Engagement.CLICK, TrackingData.UIElement.AGREEMENTS_SUMMARY, TrackingData.Screen.AGREEMENTS)
                navigationListener?.onDialogNavigationRequested(TermsUpdateDialog.newInstance(agreements.summaryText))
            }
        }

        val termsDescription = view.findViewById<TextView>(R.id.terms_description)
        termsDescription.setText(if (isUserAvailable) R.string.schacc_terms_sign_up_description else R.string.schacc_terms_sign_in_description)

        primaryActionView.setOnClickListener {
            BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.AGREEMENTS)
            presenter.verifyBoxes(termsCheckView, privacyCheckView)
        }
    }

    /**
     * Set up text views to look like a web link and redirect to the associated agreement url when
     * the user click on the textview
     *
     * @param agreements contains all agreements urls.
     */
    private fun setAgreementLinks(agreements: AgreementLinksResponse) {

        //get data to build texts
        val spidLabel = getString(R.string.schacc_spid_label)
        val clientLabel = uiConf.clientName
        val privacyText: String
        val termsText: String

        privacyText = if (TextUtils.isEmpty(agreements.clientPrivacyUrl)) {
            getString(R.string.schacc_privacy_policy_spid_only, spidLabel)
        } else {
            getString(R.string.schacc_privacy_policy, spidLabel, clientLabel)
        }

        termsText = if (TextUtils.isEmpty(agreements.clientTermsUrl)) {
            getString(R.string.schacc_terms_policy_spid_only, spidLabel)
        } else {
            getString(R.string.schacc_terms_policy, spidLabel, clientLabel)
        }

        //build texts
        val spannableTermsText = buildLinkText(termsText, R.color.schacc_primaryEnabled, spidLabel, clientLabel)
        val spannablePrivacyText = buildLinkText(privacyText, R.color.schacc_primaryEnabled, spidLabel, clientLabel)

        makeTextClickable(spannablePrivacyText, spidLabel, agreements.spidPrivacyUrl)
        makeTextClickable(spannablePrivacyText, clientLabel, agreements.clientPrivacyUrl)

        makeTextClickable(spannableTermsText, spidLabel, agreements.spidTermsUrl)
        makeTextClickable(spannableTermsText, clientLabel, agreements.clientTermsUrl)
        //we assign text to the view
        termsCheckView.textView.text = spannableTermsText
        privacyCheckView.textView.text = spannablePrivacyText
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        setAgreementLinks(agreements)
    }

    /**
     * take a text then colorize and underline words in order to get a text looking like a link to click on
     *
     * @param fullText        the text where we have to find the text to colorize
     * @param color           the color we want to apply
     * @param textToCustomize the text to colorize
     * @return [Spannable] the colorized text
     */
    private fun buildLinkText(fullText: String, @ColorRes color: Int, vararg textToCustomize: String): SpannableString {
        val spannableString = SpannableString(fullText)
        for (text in textToCustomize) {
            val pattern = Pattern.compile(text, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(fullText)
            if (matcher.find()) {
                spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, color)), matcher.start(), matcher.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                spannableString.setSpan(UnderlineSpan(), matcher.start(), matcher.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
        return spannableString
    }

    /**
     * make part of a text redirecting to a website
     *
     * @param fullText the original text containing the text to click on
     * @param linkText the text the user has to click on to display the website
     * @param link     the website link
     */
    private fun makeTextClickable(fullText: SpannableString, linkText: String, link: String) {
        val pattern = Pattern.compile(linkText, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(fullText)
        if (matcher.find()) {
            fullText.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    BaseLoginActivity.tracker?.let {
                        val element = when (link) {
                            agreements.spidTermsUrl -> TrackingData.UIElement.AGREEMENTS_SPID
                            agreements.spidPrivacyUrl -> TrackingData.UIElement.PRIVACY_SPID
                            agreements.clientTermsUrl -> TrackingData.UIElement.AGREEMENTS_CLIENT
                            else -> TrackingData.UIElement.PRIVACY_CLIENT
                        }
                        it.eventEngagement(TrackingData.Engagement.CLICK, element, TrackingData.Screen.AGREEMENTS)
                    }
                    requestNavigationToWebView(link)
                }

                override fun updateDrawState(ds: TextPaint) {

                }
            }, matcher.start(), matcher.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
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
        if (navigationListener != null) {
            navigationListener!!.onWebViewNavigationRequested(WebFragment.newInstance(link, uiConf.redirectUri), LoginScreen.WEB_TC_SCREEN)
        }
    }

    override fun showErrorDialog(error: ClientError, errorMessage: String?) {
        displayErrorDialog(error, errorMessage)
    }

    companion object {

        private const val KEY_LINKS = "LINKS"
        private const val KEY_UI_CONF = "UI_CONF"
        private const val KEY_USER_AVAILABLE = "USER_AVAILABLE"

        fun newInstance(uiConfiguration: UiConfiguration, isUserAvailable: Boolean, agreementLinks: AgreementLinksResponse): TermsFragment {
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

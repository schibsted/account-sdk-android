/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.term;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import com.schibsted.account.common.tracking.UiTracking;
import com.schibsted.account.common.tracking.TrackingData;
import com.schibsted.account.model.error.ClientError;
import com.schibsted.account.network.response.AgreementLinksResponse;
import com.schibsted.account.ui.R;
import com.schibsted.account.ui.UiConfiguration;
import com.schibsted.account.ui.login.BaseLoginActivity;
import com.schibsted.account.ui.login.screen.LoginScreen;
import com.schibsted.account.ui.ui.FlowFragment;
import com.schibsted.account.ui.ui.WebFragment;
import com.schibsted.account.ui.ui.component.CheckBoxView;
import com.schibsted.account.ui.ui.component.TermsUpdateDialog;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a {@link Fragment} displaying the terms and conditions screen
 */
public class TermsFragment extends FlowFragment<TermsContract.Presenter> implements TermsContract.View {

    private static final String KEY_LINKS = "LINKS";
    private static final String KEY_UI_CONF = "UI_CONF";
    private static final String KEY_USER_AVAILABLE = "USER_AVAILABLE";
    /**
     * the presenter of this view
     *
     * @see TermsPresenter
     */
    private TermsContract.Presenter presenter;

    /**
     * {@link CheckBox} allowing the use to accept terms policy
     */
    private CheckBoxView termsCheckView;

    /**
     * {@link CheckBox} allowing the use to accept privacy policy
     */
    private CheckBoxView privacyCheckView;

    private AgreementLinksResponse agreements;
    private UiConfiguration uiConf;
    private boolean isUserAvailable;

    public static TermsFragment newInstance(UiConfiguration uiConfiguration, boolean isUserAvailable, AgreementLinksResponse agreementLinks) {
        final TermsFragment fragment = new TermsFragment();
        final Bundle args = new Bundle();
        args.putParcelable(KEY_UI_CONF, uiConfiguration);
        args.putBoolean(KEY_USER_AVAILABLE, isUserAvailable);
        args.putParcelable(KEY_LINKS, agreementLinks);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = savedInstanceState == null ? getArguments() : savedInstanceState;
        if (args != null) {
            if (args.getParcelable(KEY_UI_CONF) != null) {
                uiConf = (UiConfiguration) args.get(KEY_UI_CONF);
            }

            if (args.getParcelable(KEY_LINKS) != null) {
                agreements = args.getParcelable(KEY_LINKS);
            }
            isUserAvailable = args.getBoolean(KEY_USER_AVAILABLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        final View view = inflater.inflate(R.layout.schacc_terms_fragment_layout, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_LINKS, agreements);
        outState.putParcelable(KEY_UI_CONF, uiConf);
        outState.putBoolean(KEY_USER_AVAILABLE, isUserAvailable);
    }

    /**
     * initialize texts and behaviors of views
     *
     * @param view the inflated view
     * @see #onCreateView(LayoutInflater, ViewGroup, Bundle)
     */

    private void initViews(final View view) {
        //find view
        primaryActionView = view.findViewById(R.id.terms_button_continue);
        termsCheckView = view.findViewById(R.id.terms_box);
        privacyCheckView = view.findViewById(R.id.privacy_box);
        final TextView updateLinkView = view.findViewById(R.id.terms_update_link);

        final MovementMethod linkMovementMethod = LinkMovementMethod.getInstance();
        termsCheckView.getTextView().setMovementMethod(linkMovementMethod);
        privacyCheckView.getTextView().setMovementMethod(linkMovementMethod);
        termsCheckView.setError(R.string.schacc_terms_terms_error);
        privacyCheckView.setError(R.string.schacc_terms_privacy_error);

        updateLinkView.setVisibility(isUserAvailable ? View.GONE : View.VISIBLE);
        updateLinkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (navigationListener != null) {
                    UiTracking tracker = BaseLoginActivity.getTracker();
                    if (tracker != null) {
                        tracker.eventEngagement(TrackingData.Engagement.CLICK, TrackingData.UIElement.AGREEMENTS_SUMMARY, TrackingData.Screen.AGREEMENTS);
                    }

                    navigationListener.onDialogNavigationRequested(TermsUpdateDialog.newInstance(agreements.getSummary()));
                }
            }
        });

        final TextView termsDescription = view.findViewById(R.id.terms_description);
        termsDescription.setText(isUserAvailable ? R.string.schacc_terms_sign_up_description : R.string.schacc_terms_sign_in_description);

        primaryActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final UiTracking tracker = BaseLoginActivity.getTracker();
                if (tracker != null) {
                    tracker.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.AGREEMENTS);
                }

                presenter.verifyBoxes(termsCheckView, privacyCheckView);
            }
        });
    }

    /**
     * Set up text views to look like a web link and redirect to the associated agreement url when
     * the user click on the textview
     *
     * @param agreements contains all agreements urls.
     */
    private void setAgreementLinks(AgreementLinksResponse agreements) {

        //get data to build texts
        final String spidLabel = getString(R.string.schacc_spid_label);
        final String clientLabel = uiConf.getClientName();
        final String privacyText;
        final String termsText;

        if (TextUtils.isEmpty(agreements.getClientPrivacyUrl())) {
            privacyText = getString(R.string.schacc_privacy_policy_spid_only, spidLabel);
        } else {
            privacyText = getString(R.string.schacc_privacy_policy, spidLabel, clientLabel);
        }

        if (TextUtils.isEmpty(agreements.getClientTermsUrl())) {
            termsText = getString(R.string.schacc_terms_policy_spid_only, spidLabel);
        } else {
            termsText = getString(R.string.schacc_terms_policy, spidLabel, clientLabel);
        }

        //build texts
        final SpannableString spannableTermsText = buildLinkText(termsText, R.color.schacc_primaryEnabled, spidLabel, clientLabel);
        final SpannableString spannablePrivacyText = buildLinkText(privacyText, R.color.schacc_primaryEnabled, spidLabel, clientLabel);

        makeTextClickable(spannablePrivacyText, spidLabel, agreements.getSpidPrivacyUrl());
        makeTextClickable(spannablePrivacyText, clientLabel, agreements.getClientPrivacyUrl());

        makeTextClickable(spannableTermsText, spidLabel, agreements.getSpidTermsUrl());
        makeTextClickable(spannableTermsText, clientLabel, agreements.getClientTermsUrl());
        //we assign text to the view
        termsCheckView.getTextView().setText(spannableTermsText);
        privacyCheckView.getTextView().setText(spannablePrivacyText);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        setAgreementLinks(agreements);
    }

    /**
     * take a text then colorize and underline words in order to get a text looking like a link to click on
     *
     * @param fullText        the text where we have to find the text to colorize
     * @param color           the color we want to apply
     * @param textToCustomize the text to colorize
     * @return {@link Spannable} the colorized text
     */
    private SpannableString buildLinkText(String fullText, @ColorRes int color, String... textToCustomize) {
        final SpannableString spannableString = new SpannableString(fullText);
        for (String text : textToCustomize) {
            final Pattern pattern = Pattern.compile(text, Pattern.CASE_INSENSITIVE);
            final Matcher matcher = pattern.matcher(fullText);
            if (matcher.find()) {
                spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), color)), matcher.start(), matcher.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spannableString.setSpan(new UnderlineSpan(), matcher.start(), matcher.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        return spannableString;
    }

    /**
     * make part of a text redirecting to a website
     *
     * @param fullText the original text containing the text to click on
     * @param linkText the text the user has to click on to display the website
     * @param link     the website link
     */
    private void makeTextClickable(final SpannableString fullText, final String linkText, final String link) {
        final Pattern pattern = Pattern.compile(linkText, Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(fullText);
        if (matcher.find()) {
            fullText.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    final UiTracking tracker = BaseLoginActivity.getTracker();
                    TrackingData.UIElement element;
                    if (tracker != null) {
                        if (link.equals(agreements.getSpidTermsUrl())) {
                            element = TrackingData.UIElement.AGREEMENTS_SPID;
                        } else if (link.equals(agreements.getSpidPrivacyUrl())) {
                            element = TrackingData.UIElement.PRIVACY_SPID;
                        } else if (link.equals(agreements.getClientTermsUrl())) {
                            element = TrackingData.UIElement.AGREEMENTS_CLIENT;
                        } else {
                            element = TrackingData.UIElement.PRIVACY_CLIENT;
                        }

                        tracker.eventEngagement(TrackingData.Engagement.CLICK, element, TrackingData.Screen.AGREEMENTS);
                    }

                    requestNavigationToWebView(link);
                }

                @Override
                public void updateDrawState(TextPaint ds) {

                }
            }, matcher.start(), matcher.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
    }

    /**
     * ties a presenter to this view
     *
     * @param presenter the presenter to tie with this view
     */
    @Override
    public void setPresenter(TermsContract.Presenter presenter) {
        this.presenter = presenter;
    }

    /**
     * Request a navigation to a {@link WebFragment} to shows terms and policies web page
     *
     * @param link the client link to go to.
     */
    @Override
    public void requestNavigationToWebView(@NonNull final String link) {
        if (navigationListener != null) {
            navigationListener.onWebViewNavigationRequested(WebFragment.newInstance(link, uiConf.getRedirectUri()), LoginScreen.WEB_TC_SCREEN);
        }
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showErrorDialog(@NotNull ClientError error, @Nullable String errorMessage) {
        displayErrorDialog(error, errorMessage);
    }
}

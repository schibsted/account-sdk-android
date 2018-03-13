/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.identification.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.schibsted.account.common.tracking.TrackingData;
import com.schibsted.account.common.tracking.UiTracking;
import com.schibsted.account.common.util.Logger;
import com.schibsted.account.model.error.ClientError;
import com.schibsted.account.ui.R;
import com.schibsted.account.ui.UiConfiguration;
import com.schibsted.account.ui.login.BaseLoginActivity;
import com.schibsted.account.ui.login.screen.LoginScreen;
import com.schibsted.account.ui.login.screen.identification.IdentificationContract;
import com.schibsted.account.ui.ui.FlowFragment;
import com.schibsted.account.ui.ui.InputField;
import com.schibsted.account.ui.ui.WebFragment;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class containing the common logic and ui for the identification process
 *
 * @see MobileIdentificationFragment
 * @see EmailIdentificationFragment
 */
public abstract class AbstractIdentificationFragment extends FlowFragment<IdentificationContract.Presenter> implements IdentificationContract.View {

    protected static final String KEY_UI_CONF = "UI_CONF";
    /**
     * The presenter tied with this {@link com.schibsted.account.ui.login.screen.identification.IdentificationContract.View}
     */
    protected IdentificationContract.Presenter identificationPresenter;

    protected TextView teaserText;

    /**
     * this reference is used to add a child view in extended class
     *
     * @see MobileIdentificationFragment#onCreateView(LayoutInflater, ViewGroup, Bundle)
     * @see EmailIdentificationFragment#onCreateView(LayoutInflater, ViewGroup, Bundle)
     */
    protected FrameLayout inputViewContainer;

    /**
     * Field used to display the policy of SPiD and the clientAccepted.
     */
    protected TextView identificationPolicy;

    protected TextView linkView;
    protected UiConfiguration uiConf;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = savedInstanceState == null ? getArguments() : savedInstanceState;
        if (args != null) {
            if (args.getParcelable(KEY_UI_CONF) != null) {
                uiConf = (UiConfiguration) args.get(KEY_UI_CONF);
            }
        }

        if (this.uiConf == null && getContext() != null) {
            this.uiConf = UiConfiguration.Builder.fromManifest(getContext().getApplicationContext()).build();
            Logger.INSTANCE.warn(Logger.getDEFAULT_TAG(), "AbstractIdentificationFragment: Falling back to UiConfiguration from manifest", null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final UiTracking tracker = BaseLoginActivity.getTracker();
        if (tracker != null) {
            tracker.resetContext();
        }

        final View view = inflater.inflate(R.layout.schacc_abstract_identification_fragment_layout, container, false);
        primaryActionView = view.findViewById(R.id.identification_button_continue);
        inputViewContainer = view.findViewById(R.id.identification_input_view);
        identificationPolicy = view.findViewById(R.id.identification_share_policy);
        linkView = view.findViewById(R.id.help_link);
        linkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (navigationListener != null) {
                    navigationListener.onWebViewNavigationRequested(WebFragment.newInstance(getString(R.string.schacc_identification_help_link), uiConf.getRedirectUri()), LoginScreen.WEB_NEED_HELP_SCREEN);

                    final UiTracking tracker = BaseLoginActivity.getTracker();
                    if (tracker != null) {
                        tracker.eventEngagement(TrackingData.Engagement.CLICK, TrackingData.UIElement.HELP, TrackingData.Screen.IDENTIFICATION);
                    }
                }
            }
        });

        this.teaserText = view.findViewById(R.id.schacc_teaser_text);
        if (!TextUtils.isEmpty(uiConf.getTeaserText())) {
            this.teaserText.setText(uiConf.getTeaserText());
            this.teaserText.setVisibility(View.VISIBLE);
        }

        return view;
    }

    protected void identifyUser(InputField inputField) {
        final UiTracking tracker = BaseLoginActivity.getTracker();
        if (tracker != null) {
            tracker.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.IDENTIFICATION);
        }
        identificationPresenter.verifyInput(inputField, uiConf.getIdentifierType(), uiConf.getSignUpEnabled(), uiConf.getSignUpNotAllowedErrorMessage());
    }

    abstract protected void prefillIdentifier(String identifier);

    /**
     * ties a presenter to this view
     *
     * @param presenter the presenter to tie with this view
     */
    @Override
    public void setPresenter(IdentificationContract.Presenter presenter) {
        identificationPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_UI_CONF, uiConf);
    }

    @Override
    public void showErrorDialog(@NotNull ClientError error, @Nullable String errorMessage ) {
        displayErrorDialog(error, errorMessage);
    }
}

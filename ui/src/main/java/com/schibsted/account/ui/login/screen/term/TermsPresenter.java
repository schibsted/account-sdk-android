/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.term;


import com.schibsted.account.common.tracking.TrackingData;
import com.schibsted.account.common.tracking.UiTracking;
import com.schibsted.account.engine.input.Agreements;
import com.schibsted.account.engine.integration.InputProvider;
import com.schibsted.account.engine.integration.ResultCallback;
import com.schibsted.account.model.error.ClientError;
import com.schibsted.account.ui.login.BaseLoginActivity;
import com.schibsted.account.ui.ui.component.CheckBoxView;

import org.jetbrains.annotations.NotNull;

/**
 * Following the MVP design pattern this interface represent the implementation of the {@link TermsContract.Presenter}.
 * this class executes the terms and condition business logic and ask for UI updates depending on results.
 */
public class TermsPresenter implements TermsContract.Presenter {

    private InputProvider<Agreements> provider;
    /**
     * the view responsible for UI update
     */
    private TermsContract.View termsView;

    public TermsPresenter(TermsContract.View view, InputProvider<Agreements> provider) {
        termsView = view;
        termsView.setPresenter(this);
        this.provider = provider;
    }

    /**
     * Verify if given checkboxes are all checked.
     * Calls {@link #acceptAgreements()}  if <code>true</code>
     * Shows errors if <code>false</code>
     *
     * @param privacyBox {@link CheckBoxView}  the privacy checkbox
     * @param termsBox   {@link CheckBoxView}  the terms checkbox
     */
    @Override
    public void verifyBoxes(CheckBoxView privacyBox, CheckBoxView termsBox) {
        if (termsView.isActive()) {
            if (privacyBox.isChecked() && termsBox.isChecked()) {
                acceptAgreements();
            } else {
                final UiTracking tracker = BaseLoginActivity.getTracker();
                if (tracker != null) {
                    tracker.eventError(TrackingData.UIError.AgreementsNotAccepted.INSTANCE, TrackingData.Screen.AGREEMENTS);
                }

                if (!privacyBox.isChecked()) {
                    termsView.showError(privacyBox);
                }

                if (!termsBox.isChecked()) {
                    termsView.showError(termsBox);
                }
            }
        }
    }

    /**
     * Accepts TC on backend side.
     * Order a navigation to an other screen if request succeeded, show an error otherwise
     */
    private void acceptAgreements() {
        termsView.showProgress();
        provider.provide(new Agreements(true), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void res) {
                final UiTracking tracker = BaseLoginActivity.getTracker();
                if (tracker != null) {
                    tracker.eventActionSuccessful(TrackingData.SpidAction.AGREEMENTS_ACCEPTED);
                }
            }

            @Override
            public void onError(@NotNull ClientError error) {
                if (termsView.isActive()) {
                    termsView.hideProgress();

                    final UiTracking tracker = BaseLoginActivity.getTracker();
                    if (tracker != null && error.getErrorType() == ClientError.ErrorType.NETWORK_ERROR) {
                        tracker.eventError(TrackingData.UIError.NetworkError.INSTANCE, TrackingData.Screen.AGREEMENTS);
                    }
                }
            }
        });
    }
}

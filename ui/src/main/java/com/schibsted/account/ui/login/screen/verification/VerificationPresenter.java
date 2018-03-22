/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.verification;

import com.schibsted.account.common.tracking.TrackingData;
import com.schibsted.account.common.tracking.UiTracking;
import com.schibsted.account.engine.controller.PasswordlessController;
import com.schibsted.account.engine.input.VerificationCode;
import com.schibsted.account.engine.integration.InputProvider;
import com.schibsted.account.engine.integration.ResultCallback;
import com.schibsted.account.model.NoValue;
import com.schibsted.account.model.error.ClientError;
import com.schibsted.account.ui.ErrorUtil;
import com.schibsted.account.ui.login.BaseLoginActivity;
import com.schibsted.account.ui.ui.ErrorField;
import com.schibsted.account.ui.ui.component.CodeInputView;

import org.jetbrains.annotations.NotNull;

/**
 * Following the MVP design pattern this interface represent the implementation of the {@link VerificationContract.Presenter}.
 * this class executes the code verification business logic and ask for UI updates depending on results.
 */
public class VerificationPresenter implements VerificationContract.Presenter {

    private final InputProvider<? super VerificationCode> provider;
    /**
     * the view responsible for UI updates
     */
    private VerificationContract.View verificationView;


    public VerificationPresenter(VerificationContract.View view, InputProvider<VerificationCode> provider) {
        verificationView = view;
        verificationView.setPresenter(this);
        this.provider = provider;
    }

    /**
     * Used to resend a code verification to the identifier.
     * It will ask the view to show an error if any failure or to
     * show a pop-up in case of success
     */
    @Override
    public void resendCode(PasswordlessController passwordlessController) {
        passwordlessController.resendCode(new ResultCallback<NoValue>() {
            @Override
            public void onSuccess(NoValue res) {
                if (verificationView.isActive()) {
                    verificationView.showResendCodeView();

                    final UiTracking tracker = BaseLoginActivity.getTracker();
                    if (tracker != null) {
                        tracker.eventActionSuccessful(TrackingData.SpidAction.VERIFICATION_CODE_SENT);
                    }
                }
            }

            @Override
            public void onError(@NotNull ClientError error) {
                if (verificationView.isActive()) {
                    verificationView.showErrorDialog(error, null);

                    final UiTracking tracker = BaseLoginActivity.getTracker();
                    if (tracker != null && error.getErrorType() == ClientError.ErrorType.NETWORK_ERROR) {
                        tracker.eventError(TrackingData.UIError.NetworkError.INSTANCE, TrackingData.Screen.VERIFICATION_CODE);
                    }
                }
            }
        });
    }

    /**
     * Used to verify if the code provided by the user is valid
     * If there is an agreement update a navigation to {@link com.schibsted.account.ui.login.screen.term.TermsFragment}
     * will be asked.
     * If the verification is successful a navigation to {@link com.schibsted.account.ui.login.screen.information.RequiredFieldsFragment}
     * will be asked.
     * If there is a failure  the view will be requested to show an error
     *
     * @param codeInputView the provided code
     */
    @Override
    public void verifyCode(final CodeInputView codeInputView, boolean keepMeLoggedIn) {
        if (verificationView.isActive()) {
            verificationView.hideError(codeInputView);
            if (codeInputView.isInputValid()) {
                verificationView.showProgress();

                provider.provide(new VerificationCode(codeInputView.getInput(), keepMeLoggedIn), new ResultCallback<NoValue>() {
                    @Override
                    public void onSuccess(NoValue res) {
                        final UiTracking tracker = BaseLoginActivity.getTracker();
                        if (tracker != null) {
                            tracker.eventActionSuccessful(TrackingData.SpidAction.VERIFICATION_CODE_SENT);
                        }
                    }

                    @Override
                    public void onError(@NotNull ClientError error) {
                        showError(error, codeInputView);

                        final UiTracking tracker = BaseLoginActivity.getTracker();
                        if (tracker != null) {
                            tracker.eventError(TrackingData.UIError.InvalidVerificationCode.INSTANCE, TrackingData.Screen.VERIFICATION_CODE);
                        }
                    }
                });

            } else {
                verificationView.showError(codeInputView);
            }
        }
    }

    private void showError(ClientError error, ErrorField errorField) {
        if (verificationView.isActive()) {
            if (ErrorUtil.INSTANCE.isServerError(error.getErrorType())) {
                verificationView.showErrorDialog(error, null);
            } else {
                verificationView.showError(errorField);
            }
            verificationView.hideProgress();
        }
    }
}

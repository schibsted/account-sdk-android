/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.identification;

import com.schibsted.account.common.tracking.TrackingData;
import com.schibsted.account.common.tracking.UiTracking;
import com.schibsted.account.engine.input.Identifier;
import com.schibsted.account.engine.input.Identifier.IdentifierType;
import com.schibsted.account.engine.integration.InputProvider;
import com.schibsted.account.engine.integration.ResultCallback;
import com.schibsted.account.model.NoValue;
import com.schibsted.account.model.error.ClientError;
import com.schibsted.account.network.response.AccountStatusResponse;
import com.schibsted.account.ui.ErrorUtil;
import com.schibsted.account.ui.login.BaseLoginActivity;
import com.schibsted.account.ui.login.flow.password.FlowSelectionListener;
import com.schibsted.account.ui.ui.InputField;
import com.schibsted.account.ui.ui.component.PhoneInputView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Following the MVP design pattern this interface represent the implementation of the {@link IdentificationContract.Presenter}.
 * this class executes the mobile identification business logic and ask for UI updates depending on results.
 */
public class IdentificationPresenter implements IdentificationContract.Presenter {

    private FlowSelectionListener flowSelectionListener;
    private InputProvider<Identifier> provider;
    /**
     * the view responsible for UI updates
     */
    private IdentificationContract.View identificationView;

    public IdentificationPresenter(IdentificationContract.View view, @Nullable InputProvider<Identifier> provider, FlowSelectionListener flowSelectionListener) {
        identificationView = view;
        identificationView.setPresenter(this);
        this.flowSelectionListener = flowSelectionListener;
        this.provider = provider;
    }

    private void trackError(final ClientError error) {
        final UiTracking tracker = BaseLoginActivity.getTracker();
        if (tracker != null) {
            if (error.getErrorType() == ClientError.ErrorType.NETWORK_ERROR) {
                tracker.eventError(TrackingData.UIError.NetworkError.INSTANCE, TrackingData.Screen.IDENTIFICATION);
            } else if (error.getErrorType() == ClientError.ErrorType.INVALID_EMAIL) {
                tracker.eventError(TrackingData.UIError.InvalidEmail.INSTANCE, TrackingData.Screen.IDENTIFICATION);
            } else if (error.getErrorType() == ClientError.ErrorType.INVALID_PHONE_NUMBER) {
                tracker.eventError(TrackingData.UIError.InvalidPhone.INSTANCE, TrackingData.Screen.IDENTIFICATION);
            }
        }
    }

    private void identifyUser(final Identifier.IdentifierType identifierType, final String input, final InputField identifier) {
        provider.provide(new Identifier(identifierType, input), new ResultCallback<NoValue>() {
            @Override
            public void onSuccess(NoValue res) {
                identificationView.clearField();
            }

            @Override
            public void onError(@NotNull ClientError error) {
                if (identificationView.isActive()) {
                    if (ErrorUtil.INSTANCE.isServerError(error.getErrorType())) {
                        identificationView.showErrorDialog(error, null);
                    } else {
                        identificationView.showError(identifier);
                    }
                    identificationView.hideProgress();
                    trackError(error);
                }
            }
        });
    }

    private void getAccountStatus(final IdentifierType identifierType, final String input, final InputField identifier, final boolean allowSignUp, final String signUpErrorMessage) {
        final Identifier id = new Identifier(identifierType, input);
        id.getAccountStatus(new ResultCallback<AccountStatusResponse>() {
            @Override
            public void onSuccess(AccountStatusResponse result) {
                final UiTracking tracker = BaseLoginActivity.getTracker();
                if (tracker != null) {
                    tracker.setIntent(result.isAvailable() ? TrackingData.UserIntent.CREATE : TrackingData.UserIntent.LOGIN);
                }

                if (result.isAvailable() && !allowSignUp) {
                    onError(new ClientError(ClientError.ErrorType.SIGNUP_FORBIDDEN, "Signup is not allowed"));
                    return;
                }

                if (provider == null) { // Having no provider means we have a password flow
                    identificationView.clearField();
                    final FlowSelectionListener.FlowType flowType = result.isAvailable() ? FlowSelectionListener.FlowType.SIGN_UP : FlowSelectionListener.FlowType.LOGIN;
                    flowSelectionListener.onFlowSelected(flowType, id);
                } else { // Otherwise, passwordless
                    identifyUser(identifierType, input, identifier);
                }
            }

            @Override
            public void onError(@NotNull ClientError error) {
                if (identificationView.isActive()) {
                    boolean isSignUpForbidden = error.getErrorType() == ClientError.ErrorType.SIGNUP_FORBIDDEN;
                    boolean showDialog = isSignUpForbidden || ErrorUtil.INSTANCE.isServerError(error.getErrorType());
                    if (showDialog) {
                        if (isSignUpForbidden) {
                            identificationView.showErrorDialog(error, signUpErrorMessage);
                        } else {
                            identificationView.showErrorDialog(error, null);
                        }
                    } else {
                        identificationView.showError(identifier);
                    }
                    identificationView.hideProgress();
                }
            }
        });
    }

    /**
     * Verify the input of the user, the input could be a phone number or an email address.
     * <p>
     * This method request a navigation to the next screen if the call was successful or show an error
     * otherwise.
     *
     * @param identifier {@link InputField} representing the input
     * @see PhoneInputView#getInput()
     */
    @Override
    public void verifyInput(final InputField identifier, final Identifier.IdentifierType identifierType, final boolean allowSignup, final String signUpErrorMessage) {
        if (identificationView.isActive()) {
            identificationView.hideError(identifier);
            if (identifier.isInputValid()) {
                identificationView.showProgress();
                final String input = identifier.getInput();
                getAccountStatus(identifierType, input, identifier, allowSignup, signUpErrorMessage);

            } else {
                final UiTracking tracker = BaseLoginActivity.getTracker();
                if (tracker != null) {
                    final TrackingData.UIError e = identifierType == Identifier.IdentifierType.SMS ? TrackingData.UIError.InvalidPhone.INSTANCE :
                            TrackingData.UIError.InvalidEmail.INSTANCE;
                    tracker.eventError(e, TrackingData.Screen.IDENTIFICATION);
                }
                identificationView.showError(identifier);
            }
        }
    }
}

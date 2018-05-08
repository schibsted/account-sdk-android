/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.verification;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.schibsted.account.common.tracking.TrackingData;
import com.schibsted.account.common.tracking.UiTracking;
import com.schibsted.account.engine.controller.PasswordlessController;
import com.schibsted.account.engine.input.Identifier;
import com.schibsted.account.model.error.ClientError;
import com.schibsted.account.ui.R;
import com.schibsted.account.ui.login.BaseLoginActivity;
import com.schibsted.account.ui.ui.FlowFragment;
import com.schibsted.account.ui.ui.component.AccountSelectorView;
import com.schibsted.account.ui.ui.component.CheckBoxView;
import com.schibsted.account.ui.ui.component.CodeInputView;
import com.schibsted.account.ui.ui.dialog.InformationDialogFragment;
import com.schibsted.account.ui.ui.dialog.SelectorDialog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;

/**
 * a {@link Fragment} displaying the code verification screen
 */
public class VerificationFragment extends FlowFragment<VerificationContract.Presenter> implements VerificationContract.View, AccountSelectorView.Listener {

    private static final String KEY_IDENTIFIER = "IDENTIFIER";

    /**
     * the presenter of this view
     *
     * @see VerificationPresenter
     */
    private VerificationContract.Presenter mobileVerificationPresenter;

    /**
     * the view allowing the user to enter the verification code
     */
    private CodeInputView codeInputView;

    /**
     * the user phone number
     */
    private Identifier identifier;
    private PasswordlessController passwordlessController;
    private CheckBoxView rememberMeView;

    /**
     * provide a new instance of this {@link Fragment}
     *
     * @param identifier the user identifier
     */
    public static VerificationFragment newInstance(Identifier identifier) {
        final Bundle args = new Bundle();
        final VerificationFragment fragment = new VerificationFragment();
        args.putParcelable(KEY_IDENTIFIER, identifier);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = savedInstanceState == null ? getArguments() : savedInstanceState;
        if (arguments != null) {
            identifier = arguments.getParcelable(KEY_IDENTIFIER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.schacc_verification_fragment_layout, container, false);
        primaryActionView = view.findViewById(R.id.mobile_verification_button_continue);
        secondaryActionView = view.findViewById(R.id.mobile_verification_button_resend);
        codeInputView = view.findViewById(R.id.verification_code_input_view);
        rememberMeView = view.findViewById(R.id.remember_me);

        final AccountSelectorView accountSelectorView = view.findViewById(R.id.identifier_modifier);
        ArrayList<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);
        accountSelectorView.setAccountIdentifier(identifiers);
        accountSelectorView.setActionListener(this);

        rememberMeView.setChecked(true);
        rememberMeView.getTextView().setText(getString(R.string.schacc_remember_me));

        final TextView rememberMeInfoView = view.findViewById(R.id.remember_me_info);
        rememberMeInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (navigationListener != null) {
                    navigationListener.onDialogNavigationRequested(
                            InformationDialogFragment.newInstance(
                                    getString(R.string.schacc_dialog_remember_me_title),
                                    getString(R.string.schacc_dialog_remember_me_description),
                                    R.drawable.schacc_ic_remember_me,
                                    null
                            ));
                }
            }
        });


        registerListeners();
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_IDENTIFIER, identifier);
    }

    /**
     * register listeners for the views of this class
     */
    private void registerListeners() {
        primaryActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCode();
            }
        });
        if (secondaryActionView != null) {
            secondaryActionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final UiTracking tracker = BaseLoginActivity.getTracker();
                    if (tracker != null) {
                        tracker.eventEngagement(TrackingData.Engagement.CLICK, TrackingData.UIElement.RESEND_VERIFICATION_CODE, TrackingData.Screen.VERIFICATION_CODE);
                    }

                    mobileVerificationPresenter.resendCode(passwordlessController);
                }
            });
        }

        codeInputView.setImeAction(EditorInfo.IME_ACTION_NEXT, new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    verifyCode();
                }
                return false;
            }
        });
    }

    private void verifyCode() {
        UiTracking tracker = BaseLoginActivity.getTracker();
        if (tracker != null) {
            tracker.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.VERIFICATION_CODE);
        }
        mobileVerificationPresenter.verifyCode(codeInputView, rememberMeView.isChecked());
    }

    public Boolean isRememberMeEnabled() {
        return rememberMeView.isChecked();
    }

    /**
     * set the presenter of this view
     *
     * @param presenter
     */
    @Override
    public void setPresenter(VerificationContract.Presenter presenter) {
        mobileVerificationPresenter = presenter;
    }

    public void setPasswordlessController(PasswordlessController passwordlessController) {
        this.passwordlessController = passwordlessController;
    }

    /**
     * Builds an {@link InformationDialogFragment} an ask the {@link #navigationListener}
     * to show it.
     * The {@link InformationDialogFragment} inform the user that the code was successfully resent
     */
    @Override
    public void showResendCodeView() {
        if (navigationListener != null) {
            final InformationDialogFragment dialog = InformationDialogFragment.newInstance(
                    getString(R.string.schacc_verification_dialog_title),
                    (String.format(Locale.ENGLISH, getString(R.string.schacc_verification_dialog_information), identifier.getIdentifier())),
                    R.drawable.schacc_ic_email, getActionLabel());
            navigationListener.onDialogNavigationRequested(dialog);
            dialog.setActionListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    navigationListener.onNavigateBackRequested();
                }
            });
        }
    }

    /**
     * This method provide the right action label to build the {@link InformationDialogFragment}
     *
     * @return the label of the action button contained by the {@link InformationDialogFragment}
     * @see #showResendCodeView()
     */
    private String getActionLabel() {
        if (identifier.getIdentifierType() == Identifier.IdentifierType.EMAIL) {
            return getString(R.string.schacc_verification_edit_email_address);
        } else {
            return getString(R.string.schacc_verification_edit_phone_number);
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

    @Override
    public void onDialogRequested(@NotNull final SelectorDialog selectorDialog) {
        if (navigationListener != null) {
            selectorDialog.setActionListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final UiTracking tracker = BaseLoginActivity.getTracker();
                    if (tracker != null) {
                        tracker.eventEngagement(TrackingData.Engagement.CLICK, TrackingData.UIElement.CHANGE_IDENTIFIER, TrackingData.Screen.VERIFICATION_CODE);
                    }

                    navigationListener.onNavigateBackRequested();
                    selectorDialog.dismiss();
                }
            });
            navigationListener.onDialogNavigationRequested(selectorDialog);
        }
    }
}

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.identification.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.schibsted.account.common.util.Logger;
import com.schibsted.account.ui.R;
import com.schibsted.account.ui.UiConfiguration;
import com.schibsted.account.ui.login.screen.identification.IdentificationContract;
import com.schibsted.account.ui.ui.component.InputFieldView;
import com.schibsted.account.ui.ui.rule.EmailValidationRule;

/**
 * a {@link Fragment} displaying the email identification screen
 */
public class EmailIdentificationFragment extends AbstractIdentificationFragment implements IdentificationContract.View {

    /**
     * Provides a way to the user to enter his identifier.
     */
    protected InputFieldView inputFieldView;

    /**
     * provide a new instance of this {@link Fragment}
     *
     * @param uiConfiguration
     * @return a parametrized instance of {@link MobileIdentificationFragment}
     */
    public static EmailIdentificationFragment newInstance(UiConfiguration uiConfiguration) {
        final Bundle args = new Bundle();
        EmailIdentificationFragment fragment = new EmailIdentificationFragment();
        args.putParcelable(KEY_UI_CONF, uiConfiguration);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        inputFieldView = new InputFieldView.Builder(getContext(), EmailValidationRule.INSTANCE)
                .setCancelable(true)
                .setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS)
                .setImeOption(EditorInfo.IME_ACTION_DONE)
                .setError(R.string.schacc_email_identification_error)
                .setTitle(R.string.schacc_email_label)
                .build();
        inputViewContainer.addView(inputFieldView);
        prefillIdentifier(uiConf.getIdentifier());
        identificationPolicy.setText(getString(R.string.schacc_email_privacy_information));
        return view;
    }

    @Override
    public void prefillIdentifier(String identifier) {
        final String tag = Logger.DEFAULT_TAG + "-" + this.getClass().getSimpleName();
        Logger.info(tag, "Attempting to prefill  email");
        if (TextUtils.isEmpty(identifier)) {
            Logger.info(tag, "email wasn't found");
        } else {
            if (EmailValidationRule.INSTANCE.isValid(identifier)) {
                inputFieldView.getInputView().setText(uiConf.getIdentifier());
                Logger.info(tag, "email has been prefilled");

            } else {
                Logger.warn(tag, "Failed to prefill the email - Wrong format");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerListeners();
    }

    /**
     * setup listeners for the views of this class
     */
    private void registerListeners() {
        primaryActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                identifyUser(inputFieldView);
            }
        });

        inputFieldView.setImeAction(EditorInfo.IME_ACTION_NEXT, new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    identifyUser(inputFieldView);
                }
                return false;
            }
        });
    }

    @Override
    public void clearField() {
        inputFieldView.reset();
    }
}


/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.identification.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.schibsted.account.ui.ui.component.PhoneInputView;

/**
 * a {@link Fragment} displaying the phone number identification screen
 */
public class MobileIdentificationFragment extends AbstractIdentificationFragment {

    /**
     * Provides a way to the user to enter his identifier.
     */
    protected PhoneInputView inputFieldView;

    /**
     * provide a new instance of this {@link Fragment}
     *
     * @param uiConfiguration
     * @return a parametrized instance of {@link MobileIdentificationFragment}
     */
    public static MobileIdentificationFragment newInstance(UiConfiguration uiConfiguration) {
        final Bundle args = new Bundle();
        final MobileIdentificationFragment fragment = new MobileIdentificationFragment();
        args.putParcelable(KEY_UI_CONF, uiConfiguration);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        inputFieldView = new PhoneInputView(getContext());
        inputFieldView.setPhonePrefixHint(uiConf.getDefaultPhonePrefix());
        inputViewContainer.addView(inputFieldView);
        identificationPolicy.setText(getString(R.string.schacc_mobile_privacy_information));

        prefillIdentifier(uiConf.getIdentifier());
        return view;
    }

    @Override
    protected void prefillIdentifier(String phoneNumber) {
        final String tag = Logger.DEFAULT_TAG + "-" + this.getClass().getSimpleName();
        Logger.info(tag, "Attempting to prefill the phone number");
        if (TextUtils.isEmpty(phoneNumber)) {
            Logger.info(tag, "The phone number wasn't found");
        } else {
            if (TextUtils.isDigitsOnly(phoneNumber)) {
                inputFieldView.setPhonePrefix(uiConf.getDefaultPhonePrefix());
                inputFieldView.setPhoneNumber(uiConf.getIdentifier());
                Logger.info(tag, "The phone number has been prefilled");
            } else {
                Logger.warn(tag, "Failed to prefill the phone number - Wrong format");
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
    public void onDestroyView() {
        inputFieldView.reset();
        super.onDestroyView();
    }

    @Override
    public void clearField() {
        inputFieldView.reset();
    }
}

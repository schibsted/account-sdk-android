/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.component;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.schibsted.account.ui.R;
import com.schibsted.account.ui.ui.InputField;
import com.schibsted.account.ui.ui.rule.BasicValidationRule;
import com.schibsted.account.ui.ui.rule.MobileValidationRule;
import com.schibsted.account.ui.ui.rule.ValidationRule;

import org.jetbrains.annotations.NotNull;

/**
 * View use for phone number input
 */
public class PhoneInputView extends LinearLayout implements InputField {


    private final EditText prefixView;
    private final InputFieldView mobileNumberView;
    private final TextView errorView;
    private final ValidationRule validationRule;

    /**
     * Constructor
     *
     * @param context
     */
    public PhoneInputView(Context context) {
        this(context, null);
    }

    /**
     * Constructor
     *
     * @param context
     * @param attrs
     */
    public PhoneInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final View view = LayoutInflater.from(context).inflate(R.layout.schacc_phone_widget, this);

        prefixView = view.findViewById(R.id.prefix);
        final FrameLayout digitsContainer = view.findViewById(R.id.number);
        errorView = view.findViewById(R.id.mobile_error);

        mobileNumberView = new InputFieldView.Builder(context, BasicValidationRule.INSTANCE)
                .setCancelable(true)
                .setTitleVisible(false)
                .setImeOption(EditorInfo.IME_ACTION_DONE)
                .setInputType(InputType.TYPE_CLASS_PHONE)
                .setHint(R.string.schacc_mobile_number_hint)
                .build();

        digitsContainer.addView(mobileNumberView);

        validationRule = MobileValidationRule.INSTANCE;

        prefixView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int ime, KeyEvent keyEvent) {
                if (ime == EditorInfo.IME_ACTION_NEXT) {
                    mobileNumberView.inputField.requestFocus();
                }
                return false;
            }
        });

        OnFocusChangeListener focusListener = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                setBackgroundDependingOnFocus();
            }
        };
        mobileNumberView.inputField.setOnFocusChangeListener(focusListener);
        prefixView.setOnFocusChangeListener(focusListener);

        mobileNumberView.inputField.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mobileNumberView.inputField.requestFocus();
            }
        });

        final TextWatcher internalWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence sequence, int start, int count, int after) {
                hideErrorView();
            }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                hideErrorView();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        mobileNumberView.setTextWatcher(internalWatcher);
        prefixView.addTextChangedListener(internalWatcher);
    }

    public void setPhonePrefixHint(int phonePrefix) {
        final String prefixText = getResources().getString(R.string.schacc_mobile_prefix_hint);
        prefixView.setHint(String.format(prefixText, phonePrefix));
    }

    public void setPhonePrefix(int phonePrefix) {
        final String prefixText = getResources().getString(R.string.schacc_mobile_prefix_hint);
        prefixView.setText(String.format(prefixText, phonePrefix));
    }

    public void setPhoneNumber(String phoneNumber) {
        mobileNumberView.getInputView().setText(phoneNumber);
    }

    /**
     * Displays a different background indicating if this view has the focus or not
     */
    protected void setBackgroundDependingOnFocus() {
        if (!isErrorVisible()) {
            if (prefixView.hasFocus() || mobileNumberView.hasFocus()) {
                mobileNumberView.inputField.setBackgroundResource(R.drawable.schacc_field_shape_focused);
                prefixView.setBackgroundResource(R.drawable.schacc_field_shape_focused);
            } else {
                prefixView.setBackgroundResource(R.drawable.schacc_field_shape_unfocused);
                mobileNumberView.inputField.setBackgroundResource(R.drawable.schacc_field_shape_unfocused);
            }
        }
    }

    public void reset() {
        mobileNumberView.reset();
        prefixView.setText("");
    }

    /**
     * Provides the input
     *
     * @return The input of the field, without visual formatting
     */
    @Override
    public String getInput() {
        return prefixView.getText().toString().trim() + mobileNumberView.getInput();
    }

    /**
     * Verifies if the input is a valid.
     * To be valid the input should not be empty, should only contain digits after the first character,
     * and should not be equal to the default prefix provided by the client.
     *
     * @return true if input is valid false otherwise
     */
    @Override
    public boolean isInputValid() {
        return validationRule.isValid(getInput());
    }

    @Override
    public void showErrorView() {
        if (!isErrorVisible()) {
            errorView.setVisibility(VISIBLE);
            mobileNumberView.inputField.setBackgroundResource(R.drawable.schacc_input_field_widget_shape_error);
            prefixView.setBackgroundResource(R.drawable.schacc_input_field_widget_shape_error);
        }
    }

    @Override
    public void hideErrorView() {
        if (isErrorVisible()) {
            mobileNumberView.hideErrorView();
            errorView.setVisibility(GONE);
            setBackgroundDependingOnFocus();
        }
    }

    @Override
    public boolean isErrorVisible() {
        return errorView.getVisibility() == VISIBLE;
    }

    @Override
    public void setTextWatcher(TextWatcher textWatcher) {
        mobileNumberView.setTextWatcher(textWatcher);
        prefixView.addTextChangedListener(textWatcher);
    }

    @Override
    public void giveFocus() {
        prefixView.requestFocus();
    }

    @Override
    public void setOnEditorActionListener(TextView.OnEditorActionListener onEditorActionListener) {

    }

    @Override
    public void setError(int message) {
        errorView.setText(message);
    }

    @Override
    public void setError(@NotNull String message) {
        errorView.setText(message);
    }
}

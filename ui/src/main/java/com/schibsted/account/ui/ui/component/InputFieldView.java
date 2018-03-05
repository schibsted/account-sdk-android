/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.schibsted.account.ui.R;
import com.schibsted.account.ui.ui.InputField;
import com.schibsted.account.ui.ui.rule.BasicValidationRule;
import com.schibsted.account.ui.ui.rule.ValidationRule;

/**
 * Base class for creating a cancelable input view,
 * a cancelable input view allow you to erase the text of {@link #inputField}
 * thanks to a cross on the right of the field.
 */
public class InputFieldView extends LinearLayout implements InputField {

    /**
     * The field use to be filled in by the user.
     */
    protected EditText inputField;

    /**
     * The field used to display an error message.
     */
    protected TextView errorView;

    /**
     * The label of the widget.
     */
    protected TextView labelView;
    /**
     * The field used to display optional extra information
     */
    protected TextView infoView;
    private ValidationRule validationRule;
    private boolean isCancelable = false;

    protected InputFieldView(Context context) {
        this(context, null);
    }

    public InputFieldView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        final View view = LayoutInflater.from(context).inflate(R.layout.schacc_input_field_widget, this);
        errorView = view.findViewById(R.id.input_error_view);
        infoView = view.findViewById(R.id.input_extra_info_view);
        labelView = view.findViewById(R.id.input_field_widget_label);
        inputField = view.findViewById(R.id.input);
        validationRule = BasicValidationRule.INSTANCE;

        getXmlProperties(context, attrs);

        init();
    }

    private void getXmlProperties(Context context, AttributeSet attrs) {
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.InputFieldView, 0, 0);
        try {
            if (ta.hasValue(R.styleable.InputFieldView_inputType)) {
                inputField.setInputType(getInputTypeFromAttrs(ta) | InputType.TYPE_CLASS_TEXT);
            }
            if (ta.hasValue(R.styleable.InputFieldView_imeOptions)) {
                inputField.setImeOptions(getImeOptionsFromAttrs(ta));
            }
            if (ta.hasValue(R.styleable.InputFieldView_cancelable)) {
                setCancelable(ta.getBoolean(R.styleable.InputFieldView_cancelable, false));
            }
            if (ta.hasValue(R.styleable.InputFieldView_errorText)) {
                errorView.setText(ta.getString(R.styleable.InputFieldView_errorText));
            }

            if (ta.hasValue(R.styleable.InputFieldView_titleText)) {
                labelView.setText(ta.getString(R.styleable.InputFieldView_titleText));
            }

            if (ta.hasValue(R.styleable.InputFieldView_infoText)) {
                infoView.setText(ta.getString(R.styleable.InputFieldView_infoText));
            }
        } finally {
            ta.recycle();
        }
    }

    private int getInputTypeFromAttrs(TypedArray typedArray) {
        final int inputType = typedArray.getInt(R.styleable.InputFieldView_inputType, InputType.TYPE_CLASS_TEXT);
        switch (inputType) {
            case 0:
                inputField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                return InputType.TYPE_TEXT_VARIATION_PASSWORD;
            case 1:
                return InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS;
            case 2:
                return InputType.TYPE_CLASS_PHONE;
            default:
                return InputType.TYPE_CLASS_TEXT;
        }
    }

    private int getImeOptionsFromAttrs(TypedArray typedArray) {
        final int imeOptions = typedArray.getInt(R.styleable.InputFieldView_imeOptions, EditorInfo.IME_ACTION_NONE);
        switch (imeOptions) {
            case 0:
                return EditorInfo.IME_ACTION_DONE;
            case 1:
                return EditorInfo.IME_ACTION_NEXT;
            case 2:
                return EditorInfo.IME_ACTION_GO;
            default:
                return EditorInfo.IME_ACTION_NONE;
        }
    }

    /**
     * 1- Add a {@link android.view.View.OnFocusChangeListener} on {@link #inputField}
     * in order to show or hide the {@link #errorView} and call {@link #setBackgroundDependingOnFocus(boolean, View)} )}
     * <p>
     * 2 - Add a {@link android.view.View.OnTouchListener} on {@link #inputField}
     * in order to erase the text when the cross is clicked.
     * <p>
     * 3- Add a {@link TextWatcher} on {@link #inputField} in order to show or hide the cross if there is
     * or not a text to erase.
     */
    private void init() {
        final OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (errorView.getVisibility() == VISIBLE) {
                    errorView.setVisibility(GONE);
                }
                setBackgroundDependingOnFocus(hasFocus, inputField);
                if (isCancelable) {
                    if (hasFocus) {
                        updateCancelAction(inputField.getText().toString());
                    } else {
                        inputField.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    }
                }
            }
        };
        inputField.setOnFocusChangeListener(focusChangeListener);
        inputField.setSingleLine();

        infoView.setVisibility(TextUtils.isEmpty(infoView.getText()) ? GONE : VISIBLE);

        labelView.setTextIsSelectable(true);
    }

    public EditText getInputView() {
        return inputField;
    }

    @Override
    public void setError(@StringRes int error) {
        errorView.setText(error);
    }

    @Override
    public void setError(@NonNull String error) {
        errorView.setText(error);
    }

    public void reset() {
        inputField.setText("");
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setCancelable(final boolean isCancelable) {
        this.isCancelable = isCancelable;
        final TextWatcher internalWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence sequence, int start, int count, int after) {
                hideErrorView();
            }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                hideErrorView();
                if (isCancelable) {
                    updateCancelAction(sequence);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        if (isCancelable) {
            inputField.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        final Rect rect = new Rect();
                        inputField.getGlobalVisibleRect(rect);
                        //if we click on the drawable attached to the editText
                        if (motionEvent.getRawX() >= rect.right - inputField.getTotalPaddingRight()) {
                            inputField.setText("");
                        }
                    }
                    return false;
                }
            });


        } else {
            inputField.setOnTouchListener(null);
        }
        inputField.addTextChangedListener(internalWatcher);
    }

    private void updateCancelAction(CharSequence sequence) {
        if (TextUtils.isEmpty(sequence)) {
            inputField.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        } else {
            inputField.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getContext(), R.drawable.schacc_ic_cancel), null);
        }
    }

    public void setInfoText(String infoText) {
        infoView.setText(infoText);
        infoView.setVisibility(TextUtils.isEmpty(infoText) ? GONE : VISIBLE);
    }

    public void setValidationRule(ValidationRule validationRule) {
        this.validationRule = validationRule;
    }

    protected void setTitleVisible(boolean labelVisible) {
        labelView.setVisibility(labelVisible ? VISIBLE : GONE);
    }

    public void setTitle(@StringRes final int label) {
        labelView.setText(label);
    }

    public void setTitle(String title) {
        labelView.setText(title);
    }

    @Override
    public void setTextWatcher(TextWatcher textWatcher) {
        inputField.addTextChangedListener(textWatcher);
    }

    @Override
    public void giveFocus() {
        inputField.requestFocus();
    }

    @Override
    public void setOnEditorActionListener(TextView.OnEditorActionListener onEditorActionListener) {
        inputField.setOnEditorActionListener(onEditorActionListener);
    }

    public void setInformationMessage(@NonNull String message) {
        infoView.setText(message);
        infoView.setVisibility(TextUtils.isEmpty(message) ? GONE : VISIBLE);
    }

    /**
     * Displays a different background indicating if this view has the focus or not
     *
     * @param hasFocus <code>true</code> if the view has the focus <code>false</code> otherwise
     */
    protected void setBackgroundDependingOnFocus(final boolean hasFocus, final View view) {
        if (hasFocus) {
            view.setBackgroundResource(R.drawable.schacc_field_shape_focused);
        } else {
            view.setBackgroundResource(R.drawable.schacc_field_shape_unfocused);
        }
    }

    public void showKeyboard(boolean showKeyboard) {
        labelView.setTextIsSelectable(!showKeyboard);
    }

    /**
     * Show an error {@link TextView} and modify the background of this view accordingly.
     *
     * @see #hideErrorView()
     */
    @Override
    public void showErrorView() {
        if (!isErrorVisible()) {
            errorView.setVisibility(VISIBLE);
            inputField.setBackgroundResource(R.drawable.schacc_input_field_widget_shape_error);
        }
    }

    /**
     * hide the previously displayed error and modify the background of this view accordingly.
     *
     * @see #showErrorView()
     */
    @Override
    public void hideErrorView() {
        if (isErrorVisible()) {
            errorView.setVisibility(GONE);
            setBackgroundDependingOnFocus(hasFocus(), inputField);
        }
    }

    @Override
    public boolean isErrorVisible() {
        return errorView.getVisibility() == VISIBLE;
    }

    /**
     * Provides the input
     *
     * @return The input of the field
     */
    @Override
    public String getInput() {
        return inputField.getText().toString().trim();
    }

    @Override
    public boolean isInputValid() {
        return validationRule.isValid(getInput());
    }

    public static class Builder {

        private final Context context;
        private final InputFieldView view;

        public Builder(Context context, ValidationRule validationRule) {
            this.context = context;
            view = new InputFieldView(this.context);
            view.setValidationRule(validationRule);
        }

        public Builder setCancelable(boolean isCancelable) {
            view.setCancelable(isCancelable);
            return this;
        }

        public Builder setTitleVisible(boolean isVisible) {
            view.setTitleVisible(isVisible);
            return this;
        }

        public Builder setTitle(@StringRes int titleRes) {
            view.setTitle(titleRes);
            return this;
        }

        public Builder setTitle(String title) {
            view.setTitle(title);
            return this;
        }

        public Builder setError(String error) {
            view.setError(error);
            return this;
        }

        public Builder setError(@StringRes int error) {
            view.setError(error);
            return this;
        }

        public Builder setHint(@StringRes int hint) {
            view.inputField.setHint(hint);
            return this;
        }

        public Builder setHint(String hint) {
            view.inputField.setHint(hint);
            return this;
        }

        public Builder setImeOption(final int imeOption) {
            view.inputField.setImeOptions(imeOption);
            return this;
        }

        public Builder setInputType(final int inputType) {
            view.inputField.setInputType(inputType);
            return this;
        }

        public Builder setInformationMessage(final String message) {
            view.setInformationMessage(message);
            return this;
        }

        public InputFieldView build() {
            return view;
        }

    }

}

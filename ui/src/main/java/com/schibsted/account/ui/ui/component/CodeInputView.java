/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.component;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.schibsted.account.ui.R;
import com.schibsted.account.ui.ui.InputField;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Widget made to allow the user to enter a 6 digits code
 */
public class CodeInputView extends RelativeLayout implements InputField, View.OnClickListener, CustomEditText.KeyEventListener {

    /**
     * The expected length the verification code.
     */
    private static int EXPECTED_LENGTH = 6;

    /**
     * List of {@link EditText} used to put in the verification digits
     */
    List<CustomEditText> inputViews;

    /**
     * The index of the focused {@link EditText} inside {@link #inputViews}
     */
    private int focusedViewPosition;

    /**
     * a {@link TextView} used to display an error message
     */
    private TextView errorView;

    /**
     * The {@link EditText} used to enter the first digit
     */
    private CustomEditText digit1;

    public CodeInputView(Context context) {
        this(context, null);
    }

    public CodeInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initializes views and listeners
     */
    private void init() {
        final Context context = getContext();
        final View view = LayoutInflater.from(context).inflate(R.layout.schacc_code_verification_widget, this);
        errorView = view.findViewById(R.id.input_error_view);
        inputViews = initializeInputViews(view);

        final OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (errorView.getVisibility() == VISIBLE) {
                    errorView.setVisibility(INVISIBLE);
                }
                applyEditTextLayout(inputViews);
                final int length = ((EditText) view).getText().length();
                ((EditText) view).setSelection(length);
                focusedViewPosition = inputViews.indexOf(view);
            }
        };


        final TextWatcher internalTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence sequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                if (!TextUtils.isEmpty(sequence) && before < count) {
                    setFocusOnNextInputView(inputViews);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        setDeleteKeyListener(this, inputViews);
        setInputViewsFocusListener(focusChangeListener, inputViews);
        setInputViewsTextWatcher(internalTextWatcher, inputViews);
        setOnClickListener(this, inputViews);
    }

    private void setDeleteKeyListener(CustomEditText.KeyEventListener eventListener, List<CustomEditText> inputViews) {
        for (CustomEditText inputView : inputViews) {
            inputView.setKeyEventListener(eventListener);
        }
    }

    /**
     * Called in {@link #init()} this method populates the {@link List<EditText>} with views defined in
     * the xml file
     *
     * @param view the inflated view
     * @return a {@link List<EditText>} to assign to {@link #inputViews}
     */
    private List<CustomEditText> initializeInputViews(View view) {
        final List<CustomEditText> inputViews = new ArrayList<>();
        digit1 = view.findViewById(R.id.input_1);
        final CustomEditText digit2 = view.findViewById(R.id.input_2);
        final CustomEditText digit3 = view.findViewById(R.id.input_3);
        final CustomEditText digit4 = view.findViewById(R.id.input_4);
        final CustomEditText digit5 = view.findViewById(R.id.input_5);
        final CustomEditText digit6 = view.findViewById(R.id.input_6);

        digit1.setContentDescription(getContext().getString(R.string.schacc_accessibility_verification_code_no, 1, EXPECTED_LENGTH));
        digit2.setContentDescription(getContext().getString(R.string.schacc_accessibility_verification_code_no, 2, EXPECTED_LENGTH));
        digit3.setContentDescription(getContext().getString(R.string.schacc_accessibility_verification_code_no, 3, EXPECTED_LENGTH));
        digit4.setContentDescription(getContext().getString(R.string.schacc_accessibility_verification_code_no, 4, EXPECTED_LENGTH));
        digit5.setContentDescription(getContext().getString(R.string.schacc_accessibility_verification_code_no, 5, EXPECTED_LENGTH));
        digit6.setContentDescription(getContext().getString(R.string.schacc_accessibility_verification_code_no, 6, EXPECTED_LENGTH));

        inputViews.add(digit1);
        inputViews.add(digit2);
        inputViews.add(digit3);
        inputViews.add(digit4);
        inputViews.add(digit5);
        inputViews.add(digit6);

        final AccessibilityManager am = (AccessibilityManager)getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null || !am.isEnabled()) {
            digit1.setFocusableInTouchMode(true);
            digit1.requestFocus();
        }

        return inputViews;
    }

    /**
     * Attributes a {@link TextWatcher} to each {@link EditText} of a list of it.
     *
     * @param textWatcher the {@link TextWatcher} to attribute
     * @param editTexts   the list of {@link EditText}
     */
    private void setInputViewsTextWatcher(TextWatcher textWatcher, List<CustomEditText> editTexts) {
        for (EditText editText : editTexts) {
            editText.addTextChangedListener(textWatcher);
        }
    }

    /**
     * Attributes a {@link android.view.View.OnFocusChangeListener} to each {@link EditText} of a list of it.
     *
     * @param focusChangeListener the {@link android.view.View.OnFocusChangeListener} to attribute
     * @param editTexts           the list of {@link EditText}
     */
    private void setInputViewsFocusListener(View.OnFocusChangeListener focusChangeListener, List<CustomEditText> editTexts) {
        for (EditText editText : editTexts) {
            editText.setOnFocusChangeListener(focusChangeListener);
        }
    }

    /**
     * Attributes a {@link android.view.View.OnClickListener} to each {@link EditText} of a list of it.
     *
     * @param clickListener the {@link android.view.View.OnClickListener} to attribute
     * @param inputViews    the list of {@link EditText}
     */
    private void setOnClickListener(OnClickListener clickListener, List<CustomEditText> inputViews) {
        for (EditText inputView : inputViews) {
            inputView.setOnClickListener(clickListener);
        }
    }

    /**
     * Request the focus on the next {@link EditText} contained in the {@link #inputViews}
     * This method disabled the focusable property of the previous focused view and enable this
     * property for the new focused {@link EditText}
     */
    private void setFocusOnPreviousInputView() {
        final int previousViewPosition = focusedViewPosition - 1;
        if (previousViewPosition >= 0) {
            final EditText view = inputViews.get(previousViewPosition);
            inputViews.get(focusedViewPosition).setFocusableInTouchMode(false);
            view.setFocusableInTouchMode(true);
            view.requestFocus();
        }
    }

    /**
     * Requests focus on the next {@link EditText} in {@link #inputViews}
     * This method disabled the focusable property of the previous focused view and enable this
     * property for the new focused {@link EditText}
     * If the focused view is the last one in {@link #inputViews} we keep the focus on it.
     *
     * @param inputViews the {@link List<EditText>}
     */
    private void setFocusOnNextInputView(List<CustomEditText> inputViews) {
        final int nextFocusedViewPosition = focusedViewPosition + 1;
        if (nextFocusedViewPosition < inputViews.size()) {
            //if we haven't reached the last digit yet focus the next one.
            final EditText view = inputViews.get(nextFocusedViewPosition);
            view.setFocusableInTouchMode(true);
            inputViews.get(focusedViewPosition).setFocusableInTouchMode(false);
            view.requestFocus();
        } else {
            /*
            we're one the last digit already, just be sure to place the cursor after the digit
            to ensure it to be erased when the delete key is pressed
             */
            final EditText view = inputViews.get(focusedViewPosition);
            final int length = view.getText().length();
            view.setSelection(length);
        }
    }

    /**
     * Applies colors on the {@link EditText} underline depending on the focus and the state of the
     * {@link EditText}
     * If the view has the focus or is filled in with a digit we apply <code>R.color.primaryEnabled</code>
     * <code>R.color.darkGrey</code> otherwise
     *
     * @param editTexts the {@link List<EditText>} to iterate on
     */
    private void applyEditTextLayout(List<CustomEditText> editTexts) {

        for (EditText editText : editTexts) {
            if (editText.hasFocus()) {
                editText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.schacc_primaryEnabled), PorterDuff.Mode.SRC_ATOP);
            } else {
                final String fieldText = editText.getText().toString();
                if (TextUtils.isEmpty(fieldText)) {
                    editText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.schacc_darkGrey), PorterDuff.Mode.SRC_ATOP);
                } else {
                    editText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.schacc_primaryEnabled), PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
    }

    /**
     * Resets color, input and focus of each {@link EditText} contained in {@link #inputViews}
     * This method should be called when an error occurred and the user try to edit the code.
     *
     * @param inputViews a {@link List<EditText>}
     */
    private void resetFields(final List<CustomEditText> inputViews) {
        for (EditText inputView : inputViews) {
            inputView.setText("");
            inputView.setFocusableInTouchMode(false);
        }
        digit1.setFocusableInTouchMode(true);
        digit1.requestFocus();
        applyEditTextLayout(inputViews);
    }

    /**
     * Return the current input
     *
     * @return result of {@link #getFullInput()}
     */
    @Nullable
    @Override
    public String getInput() {
        return getFullInput();
    }

    /**
     * Concatenates input of each {@link EditText} of {@link #inputViews} and return the result
     *
     * @return {@link String} representing the concatenated
     */
    private String getFullInput() {
        final StringBuilder finalInputBuilder = new StringBuilder();
        for (EditText inputView : inputViews) {
            finalInputBuilder.append(inputView.getText().toString());
        }
        return finalInputBuilder.toString();
    }

    /**
     * Checks if the code returned by {@link #getFullInput()} matches requirement
     *
     * @return <code>true</code> if the code is valid <code>false</code> otherwise
     */
    @Override
    public boolean isInputValid() {
        final CharSequence code = getFullInput().trim();
        return code.length() == EXPECTED_LENGTH && TextUtils.isDigitsOnly(code);
    }

    /**
     * Shows the error message and apply a red color filter to {@link #inputViews}'s {@link EditText}
     */
    @Override
    public void showErrorView() {
        for (EditText editText : inputViews) {
            editText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.schacc_error), PorterDuff.Mode.SRC_ATOP);
        }
        errorView.setVisibility(VISIBLE);
    }

    /**
     * Hides error message and call {@link #resetFields(List)}
     */
    @Override
    public void hideErrorView() {
        errorView.setVisibility(GONE);
        resetFields(inputViews);
    }

    /**
     * Checks if the error message is displayed
     *
     * @return <code>true</code> if the error message is displayed, <code>false</code> otherwise
     */
    @Override
    public boolean isErrorVisible() {
        return errorView.getVisibility() == VISIBLE;
    }

    /**
     * Sets a {@link TextWatcher} to {@link #inputViews}'s {@link EditText}
     *
     * @param textWatcher the {@link TextWatcher} used to interact with the input field.
     */
    public void setTextWatcher(final TextWatcher textWatcher) {
        setInputViewsTextWatcher(textWatcher, inputViews);
    }

    @Override
    public void setImeAction(int imeOption, TextView.OnEditorActionListener editorActionListener) {
        CustomEditText lastDigit = inputViews.get(inputViews.size() - 1);
        lastDigit.setImeOptions(imeOption);
        lastDigit.setOnEditorActionListener(editorActionListener);
    }

    /**
     * Calls {@link #hideErrorView()} if {@link #isErrorVisible()} return true
     *
     * @param view the view clicked by the user
     */
    @Override
    public void onClick(View view) {
        if (isErrorVisible()) {
            hideErrorView();
        }
    }

    @Override
    public void onDeleteKeyPressed() {
        setFocusOnPreviousInputView();
        inputViews.get(focusedViewPosition).setText("");
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

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui;

import android.support.annotation.Nullable;
import android.text.TextWatcher;
import android.widget.TextView;

/**
 * Describes an input field which is entitled to prevent access to its content if it is considered invalid.
 * Implementations should verify their input before potentially providing it.
 */
public interface InputField extends ErrorField {

    /**
     * Provides the complete input of this field if valid. Implementations should return
     * <code>null</code> if the input is not valid.
     *
     * @return The complete input of this field if valid. Implementations should return
     * <code>null</code> if the input is not valid.
     */
    @Nullable
    String getInput();

    /**
     * Checks if rules are respected for the input to be valid
     *
     * @return <code>true</code> if the input matches requirements <code>false</code> otherwise
     */
    boolean isInputValid();


    /**
     * Sets an external textWatcher to interact with the input field.
     *
     * @param textWatcher the {@link TextWatcher} used to interact with the input field.
     */
    void setTextWatcher(TextWatcher textWatcher);

    void giveFocus();

    void setOnEditorActionListener(TextView.OnEditorActionListener onEditorActionListener);
}

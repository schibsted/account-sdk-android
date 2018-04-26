/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.schibsted.account.ui.R;
import com.schibsted.account.ui.ui.ErrorField;

import org.jetbrains.annotations.NotNull;

/**
 * Defines a custom checkbox error resources and attach a text to this checkbox
 * If you have a {@link android.text.style.ClickableSpan} inside your text you should use
 * this class rather than {@link CheckBox} in order to avoid to give a focus to the {@link android.text.style.ClickableSpan}
 * when clicking on the {@link CheckBox}
 */
public class CheckBoxView extends LinearLayout implements ErrorField {

    /**
     * The error view used to display the error message
     */
    private final TextView errorView;

    /**
     * An usual {@link CheckBox}
     */
    private final CheckBox checkbox;

    /**
     * View used to display the text attached to the {@link CheckBox}
     */
    private final TextView textView;

    public CheckBoxView(Context context) {
        this(context, null);
    }

    public CheckBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final View view = LayoutInflater.from(context).inflate(R.layout.schacc_chexbox_widget, this);
        checkbox = view.findViewById(R.id.checkbox);
        errorView = view.findViewById(R.id.checkbox_error);
        textView = view.findViewById(R.id.checkbox_text);

        checkbox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isErrorVisible()) {
                    hideErrorView();
                }
            }
        });

        if (isInEditMode()) {
            final int attrRes = attrs.getAttributeResourceValue(null, "text", 0);
            if (attrRes > 0) {
                textView.setText(context.getString(attrRes, "Schibsted Account", "My Client"));
            } else {
                final String previewText = attrs.getAttributeValue(null, "text");
                textView.setText(previewText);
            }
        }
    }

    /**
     * Sets {@link #errorView} visibility to <code>VISIBLE</code>
     * Changes the button drawable of {@link #checkbox} to display the error behavior
     */
    @Override
    public void showErrorView() {
        errorView.setVisibility(VISIBLE);
        checkbox.setButtonDrawable(R.drawable.schacc_ic_checkbox_error);
    }

    /**
     * Sets {@link #errorView} visibility to <code>GONE</code>
     * Changes the button drawable of {@link #checkbox} to display the normal behavior
     */
    @Override
    public void hideErrorView() {
        errorView.setVisibility(GONE);
        checkbox.setButtonDrawable(R.drawable.schacc_checkbox_shape);

    }

    /**
     * Used to get {@link #textView}
     *
     * @return {@link TextView}
     */
    public TextView getTextView() {
        return textView;
    }

    public TextView getErrorView() {
        return errorView;
    }

    /**
     * @return <code>true</code> if the {@link #checkbox} is checked
     */
    public boolean isChecked() {
        return checkbox.isChecked();
    }

    public void setChecked(boolean checked) {
        checkbox.setChecked(checked);
    }

    /**
     * @return <code>true</code> if {@link #errorView} is visible <code>false</code> otherwise
     */
    @Override
    public boolean isErrorVisible() {
        return errorView.getVisibility() == VISIBLE;
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

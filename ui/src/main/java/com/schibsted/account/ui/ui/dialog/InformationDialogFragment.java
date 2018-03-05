/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.schibsted.account.ui.R;
import com.schibsted.account.util.Preconditions;

/**
 * Defines an information pop up containing an image
 * a title, a description and an additional action
 * this class could be use for any kind of information as error or basic feedback
 */
public class InformationDialogFragment extends DialogFragment {
    private static final String KEY_MESSAGE = "MESSAGE";
    private static final String KEY_DRAWABLE = "DRAWABLE";
    private static final String KEY_TITLE = "TITLE";
    private static final String KEY_ACTION = "ACTION";
    private View.OnClickListener actionListener;
    private DismissListener dismissListener;

    /**
     * constructor
     *
     * @param title       the title of the pop-up
     * @param message     the message of the pop-up
     * @param drawable    the picture to show
     * @param actionLabel the label of the action if any
     * @return a parametrized {@link InformationDialogFragment}
     */
    public static InformationDialogFragment newInstance(String title, final String message, @DrawableRes final int drawable, @Nullable final String actionLabel) {
        Preconditions.checkNotNull(title, message);
        final Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);
        args.putInt(KEY_DRAWABLE, drawable);
        args.putString(KEY_TITLE, title);
        args.putString(KEY_ACTION, actionLabel);

        final InformationDialogFragment fragment = new InformationDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.schacc_information_dialog_layout, container, false);
        final Button continueView = view.findViewById(R.id.information_button_continue);
        final TextView actionView = view.findViewById(R.id.information_optional_action);
        final TextView messageView = view.findViewById(R.id.information_description);
        final TextView titleView = view.findViewById(R.id.information_title);
        final ImageView imageView = view.findViewById(R.id.information_image);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            messageView.setText(arguments.getString(KEY_MESSAGE));
            titleView.setText(arguments.getString(KEY_TITLE));
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), arguments.getInt(KEY_DRAWABLE)));
            actionView.setText(arguments.getString(KEY_ACTION));
        }

        actionView.setVisibility(TextUtils.isEmpty(actionView.getText()) ? View.GONE : View.VISIBLE);
        actionView.setOnClickListener(actionListener);

        continueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return view;
    }

    /**
     * set the action actionListener tied to the actionView
     *
     * @param onClickListener the action actionListener to use
     */
    public void setActionListener(View.OnClickListener onClickListener) {
        actionListener = onClickListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissListener != null) {
            dismissListener.onDismiss();
        }

    }

    public void setOnDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss();
    }

}

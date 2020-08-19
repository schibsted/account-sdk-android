/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.component

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.ui.R
import com.schibsted.account.ui.ui.dialog.SelectorDialog
import kotlinx.android.synthetic.main.schacc_account_selector_widget.view.*
import java.util.ArrayList

class AccountSelectorView : LinearLayout {

    private var identifierView: TextView
    private var actionView: View
    private var identifier: ArrayList<Identifier?>? = null
    var actionListener: Listener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        val ta = context.obtainStyledAttributes(attrs, R.styleable.AccountSelectorView, 0, 0)
        if (ta.hasValue(R.styleable.AccountSelectorView_actionText)) {
            actionView = TextView(context)
            with(actionView as TextView) {
                text = ta.getString(R.styleable.AccountSelectorView_actionText)
                TextViewCompat.setTextAppearance(this, R.style.schacc_text_action)
            }
        } else {
            actionView = ImageView(context)
            with(actionView as ImageView) {
                setImageResource(R.drawable.schacc_ic_expand_more)
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            }
        }
        action_button.addView(actionView)
        actionView.setOnClickListener {
            actionListener?.onDialogRequested(SelectorDialog.newInstance(identifier, Pair(action_button.x, (parent as View).y), Pair(action_button.width, action_button.height)))
        }
        ta.recycle()
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.schacc_account_selector_widget, this)
        identifierView = view.findViewById(R.id.account_identifier)
    }

    fun setAccountIdentifier(identifier: ArrayList<Identifier?>) {
        this.identifier = identifier
        identifierView.text = identifier[0]?.identifier
    }

    interface Listener {
        fun onDialogRequested(selectorDialog: SelectorDialog)
    }
}

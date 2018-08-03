/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui.ui.component

import android.content.Context
import android.support.annotation.StringRes
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.schibsted.account.ui.ui.ErrorField
import org.jetbrains.annotations.NotNull

abstract class SchibstedView : LinearLayout, ErrorField {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    /**
     * The error view used to display the error message
     */
    var errorView: TextView? = null
        protected set

    /**
     * View used to display the label attached to the view
     */
    lateinit var labelView: TextView

    override fun setError(@StringRes message: Int) {
        errorView?.setText(message)
    }

    override fun setError(@NotNull message: String) {
        errorView?.text = message
    }

    protected fun setTitleVisible(labelVisible: Boolean) {
        labelView.visibility = if (labelVisible) View.VISIBLE else View.GONE
    }

    fun setTitle(@StringRes label: Int) = labelView.setText(label)

    fun setTitle(title: String) {
        labelView.text = title
    }

    override var isErrorVisible: Boolean
        get() = errorView?.visibility == View.VISIBLE
        set(value) {}
}
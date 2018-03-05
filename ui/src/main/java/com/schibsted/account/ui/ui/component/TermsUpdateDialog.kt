/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.component

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.Html
import android.text.Spanned
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.schibsted.account.ui.R
import kotlinx.android.synthetic.main.schacc_terms_dialog_layout.*

private const val KEY_SUMMARY = "SUMMARY"
class TermsUpdateDialog : DialogFragment() {
    private lateinit var summaryText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = savedInstanceState ?: arguments
        args?.let { summaryText = args.getString(KEY_SUMMARY) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.schacc_terms_dialog_layout, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        summary_view.text = summaryText.asHtml()
        summary_view.movementMethod = ScrollingMovementMethod()
        close_dialog_view.setOnClickListener { dismiss() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SUMMARY, summaryText)
    }

    companion object {
        @JvmStatic
        fun newInstance(summaryText: String): TermsUpdateDialog {
            val fragment = TermsUpdateDialog()
            val arg = Bundle()
            arg.putString(KEY_SUMMARY, summaryText)
            fragment.arguments = arg
            return fragment
        }
    }
}

private fun String.asHtml(): Spanned? {
    return Html.fromHtml(this.replace("\\n", "<br/>"))
}

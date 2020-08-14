/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.dialog

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.util.TypedValue
import android.view.Gravity.START
import android.view.Gravity.TOP
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.ui.R

private const val KEY_ITEMS = "ITEMS"
private const val KEY_POSITION = "POSITION"
private const val KEY_DIMENSION = "DIMENSION"

class SelectorDialog : DialogFragment() {

    private var items: ArrayList<Identifier>? = null
    private lateinit var containerView: FrameLayout
    var actionListener: View.OnClickListener? = null
    private lateinit var actionView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.schacc_selector_dialog, container, false) as View
        containerView = view.findViewById(R.id.account_container)
        actionView = view.findViewById(R.id.selector_action)
        val arg = savedInstanceState ?: arguments
        items = arg?.getParcelableArrayList(KEY_ITEMS) ?: arguments?.getParcelableArrayList(KEY_ITEMS)

        addItems(items, container)
        showDialog(arg)
        return view
    }

    private fun showDialog(arg: Bundle?) {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        // Calculate ActionBar height
        var actionBarHeight = 0
        activity?.theme?.let {
            val outValue = TypedValue()
            if (activity!!.theme.resolveAttribute(android.R.attr.actionBarSize, outValue, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(outValue.data, resources.displayMetrics)
            }
        }

        // open the dialog where the button we've just clicked is
        val attributes = dialog?.window?.attributes
        if (attributes != null) {
            with(attributes) {
                gravity = TOP or START
                arg?.getFloatArray(KEY_POSITION)?.let { arrayOfPosition ->
                    arg.getIntArray(KEY_DIMENSION)?.let { arrayOfDimension ->
                        x = (arrayOfPosition[0] - (arrayOfDimension[0] / 2)).toInt()
                        y = (actionBarHeight + arrayOfPosition[1] - (arrayOfDimension[1] / 2)).toInt()
                    }
                }
            }
        }
    }

    private fun addItems(items: java.util.ArrayList<Identifier>?, container: ViewGroup?) {
        items?.forEach {
            val checkView = LayoutInflater.from(context).inflate(R.layout.schacc_select_dialog_singlechoice, container)
            val tv: TextView = checkView.findViewById(R.id.account_text)
            with(tv) {
                textSize = 14F
                text = it.identifier
            }
            containerView.addView(checkView)
        }

        val firstCheck: ImageView = containerView.getChildAt(0).findViewById(R.id.account_check)
        firstCheck.visibility = View.VISIBLE
        actionView.setOnClickListener(actionListener)

        actionView.text = if (items?.get(0)?.identifierType == Identifier.IdentifierType.SMS) {
            getString(R.string.schacc_password_change_phone_number)
        } else {
            getString(R.string.schacc_password_change_email)
        }
    }

    companion object {

        fun newInstance(items: ArrayList<Identifier?>?, callerPosition: Pair<Float, Float>, pair: Pair<Int, Int>): SelectorDialog {
            val arg = Bundle()
            arg.putParcelableArrayList(KEY_ITEMS, items)
            arg.putFloatArray(KEY_POSITION, callerPosition.toList().toFloatArray())
            arg.putIntArray(KEY_DIMENSION, pair.toList().toIntArray())
            val fragment = SelectorDialog()
            fragment.arguments = arg
            return fragment
        }
    }
}

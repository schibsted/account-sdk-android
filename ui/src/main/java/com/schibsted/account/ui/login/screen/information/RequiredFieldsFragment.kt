/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.information

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.R
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.ui.FlowFragment
import com.schibsted.account.ui.ui.InputField
import com.schibsted.account.ui.ui.component.BirthdayInputView
import com.schibsted.account.ui.ui.component.InputFieldView
import com.schibsted.account.ui.ui.rule.ValidationRule
import kotlinx.android.synthetic.main.schacc_required_fields_layout.*

class RequiredFieldsFragment : FlowFragment<RequiredFieldsContract.Presenter>(), RequiredFieldsContract.View {
    override val isActive: Boolean get() = isAdded
    private lateinit var requiredFieldsPresenter: RequiredFieldsContract.Presenter
    private val generatedFields: MutableMap<String, InputField> = mutableMapOf()
    lateinit var missingField: Set<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.schacc_required_fields_layout, container, false)
        primaryActionView = view.findViewById(R.id.required_fields_button_continue)
        primaryActionView.setOnClickListener {
            BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.REQUIRED_FIELDS)
            requiredFieldsPresenter.updateMissingFields(generatedFields)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var lastView: InputFieldView? = null

        RequiredFields.values().forEach {
            missingField.run {
                if (contains(it.fieldsValue)) {
                    val generatedView = generateField(it.fieldsValue, it.titleRes, it.validationRule)
                    fields_container.addView(generatedView)
                    generatedFields[it.fieldsValue] = generatedView

                    lastView = generatedView
                }
            }
        }

        lastView?.inputView?.imeOptions = EditorInfo.IME_ACTION_DONE
    }

    private fun generateField(fieldName: String, titleRes: Int, validationRule: ValidationRule): InputFieldView {
        val generatedView: InputFieldView = if (fieldName == RequiredFields.BIRTHDAY.fieldsValue) {
            BirthdayInputView(context)
        } else {
            InputFieldView.Builder(context, validationRule)
                    .setInputType(EditorInfo.TYPE_CLASS_TEXT)
                    .setError(R.string.schacc_required_fields_error)
                    .setCancelable(true)
                    .setTitle(titleRes)
                    .build()
        }

        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 0, 0,
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        INFORMATION_FIELD_MARGIN_BOTTOM,
                        resources.displayMetrics).toInt())

        generatedView.layoutParams = params
        return generatedView
    }

    override fun hideErrors() {
        generatedFields.forEach { it.value.hideErrorView() }
    }

    override fun setPresenter(presenter: RequiredFieldsContract.Presenter) {
        requiredFieldsPresenter = presenter
    }

    override fun showErrorDialog(error: ClientError, errorMessage: String?) {
        displayErrorDialog(error, errorMessage)
    }

    companion object {

        @JvmStatic
        fun newInstance(): RequiredFieldsFragment {
            val fragment = RequiredFieldsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        const val INFORMATION_FIELD_MARGIN_BOTTOM = 28F
    }
}

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.information

import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.ui.InternalUiConfiguration
import com.schibsted.account.ui.R
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.login.screen.LoginScreen
import com.schibsted.account.ui.setPartAsClickableLink
import com.schibsted.account.ui.ui.FlowFragment
import com.schibsted.account.ui.ui.InputField
import com.schibsted.account.ui.ui.WebFragment
import com.schibsted.account.ui.ui.component.BirthdayInputView
import com.schibsted.account.ui.ui.component.FieldView
import com.schibsted.account.ui.ui.component.PhoneInputView
import com.schibsted.account.ui.ui.component.SingleFieldView
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
        primaryActionView.setOnClickListener { updateMissingFields() }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        required_fields_links.movementMethod = LinkMovementMethod.getInstance()
        required_fields_links.text = getDescriptionText()

        var lastView: FieldView? = null
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

        lastView?.setImeAction(EditorInfo.IME_ACTION_NEXT) { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                updateMissingFields()
            }
            return@setImeAction false
        }
    }

    private fun getDescriptionText(): SpannableString {
        @ColorInt val color = ContextCompat.getColor(context!!, R.color.schacc_primaryEnabled)
        val dataUsageText = getString(R.string.schacc_required_fields_privacy_adjustment)
        val privacyText = getString(R.string.schacc_required_fields_schibsted_data)
        val description = SpannableString(getString(R.string.schacc_required_fields_gdpr_description))

        val privacyLink = getString(R.string.schacc_required_fields_privacy_adjustment_link)
        val dataUsageLink = getString(R.string.schacc_required_fields_schibsted_data_link)

        description.setPartAsClickableLink(color, privacyText, getLinkAction(privacyLink, TrackingData.UIElement.ADJUST_PRIVACY_CHOICES))
        description.setPartAsClickableLink(color, dataUsageText, getLinkAction(dataUsageLink, TrackingData.UIElement.LEARN_MORE_ABOUT_SCHIBSTED))

        return description
    }

    private fun getLinkAction(link: String, uiElement: TrackingData.UIElement): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(widget: View) {
                navigationListener?.onWebViewNavigationRequested(WebFragment.newInstance(link, uiConf.redirectUri), LoginScreen.WEB_TC_SCREEN)
                BaseLoginActivity.tracker?.eventEngagement(TrackingData.Engagement.CLICK, uiElement, TrackingData.Screen.REQUIRED_FIELDS)
            }

            override fun updateDrawState(ds: TextPaint) {
            }
        }
    }

    private fun updateMissingFields() {
        BaseLoginActivity.tracker?.eventInteraction(TrackingData.InteractionType.SEND, TrackingData.Screen.REQUIRED_FIELDS)
        requiredFieldsPresenter.updateMissingFields(generatedFields)
    }

    private fun generateField(fieldName: String, titleRes: Int, validationRule: ValidationRule): FieldView {
        val generatedView: FieldView = when (fieldName) {
            RequiredFields.BIRTHDAY.fieldsValue -> BirthdayInputView(context)
            RequiredFields.PHONE_NUMBER.fieldsValue -> PhoneInputView(context).also {
                it.setTitle(titleRes)
            }
            else ->
                SingleFieldView.create(context!!) {
                    inputType { EditorInfo.TYPE_CLASS_TEXT }
                    validationRule { validationRule }
                    error { getString(R.string.schacc_required_fields_error) }
                    isCancelable { true }
                    title { getString(titleRes) }
                }
        }

        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 0, 0,
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        INFORMATION_FIELD_MARGIN_BOTTOM,
                        resources.displayMetrics).toInt())

        generatedView.layoutParams = params
        if (generatedView is SingleFieldView) {
            if (generatedView.inputField.hint.isNullOrBlank()) {
                generatedView.inputField.hint = getString(titleRes)
            }
        }
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
        fun newInstance(uiConfiguration: InternalUiConfiguration): RequiredFieldsFragment {
            val fragment = RequiredFieldsFragment()
            val args = Bundle()
            args.putParcelable(KEY_UI_CONF, uiConfiguration)
            fragment.arguments = args
            return fragment
        }

        const val INFORMATION_FIELD_MARGIN_BOTTOM = 28F
    }
}

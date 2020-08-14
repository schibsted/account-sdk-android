/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.information

import androidx.annotation.StringRes
import com.schibsted.account.ui.R
import com.schibsted.account.ui.ui.rule.BasicValidationRule
import com.schibsted.account.ui.ui.rule.BirthdayValidationRule
import com.schibsted.account.ui.ui.rule.EmailValidationRule
import com.schibsted.account.ui.ui.rule.MobileValidationRule
import com.schibsted.account.ui.ui.rule.NameValidationRule
import com.schibsted.account.ui.ui.rule.ValidationRule

enum class RequiredFields(val fieldsValue: String, @StringRes val titleRes: Int, val validationRule: ValidationRule) {
    GIVEN_NAME("name.given_name", R.string.schacc_required_field_given_name, NameValidationRule),
    FAMILY_NAME("name.family_name", R.string.schacc_required_field_family_name, NameValidationRule),
    PHONE_NUMBER("phone_number", R.string.schacc_required_field_phone_number, MobileValidationRule),
    GENDER("gender", R.string.schacc_required_field_gender, BasicValidationRule),
    BIRTHDAY("birthday", R.string.schacc_required_field_birthday, BirthdayValidationRule),
    HOME_ADDRESS("addresses.home", R.string.schacc_required_field_address_home, BasicValidationRule),
    DELIVERY_ADDRESS("addresses.delivery", R.string.schacc_required_field_address_delivery, BasicValidationRule),
    INVOICE_ADDRESS("addresses.invoice", R.string.schacc_required_field_address_invoice, BasicValidationRule),
    EMAIL("email", R.string.schacc_required_field_email, EmailValidationRule),
    DISPLAY_NAME("displayName", R.string.schacc_required_field_display_name, BasicValidationRule),
}

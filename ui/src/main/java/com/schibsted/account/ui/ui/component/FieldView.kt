/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui.ui.component

import android.content.Context
import android.util.AttributeSet
import com.schibsted.account.ui.ui.InputField
import com.schibsted.account.ui.ui.rule.ValidationRule

abstract class FieldView : SchibstedView, InputField {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    lateinit var validationRule: ValidationRule
}
/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import android.content.SharedPreferences
import kotlin.reflect.KProperty

class StorageDelegate<T>(
    private val prefs: SharedPreferences,
    private val getter: SharedPreferences.(String, T) -> T,
    private val setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor,
    val defaultValue: T,
    private val key: String? = null
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = prefs.getter(key
            ?: property.name, defaultValue)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = prefs.edit().setter(key
            ?: property.name, value).apply()
}

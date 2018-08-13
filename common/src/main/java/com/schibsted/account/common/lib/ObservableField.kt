/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.common.lib

import kotlin.properties.Delegates

open class ObservableField<T>(initialValue: T) {
    interface Observer<T> {
        fun onChange(newValue: T)
    }

    interface Single<T> : Observer<T>

    private val listeners = mutableSetOf<Observer<T>>()

    var value: T by Delegates.observable(initialValue,
            { _, oldValue, newValue ->
                if (newValue != oldValue) {
                    listeners.forEach { it.onChange(newValue) }
                    listeners.removeAll { it is Single }
                }
            })

    fun addListener(listener: Observer<T>, notifyInitially: Boolean = false) {
        listeners.add(listener)
        if (notifyInitially) {
            listener.onChange(value)
        }
    }

    fun addListener(single: Boolean = false, notifyInitially: Boolean = false, lambda: (T) -> Unit): Observer<T> {
        return if (single) {
            object : Single<T> {
                override fun onChange(newValue: T) {
                    lambda(newValue)
                }
            }
        } else {
            object : Observer<T> {
                override fun onChange(newValue: T) {
                    lambda(newValue)
                }
            }
        }.also { addListener(it, notifyInitially) }
    }

    fun removeListener(listener: Observer<T>) {
        listeners.remove(listener)
    }

    fun hasObservers() = listeners.isNotEmpty()
}

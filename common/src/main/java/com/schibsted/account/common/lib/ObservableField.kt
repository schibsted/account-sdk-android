/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.common.lib

import kotlin.properties.Delegates

/**
 * Allows to reactively observe value and its changes. Emitted values are guaranteed
 * to be distinct.
 */
class ObservableField<T>(initialValue: T) {

    /**
     * Interface for value observers.
     */
    interface Observer<T> {

        /**
         * This method is called when value changes. New value is guaranteed to be not equal
         * to the old value.
         */
        fun onChange(newValue: T)
    }

    /**
     * Interface for value observers that are supposed to be invoked only once after
     * a value change. Note, if an Single is added to ObservableField with flag
     * `notifyInitially = true`, it will be called immediately AND it will be called again
     * after a value change.
     */
    interface Single<T> : Observer<T>

    private val listeners = mutableSetOf<Observer<T>>()

    /**
     * Value to be observed.
     */
    var value: T by Delegates.observable(initialValue,
            { _, oldValue, newValue ->
                if (newValue != oldValue) {
                    listeners.forEach { it.onChange(newValue) }
                    listeners.removeAll { it is Single }
                }
            })

    /**
     * Adds an observer for the value. If `notifyInitially = true`, it will be called immediately
     * with the currently existing value.
     */
    fun addListener(listener: Observer<T>, notifyInitially: Boolean = false) {
        listeners.add(listener)
        if (notifyInitially) {
            listener.onChange(value)
        }
    }

    /**
     * Adds lambda as an observer for the value. If `notifyInitially = true`, it will be called
     * immediately with the currently existing value. If  `single = true`, the observer will be
     * automatically removed after the first change of the value.
     */
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

    /**
     * Removes an observer. Removed observers will not be notified any more.
     */
    fun removeListener(listener: Observer<T>) {
        listeners.remove(listener)
    }

    /**
     * Returns true if the observed value has active observers.
     */
    fun hasObservers() = listeners.isNotEmpty()
}

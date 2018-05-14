package com.schibsted.account.common.lib

import kotlin.properties.Delegates

class ObservableField<T>(initialValue: T) {
    interface ValueChangedListener<T> {
        fun onValueChange(newValue: T)
    }

    private val listeners = mutableSetOf<ValueChangedListener<T>>()

    var value: T by Delegates.observable(initialValue,
            { _, oldValue, newValue ->
                if (newValue != oldValue) {
                    listeners.forEach { it.onValueChange(newValue) }
                }
            })

    fun addListener(listener: ValueChangedListener<T>) {
        listeners.add(listener)
        listener.onValueChange(value)
    }

    fun addListener(lambda: (T) -> Unit): ValueChangedListener<T> {
        return object : ValueChangedListener<T> {
            override fun onValueChange(newValue: T) {
                lambda(newValue)
            }
        }.also { addListener(it) }
    }

    fun removeListener(listener: ValueChangedListener<T>) {
        listeners.remove(listener)
    }
}

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.common.lib

import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class ObservableFieldTest : StringSpec() {
    init {
        "Initial value should be respected" {
            val field = ObservableField(123)
            field.value shouldBe 123
        }

        "Observer should be notified when notifyInitially is true" {
            val field = ObservableField("Hello")
            val listener = observer()

            field.addListener(listener, notifyInitially = true)

            verify(listener).onChange(eq("Hello"))
        }

        "Single should be notified when notifyInitially is true" {
            val field = ObservableField("Hello")
            val listener = single()

            field.addListener(listener, notifyInitially = true)

            verify(listener).onChange(eq("Hello"))
        }

        "Observer should not be notified when notifyInitially is false" {
            val field = ObservableField("Hello")
            val listener = observer()

            field.addListener(listener, notifyInitially = false)

            verify(listener, never()).onChange(any())
        }

        "Single should not be notified when notifyInitially is false" {
            val field = ObservableField("Hello")
            val listener = single()

            field.addListener(listener, notifyInitially = false)

            verify(listener, never()).onChange(any())
        }

        "Observer should be notified on value change when notifyInitially is true" {
            val field = ObservableField("Hello")
            val listener = observer()

            field.addListener(listener, notifyInitially = true)
            verify(listener).onChange(eq("Hello"))

            field.value = "Hello again"
            verify(listener).onChange(eq("Hello again"))
        }

        "Single should be notified AGAIN on value change when notifyInitially is true" {
            val field = ObservableField("Hello")
            val listener = single()

            field.addListener(listener, notifyInitially = true)
            verify(listener).onChange(eq("Hello"))

            field.value = "Hello again"
            verify(listener).onChange(eq("Hello again"))
        }

        "Single should not be notified on the second value change" {
            val field = ObservableField("Hello")
            val listener = single()

            field.addListener(listener)

            field.value = "Hello again"
            verify(listener).onChange(eq("Hello again"))

            field.value = "Bye"
            verify(listener, never()).onChange(eq("Bye"))
        }

        "Listeners should be notified on value change" {
            val field = ObservableField("Hello")
            val listener = observer()

            field.addListener(listener)
            field.value = "Hello again"

            verify(listener).onChange(eq("Hello again"))
        }

        "Listeners should not be notified when the value is the same" {
            val field = ObservableField("Hello")
            val listener = observer()

            field.addListener(listener, notifyInitially = true)
            verify(listener).onChange(eq("Hello"))
            reset(listener)

            field.value = "Hello"
            verify(listener, never()).onChange(eq("Hello"))
            reset(listener)

            field.value = "Hello again"
            verify(listener).onChange(eq("Hello again"))
            reset(listener)

            field.value = "Hello again"
            verify(listener, never()).onChange(eq("Hello again"))
            reset(listener)
        }

        "Listener added multiple times should only be notified once" {
            val field = ObservableField("Hello")
            val listener = observer()

            field.addListener(listener)
            field.addListener(listener)

            field.value = "Bye"
            verify(listener).onChange(eq("Bye"))
        }

        "Listeners should no longer be notified after they have been removed" {
            val field = ObservableField("Hello")
            val listener = observer()

            field.addListener(listener)

            field.value = "Hello again"
            verify(listener).onChange(eq("Hello again"))

            field.removeListener(listener)

            field.value = "Bye"
            verify(listener, never()).onChange(eq("Bye"))
        }

        "Lambda should be identical to Observer when single = false" {
            val field = ObservableField("Hello")
            val listener = observer()

            field.addListener(single = false, notifyInitially = true) { listener.onChange(it) }
            verify(listener).onChange(eq("Hello"))

            field.value = "Hello again"
            verify(listener).onChange(eq("Hello again"))

            field.value = "Bye"
            verify(listener).onChange(eq("Bye"))
        }

        "Lambda should be identical to Single when single = true" {
            val field = ObservableField("Hello")
            val listener = single()

            field.addListener(single = true, notifyInitially = true) { listener.onChange(it) }
            verify(listener).onChange(eq("Hello"))

            field.value = "Hello again"
            verify(listener).onChange(eq("Hello again"))

            field.value = "Bye"
            verify(listener, never()).onChange(eq("Bye"))
        }

        "hasObservers() should return false when there are no active observers" {
            val field = ObservableField("Hello")
            field.hasObservers() shouldBe false
        }

        "hasObservers() should return true when there are active observers" {
            val field = ObservableField("Hello")
            val listener = observer()

            field.addListener(listener)

            field.hasObservers() shouldBe true
        }

        "hasObservers() should return false when all observers are removed" {
            val field = ObservableField("Hello")
            val listener = observer()

            field.addListener(listener)
            field.removeListener(listener)

            field.hasObservers() shouldBe false
        }

        "hasObservers() should return false when all Singles are auto-removed" {
            val field = ObservableField("Hello")
            val listener = single()

            field.addListener(listener)
            field.value = "Hello again"

            field.hasObservers() shouldBe false
        }

        "Observer that removes itself should be handled properly" {
            val field = ObservableField("Hello")

            val listener = object : ObservableField.Observer<String> {
                override fun onChange(newValue: String) =
                        field.removeListener(this) // remove itself
            }

            field.addListener(listener)
            field.value = "Hello again"

            field.hasObservers() shouldBe false
        }
    }

    private fun observer() = mock<ObservableField.Observer<String>>()
    private fun single() = mock<ObservableField.Single<String>>()
}

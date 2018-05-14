package com.schibsted.account.common.lib

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class ObservableFieldTest : StringSpec({
    "Initial value should be respected" {
        val field = ObservableField(123)
        field.value shouldBe 123
    }

    "Listeners should be notified when added" {
        val field = ObservableField("Hello")
        var cdl = 1 // Once on start
        field.addListener {
            cdl--
        }

        cdl shouldBe 0
    }

    "Listeners should be notified on value change" {
        val field = ObservableField("Hello")
        var cdl = 2 // Once on start, once on change
        field.addListener {
            cdl--
        }

        field.value = "Hello again"
        cdl shouldBe 0
    }

    "Listeners should not be notified when the value is the same" {
        val field = ObservableField("Hello")
        var cdl = 1 // Once on start
        field.addListener {
            cdl--
        }

        field.value = "Hello" // Should be ignored
        cdl shouldBe 0
    }

    "Listeners should only be notified once" {
        val field = ObservableField("Hello")
        var cdl = 3 // Once for the two adds, one for notifying

        val listener = object : ObservableField.ValueChangedListener<String> {
            override fun onValueChange(newValue: String) {
                cdl--
            }
        }

        field.addListener(listener)
        field.addListener(listener)

        field.value = "asdsad"
        cdl shouldBe 0
    }

    "Listeners should no longer be notified after they have been removed" {
        val field = ObservableField("Hello")
        var cdl = 2 // Once on start
        val listener = field.addListener {
            cdl--
        }
        field.value = "Yo!" // Should be ignored
        field.removeListener(listener)
        field.value = "Yo yo!"
        cdl shouldBe 0
    }
})

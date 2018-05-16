/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.common.lib

import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.fail
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class ObservableFieldTest : StringSpec({
    "Initial value should be respected" {
        val field = ObservableField(123)
        field.value shouldBe 123
    }

    "notify initially should be respected" {
        val field = ObservableField("Hello")
        var cdl = 1 // Once on start
        field.addListener(notifyInitially = true) {
            cdl--
        }
        cdl shouldBe 0

        field.addListener {
            fail("Field was notified initially when not set")
        }
    }

    "Listeners should be notified on value change" {
        val field = ObservableField("Hello")
        var cdl = 1
        field.addListener {
            cdl--
        }

        field.value = "Hello again"
        cdl shouldBe 0
    }

    "Listeners should not be notified when the value is the same" {
        val field = ObservableField("Hello")
        field.addListener {
            fail("Observer was notified")
        }

        field.value = "Hello" // Should be ignored
    }

    "Listeners should only be notified once" {
        val field = ObservableField("Hello")
        var cdl = 1

        val listener = object : ObservableField.Observer<String> {
            override fun onChange(newValue: String) {
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
        var cdl = 1
        val listener = field.addListener {
            cdl--
        }
        field.value = "Yo!" // Should be ignored
        field.removeListener(listener)
        field.value = "Yo yo!"
        cdl shouldBe 0
    }

    "Single observers should only be notified once" {
        var cdl = 1
        val listener = object : ObservableField.Single<String> {
            override fun onChange(newValue: String) {
                cdl--
            }
        }

        val field = ObservableField("aaaa")
        field.addListener(listener)

        field.value = "bbb"
        field.value = "ccc"

        cdl shouldBe 0
    }

    "Lambda for single should work" {
        val field = ObservableField("aaaa")
        field.addListener(single = true) {} should beInstanceOf(ObservableField.Single::class)
    }

    "Single observers should be notified on initialization as well if specified" {
        var cdl = 2 // Once initially, once for change
        val field = ObservableField("aaaa")
        field.addListener(single = true, notifyInitially = true) {
            cdl--
        }

        field.value = "bbb"

        cdl shouldBe 0
    }
})

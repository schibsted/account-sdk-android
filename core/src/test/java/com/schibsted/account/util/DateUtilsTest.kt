package com.schibsted.account.util

import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import java.util.Date

class DateUtilsTest : WordSpec({

    "parsing a string " should {
        "return a date if the format is valid" {
            DateUtils.fromString("2018-07-11T17:12:37") shouldBe beInstanceOf(Date::class)
        }

        "return null if the format is not valid" {
            DateUtils.fromString("") shouldBe null
            DateUtils.fromString("14 juli 2018 10:18:54") shouldBe null
            DateUtils.fromString("14 jul T17:12:37 AM") shouldBe null
            DateUtils.fromString("23987fhas") shouldBe null
        }
    }
})
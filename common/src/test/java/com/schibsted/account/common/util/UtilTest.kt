/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.common.util

import io.kotlintest.matchers.haveSubstring
import io.kotlintest.shouldNot
import io.kotlintest.specs.StringSpec

class UtilTest : StringSpec({
    val url = "http://www.example.com/path?query=123".safeUrl()
    url shouldNot haveSubstring("123")
})

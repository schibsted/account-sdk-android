/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.model

import com.google.gson.Gson
import com.schibsted.account.test.TestUtil
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class UserIdTest : StringSpec({
    val token = Gson().fromJson(TestUtil.readResource("json/user_token.json"), UserToken::class.java)

    "extractFields should correctly decode a payload" {
        val fields = UserId.extractFields(token.idToken!!)!!
        fields["sub"] shouldBe "e0616270-2092-5e9d-856b-48e065d4899f"
        fields["legacy_user_id"] shouldBe "11099464"
    }

    "fromTokenResponse should correctly decode a token response" {
        val userid = UserId.fromTokenResponse(token)
        userid.id shouldBe "e0616270-2092-5e9d-856b-48e065d4899f"
        userid.legacyId shouldBe "11099464"
    }
})

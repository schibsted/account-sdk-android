/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.model

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.schibsted.account.test.TestUtil
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.StringSpec

class UserIdTest : StringSpec({
    val token = Gson().fromJson(TestUtil.readResource("json/user_token.json"), UserToken::class.java)

    "fromTokenResponse should correctly decode a token response" {
        val userid = UserId.fromTokenResponse(token)
        userid.id shouldBe "e0616270-2092-5e9d-856b-48e065d4899f"
        userid.legacyId shouldBe "11099464"
    }

    "extractPayload should correctly Base64 decode a Base64 string" {
        val res = UserId.extractPayload("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImM3ZjYwMzY5LTgwMzItNDUxYS04NzFkLWI0OGQzMDRhMmIzNSJ9.eyJpc3MiOiJodHRwczpcL1wvaWRlbnRpdHktcHJlLnNjaGlic3RlZC5jb21cLyIsImNsYXNzIjoidG9rZW4uSURUb2tlbiIsImV4cCI6MTUzMjUxOTU2NywiaWF0IjoxNTI5OTI3NTY3LCJzdWIiOiJlMDYxNjI3MC0yMDkyLTVlOWQtODU2Yi00OGUwNjVkNDg5OWYiLCJhdWQiOlsiNThjZmY5OGYxN2U1OTY4NjE1OGI0NTY3IiwiaHR0cHM6XC9cL3NlbGZzZXJ2aWNlLmlkZW50aXR5LXByZS5zY2hpYnN0ZWQuY29tXC8iXSwiYXpwIjoiNThjZmY5OGYxN2U1OTY4NjE1OGI0NTY3IiwibGVnYWN5X3VzZXJfaWQiOiIxMTA5OTQ2NCJ9.ZzoDH-WQJAsuxZlVWX25nk9zAKc-9N0LGb4rYDM40OjjXQllgyN4NJymMBFCIlUVsJ7Nj4xwEhGLkjxETkiUR1CCuWe6k3LFVX-B3yRUd2HuoNBGzWdWumFlohqYh7gVkDVrRS4MhnF3Ogwcgvb-Zc6CR0i1vL0sIJhheaqYLsiVC7-RFoLUpLEjsvSaDfXZLHZwfU-UAPP3V_tfm5juFGNTTTBaiJ3sfat-MURVLzeGaZdPPCael_LSuVFAIIJWvgv_Oxkg1zjYBuTLS8WaS3WjWc-beml-e0HGYjTSDz1ia3-OK0I0hvDFONTqXhqO3ANUrLblYHF1i3adnDV9RQ")
        val parsed = JsonParser().parse(res)
        parsed.isJsonObject shouldBe true
        parsed.asJsonObject["sub"].asString shouldNotBe null
        parsed.asJsonObject["legacy_user_id"].asString shouldNotBe null
    }

    "extractFields should extract fields from json payload" {
        val json = """{"iss":"https:\/\/identity-pre.schibsted.com\/","class":"token.IDToken","exp":1532519567,"iat":1529927567,"sub":"e0616270-2092-5e9d-856b-48e065d4899f","aud":["58cff98f17e59686158b4567","https:\/\/selfservice.identity-pre.schibsted.com\/"],"azp":"58cff98f17e59686158b4567","legacy_user_id":"11099464"}"""
        val res = UserId.extractFields(json)!!
        res.first shouldEqual "e0616270-2092-5e9d-856b-48e065d4899f"
        res.second shouldEqual "11099464"
    }

    "extractFields should only return fields from json payload if they match the type" {
        val json = """{"iss":"https:\/\/identity-pre.schibsted.com\/","class":"token.IDToken","exp":1532519567,"iat":1529927567,"sub":false,"aud":["58cff98f17e59686158b4567","https:\/\/selfservice.identity-pre.schibsted.com\/"],"azp":"58cff98f17e59686158b4567","legacy_user_id":123}"""
        val res = UserId.extractFields(json)!!
        res.first shouldBe null
        res.second shouldBe null
    }
})

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.util

import com.google.gson.GsonBuilder
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

private data class Person(val name: String?, val age: Int)

class TypeSafeStringDeserializerTest : StringSpec({
    "Should not leniently deserialize strings" {
        val gson = GsonBuilder().registerTypeAdapter(String::class.java, TypeSafeStringDeserializer()).create()

        val json1 = """{
            |"name": false,
            |"age": 17
            |}""".trimMargin()

        val json2 = """{
            |"name": 123,
            |"age": 17
            |}""".trimMargin()

        val json3 = """{
            |"name": "Ola Nordmann",
            |"age": 17
            |}""".trimMargin()

        val res1 = gson.fromJson(json1, Person::class.java)
        val res2 = gson.fromJson(json2, Person::class.java)
        val res3 = gson.fromJson(json3, Person::class.java)

        res1.name shouldBe null
        res2.name shouldBe null
        res3.name shouldBe "Ola Nordmann"
    }
})

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.response

import android.location.Address
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.schibsted.account.test.TestUtil
import com.schibsted.account.util.LenientAccountsDeserializer
import io.kotlintest.matchers.beGreaterThan
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

class ProfileDataTest : WordSpec({
    val GSON = GsonBuilder().registerTypeAdapter(LenientAccountsDeserializer.type, LenientAccountsDeserializer()).create()

    "Parsing a JSON string of profile data" should {
        "not crash when parsing the invalid SPiD format" {
            val json = TestUtil.readResource("json/profile_data_invalid.json")
            val typeToken = object : TypeToken<ApiContainer<ProfileData>>() {}.type
            val data = GSON.fromJson<ApiContainer<ProfileData>>(json, typeToken).data

            data.email shouldBe "havard.kindem@schibsted.com"
            data.id shouldBe "588f0d7b9446df411bdde132"
            data.name!!.formatted!!.length should beGreaterThan(5)
            data.addresses shouldBe emptyMap<String, Address>()

            val accountPair = data.accounts!!.toList().first()
            accountPair.first shouldBe accountPair.second.id
        }

        "parse the format correctly" {
            val json = TestUtil.readResource("json/profile_data.json")
            val typeToken = object : TypeToken<ApiContainer<ProfileData>>() {}.type
            val data = GSON.fromJson<ApiContainer<ProfileData>>(json, typeToken).data

            data.email shouldBe "havard.kindem@schibsted.com"
            data.id shouldBe "588f0d7b9446df411bdde132"
            data.name!!.formatted!!.length should beGreaterThan(5)
            data.addresses!!["home"]!!.type shouldBe ProfileData.Address.AddressType.HOME

            val accountPair = data.accounts!!.toList().first()
            accountPair.first shouldBe accountPair.second.id
        }
    }

    "Generating JSON from ProfileData" should {
        "generate an empty object if no fields are set" {
            val jsonRes = GSON.toJson(ProfileData())
            jsonRes shouldBe "{}"
        }

        "only generate JSON for non-null fields" {
            val jsonRes = GSON.toJson(ProfileData(gender = "male", name = ProfileData.Name(familyName = "Bond")))
            jsonRes shouldBe "{\"gender\":\"male\",\"name\":{\"family_name\":\"Bond\"}}"
        }
    }
})

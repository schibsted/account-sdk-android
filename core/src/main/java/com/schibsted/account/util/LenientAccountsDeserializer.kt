package com.schibsted.account.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.schibsted.account.network.response.ProfileData
import java.lang.reflect.Type

class LenientAccountsDeserializer : JsonDeserializer<Map<String, ProfileData.Address>> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Map<String, ProfileData.Address>? {
        return json.takeIf { it.isJsonObject }
                ?.let { context.deserialize<Map<String, ProfileData.Address>>(json, type) }
    }

    companion object {
        @JvmField
        val type: Type = object : TypeToken<Map<String, ProfileData.Address>>() {}.type
    }
}

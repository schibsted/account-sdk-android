/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.schibsted.account.ResponseContainer
import java.lang.reflect.Type

class CustomDeserializer<T> : JsonDeserializer<ResponseContainer<T>> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ResponseContainer<T> {
        return elements(json)
    }

    private fun elements(json: JsonElement?): ResponseContainer<T> {
        val elements = mutableListOf<T>()
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
        val data = (json as JsonObject).get("data") as? JsonObject
        data?.entrySet()?.forEach { elements.add(gson.fromJson<T>(it.value, type<T>())) }
        return ResponseContainer(elements)
    }

    companion object {
        private fun <T> type(): Type? = object : TypeToken<T>() {}.type
    }
}
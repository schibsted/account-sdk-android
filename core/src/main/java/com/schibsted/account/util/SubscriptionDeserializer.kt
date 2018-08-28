package com.schibsted.account.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.schibsted.account.network.response.Subscription
import com.schibsted.account.network.response.SubscriptionsResponse
import java.lang.reflect.Type

class SubscriptionDeserializer : JsonDeserializer<SubscriptionsResponse> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SubscriptionsResponse {
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
        val data = (json as JsonObject).get("data") as? JsonObject
        val subscriptions = mutableListOf<Subscription>()

        data?.let {
            for (item in data.entrySet()) {
                subscriptions.add(gson.fromJson<Subscription>(item.value, Subscription::class.java))
            }
        }
        return SubscriptionsResponse(subscriptions)
    }

    companion object {
        @JvmField
        val type: Type = object : TypeToken<SubscriptionsResponse>() {}.type
    }
}
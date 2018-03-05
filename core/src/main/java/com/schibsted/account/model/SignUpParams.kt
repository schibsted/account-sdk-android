/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.model

import com.schibsted.account.network.response.ProfileData

data class SignUpParams(
        val password: String? = null,
        val redirectUri: String? = null,
        val displayName: String? = null,
        val name: ProfileData.Name? = null,
        val birthday: String? = null,
        val addresses: Map<ProfileData.Address.AddressType, ProfileData.Address>? = null,
        val gender: String? = null,
        val acceptTerms: Boolean? = null) {

    fun getParams(): Map<String, Any> {
        val m = mutableMapOf<String, Any>()
        password?.let { m.put("password", it) }
        redirectUri?.let { m.put("redirectUri", it) }
        displayName?.let { m.put("displayName", it) }
        name?.let { m.put("name", it) }
        birthday?.let { m.put("birthday", it) }
        addresses?.let { m.put("addresses", it) }
        gender?.let { m.put("gender", it) }
        acceptTerms?.let { m.put("acceptTerms", it) }
        return m
    }
}

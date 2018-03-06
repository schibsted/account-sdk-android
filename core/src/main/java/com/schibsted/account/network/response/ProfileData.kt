/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.response

import com.google.gson.annotations.SerializedName

data class ProfileData(
        val id: String? = null,
        val userId: String? = null,
        val uuid: String? = null,
        val status: Int? = null,
        val email: String? = null,
        val emails: List<Email>? = null,
        val displayName: String? = null,
        val preferredUsername: String? = null,
        val gender: String? = null,
        val utcOffset: String? = null,
        val birthday: String? = null,
        val photo: String? = null,
        val url: String? = null,
        val phoneNumber: String? = null,
        val name: Name? = null,
        val accounts: Map<String, Account>? = null,
        val addresses: List<Address>? = null,
        val currentLocation: List<Address>? = null,
        val phoneNumbers: List<PhoneNumber>? = null,
        val emailVerified: String? = null,
        val phoneNumberVerified: Boolean? = null,
        val published: String? = null,
        val updated: String? = null,
        val lastAuthenticated: String? = null,
        val passwordChanged: String? = null,
        val lastLoggedIn: String? = null,
        val locale: String? = null,
        val merchants: List<Int>? = null,
        val verified: String? = null) {

    data class Email(
            val value: String? = null,
            val type: String? = null,
            val primary: String? = null,
            val verified: String? = null,
            val verifiedTime: String? = null)

    // TODO: is family_name or familyName the correct thing to use? Signup requires family_name at least
    data class Name(@SerializedName("family_name", alternate = ["familyName"]) val familyName: String? = null,
            @SerializedName("given_name", alternate = ["givenName"]) val givenName: String? = null,
            val formatted: String? = null)

    data class Account(val id: String? = null,
            val accountName: String? = null,
            val domain: String? = null,
            val connected: String? = null)

    data class Address(val formatted: String? = null,
            val streetAddress: String? = null,
            val postalCode: String? = null,
            val country: String? = null,
            val locality: String? = null,
            val region: String? = null,
            val latitude: String? = null,
            val longitude: String? = null,
            val altitude: String? = null,
            val type: AddressType? = null) {

        enum class AddressType {
            HOME, DELIVERY, INVOICE;

            override fun toString(): String = super.toString().toLowerCase()
        }
    }

    data class PhoneNumber(val value: String? = null,
            val type: String? = null,
            val primary: Boolean? = null,
            val verified: Boolean? = null,
            val verifiedTime: String? = null)
}

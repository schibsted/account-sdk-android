/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

private const val FIELD_NAMES = "names"
private const val GIVEN_NAME = "name.given_name"
private const val FAMILY_NAME = "name.family_name"

@Parcelize
data class ClientInfo(
    val id: String,
    val name: String,
    val alias: String,
    val fields: Map<String, Boolean>,
    val domain: String,
    val merchantId: Int,
    val css: Map<String, @RawValue Any>,
    val termsCss: Map<String, @RawValue Any>,
    val merchant: Merchant
) : Parcelable {

    @Deprecated("Provide the proper JSON object instead")
    fun requiredFields(): Set<String> {
        val fields: MutableSet<String> = fields.filter { it.value }.keys.toMutableSet()
        if (fields.contains(FIELD_NAMES)) {
            fields.remove(FIELD_NAMES)
            fields.add(GIVEN_NAME)
            fields.add(FAMILY_NAME)
        }
        return fields
    }
}

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.response

import com.google.gson.annotations.SerializedName

data class RequiredFieldsResponse(@SerializedName("requiredFields") val fields: Set<String>)

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.network.service.session

import com.schibsted.account.network.response.TokenExchangeResponse
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface SessionContract {
    /**
     * @param params The parameters to send along the request.
     * @return A representation of the request ready for execution.
     */
    @FormUrlEncoded
    @POST("api/2/oauth/exchange")
    fun exchange(@Header("Authorization") bearerAny: String, @FieldMap params: Map<String, String>): Call<TokenExchangeResponse>
}

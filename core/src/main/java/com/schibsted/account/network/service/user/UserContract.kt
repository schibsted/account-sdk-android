/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.service.user

import com.schibsted.account.network.response.AcceptAgreementResponse
import com.schibsted.account.network.response.AgreementsResponse
import com.schibsted.account.network.response.ApiContainer
import com.schibsted.account.network.response.ProfileData
import com.schibsted.account.network.response.RequiredFieldsResponse
import com.schibsted.account.network.response.SubscriptionsResponse
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Describes profile interactions with the SPiD backend.
 */
internal interface UserContract {

    @GET("api/2/logout")
    fun logout(@Header(KEY_AUTHORIZATION) userBearer: String): Call<Unit>

    @POST("api/2/user/{userId}/agreements/accept")
    fun agreementAccept(@Header(KEY_AUTHORIZATION) userBearer: String, @Path(KEY_USER_ID) userId: String): Call<ApiContainer<AcceptAgreementResponse>>

    @GET("api/2/user/{userId}/agreements")
    fun agreements(@Header(KEY_AUTHORIZATION) userBearer: String, @Path(KEY_USER_ID) userId: String): Call<ApiContainer<AgreementsResponse>>

    @GET("api/2/user/{userId}/required_fields")
    fun requiredFields(@Header(KEY_AUTHORIZATION) userBearer: String, @Path(KEY_USER_ID) userId: String): Call<ApiContainer<RequiredFieldsResponse>>

    @FormUrlEncoded
    @POST("api/2/user/{userId}")
    fun updateUserProfile(@Header(KEY_AUTHORIZATION) userBearer: String, @Path(KEY_USER_ID) userId: String, @FieldMap profile: Map<String, @JvmSuppressWildcards Any>): Call<Unit>

    @GET("api/2/user/{userId}")
    fun getUserProfile(@Header(KEY_AUTHORIZATION) userBearer: String, @Path(KEY_USER_ID) userId: String): Call<ApiContainer<ProfileData>>

    @GET("api/2/subscriptions")
    fun subscriptions(@Header(KEY_AUTHORIZATION) userBearer: String): Call<SubscriptionsResponse>

    companion object {
        const val KEY_AUTHORIZATION = "Authorization"
        const val KEY_USER_ID = "userId"
    }
}

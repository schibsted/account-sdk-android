/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.service.client

import com.schibsted.account.network.response.AccountStatusResponse
import com.schibsted.account.network.response.AgreementLinksResponse
import com.schibsted.account.network.response.ApiContainer
import com.schibsted.account.network.response.ClientInfo
import com.schibsted.account.network.response.ProfileData
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ClientContract {
    /**
     * Creates a user and associates it to an e-mail identifier (should it not be associated to any
     * yet) and requests the backend to send a confirmation e-mail to it.
     *
     * @param params The parameters to send along the request.
     * @return A representation of the request ready for execution.
     */
    @FormUrlEncoded
    @POST("api/2/signup")
    fun signUp(@Header("Authorization") clientBearer: String, @FieldMap params: Map<String, @JvmSuppressWildcards Any>): Call<ApiContainer<ProfileData>>

    /**
     * Checks the signup status of the given phone identifier.
     * @param clientToken Client token.
     * @param identifier The identifier whose status is to be queried.
     * @return A representation of the call ready for execution.
     * @see [
     * GET /phone/{phone}/status | SPiD API Documentation](http://techdocs.spid.no/endpoints/GET/phone/%7Bphone%7D/status/)
     */
    @GET("api/2/phone/{phone}/status")
    fun checkPhoneStatus(@Header("Authorization") clientBearer: String, @Path("phone") phoneBase64: String): Call<ApiContainer<AccountStatusResponse>>

    /**
     * Checks the signup status of the given phone identifier.
     * @param clientToken Client token.
     * @param identifier The identifier whose status is to be queried.
     * @return A representation of the call ready for execution.
     * @see [
     * GET /phone/{phone}/status | SPiD API Documentation](http://techdocs.spid.no/endpoints/GET/phone/%7Bphone%7D/status/)
     */
    @GET("api/2/email/{email}/status")
    fun checkEmailStatus(
        @Header("Authorization") clientBearer: String,
        @Path("email") emailBase64: String): Call<ApiContainer<AccountStatusResponse>>

    @GET("api/2/terms")
    fun retrieveTermsLinks(@Query("client_id") clientId: String): Call<ApiContainer<AgreementLinksResponse>>

    @GET("api/2/client/{clientid}")
    fun getClientInformation(@Header("Authorization") clientBearer: String, @Path("clientid") clientId: String): Call<ApiContainer<ClientInfo>>
}

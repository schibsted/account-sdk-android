/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.service.authentication;

import android.support.annotation.NonNull;

import com.schibsted.account.network.response.ClientTokenResponse;
import com.schibsted.account.network.response.UserTokenResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Describes OAuth interactions with the Schibsted account backend.
 */
interface OAuthContract {
    /**
     * Requests to a resource owner.
     *
     * @param params The parameters to send along the request.
     * @return A representation of the request ready for execution.
     */
    @FormUrlEncoded
    @POST("oauth/ro")
    Call<UserTokenResponse> resourceOwner(@Header("Authorization") String basicClient, @FieldMap @NonNull Map<String, String> params);

    /**
     * Retrieves user token. This overload is meant to be used when authentication is required.
     *
     * @param params The parameters to send along the request.
     * @return A representation of the request ready for execution.
     */
    @FormUrlEncoded
    @POST("oauth/token")
    Call<UserTokenResponse> token(@Header("Authorization") String basicClient, @NonNull @FieldMap Map<String, String> params);


    /**
     * Retrieves client token.
     */
    @FormUrlEncoded
    @POST("oauth/token")
    Call<ClientTokenResponse> client(@Header("Authorization") String basicClient, @NonNull @FieldMap Map<String, String> params);
}

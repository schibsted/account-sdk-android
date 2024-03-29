/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.service.authentication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.schibsted.account.network.response.TokenResponse;

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
    Call<TokenResponse> resourceOwner(@Header("Authorization") String basicClient, @FieldMap @NonNull Map<String, String> params);

    /**
     * Requests to a token. This overload is meant to be used when authentication is required.
     *
     * @param params The parameters to send along the request.
     * @return A representation of the request ready for execution.
     */
    @FormUrlEncoded
    @POST("oauth/token")
    Call<TokenResponse> token(@Header("Authorization") String basicClient, @NonNull @FieldMap Map<String, String> params);
}

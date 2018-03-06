/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.service.passwordless;

import android.support.annotation.NonNull;

import com.schibsted.account.network.response.PasswordlessToken;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Describes passwordless authentication interactions with the SPiD backend.
 */
interface PasswordlessContract {
    /**
     * Requests a code to be sent to an identifier.
     *
     * @param params The parameters to send along the request.
     * @return A representation of the request ready for execution.
     */
    @FormUrlEncoded
    @POST("passwordless/start")
    Call<PasswordlessToken> requestCode(@FieldMap @NonNull Map<String, String> params);

    /**
     * Requests a code that had already been sent to an identifier to be resent.
     *
     * @param params The parameters to send along the request.
     * @return A representation of the request ready for execution.
     */
    @FormUrlEncoded
    @POST("passwordless/resend")
    Call<PasswordlessToken> resendCode(@FieldMap @NonNull Map<String, String> params);
}

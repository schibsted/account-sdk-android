/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.network.service;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.schibsted.account.network.Environment;
import com.schibsted.account.util.Preconditions;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseNetworkService {


    protected static final String PARAM_PASSWORDLESS_TOKEN = "passwordless_token";
    protected static final String PARAM_REDIRECT_URI_NO_UNDERSCORE = "redirectUri";
    @RestrictTo(RestrictTo.Scope.TESTS)
    @VisibleForTesting
    protected static final String PARAM_CLIENT_ID = "client_id";

    @RestrictTo(RestrictTo.Scope.TESTS)
    @VisibleForTesting
    protected static final String PARAM_CLIENT_SECRET = "client_secret";
    @RestrictTo(RestrictTo.Scope.TESTS)
    @VisibleForTesting
    protected static final String PARAM_PASSWORD = "password";
    private final String environment;
    private OkHttpClient okHttpClient;

    protected BaseNetworkService(@Environment String environment, @NonNull OkHttpClient okHttpClient) {
        this.environment = environment;
        this.okHttpClient = okHttpClient;
    }

    /**
     * Creates a service to perform network interactions against.
     *
     * @param service The class describing the service.
     * @return A service to perform network interactions against.
     */
    protected <T> T createService(@NonNull final Class<T> service) {
        Preconditions.checkNotNull(service);
        final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        return new Retrofit.Builder()
                .client(this.okHttpClient)
                .baseUrl(this.environment)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(service);
    }
}

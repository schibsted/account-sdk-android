/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.service;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.schibsted.account.ListContainer;
import com.schibsted.account.model.Product;
import com.schibsted.account.network.Environment;
import com.schibsted.account.network.response.Subscription;
import com.schibsted.account.util.LenientAccountsDeserializer;
import com.schibsted.account.util.ListDeserializer;
import com.schibsted.account.util.Preconditions;
import com.schibsted.account.util.TypeSafeStringDeserializer;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseNetworkService {

    protected static final String PARAM_PASSWORDLESS_TOKEN = "passwordless_token";
    protected static final String PARAM_REDIRECT_URI_NO_UNDERSCORE = "redirectUri";
    private final String environment;
    private final OkHttpClient okHttpClient;

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
        final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd")
                .registerTypeAdapter(LenientAccountsDeserializer.type, new LenientAccountsDeserializer())
                .registerTypeAdapter(new TypeToken<ListContainer<Subscription>>() {}.getType(), new ListDeserializer<Subscription>())
                .registerTypeAdapter(new TypeToken<ListContainer<Product>>() {}.getType(), new ListDeserializer<Product>())
                .registerTypeAdapter(String.class, new TypeSafeStringDeserializer())
                .create();
        return new Retrofit.Builder()
                .client(this.okHttpClient)
                .baseUrl(this.environment)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(service);
    }
}

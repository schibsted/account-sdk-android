/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.network.response;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

// TODO Remove OIDC-specific getters from here, the client should not need them

/**
 * Models a <a href="https://jwt.io/">JSON web token</a>.
 */
public class TokenExchangeResponse {

    @SerializedName("data")
    private JsonObject data;

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        final TokenExchangeResponse that = (TokenExchangeResponse) object;
        return this.data.equals(that.data);
    }

    @Override
    public String toString() {
        return "TokenExchangeResponse{" +
                "data='" + data + '\'' +
                '}';
    }

    /**
     * Provides the code.
     *
     * @return The code.
     */
    public String getCode() {
        return data.get("code").getAsString();
    }
}

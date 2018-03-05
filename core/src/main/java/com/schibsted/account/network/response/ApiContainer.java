/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.network.response;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the relevant parts of the container in which some API responses come.
 *
 * @param <T> The type of the payload in the response.
 */
public class ApiContainer<T> {
    @SerializedName("data")
    private T data;

    /**
     * Constructor.
     */
    public ApiContainer() {
        // GSON will override this assignment, but we need to have it because data is final
    }

    /**
     * Gets the payload.
     *
     * @return The response payload.
     */
    public T getData() {
        return this.data;
    }
}

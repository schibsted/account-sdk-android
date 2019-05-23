/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.response;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the signup status for an identifier.
 *
 * @see <a href="https://techdocs.spid.no/endpoints/GET/phone/%7Bphone%7D/status/">
 * GET /phone/{phone}/status | Schibsted account API Documentation</a>
 * @see <a href="https://techdocs.spid.no/endpoints/GET/email/%7Bemail%7D/status/">
 * GET /email/{email}/status | Schibsted account API Documentation</a>
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class AccountStatusResponse {
    @SerializedName("exists")
    private boolean exists;
    @SerializedName("available")
    private boolean available;
    @SerializedName("verified")
    private boolean verified;

    /**
     * Constructor.
     */
    private AccountStatusResponse() {
        // For GSON
    }

    /**
     * States whether the identifier exists or not.
     *
     * @return <code>true</code> if the queried identifier exists, <code>false</code> if it does
     * not.
     */
    public boolean exists() {
        return exists;
    }

    /**
     * States whether the identifier is available or not.
     *
     * @return <code>true</code> if the queried identifier is available, <code>false</code> if it is
     * not.
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * States whether the identifier is verified or not.
     *
     * @return <code>true</code> if the queried identifier is verified, <code>false</code> if it is
     * not.
     */
    public boolean isVerified() {
        return verified;
    }
}

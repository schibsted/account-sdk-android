/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.util;

import android.util.Patterns;

import java.util.Locale;

/**
 * Checks for conditions that must be met at certain points in the sdk.
 */
public final class Preconditions {
    /**
     * Constructor.
     */
    private Preconditions() {
        throw new IllegalAccessError("No instances.");
    }

    /**
     * Checks that none of the given values is <code>null</code>.
     */
    public static void checkNotNull(final Object... params) {
        // TODO Add some way of knowing here if we're debugging or not and just log at error level
        // if not debugging
        for (int index = 0; index < params.length; index++) {
            if (params[index] == null) {
                throw new IllegalArgumentException(String.format(Locale.ENGLISH,
                        "Parameter %d must be non-null.", index));
            }
        }
    }

    /**
     * Verifies that the given candidate is a well-formed e-mail address.
     *
     * @param candidate The candidate to perform the verification on.
     * @throws IllegalArgumentException If the candidate is not a well-formed e-mail address.
     */
    public static void verifyIsEmailAddress(final CharSequence candidate) {
        if (!Patterns.EMAIL_ADDRESS.matcher(candidate).matches()) {
            throw new IllegalArgumentException(String.format(Locale.ENGLISH,
                    "%s is not a well-formed e-mail address.", candidate));
        }
    }
}

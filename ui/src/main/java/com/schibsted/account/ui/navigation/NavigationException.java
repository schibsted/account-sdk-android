/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.navigation;

/**
 * an {@link RuntimeException} occurring when a navigation is not set up for a screen
 *
 */
public class NavigationException extends RuntimeException {

    /**
     * The constructor of the exception
     *
     * @param message the error message to log
     */
    public NavigationException(String message) {
        super(message);
    }
}

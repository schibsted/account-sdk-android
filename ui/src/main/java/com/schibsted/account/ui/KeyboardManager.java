/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui;

/**
 * This interface should be implemented by every activity managing the soft keyboard state
 */
public interface KeyboardManager {

    /**
     * call this method to know if the keyboard is opened or not
     *
     * @return <code>true</code> if the keyboard is currently opened
     * <code>false</code> otherwise
     */
    boolean isKeyboardOpen();

    /**
     * call this method to close down the soft keyboard
     */
    void closeKeyboard();
}

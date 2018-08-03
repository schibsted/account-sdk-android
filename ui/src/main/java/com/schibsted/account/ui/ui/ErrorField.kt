/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui

import android.support.annotation.StringRes

interface ErrorField {

    /**
     * Checks if an error view is currently displayed
     *
     * @return `true` if an error view is displayed `false` otherwise
     * @see .showErrorView
     */
    var isErrorVisible: Boolean

    /**
     * Displays an error message related to the input field
     *
     * @see .showErrorView
     */
    fun showErrorView()

    /**
     * Hides the error message previously displayed
     *
     * @see .hideErrorView
     */
    fun hideErrorView()

    fun setError(@StringRes message: Int)

    fun setError(message: String)
}

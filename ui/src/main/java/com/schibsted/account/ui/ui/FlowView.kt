/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui

import com.schibsted.account.ui.contract.BaseView

interface FlowView<in T> : BaseView<T> {
    /**
     * allow the user to click on the [.primaryActionView]
     */
    fun hideProgress()

    /**
     * prevent the user to click on the [.primaryActionView]
     */
    fun showProgress()

    /**
     * Shows a contextual error related to the user's actions
     */
    fun showError(errorField: ErrorField) {
        if (!errorField.isErrorVisible) {
            errorField.showErrorView()
        }
    }

    /**
     * Hides a previously shown error
     *
     * @see .showError
     */
    fun hideError(errorField: ErrorField) {
        if (errorField.isErrorVisible) {
            errorField.hideErrorView()
        }
    }
}

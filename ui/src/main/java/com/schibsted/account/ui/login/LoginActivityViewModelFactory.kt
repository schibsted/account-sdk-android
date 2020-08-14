/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.schibsted.account.ui.AccountUi
import com.schibsted.account.ui.InternalUiConfiguration
import com.schibsted.account.ui.smartlock.SmartlockTask

class LoginActivityViewModelFactory(
    private val smartlockTask: SmartlockTask,
    private val uiConfiguration: InternalUiConfiguration,
    private val params: AccountUi.Params
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
                smartlockTask::class.java,
                uiConfiguration::class.java,
                params::class.java).newInstance(smartlockTask, uiConfiguration, params)
    }
}

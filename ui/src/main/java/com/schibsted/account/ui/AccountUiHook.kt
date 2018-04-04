package com.schibsted.account.ui

import com.schibsted.account.session.User

interface AccountUiHook {
    class OnProceedListener(private val block: () -> Unit) {
        fun proceed() {
            block()
        }
    }

    fun onLoginCompleted(user: User, onProceedListener: OnProceedListener) {
        onProceedListener.proceed()
    }

    fun onLoginAborted(onProceedListener: OnProceedListener) {
        onProceedListener.proceed()
    }

    companion object DEFAULT : AccountUiHook
}

package com.schibsted.account.ui

import com.schibsted.account.session.User

interface AccountUiHook {
    class OnProceedListener(private val block: () -> Unit) {
        fun proceed() {
            block()
        }
    }

    fun onUiClosing(user: User, onProceedListener: OnProceedListener) {
        onProceedListener.proceed()
    }

    companion object DEFAULT : AccountUiHook
}

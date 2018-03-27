package com.schibsted.account.ui

import com.schibsted.account.session.User

interface UiHoks {
    class OnReadyListener(private val block: () -> Unit) {
        fun onReady() {
            block()
        }
    }

    fun onUiClosing(user: User, onReadyListener: OnReadyListener) {
        onReadyListener.onReady()
    }

    companion object DEFAULT : UiHoks
}

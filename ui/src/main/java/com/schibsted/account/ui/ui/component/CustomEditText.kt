/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui.component

import android.content.Context
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper

/**
 * This class extends the basic EditText and provides a custom InputConnection.
 * The custom InputConnection dispatches a key event when the delete key is pressed with a soft keyboard
 * The default EditText doesn't dispatch the delete key event after API 16, that is not considered as a bug
 * https://developer.android.com/reference/android/view/KeyEvent.html
 */
class CustomEditText(context: Context?, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        return IdentityInputConnection(super.onCreateInputConnection(outAttrs), true)
    }

    var keyEventListener: KeyEventListener? = null

    private inner class IdentityInputConnection(target: InputConnection, mutable: Boolean) : InputConnectionWrapper(target, mutable) {

        override fun sendKeyEvent(event: KeyEvent): Boolean {
            if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL) {
                if (text.isEmpty()) {
                    keyEventListener?.onDeleteKeyPressed()
                }
            }
            return super.sendKeyEvent(event)
        }

        override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
            return if (beforeLength == 1 && afterLength == 0) {
                sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)) && sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
            } else {
                super.deleteSurroundingText(beforeLength, afterLength)
            }
        }
    }

    interface KeyEventListener {
        fun onDeleteKeyPressed()
    }
}

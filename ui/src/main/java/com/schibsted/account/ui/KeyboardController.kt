/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.ui

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Rect
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import com.schibsted.account.ui.ui.BaseFragment
import com.schibsted.account.ui.ui.FlowFragment

class KeyboardController(private val activity: AppCompatActivity) : KeyboardListener {
    private val activityRoot: View = activity.findViewById(R.id.activity_layout)
    private val layoutListener: ViewTreeObserver.OnGlobalLayoutListener
    private var keyboardIsOpen: Boolean = false
    val keyboardVisibility = MutableLiveData<Boolean>()

    companion object {
        fun closeKeyboard(activity: FragmentActivity) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
        }

        fun showKeyboard(activity: FragmentActivity) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    init {
        val keyboardThreshold = 150f

        layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            private val usableSpace = Rect()
            private val pxKeyboardThreshold = keyboardThreshold * (activity.resources.displayMetrics.densityDpi / 160f)
            private val visibleThreshold = Math.round(pxKeyboardThreshold)
            private var isOpenAlready = false

            override fun onGlobalLayout() {
                activityRoot.getWindowVisibleDisplayFrame(usableSpace)
                val heightDiff = activityRoot.rootView.height - usableSpace.height()
                keyboardIsOpen = heightDiff > visibleThreshold
                if (keyboardIsOpen != isOpenAlready) {
                    isOpenAlready = keyboardIsOpen
                    keyboardVisibility.value = keyboardIsOpen
                }
            }
        }
    }

    override fun isKeyboardOpen(): Boolean = keyboardIsOpen
    /**
     * Closes down the keyboard
     * On some devices keyboard may still be showing up, even on screen without field to fill in
     * Because the keyboard is not part of the application a different behavior might occurs depending
     * on the system implementation.
     */
    override fun closeKeyboard() {
        Companion.closeKeyboard(activity)
    }

    /**
     * set up the keyboard actionListener
     * [FlowFragment.onVisibilityChanged] is called when a layout change occurs due to
     * a change of the soft keyboard visibility
     */
    fun register(baseFragment: BaseFragment?) {
        activityRoot.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        baseFragment?.registerKeyboardController(this)
    }

    fun unregister(baseFragment: BaseFragment?) {
        activityRoot.viewTreeObserver.removeGlobalOnLayoutListener(layoutListener)
        baseFragment?.unregisterKeyboardController()
    }
}

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.ui.AccountUi
import com.schibsted.account.ui.InternalUiConfiguration
import com.schibsted.account.ui.login.KeyboardVisibilityListener
import com.schibsted.account.ui.ui.component.LoadingButton

/**
 * a [Fragment] used to represents a part of a UI flow, as such, this fragment can continue a flow.
 * the flow could be continue thanks to the keyboard or a button.
 */
abstract class FlowFragment<in T> : BaseFragment(), KeyboardVisibilityListener, FlowView<T> {
    /**
     * a [Button] allowing the user to continue his path
     */
    protected lateinit var primaryActionView: LoadingButton
    @JvmField
    protected var secondaryActionView: Button? = null

    protected lateinit var uiConf: InternalUiConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = requireNotNull(savedInstanceState ?: arguments)

        this.uiConf = args.getParcelable(KEY_UI_CONF) ?: InternalUiConfiguration.resolve(activity!!.application, AccountUi.Params(), Identifier.IdentifierType.EMAIL)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpContinueViewVisibility(keyboardListener?.isKeyboardOpen ?: false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_UI_CONF, uiConf)
    }

    /**
     * This method is called when the soft keyboard has pop down or pop up.
     *
     * If the keyboard is visible the [primaryActionView] and [secondaryActionView] are animated to make a smooth
     * transition.
     * After the animation [setUpContinueViewVisibility] is called
     *
     * @param isOpen `true` if the keyboard is visible, `false` otherwise
     */
    override fun onVisibilityChanged(isOpen: Boolean) {
        if (isOpen) {
            primaryActionView.alpha = 0.0f
            secondaryActionView?.alpha = 0.0f
        } else {
            primaryActionView.animate().alpha(1.0f).duration = 200
            secondaryActionView?.animate()?.alpha(1.0f)?.duration = 200
        }
        setUpContinueViewVisibility(isOpen)
    }

    /**
     * Call this method to set up the visibility of the [primaryActionView] and [secondaryActionView]
     * depending on the soft keyboard visibility
     *
     * @param isOpen `true` if the keyboard is visible, `false` otherwise
     */
    private fun setUpContinueViewVisibility(isOpen: Boolean) {
        if (isOpen) {
            primaryActionView.visibility = View.GONE
            secondaryActionView?.visibility = View.GONE
        } else {
            primaryActionView.visibility = View.VISIBLE
            secondaryActionView?.visibility = View.VISIBLE
        }
    }

    override fun hideProgress() {
        primaryActionView.hideProgress()
        secondaryActionView?.isEnabled = true
    }

    override fun showProgress() {
        primaryActionView.showProgress()
        secondaryActionView?.isEnabled = false
        keyboardListener?.closeKeyboard()
    }

    companion object {
        const val KEY_UI_CONF = "SCHACC_UI_CONFIG"
    }
}

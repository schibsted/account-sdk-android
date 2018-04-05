/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.term

import com.schibsted.account.ui.ui.FlowView
import com.schibsted.account.ui.ui.component.CheckBoxView

/**
 * Following the MVP design pattern this interface represent the contract for the view and the presenter responsible
 * for the terms and condition feature
 *
 * @see com.schibsted.account.ui.login.screen.term
 */
interface TermsContract {

    /**
     * defines methods implemented by views related to terms and condition business
     */
    interface View : FlowView<Presenter> {

        /**
         * Requests a navigation to a [com.schibsted.account.ui.ui.WebFragment]. It should be use to show terms and policies of
         * SPiD and the client as a web page.
         *
         * @param link the client to go to.
         */
        fun requestNavigationToWebView(link: String)
    }

    /**
     * defines methods implemented by presenters performing terms and conditions business
     *
     * @see com.schibsted.account.ui.login.screen.term.TermsFragment
     */
    interface Presenter {

        /**
         * Depending on the state of checkboxes this method should change the state of the desired [android.view.View]
         *
         * @param privacyBox the state of the privacy checkbox could be checked or unchecked
         * @param termsBox the state of the terms checkbox could be checked or unchecked
         */
        fun verifyBoxes(privacyBox: CheckBoxView, termsBox: CheckBoxView)
    }
}

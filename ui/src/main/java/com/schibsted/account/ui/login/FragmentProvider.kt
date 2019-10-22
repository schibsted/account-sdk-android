/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login

import android.arch.lifecycle.MutableLiveData
import com.schibsted.account.engine.controller.PasswordlessController
import com.schibsted.account.engine.input.Agreements
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.input.RequiredFields
import com.schibsted.account.engine.input.VerificationCode
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.network.response.AgreementLinksResponse
import com.schibsted.account.network.response.ClientInfo
import com.schibsted.account.ui.AccountUi
import com.schibsted.account.ui.InternalUiConfiguration
import com.schibsted.account.ui.login.flow.password.FlowSelectionListener
import com.schibsted.account.ui.login.screen.identification.IdentificationPresenter
import com.schibsted.account.ui.login.screen.identification.ui.AbstractIdentificationFragment
import com.schibsted.account.ui.login.screen.identification.ui.EmailIdentificationFragment
import com.schibsted.account.ui.login.screen.identification.ui.MobileIdentificationFragment
import com.schibsted.account.ui.login.screen.inbox.InboxFragment
import com.schibsted.account.ui.login.screen.information.RequiredFieldsFragment
import com.schibsted.account.ui.login.screen.information.RequiredFieldsPresenter
import com.schibsted.account.ui.login.screen.onesteplogin.OneStepLoginFragment
import com.schibsted.account.ui.login.screen.onesteplogin.OneStepLoginPresenter
import com.schibsted.account.ui.login.screen.password.PasswordFragment
import com.schibsted.account.ui.login.screen.password.PasswordPresenter
import com.schibsted.account.ui.login.screen.term.TermsFragment
import com.schibsted.account.ui.login.screen.term.TermsPresenter
import com.schibsted.account.ui.login.screen.verification.VerificationFragment
import com.schibsted.account.ui.login.screen.verification.VerificationPresenter
import com.schibsted.account.ui.navigation.Navigation
import com.schibsted.account.ui.smartlock.SmartlockController
import com.schibsted.account.ui.ui.BaseFragment

class FragmentProvider(private val uiConfiguration: InternalUiConfiguration, private val navigation: Navigation) {

    fun getOrCreateIdentificationFragment(
        provider: InputProvider<Identifier>? = null,
        flowType: AccountUi.FlowType,
        flowSelectionListener: FlowSelectionListener? = null,
        clientInfo: ClientInfo
    ): BaseFragment {

        return getFragment<AbstractIdentificationFragment>(navigation.currentFragment, {
            if (flowType == AccountUi.FlowType.PASSWORDLESS_SMS) {
                MobileIdentificationFragment.newInstance(uiConfiguration, clientInfo)
            } else {
                EmailIdentificationFragment.newInstance(uiConfiguration, clientInfo)
            }
        }, {
            it.setPresenter(IdentificationPresenter(it, provider, flowSelectionListener))
        })
    }

    fun getOrCreateOneStepLoginFragment(
            idProvider: InputProvider<Identifier>? = null,
            credProvider: MutableLiveData<InputProvider<Credentials>>,
            smartlockController: SmartlockController?,
            flowSelectionListener: FlowSelectionListener? = null,
            clientInfo: ClientInfo
    ): BaseFragment {

        return getFragment<OneStepLoginFragment>(navigation.currentFragment, {
            OneStepLoginFragment.newInstance(uiConfiguration, clientInfo)
        }, {
            it.setPresenter(OneStepLoginPresenter(it, credProvider,smartlockController, flowSelectionListener))
        })
    }

    fun getOrCreatePasswordFragment(
        provider: InputProvider<Credentials>,
        currentIdentifier: Identifier,
        userAvailable: Boolean,
        smartlockController: SmartlockController?
    ): BaseFragment {
        return getFragment(navigation.currentFragment, {
            PasswordFragment.newInstance(currentIdentifier, userAvailable, uiConfiguration)
        }, {
            it.setPresenter(PasswordPresenter(it, provider, smartlockController))
        })
    }

    fun getOrCreateInboxFragment(currentIdentifier: Identifier): BaseFragment {
        return navigation.currentFragment as? InboxFragment
                ?: InboxFragment.newInstance(currentIdentifier)
    }

    fun getOrCreateTermsFragment(provider: InputProvider<Agreements>, userAvailable: Boolean, agreementLinks: AgreementLinksResponse): BaseFragment {
        return getFragment(navigation.currentFragment, {
            TermsFragment.newInstance(uiConfiguration, userAvailable, agreementLinks)
        }, {
            it.setPresenter(TermsPresenter(it, provider))
        })
    }

    fun getOrCreateRequiredFieldsFragment(provider: InputProvider<RequiredFields>, fields: Set<String>): BaseFragment {
        return getFragment(navigation.currentFragment, {
            RequiredFieldsFragment.newInstance(uiConfiguration)
        }, {
            it.setPresenter(RequiredFieldsPresenter(it, provider))
            it.missingField = fields
        })
    }

    fun getOrCreateVerificationScreen(
        provider: InputProvider<VerificationCode>,
        identifier: Identifier,
        passwordlessController: PasswordlessController
    ): BaseFragment {
        return getFragment(navigation.currentFragment, { VerificationFragment.newInstance(identifier) }, {
            it.setPresenter(VerificationPresenter(it, provider))
            it.setPasswordlessController(passwordlessController)
        })
    }

    private inline fun <reified T> getFragment(
        existingFragment: BaseFragment?,
        create: () -> T,
        applyTo: (T) -> Unit
    ): T {
        return if (existingFragment is T) {
            applyTo(existingFragment)
            existingFragment
        } else {
            val fragment = create()
            applyTo(fragment)
            fragment
        }
    }
}

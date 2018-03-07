/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.flow.password

import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.engine.input.Agreements
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.input.RequiredFields
import com.schibsted.account.engine.integration.CallbackProvider
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallbackData
import com.schibsted.account.engine.integration.contract.SignUpContract
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.AgreementLinksResponse
import com.schibsted.account.ui.login.BaseLoginActivity
import com.schibsted.account.ui.ui.FlowFragment

class SignupContractImpl(private val activity: BaseLoginActivity) : SignUpContract {
    override fun onCredentialsRequested(provider: InputProvider<Credentials>) {
        activity.currentIdentifier?.let { identifier ->
            val fragment = activity.fragmentProvider.getOrCreatePasswordFragment(
                    activity.navigationController.currentFragment,
                    provider = provider,
                    currentIdentifier = identifier,
                    userAvailable = activity.isUserAvailable(),
                    smartlockImpl = null)
            activity.navigationController.navigateToFragment(fragment)
        } ?: activity.startIdentificationFragment(if(activity is FlowSelectionListener) activity else null)
    }

    override fun onAgreementsRequested(agreementsProvider: InputProvider<Agreements>, agreementLinks: AgreementLinksResponse) {
        val fragment = activity.fragmentProvider.getOrCreateTermsFragment(activity.navigationController.currentFragment,
                provider = agreementsProvider,
                userAvailable = activity.isUserAvailable(),
                agreementLinks = agreementLinks)
        activity.navigationController.navigateToFragment(fragment)
    }

    override fun onRequiredFieldsRequested(requiredFieldsProvider: InputProvider<RequiredFields>, fields: Set<String>) {
        val fragment = activity.fragmentProvider.getOrCreateRequiredFieldsFragment(
                activity.navigationController.currentFragment,
                requiredFieldsProvider,
                fields)
        activity.navigationController.navigateToFragment(fragment)
    }

    override fun onFlowReady(callbackProvider: CallbackProvider<Identifier>) {
        callbackProvider.provide(object : ResultCallbackData<Identifier> {
            override fun onSuccess(result: Identifier) {
                val fragment = activity.fragmentProvider.getOrCreateInboxFragment(activity.navigationController.currentFragment, result)
                activity.navigationController.navigateToFragment(fragment)
                BaseLoginActivity.tracker?.eventActionSuccessful(TrackingData.SpidAction.ACCOUNT_CREATED)
            }

            override fun onError(error: ClientError) {
                val currentFragment = activity.navigationController.currentFragment
                currentFragment?.displayErrorDialog(error)

                if (currentFragment is FlowFragment<*>) {
                    currentFragment.hideProgress()
                }
                if (error.errorType == ClientError.ErrorType.NETWORK_ERROR) {
                    BaseLoginActivity.tracker?.eventError(TrackingData.UIError.NetworkError, null)
                }
            }
        })
    }
}

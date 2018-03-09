/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.controller

import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.input.Agreements
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.input.RequiredFields
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.integration.contract.Contract
import com.schibsted.account.engine.step.StepSignUpCredentials
import com.schibsted.account.engine.step.StepValidateAgreements
import com.schibsted.account.engine.step.StepValidateReqFields
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.AgreementLinksResponse
import com.schibsted.account.session.User

abstract class VerificationController<in T> : Controller<T>()
        where T : Agreements.Provider, T : RequiredFields.Provider, T : Contract<*> {

    protected fun requestAgreements(contract: T, user: User, agreementsLinks: AgreementLinksResponse): StepValidateAgreements? {
        val res = findOnStack<StepValidateAgreements>()
        if (res == null) {
            Agreements.request(contract, { input, callback ->
                user.agreements.acceptAgreements(object : ResultCallback<Void?> {
                    override fun onSuccess(result: Void?) {
                        this@VerificationController.navigation.push(StepValidateAgreements(input))
                        callback.onSuccess(null)
                        evaluate(contract)
                    }

                    override fun onError(error: ClientError) {
                        callback.onError(error)
                    }
                })
            }, agreementsLinks)
        }

        return res
    }

    protected fun requestRequiredFields(contract: T, user: User, missingFields: Set<String>): StepValidateReqFields? {
        val res = findOnStack<StepValidateReqFields>()
        if (res == null) {
            Logger.info(Logger.DEFAULT_TAG, "Missing required fields: $missingFields")

            val preFilledValues = mutableMapOf<String, String>()
            if (missingFields.contains(RequiredFields.FIELD_EMAIL)) {
                findOnStack<StepSignUpCredentials>()?.credentials?.identifier?.takeIf { it.identifierType == Identifier.IdentifierType.EMAIL }?.identifier
                        ?.let { preFilledValues.put(RequiredFields.FIELD_EMAIL, it) }
            }

            preFilledValues.forEach { Logger.info(Logger.DEFAULT_TAG, "Automatically providing required field: ${it.key}") }

            val missingFieldsAfterPreFill = missingFields - preFilledValues.keys
            val supportedFields = missingFieldsAfterPreFill.filter { RequiredFields.SUPPORTED_FIELDS.contains(it) }.toSet()

            if (supportedFields.isNotEmpty()) {
                RequiredFields.request(contract, supportedFields, { input, callback ->

                    val providedFieldsAndPreFill = RequiredFields(input.fields + preFilledValues)
                    user.profile.update(RequiredFields.transformFieldsToProfile(providedFieldsAndPreFill.fields), object : ResultCallback<Void?> {
                        override fun onSuccess(result: Void?) {
                            this@VerificationController.navigation.push(StepValidateReqFields(providedFieldsAndPreFill))
                            callback.onSuccess(null)
                            evaluate(contract)
                        }

                        override fun onError(error: ClientError) {
                            callback.onError(error)
                        }
                    })
                })
            } else {
                super.navigation.push(StepValidateReqFields(RequiredFields(preFilledValues)))
                evaluate(contract)
            }
        }

        return res
    }
}

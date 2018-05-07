/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.controller

import android.os.Parcel
import android.os.Parcelable
import com.schibsted.account.common.util.Logger
import com.schibsted.account.common.util.readStack
import com.schibsted.account.engine.input.Agreements
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.input.RequiredFields
import com.schibsted.account.engine.integration.CallbackProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.integration.contract.SignUpContract
import com.schibsted.account.engine.operation.AccountStatusOperation
import com.schibsted.account.engine.operation.ClientInfoOperation
import com.schibsted.account.engine.operation.SignUpOperation
import com.schibsted.account.engine.step.StepSignUpCredentials
import com.schibsted.account.engine.step.StepSignUpDone
import com.schibsted.account.engine.step.StepValidateAgreements
import com.schibsted.account.engine.step.StepValidateReqFields
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.SignUpParams
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.AccountStatusResponse
import com.schibsted.account.network.response.AgreementLinksResponse
import com.schibsted.account.util.DeepLink
import java.net.URI

/**
 * Controller which administrates the process of creating a user and signing in. This is
 * parcelable and should be persisted during the sign up sequence. After the sequence has been
 * completed, the reference to this can be destroyed.
 * **Note:** After an Android configuration change, make sure you call [evaluate] again to re-trigger
 * the currently active task.
 */
class SignUpController(private val baseRedirectUri: URI) : Controller<SignUpContract>() {
    constructor(parcel: Parcel) : this(URI.create(parcel.readString())) {
        this.navigation.addAll(parcel.readStack(SignUpController::class.java.classLoader))
    }

    override fun evaluate(contract: SignUpContract) {
        val credentialsStep = this.getOrRequestCredentials(contract) ?: return
        val agreementsStep = getOrRequestAgreements(contract, credentialsStep.agreementsLink)
                ?: return
        val requiredFieldsStep: StepValidateReqFields = getOrRequestRequiredFields(contract, credentialsStep.clientReqFields)
                ?: return

        val stepSignUpDone = findOnStack<StepSignUpDone>()
        if (stepSignUpDone == null) {
            contract.onFlowReady(CallbackProvider { callback ->
                val params = RequiredFields.transformFieldsToProfile(requiredFieldsStep.requiredFields.fields) +
                        SignUpParams(
                                password = credentialsStep.credentials.password,
                                acceptTerms = agreementsStep.agreements.acceptAgreements).getParams()

                val deepLink = DeepLink.ValidateAccount.createDeepLinkUri(baseRedirectUri, credentialsStep.credentials.keepLoggedIn)

                SignUpOperation(credentialsStep.credentials.identifier.identifier, deepLink, params,
                        { callback.onError(it.toClientError()) },
                        {
                            callback.onSuccess(credentialsStep.credentials.identifier)
                            super.navigation.push(StepSignUpDone(credentialsStep.credentials.identifier))
                        })
            })
        }
    }

    private fun getOrRequestCredentials(contract: SignUpContract): StepSignUpCredentials? {
        val res = findOnStack<StepSignUpCredentials>()
        if (res == null) {
            Credentials.request(contract, { input, callback ->
                AccountStatusOperation(input.identifier, { callback.onError(it.toClientError()) }, { status: AccountStatusResponse ->
                    if (!status.isAvailable) {
                        callback.onError(ClientError(ClientError.ErrorType.ALREADY_REGISTERED, "Account already registered"))
                        return@AccountStatusOperation
                    }

                    com.schibsted.account.session.Agreements.getAgreementLinks(
                            ResultCallback.fromLambda(
                                    { callback.onError(it) },
                                    { agreementsLinks ->
                                        ClientInfoOperation(
                                                { callback.onError(it.toClientError()) },
                                                { clientInfo ->
                                                    super.navigation.push(StepSignUpCredentials(input, clientInfo.requiredFields(), agreementsLinks))
                                                    callback.onSuccess(NoValue)
                                                    this.evaluate(contract)
                                                })
                                    })
                    )
                })
            })
        }
        return res
    }

    private fun getOrRequestAgreements(contract: SignUpContract, agreementsLink: AgreementLinksResponse): StepValidateAgreements? {
        val res = findOnStack<StepValidateAgreements>()
        if (res == null) {
            Agreements.request(contract, { input, callback ->
                super.navigation.push(StepValidateAgreements(input))
                callback.onSuccess(NoValue)
                this.evaluate(contract)
            }, agreementsLink)
        }

        return res
    }

    private fun getOrRequestRequiredFields(contract: SignUpContract, missingFields: Set<String>): StepValidateReqFields? {
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
                    super.navigation.push(StepValidateReqFields(RequiredFields(input.fields + preFilledValues)))
                    callback.onSuccess(NoValue)
                    this.evaluate(contract)
                })
            } else {
                super.navigation.push(StepValidateReqFields(RequiredFields(preFilledValues)))
                evaluate(contract)
            }
        }

        return res
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(this.baseRedirectUri.toString())
        super.writeToParcel(parcel, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SignUpController> {
        override fun createFromParcel(parcel: Parcel): SignUpController = SignUpController(parcel)

        override fun newArray(size: Int): Array<SignUpController?> = arrayOfNulls(size)
    }
}

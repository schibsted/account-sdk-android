/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.controller

import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import com.schibsted.account.AccountService
import com.schibsted.account.Events
import com.schibsted.account.common.util.readStack
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.input.VerificationCode
import com.schibsted.account.engine.integration.CallbackProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.integration.contract.PasswordlessContract
import com.schibsted.account.engine.operation.AccountStatusOperation
import com.schibsted.account.engine.operation.AgreementsCheckOperation
import com.schibsted.account.engine.operation.MissingFieldsOperation
import com.schibsted.account.engine.operation.ResendCodeOperation
import com.schibsted.account.engine.operation.SendValidationCodeOperation
import com.schibsted.account.engine.operation.VerifyCodeOperation
import com.schibsted.account.engine.step.StepNoPwIdentify
import com.schibsted.account.engine.step.StepNoPwValidationCode
import com.schibsted.account.model.LoginResult
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.OIDCScope
import com.schibsted.account.network.response.PasswordlessToken
import com.schibsted.account.session.Agreements
import com.schibsted.account.session.User
import java.util.Locale

/**
 * Controller which administrates the process of a login flow using passwordless login. This is
 * parcelable and should be persisted during the login sequence. After the sequence has been
 * completed, the reference to this can be destroyed.
 * **Note:** After an Android configuration change, make sure you call [evaluate] again to re-trigger
 * the currently active task.
 */
class PasswordlessController @JvmOverloads constructor(
    private val verifyUser: Boolean,
    private val locale: Locale = Locale.getDefault(),
    @OIDCScope private val scopes: Array<String> = arrayOf(OIDCScope.SCOPE_OPENID)
) : VerificationController<PasswordlessContract>() {

    constructor(parcel: Parcel) : this(
            parcel.readInt() != 0,
            Locale(parcel.readString()),
            parcel.createStringArray()) {
        super.navigation.addAll(parcel.readStack(PasswordlessController::class.java.classLoader))
    }

    override fun evaluate(contract: PasswordlessContract) {
        val idStep = this.getOrRequestIdentifier(contract) ?: return
        val validationStep = this.getOrRequestVerificationCode(contract, idStep.identifier, idStep.passwordlessToken)
                ?: return

        if (this.verifyUser) {
            if (!validationStep.agreementsAccepted) {
                super.requestAgreements(contract, validationStep.user, idStep.agreementLinks)
                        ?: return
            }
            super.requestRequiredFields(contract, validationStep.user, validationStep.missingFields)
                    ?: return

            contract.onFlowReady(CallbackProvider {
                it.onSuccess(LoginResult(validationStep.user, idStep.isNewUser))
                AccountService.localBroadcastManager?.sendBroadcast(Intent(Events.ACTION_USER_LOGIN).putExtra(Events.EXTRA_USER, validationStep.user))
            })
        } else {
            contract.onFlowReady(CallbackProvider {
                it.onSuccess(LoginResult(validationStep.user, idStep.isNewUser))
                AccountService.localBroadcastManager?.sendBroadcast(Intent(Events.ACTION_USER_LOGIN).putExtra(Events.EXTRA_USER, validationStep.user))
            })
        }
    }

    private fun getOrRequestIdentifier(provider: PasswordlessContract): StepNoPwIdentify? {
        val res = findOnStack<StepNoPwIdentify>()
        if (res == null) {
            Identifier.request(provider) { identifier, callback ->
                AccountStatusOperation(identifier, { callback.onError(it.toClientError()) }, { accountStatus ->
                    SendValidationCodeOperation(identifier, this.locale, { callback.onError(it.toClientError()) }, { passwordlessToken ->
                        Agreements.getAgreementLinks(ResultCallback.fromLambda(
                                { callback.onError(it) },
                                { agreementsLinks ->
                                    super.navigation.push(StepNoPwIdentify(identifier, passwordlessToken, accountStatus.isAvailable, agreementsLinks))
                                    callback.onSuccess(NoValue)
                                    evaluate(provider)
                                }))
                    })
                })
            }
        }

        return res
    }

    private fun getOrRequestVerificationCode(provider: PasswordlessContract, identifier: Identifier, passwordlessToken: PasswordlessToken): StepNoPwValidationCode? {
        val res = findOnStack<StepNoPwValidationCode>()
        if (res == null) {
            VerificationCode.request(provider, identifier) { verificationCode, callback ->
                VerifyCodeOperation(identifier, passwordlessToken, verificationCode, scopes, { callback.onError(it.toClientError()) },
                        { token ->
                            val user = User(token, verificationCode.keepLoggedIn)
                            user.device.createFingerprint()

                            if (this.verifyUser) {
                                AgreementsCheckOperation(user, { callback.onError(it.toClientError()) }) { agreementsCheck ->
                                    MissingFieldsOperation(user, { callback.onError(it.toClientError()) }) { missingFields ->
                                        super.navigation.push(StepNoPwValidationCode(verificationCode, user, agreementsCheck.allAccepted(), missingFields))
                                        callback.onSuccess(NoValue)
                                        evaluate(provider)
                                    }
                                }
                            } else {
                                super.navigation.push(StepNoPwValidationCode(verificationCode, user, true, setOf()))
                                callback.onSuccess(NoValue)
                                evaluate(provider)
                            }
                        })
            }
        }

        return res
    }

    /**
     * Resend the verification code to the user. Please note that this is throttled by the SPiD
     * back-end and will fail if requested too often
     */
    @Suppress("unused")
    fun resendCode(resultCallback: ResultCallback<NoValue>) {
        val top = super.navigation.peek()

        if (top is StepNoPwIdentify) {
            ResendCodeOperation(top.passwordlessToken, { resultCallback.onError(it.toClientError()) }, {
                super.navigation.pop()
                super.navigation.push(top.copy(passwordlessToken = it))
                resultCallback.onSuccess(NoValue)
            })
        } else {
            resultCallback.onError(ClientError(ClientError.ErrorType.INVALID_STATE,
                    "Could not resend verification code, as the current state (${super.navigation.peek().javaClass.simpleName}) is not PasswordlessToken"))
        }
    }

    /**
     * Changes the current identifier and sends a validation code to hte new identifier
     */
    @Suppress("unused")
    fun resetIdentifier(contract: PasswordlessContract) {
        super.navigation.clear()
        getOrRequestIdentifier(contract)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(if (verifyUser) 1 else 0)
        parcel.writeString(this.locale.toString())
        parcel.writeStringArray(scopes)
        super.writeToParcel(parcel, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PasswordlessController> {
        override fun createFromParcel(parcel: Parcel): PasswordlessController =
                PasswordlessController(parcel)

        override fun newArray(size: Int): Array<PasswordlessController?> = arrayOfNulls(size)
    }
}

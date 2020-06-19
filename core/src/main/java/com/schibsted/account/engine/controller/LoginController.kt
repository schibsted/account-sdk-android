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
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.integration.CallbackProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.integration.contract.LoginContract
import com.schibsted.account.engine.operation.AgreementsCheckOperation
import com.schibsted.account.engine.operation.LoginOperation
import com.schibsted.account.engine.operation.MissingFieldsOperation
import com.schibsted.account.engine.step.StepLoginIdentify
import com.schibsted.account.model.LoginResult
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.UserId
import com.schibsted.account.model.UserToken
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.OIDCScope
import com.schibsted.account.session.Agreements
import com.schibsted.account.session.User

/**
 * Controller which administrates the process of a login flow using credentials. This is
 * parcelable and should be persisted during the login sequence. After the sequence has been
 * completed, the reference to this can be destroyed.
 * **Note:** After an Android configuration change, make sure you call [evaluate] again to re-trigger
 * the currently active task.
 */

class LoginController @JvmOverloads constructor(
    private val verifyUser: Boolean,
    @OIDCScope private val scopes: Array<String> = arrayOf(OIDCScope.SCOPE_OPENID),
    var currentUserId: UserId? = null
) : VerificationController<LoginContract>() {

    constructor(parcel: Parcel) : this(parcel.readInt() != 0, parcel.createStringArray(), parcel.readParcelable<UserId>(UserId::class.java.classLoader)) {
        super.navigation.addAll(parcel.readStack(LoginController::class.java.classLoader))
    }

    override fun evaluate(contract: LoginContract) {
        val idLoginStep = this.requestCredentials(contract) { credentials, callback ->
            LoginOperation(credentials, scopes, {
                if (it.toClientError().errorType == ClientError.ErrorType.ACCOUNT_NOT_VERIFIED) {
                    contract.onAccountVerificationRequested(credentials.identifier)
                } else {
                    callback.onError(it.toClientError())
                }
            }) {
                val userToken = UserToken(it)
                val userId = UserId.fromUserTokenResponse(it)
                val user = User(userToken, userId, isPersistable = credentials.keepLoggedIn)
                this.currentUserId = user.userId
                user.device.createFingerprint()

                if (this.verifyUser) { // Attempt the happy path and proceed straight to login
                    AgreementsCheckOperation(user, { callback.onError(it.toClientError()) }) { agreementsCheck ->
                        Agreements.getAgreementLinks(ResultCallback.fromLambda(
                                { callback.onError(it) },
                                { agreementsLink ->
                                    MissingFieldsOperation(user, { callback.onError(it.toClientError()) }) { missingFields ->
                                        super.navigation.push(StepLoginIdentify(credentials, user, agreementsCheck.allAccepted(), missingFields, agreementsLink))
                                        callback.onSuccess(NoValue)
                                        evaluate(contract)
                                    }
                                })
                        )
                    }
                } else {
                    super.navigation.push(StepLoginIdentify(credentials, user, true, setOf()))
                    callback.onSuccess(NoValue)
                    evaluate(contract)
                }
            }
        } ?: return

        if (this.verifyUser) {
            if (!idLoginStep.agreementsAccepted) {
                super.requestAgreements(contract, idLoginStep.user, idLoginStep.agreementLinks!!)
                        ?: return
            }
            super.requestRequiredFields(contract, idLoginStep.user, idLoginStep.missingFields)
                    ?: return

            contract.onFlowReady(CallbackProvider {
                it.onSuccess(LoginResult(idLoginStep.user, false))
                AccountService.localBroadcastManager?.sendBroadcast(Intent(Events.ACTION_USER_LOGIN).putExtra(Events.EXTRA_USER, idLoginStep.user))
            })
        } else {
            contract.onFlowReady(CallbackProvider {
                it.onSuccess(LoginResult(idLoginStep.user, false))
                AccountService.localBroadcastManager?.sendBroadcast(Intent(Events.ACTION_USER_LOGIN).putExtra(Events.EXTRA_USER, idLoginStep.user))
            })
        }
    }

    private fun requestCredentials(provider: LoginContract, onProvided: (Credentials, ResultCallback<NoValue>) -> Unit): StepLoginIdentify? {
        val res = super.findOnStack<StepLoginIdentify>()
        if (res == null) {
            Credentials.request(provider) { input, callback ->
                onProvided(input, callback)
            }
        }

        return res
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(if (verifyUser) 1 else 0)
        parcel.writeStringArray(scopes)
        parcel.writeParcelable(currentUserId, 0)
        super.writeToParcel(parcel, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<LoginController> {
        override fun createFromParcel(parcel: Parcel): LoginController = LoginController(parcel)

        override fun newArray(size: Int): Array<LoginController?> = arrayOfNulls(size)
    }
}

package com.schibsted.account.persistence

import android.content.Context
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.UserToken
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.session.User

/**
 * Retrieves agreement from cache or back-end, and invokes callback depending on result.
 */
internal class ResumeDelegate(private val agreementCache: AgreementCache) {

    constructor(context: Context) : this(
            AgreementCache(AgreementStorage(context))
    )

    fun proceed(token: UserToken,
                success: (user: User) -> Unit,
                failure: (error: ClientError) -> Unit) {
        val user = User(token, isPersistable = true)
        if (agreementCache.hasValidAgreement(user.userId.id)) {
            success(user)
        } else {
            user.agreements.ensureAccepted(object : ResultCallback<NoValue> {
                override fun onError(error: ClientError) = failure(error)
                override fun onSuccess(result: NoValue) = success(user).also {
                    agreementCache.storeAgreement(user.userId.id)
                }
            })
        }
    }
}

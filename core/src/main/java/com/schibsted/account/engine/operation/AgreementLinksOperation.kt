/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.operation

import com.schibsted.account.engine.integration.ResultCallbackData
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.AgreementLinksResponse
import com.schibsted.account.session.Agreements

internal class AgreementLinksOperation(
    failure: (ClientError) -> Unit,
    success: (AgreementLinksResponse) -> Unit) {

    init {
        Agreements.getAgreementLinks(object : ResultCallbackData<AgreementLinksResponse> {
            override fun onSuccess(result: AgreementLinksResponse) {
                success(result)
            }

            override fun onError(error: ClientError) {
                failure(error)
            }
        })
    }
}

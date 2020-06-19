/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account

import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.operation.ClientTokenOperation
import com.schibsted.account.model.Product
import com.schibsted.account.model.error.NetworkError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.ApiContainer
import com.schibsted.account.network.response.ClientTokenResponse

object Client {

    @JvmStatic
    fun getProducts(resultCallback: ResultCallback<List<Product>>) {
        fetchTokenFirst(resultCallback) { fetchProducts(it, resultCallback) }
    }

    @JvmStatic
    fun getProduct(productId: String, resultCallback: ResultCallback<Product>) {
        fetchTokenFirst(resultCallback) { fetchProduct(it, productId, resultCallback) }
    }

    private fun fetchProduct(clientTokenResponse: ClientTokenResponse, productId: String, resultCallback: ResultCallback<Product>) {
        val bearerAuthHeader = "Bearer ${clientTokenResponse.serializedAccessToken}"
        ServiceHolder.clientService
                .getProduct(bearerAuthHeader, productId)
                .enqueue(object : NetworkCallback<ApiContainer<Product>>("Retrieving product with id $productId") {

                    override fun onSuccess(result: ApiContainer<Product>) =
                            resultCallback.onSuccess(result.data)

                    override fun onError(error: NetworkError) =
                            resultCallback.onError(error.toClientError())
                })
    }

    private fun fetchProducts(clientTokenResponse: ClientTokenResponse, resultCallback: ResultCallback<List<Product>>) {
        val bearerAuthHeader = "Bearer ${clientTokenResponse.serializedAccessToken}"
        ServiceHolder.clientService
                .getProducts(bearerAuthHeader)
                .enqueue(object : NetworkCallback<ListContainer<Product>>("Retrieving all client products") {

                    override fun onSuccess(result: ListContainer<Product>) =
                            resultCallback.onSuccess(result.value)

                    override fun onError(error: NetworkError) =
                            resultCallback.onError(error.toClientError())
                })
    }

    private fun fetchTokenFirst(resultCallback: ResultCallback<*>, nextAction: (t: ClientTokenResponse) -> Unit) {
        ClientTokenOperation(
                { resultCallback.onError(it.toClientError()) },
                { nextAction.invoke(it) }
        )
    }
}

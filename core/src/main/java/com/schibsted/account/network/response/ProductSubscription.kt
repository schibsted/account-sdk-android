package com.schibsted.account.network.response

data class ProductSubscription(
    val name: String,
    val code: Int,
    val data: ApiContainer<ProductAccess>
) {
    data class ProductAccess(
        val productId: String,
        val result: Boolean
    )
}

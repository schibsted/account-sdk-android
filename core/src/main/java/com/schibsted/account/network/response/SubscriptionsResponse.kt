package com.schibsted.account.network.response

import com.schibsted.account.model.Product
import java.util.Date

data class SubscriptionsResponse(val subscriptions: List<Subscription>)

data class Subscription(
    val subscriptionId: String,
    val clientId: String,
    val userId: String,
    val productId: String,
    val parentProductId: String,
    val identifierId: String,
    val orderId: String,
    val startDate: Date,
    val originalPurchaseDate: Date,
    val expires: Date,
    val autoRenew: String,
    val renewPrice: String,
    val currency: String,
    val renewPeriod: String,
    val autoRenewLockPeriod: String,
    val autoRenewDisabled: String,
    val gracePeriod: String,
    val emailReceiptCount: String,
    val finalEndDate: Date?,
    val chargeRetryCount: String,
    val chargeLastRetry: String?,
    val status: String,
    val statusChangeCode: String?,
    val statusChangeDate: Date?,
    val updated: Date?,
    val created: Date,
    val product: Product,
    val statusMsg: String
)

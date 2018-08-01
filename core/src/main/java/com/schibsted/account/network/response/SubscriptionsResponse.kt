package com.schibsted.account.network.response

import java.util.Date

data class SubscriptionsResponse(val subscriptions: List<Subscription>)
data class Subscription(
    val subscriptionId: String,
    val originalSubscriptionId: String,
    val clientId: String,
    val userId: String,
    val productId: String,
    val parentProductId: String,
    val identifierId: String?,
    val paymentType: String,
    val orderId: String?,
    val startDate: String,
    val originalPurchaseDate: Date,
    val expires: Date,
    val autoRenew: String,
    val autoRenewChangeDate: Date,
    val autoRenewChangeBy: String?,
    val renewPrice: String?,
    val currency: String,
    val renewPeriod: String,
    val autoRenewLockPeriod: String,
    val stopRenewalAfterLock: String,
    val autoRenewDisabled: String,
    val gracePeriod: String,
    val emailReceiptCount: String,
    val finalEndDate: Date,
    val chargeRetryCount: String,
    val chargeLastRetry: String?,
    val status: String,
    val statusChangeCode: String?,
    val statusChangeDate: String?,
    val updated: Date,
    val created: Date
)

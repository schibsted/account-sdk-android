package com.schibsted.account.persistence

import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Stores user ID with some random future expiration Date. If queried with the same user ID
 * before the expiration date, returns true (meaning the user has previously accepted
 * the terms and conditions and querying backend is not required), false otherwise (i.e. either
 * user ID doesn't match or the expiration date is already met).
 * Expiration date can be anything between 1 and 7 days. Why? If a significant number of users
 * open the app at the same time (for example, following a push notification), they will
 * create this cache at the same time. If cache expired at the same time too, this would create
 * a pattern when multiple users query the back-end at the same time, overloading the servers.
 */
internal class AgreementCache(private val agreementStorage: AgreementStorage) {

    companion object {
        private const val MIN_CACHE_MINUTES = 1 * 24 * 60 // One day
        private const val MAX_CACHE_MINUTES = 7 * 24 * 60 // Seven days
    }

    /**
     * Stores user ID with a random expiration date.
     */
    fun storeAgreement(userId: String) {
        agreementStorage.storeAgreement(userId, computeExpirationDate())
    }

    /**
     * Returns true, if provided user ID matches the stored user ID, and stored expiration date
     * is in future; false otherwise.
     */
    fun hasValidAgreement(userId: String): Boolean {
        val (storedId, storedDate) = agreementStorage.getAgreement() ?: return false
        return userId == storedId && Date() <= storedDate
    }

    private fun computeExpirationDate(): Date {
        val jitter = Random().nextInt(MAX_CACHE_MINUTES - MIN_CACHE_MINUTES)
        return Date(System.currentTimeMillis()
                + TimeUnit.MINUTES.toMillis(MIN_CACHE_MINUTES.toLong())
                + TimeUnit.MINUTES.toMillis(jitter.toLong()))
    }
}

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.VisibleForTesting
import com.schibsted.account.ClientConfiguration
import com.schibsted.account.common.lib.Try
import com.schibsted.account.common.lib.getOrDefault
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.UserToken
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.response.TokenResponse
import com.schibsted.account.session.User
import com.schibsted.account.util.DateUtils
import com.schibsted.account.util.KeyValueStore
import java.util.Date

/**
 * Handles persisting and resuming user's sessions. This supports multiple users, so you can resume
 * a specific user's session if required.
 * @param appContext The application context
 */
internal class UserPersistence(private val appContext: Context) {

    internal data class Session(val lastActive: Long, val userId: String, val token: UserToken)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var sessions: List<Session> by SessionStorageDelegate(appContext, PREFERENCE_FILENAME, SAVED_SESSIONS_KEY)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var acceptedAgreementsCache: String by StorageDelegate(appContext.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE),
            SharedPreferences::getString, SharedPreferences.Editor::putString, "", "AGR_CACHE")

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun termsPreviouslyAccepted(userId: String): Boolean {
        val validUntil = acceptedAgreementsCache.split(CACHE_PARAMETER_DELIMITER).takeIf { it.size == 2 && it.first() == userId }?.let { it[1] }
                ?: return false
        DateUtils.fromString(validUntil)?.let { return Date().before(it) }
        acceptedAgreementsCache = ""
        return false
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun putCacheResult(userId: String) {
        val validUntil = DateUtils.getLaterRandomDateAsString(
                UserPersistence.MIN_TERMS_CACHE_MINUTES,
                UserPersistence.MAX_TERMS_CACHE_MINUTES - UserPersistence.MIN_TERMS_CACHE_MINUTES)

        acceptedAgreementsCache = "$userId$CACHE_PARAMETER_DELIMITER$validUntil"
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun resumeSession(token: UserToken?, callback: ResultCallback<User>) {
        if (token == null) {
            callback.onError(ClientError(ClientError.ErrorType.SESSION_NOT_FOUND, "Could not find a session to resume"))
        } else {
            val user = User(token, true)

            if (termsPreviouslyAccepted(user.userId.id)) {
                callback.onSuccess(user)
            } else {
                user.agreements.ensureAccepted(ResultCallback.fromLambda({ callback.onError(it) }) {
                    putCacheResult(user.userId.id)
                    callback.onSuccess(user)
                })
            }
        }
    }

    /**
     * Resume a specific user's session. The session will be resumed and the required checks will
     * be done before a user object is returned through the contract
     * @param userId The user ID of the session to resume
     * @param callback The callback to which hte user is provided
     * @return The user object of the resumed session. Can be null
     */
    fun resume(userId: String, callback: ResultCallback<User>) {
        cleanInvalidTokens()
        val session = sessions.find { it.userId == userId }
        resumeSession(session?.token, callback)
    }

    /**
     * Resume the most recently active user session. The session will be resumed and the required checks will
     * be done before a user object is returned through the contract
     * @param callback The callback to which hte user is provided
     * @return The user object of the resumed session. Can be null
     */
    fun resumeLast(callback: ResultCallback<User>) {
        cleanInvalidTokens()
        val lastActiveSession = sessions.sortedByDescending { it.lastActive }.firstOrNull()?.token
                ?: readTokenCompat()
        resumeSession(lastActiveSession, callback)
    }

    /**
     * Remove a user session from persistence. Use this to allow users to be forgotten
     * @param userId The user ID to remove
     */
    fun remove(userId: String) {
        this.sessions = sessions.filterNot { it.userId == userId }
    }

    /**
     * Removes the last active user session
     */
    fun removeLast() {
        this.sessions = sessions.sortedByDescending { it.lastActive }.dropLast(1)
    }

    /**
     * Removes all persisted sessions
     */
    fun removeAll() {
        this.sessions = listOf()
    }

    /**
     * Persist a user session so that it can be resumed at a later point.
     * @param user The user to persist
     */
    fun persist(user: User) {
        val token = user.token?.takeIf { it.isValidToken() }

        when {
            token == null -> Logger.warn("Attempting to persist session, but the user was logged out")
            !user.isPersistable -> Logger.warn("Attempting to persist session, but the user is not flagged as persistable")
            else -> {
                val updatedSessions = (sessions.filterNot { it.userId == user.userId.id }) +
                        Session(System.currentTimeMillis(), user.userId.id, token)
                this.sessions = updatedSessions.sortedByDescending { it.lastActive }.take(MAX_SESSIONS)
            }
        }

        clearTokenCompat()
    }

    /**
     * Removes sessions with invalid tokens
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun cleanInvalidTokens() {
        val (validSessions, invalidSessions) = sessions.partition { Try { it.token.isValidToken() }.getOrDefault { false } }
        invalidSessions.forEach {
            Logger.warn("Found invalid session for user ${it.userId}")
        }
        this.sessions = validSessions
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    internal fun readTokenCompat(): TokenResponse? {
        val kvs = KeyValueStore(appContext)
        return kvs.readAccessTokenCompat(ClientConfiguration.get().clientSecret)
    }

    /**
     * Should be used when an access token is stored. We no longer need the old access tokens
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    internal fun clearTokenCompat() {
        KeyValueStore(appContext).clearAccessToken()
    }

    companion object {
        private const val PREFERENCE_FILENAME = "IDENTITY_PREFERENCES"
        private const val SAVED_SESSIONS_KEY = "IDENTITY_SESSIONS"
        private const val MAX_SESSIONS = 10
        private const val CACHE_PARAMETER_DELIMITER = "|"
        internal const val MIN_TERMS_CACHE_MINUTES = 1 * 24 * 60 // One day
        internal const val MAX_TERMS_CACHE_MINUTES = 7 * 24 * 60 // Seven days
    }
}

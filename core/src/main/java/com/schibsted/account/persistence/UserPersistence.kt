/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.schibsted.account.ClientConfiguration
import com.schibsted.account.model.UserToken
import com.schibsted.account.network.response.TokenResponse
import com.schibsted.account.session.User
import com.schibsted.account.util.KeyValueStore
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.error.ClientError

/**
 * Handles persisting and resuming user's sessions. This supports multiple users, so you can resume
 * a specific user's session if required.
 * @param appContext The application context
 */
class UserPersistence(private val appContext: Context) {
    internal data class Session(val lastActive: Long, val userId: String, val token: UserToken)

    private var sessions: List<Session> by SessionStorageDelegate(appContext, PREFERENCE_FILENAME, SAVED_SESSIONS_KEY)

    /**
     * Resume a specific user's session. The session will be resumed and the required checks will
     * be done before a user object is returned through the contract
     * @param userId The user ID of the session to resume
     * @return The user object of the resumed session. Can be null
     */
    @Deprecated("Deprecated due to GDPR compatibility where we needed to verify that the user had accepted agreements",
            ReplaceWith("this.resume(userId, object : ResultCallback<User> {})"))
    fun resume(userId: String): User? {
        val session = sessions.find { it.userId == userId }

        return session?.let {
            User(it.token, true)
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
        val session = sessions.find { it.userId == userId }
        if (session == null) {
            callback.onError(ClientError(ClientError.ErrorType.SESSION_NOT_FOUND, "Could not find a previous session for the provided user id"))
        } else {
            val user = User(session.token, true)
            user.agreements.ensureAccepted(ResultCallback.fromLambda({ callback.onError(it) }) {
                callback.onSuccess(user)
            })
        }
    }

    /**
     * Resume the most recently active user session. The session will be resumed and the required checks will
     * be done before a user object is returned through the contract
     * @return The user object of the resumed session. Can be null
     */
    @Deprecated("Deprecated due to GDPR compatibility where we needed to verify that the user had accepted agreements",
            ReplaceWith("this.resumeLast(object : ResultCallback<User> {})"))
    fun resumeLast(): User? {
        val lastActiveSession = sessions.sortedByDescending { it.lastActive }.firstOrNull()?.token ?: readTokenCompat()

        return lastActiveSession?.let {
            User(it, true)
        }
    }

    /**
     * Resume the most recently active user session. The session will be resumed and the required checks will
     * be done before a user object is returned through the contract
     * @param callback The callback to which hte user is provided
     * @return The user object of the resumed session. Can be null
     */
    fun resumeLast(callback: ResultCallback<User>) {
        val lastActiveSession = sessions.sortedByDescending { it.lastActive }.firstOrNull()?.token ?: readTokenCompat()

        if (lastActiveSession == null) {
            callback.onError(ClientError(ClientError.ErrorType.SESSION_NOT_FOUND, "Could not find any previous sessions"))
        } else {
            val user = User(lastActiveSession, true)
            user.agreements.ensureAccepted(ResultCallback.fromLambda({ callback.onError(it) }) {
                callback.onSuccess(user)
            })
        }
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
     * @return Tru if the session was successfully persisted
     */
    fun persist(user: User) {
        val token = user.token

        when {
            token == null -> Logger.warn(Logger.DEFAULT_TAG, { "Attempting to persist session, but the user was logged out" })
            !user.isPersistable -> Logger.warn(Logger.DEFAULT_TAG, { "Attempting to persist session, but the user is not flagged as persistable" })
            else -> {
                val updatedSessions = (sessions.filterNot { it.userId == user.userId.id }) +
                        Session(System.currentTimeMillis(), user.userId.id, token)
                this.sessions = updatedSessions.sortedByDescending { it.lastActive }.take(MAX_SESSIONS)
            }
        }

        clearTokenCompat()
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
    }
}

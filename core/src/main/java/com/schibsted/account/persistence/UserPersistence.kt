package com.schibsted.account.persistence

import android.content.Context
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.UserToken
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.model.error.ClientError.ErrorType.SESSION_NOT_FOUND
import com.schibsted.account.session.User
import com.schibsted.account.util.KeyValueStore

/**
 * Handles persisting and resuming user's sessions. This supports multiple users,
 * so you can resume a specific user's session if required.
 */
internal class UserPersistence(sessionStorage: SessionStorageDelegate,
                               private val resumeDelegate: ResumeDelegate,
                               private val kvs: LegacyKeyValueStore) {

    constructor(context: Context) : this(
            SessionStorageDelegate(context, PREFERENCE_FILENAME),
            ResumeDelegate(context),
            LegacyKeyValueStore(KeyValueStore(context))
    )

    internal data class Session(val lastActive: Long, val userId: String, val token: UserToken)

    private var sessions: List<Session> by sessionStorage

    /**
     * Remove a user session from persistence. Use this to allow users to be forgotten.
     *
     * @param userId The user ID to remove
     */
    fun remove(userId: String) {
        sessions = sessions.filterNot { it.userId == userId }
    }

    /**
     * Removes the oldest active user session.
     */
    fun removeLast() {
        sessions = sessions.sortedByDescending { it.lastActive }.dropLast(1)
    }

    /**
     * Removes all persisted sessions.
     */
    fun removeAll() {
        sessions = emptyList()
    }

    /**
     * Persist a user session so that it can be resumed at a later point.
     *
     * @param user The user to persist
     */
    fun persist(user: User) {
        kvs.clearToken()
        val token: UserToken? = user.token?.takeIf { it.isValidToken() }
        when {
            token == null -> Logger.warn("Attempting to persist session, but the user was logged out")
            !user.isPersistable -> Logger.warn("Attempting to persist session, but the user is not flagged as persistable")
            else -> {
                val session = Session(System.currentTimeMillis(), user.userId.id, token)
                sessions = sessions.asSequence()
                        .filterNot { it.userId == user.userId.id } // skip current user
                        .plusElement(session)
                        .sortedByDescending { it.lastActive }
                        .take(MAX_SESSIONS)
                        .toList()
            }
        }
    }

    /**
     * Resume a specific user's session. The session will be resumed and
     * the required checks will be done before a user object is returned through the contract.
     *
     * @param userId The user ID of the session to resume
     * @param callback The callback to which hte user is provided
     * @return The user object of the resumed session. Can be null
     */
    fun resume(userId: String, callback: ResultCallback<User>) {
        cleanInvalidTokens()
        val token: UserToken? = sessions.find { it.userId == userId } ?.token
        resumeSession(token, callback)
    }

    /**
     * Resume the most recently active user session. The session will be resumed and
     * the required checks will be done before a user object is returned through the contract.
     *
     * @param callback The callback to which hte user is provided
     * @return The user object of the resumed session. Can be null
     */
    fun resumeLast(callback: ResultCallback<User>) {
        cleanInvalidTokens()
        val token: UserToken? = sessions.maxBy { it.lastActive } ?.token ?: kvs.readToken()
        resumeSession(token, callback)
    }

    private fun resumeSession(token: UserToken?, callback: ResultCallback<User>) {
        if (token != null) {
            val success = { user: User -> callback.onSuccess(user) }
            val failure = { error: ClientError -> callback.onError(error) }
            resumeDelegate.proceed(token, success, failure)

        } else {
            val error = ClientError(SESSION_NOT_FOUND, "Could not find a session to resume")
            callback.onError(error)
        }
    }

    private fun cleanInvalidTokens() {
        val (valid, invalid) = sessions.partition { it.token.isValidToken() }
        sessions = valid
        invalid.forEach {
            Logger.warn("Found invalid session for user ${it.userId}")
        }
    }

    companion object {
        private const val PREFERENCE_FILENAME = "IDENTITY_PREFERENCES"
        private const val MAX_SESSIONS = 10
    }
}

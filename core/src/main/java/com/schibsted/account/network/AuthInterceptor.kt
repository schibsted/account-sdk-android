/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.network

import android.os.ConditionVariable
import android.support.annotation.VisibleForTesting
import com.schibsted.account.common.util.Logger
import com.schibsted.account.common.util.safeUrl
import com.schibsted.account.model.UserToken
import com.schibsted.account.session.User
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * Creates an interceptor which will do authenticated requests to whitelisted urls. By default, requests to non-whitelisted
 * domains will be rejected as well as non-https requests. This can be overridden using [allowNonWhitelistedDomains] and
 *  [allowNonHttps]. Will throw an [IllegalArgumentException] if whitelisted domains are not over HTTPS if non-https is not allowed
 * @param user The user session to bind
 * @param urlWhitelist The whitelist of URLs which be authenticated against
 * @param allowNonWhitelistedDomains By default, all non-whitelisted domains will be rejected unless this is set to true.
 * Please note that the auth header will not be injected for any domains not in the whitelist. Defaults to false
 * @param allowNonHttps Whether or not non-https domains should be allowed. Defaults to false
 * @param timeout The timeout for token refreshing
 */
class AuthInterceptor internal constructor(
        private val user: User,
        private val urlWhitelist: List<String>,
        private val allowNonHttps: Boolean = false,
        private val allowNonWhitelistedDomains: Boolean = false,
        private val timeout: Long = 10_000) : Interceptor {

    private val lock = ConditionVariable()
    private val refreshInProgress = AtomicBoolean(false)
    private val activeToken = AtomicReference<UserToken?>(null)
    private val requestNo = AtomicInteger(0)

    init {
        val parsedUrls = urlWhitelist.map {
            HttpUrl.parse(it) ?: throw IllegalArgumentException("Illegal URL format: $it")
        }
        if (!allowNonHttps) {
            parsedUrls.find { !it.isHttps }?.let {
                throw IllegalArgumentException("Authenticated requests can only be done over HTTPS unless specifically allowed")
            }
        }

        activeToken.set(user.token)
    }

    private fun urlInWhitelist(url: HttpUrl): Boolean = urlWhitelist.find { url.toString().startsWith(it) } != null

    private fun logAndThrow(message: String) {
        Logger.error(TAG, message)
        throw AuthException(message)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    internal fun protocolCheck(req: Request, reqId: Int) {
        if (!req.url().isHttps && !this.allowNonHttps) {
            logAndThrow("Authenticated request (ReqId:$reqId) failed: Request protocol is not HTTPS")
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    internal fun whitelistCheck(req: Request, reqId: Int) {
        if (!urlInWhitelist(req.url()) && !this.allowNonWhitelistedDomains) {
            logAndThrow("Authenticated request (ReqId:$reqId) failed: Authenticated requests can only be done to the specified urls, unless specifically enabled: ${urlWhitelist.joinToString { ", " }}")
        }
    }

    @Throws(AuthException::class, IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url()
        val reqId = requestNo.getAndIncrement()

        Logger.verbose(TAG, { "Attempting to perform authenticated request (ReqId:$reqId) to ${originalUrl.toString().safeUrl()}" })

        protocolCheck(originalRequest, reqId)
        whitelistCheck(originalRequest, reqId)

        Logger.verbose(TAG, { "Security checks passed for request (ReqId:$reqId) to ${originalUrl.toString().safeUrl()}" })

        val token = activeToken.get()
        if (token == null) {
            Logger.error(TAG, { "Unable to perform authenticated request (ReqId:$reqId) to ${originalUrl.toString().safeUrl()}. Reason: User logged out" })
            throw AuthException("Unable to perform authenticated request (ReqId:$reqId) to ${originalUrl.toString().safeUrl()}. Reason: User logged out")
        }

        val request = with(originalRequest.newBuilder()) {
            if (urlInWhitelist(originalUrl)) {
                addAuthHeaderIfNeeded(originalRequest, token)
            } else {
                removeHeader("Authorization")
                Logger.info(TAG, { "URL is not whitelisted, not attaching Authorization header for request (ReqId:$reqId)" })
            }
            build()
        }

        val response = chain.proceed(request)

        return if (response.code() == 401) {
            Logger.verbose(TAG, { "Request (ReqId:$reqId) returned 401, checking if token should be refreshed" })

            when {
                !urlInWhitelist(response.request().url()) -> {
                    Logger.verbose(TAG, { "Not refreshing token for request (ReqId: $reqId), as the URL is not whitelisted" })
                    response
                }
                response.request().url() != originalUrl -> {
                    Logger.verbose(TAG, { "Not refreshing token, as the current URL(${response.request().url().toString().safeUrl()}) does not match the original ${originalUrl.toString().safeUrl()}" })
                    response
                }
                activeToken.get() == null -> {
                    Logger.verbose(TAG, { "Not refreshing token for request (ReqId: $reqId), as the user is not logged in" })
                    response
                }
                else -> {
                    Logger.verbose(TAG, { "Found that token should be refreshed for request (ReqId:$reqId)" })
                    refreshToken(response, chain, reqId)
                }
            }
        } else {
            response
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    internal fun refreshToken(failedResponse: Response, chain: Interceptor.Chain, reqId: Int): Response =
            when {
                refreshInProgress.compareAndSet(false, true) -> {
                    lock.close()
                    Logger.verbose(TAG, { "Refreshing token from request (ReqId:$reqId)" })

                    val refreshResult = user.refreshToken()
                    val newToken = user.token

                    val resp = if (refreshResult && newToken != null) {
                        activeToken.set(newToken)
                        Logger.verbose(TAG, { "Re-firing request (ReqId:$reqId) after token refreshing" })
                        chain.proceed(failedResponse.request().replaceAuthHeader(newToken))
                    } else {
                        activeToken.set(null)
                        Logger.error(TAG, { "Token refresh failed (ReqId:$reqId)" })
                        failedResponse
                    }
                    refreshInProgress.set(false)
                    lock.open()
                    resp
                }
                lock.block(timeout) && activeToken.get() != null -> {
                    Logger.verbose(TAG, { "Re-firing request (ReqId:$reqId) after waiting for token refreshing" })
                    val newToken = requireNotNull(activeToken.get(), { "Re-firing request after token refresh, but some other thread unset the token in the meantime" })
                    chain.proceed(failedResponse.request().replaceAuthHeader(newToken))
                }
                else -> {
                    Logger.verbose(TAG, { "Refreshing token failed or timed out, this request (ReqId:$reqId) fail as well" })
                    failedResponse
                }
            }

    companion object {
        val TAG = Logger.DEFAULT_TAG + "-ICPT"

        @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
        internal fun Request.replaceAuthHeader(userToken: UserToken): Request =
                this.newBuilder().header("Authorization", userToken.bearerAuthHeader()).build()

        @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
        internal fun Request.Builder.addAuthHeaderIfNeeded(request: Request, userToken: UserToken): Request.Builder {
            return if (request.header("Authorization") == null) {
                this.addHeader("Authorization", userToken.bearerAuthHeader())
            } else {
                this
            }
        }
    }
}

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network

import android.os.ConditionVariable
import android.support.annotation.VisibleForTesting
import com.schibsted.account.common.util.Logger
import com.schibsted.account.common.util.safeUrl
import com.schibsted.account.session.User
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

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
    private val timeout: Long = 10_000,
    private val authSecurityCheck: AuthSecurityCheck = AuthSecurityCheck.default(urlWhitelist, allowNonHttps, allowNonWhitelistedDomains),
    private val authResponseCheck: AuthResponseCheck = AuthResponseCheck.default(urlWhitelist)
) : Interceptor {

    private val lock = ConditionVariable()
    private val refreshInProgress = AtomicBoolean(false)
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
    }

    private fun urlInWhitelist(url: HttpUrl): Boolean = urlWhitelist.find { url.toString().startsWith(it) } != null

    @Throws(AuthException::class, IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url()
        val reqId = requestNo.getAndIncrement()

        Logger.verbose(TAG, { "Attempting to perform authenticated request (ReqId:$reqId) to ${originalUrl.toString().safeUrl()}" })

        // Preform security checks
        val securityCheckResult = authSecurityCheck.securityCheck(originalRequest)
        if ( securityCheckResult is AuthCheckResult.Failed ) {
            Logger.error(TAG, { "Cannot perform authenticated request (ReqId:$reqId): ${securityCheckResult.reason}" })
            throw AuthException("Cannot perform authenticated request (ReqId:$reqId): ${securityCheckResult.reason}")
        }
        Logger.verbose(TAG, { "Security checks passed for request (ReqId:$reqId)" })

        val token = user.token ?: throw AuthException("Cannot perform authenticated request (ReqId:$reqId) when the user is logged out")

        // Add auth header if the URL is whitelisted
        val request = with(originalRequest.newBuilder()) {
            if (urlInWhitelist(originalUrl)) {
                addHeader("Authorization", token.bearerAuthHeader())
            }
            build()
        }

        // TODO: Eagerly refresh

        val response = chain.proceed(request)

        if (response.code() == 401) {
            Logger.verbose(TAG, { "Request (ReqId:$reqId) returned 401, checking if token should be refreshed" })

            val responseCheckResult = authResponseCheck.responseCheck(originalRequest, response)
            return if (responseCheckResult is AuthCheckResult.Failed) {
                Logger.verbose(TAG, { "Not refreshing token for request (ReqId: $reqId).. Reason :${responseCheckResult.reason}" })
                response
            } else {
                Logger.verbose(TAG, { "Found that token should be refreshed for request (ReqId:$reqId)" })
                refreshToken(response, chain, reqId)
            }
        }

        return response
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    internal fun refreshToken(failedResponse: Response, chain: Interceptor.Chain, reqId: Int): Response =
            when {
            // Only the first request should refresh
                refreshInProgress.compareAndSet(false, true) -> {
                    lock.close()
                    Logger.verbose(TAG, { "Refreshing token from request (ReqId:$reqId)" })

                    val refreshResult = user.refreshToken()
                    val newToken = user.token

                    val resp = if (refreshResult && newToken != null) {
                        Logger.verbose(TAG, { "Re-firing request (ReqId:$reqId) after token refreshing" })
                        chain.proceed(failedResponse.request().newBuilder().header("Authorization", newToken.bearerAuthHeader()).build())
                    } else {
                        Logger.error(TAG, { "Token refresh failed (ReqId:$reqId)" })
                        failedResponse
                    }
                    refreshInProgress.set(false)
                    lock.open()
                    resp
                }
            // All subsequent refresh requests will wait instead
                lock.block(timeout) -> {
                    val newToken = user.token
                    if (newToken != null) {
                        Logger.verbose(TAG, { "Re-firing request (ReqId:$reqId) after waiting for token refreshing" })
                        chain.proceed(failedResponse.request().newBuilder().header("Authorization", newToken.bearerAuthHeader()).build())
                    } else {
                        Logger.verbose(TAG, { "Auth token was null after waiting for refreshing. This request (ReqId:$reqId) will fail" })
                        failedResponse
                    }
                }
            // The previous check will return false on timeout, calling this block
                else -> {
                    Logger.verbose(TAG, { "Refreshing token timed out, failing this request (ReqId:$reqId)" })
                    failedResponse
                }
            }

    companion object {
        const val TAG = Logger.DEFAULT_TAG + "-ICPT"
    }
}

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.schibsted.account.ClientConfiguration
import com.schibsted.account.common.util.Logger
import com.schibsted.account.model.UserToken
import com.schibsted.account.session.User
import com.schibsted.account.test.TestUtil
import io.kotlintest.*
import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.specs.WordSpec
import kotlinx.coroutines.async
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import okhttp3.*

class AuthInterceptorTest : WordSpec() {
    private val userToken = UserToken(null, "userId", "eyJ0eXAiOiJKV1MiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczpcL1wvaWRlbnRpdHktcHJlLnNjaGlic3RlZC5jb21cLyIsImNsYXNzIjoidG9rZW4uT0F1dGhVc2VyQWNjZXNzVG9rZW4iLCJleHAiOjE1MTA5MTgyNTMsImlhdCI6MTUxMDMxMzQ1MywianRpIjoiODI5Mzc2MDYtMjRiZS00OWMxLWJjZTktNzgzOTgwYWUyZDNiIiwic3ViIjoiZTA2MTYyNzAtMjA5Mi01ZTlkLTg1NmItNDhlMDY1ZDQ4OTlmIiwic2NvcGUiOiIiLCJ1c2VyX2lkIjoiMTEwOTk0NjQiLCJhenAiOiI1OGNmZjk4ZjE3ZTU5Njg2MTU4YjQ1NjciLCJjbGllbnRfaWQiOiI1OGNmZjk4ZjE3ZTU5Njg2MTU4YjQ1NjcifQ.gS0h44PX42hwv6P7TYjaR4Dskl3X0lT716-_iW_Wd2E",
            "eyJ0eXAiOiJKV1MiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczpcL1wvaWRlbnRpdHktcHJlLnNjaGlic3RlZC5jb21cLyIsImNsYXNzIjoidG9rZW4uT0F1dGhVc2VyUmVmcmVzaFRva2VuIiwiZXhwIjoxNTEyMTI3ODUzLCJpYXQiOjE1MTAzMTM0NTMsImp0aSI6Ijc2ODBjYTgwLTZjNzAtNGQ5YS04OTdlLTUzNmM0ZTQyYzM1ZCIsInN1YiI6ImUwNjE2MjcwLTIwOTItNWU5ZC04NTZiLTQ4ZTA2NWQ0ODk5ZiIsImFqdGkiOiI4MjkzNzYwNi0yNGJlLTQ5YzEtYmNlOS03ODM5ODBhZTJkM2IiLCJhenAiOiI1OGNmZjk4ZjE3ZTU5Njg2MTU4YjQ1NjciLCJjbGllbnRfaWQiOiI1OGNmZjk4ZjE3ZTU5Njg2MTU4YjQ1NjciLCJzY29wZSI6IiIsInVzZXJfaWQiOiIxMTA5OTQ2NCJ9.5nVYDsdJydFZRSawDWZ44SKJzHup0OUcAJZ8VmD_Hzw",
            "", "Bearer", 604800)

    private val respBuilder = okhttp3.Response.Builder()
            .message("message")
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("https://example.com")
                    .header("Authorization", "initialHeaderValue")
                    .build())
            .body(ResponseBody.create(MediaType.parse("application/json"), "{}"))

    private val defaultMockUser: User = mock {
        on { token }.thenReturn(userToken)
    }

    private val failedResp = respBuilder.code(401).build()
    private val sucessfulResp = respBuilder.code(200).build()

    init {
        Logger.logWorker = TestUtil.testLogger
        ClientConfiguration.set(ClientConfiguration("http://myenv.com", "id", "secret"))
        "interceptor" should {
            "not allow non-HTTPS schemes if not specifically allowed" {
                shouldThrow<IllegalArgumentException> {
                    AuthInterceptor(mock(), listOf("http://example.com"), allowNonHttps = false)
                }
            }

            "allow non-HTTPS schemes if specified" {
                AuthInterceptor(mock(), listOf("http://example.com"), allowNonHttps = true)
            }
        }

        "intercept" should {
            val mockUrl: HttpUrl = mock {
                on { toString() }.thenReturn("https://example.com")
            }

            val mockRequest: Request = mock {
                on { url() }.thenReturn(mockUrl)
            }

            val mockChain: Interceptor.Chain = mock {
                on { request() }.thenReturn(mockRequest)
            }

            val mockUser: User = mock {
                on { refreshToken() }.thenReturn(true)
                on { token }.thenReturn(userToken)
            }

            "throw an exception when the URL is not whitelisted" {
                shouldThrow<AuthException> {
                    val icpt = AuthInterceptor(mock(), listOf("https://www.some-other-example.com"))
                    icpt.intercept(mockChain)
                }
            }

            "throw an AuthException if the user is logged out" {
                val mockedUserLoggedOut: User = mock {
                    on { token }.thenReturn(null)
                }

                val icpt = AuthInterceptor(mockedUserLoggedOut, listOf("https://example.com"))
                shouldThrow<AuthException> {
                    icpt.intercept(mockChain)
                }
            }

            "override existing auth headers" {
                val req = Request.Builder().url("https://example.com")
                        .header("Authorization", "originalValue")
                        .build()

                val mockChainWithAuthHeader: Interceptor.Chain = mock {
                    on { request() }.thenReturn(req)
                    on { proceed(any()) }.then {
                        val inReq: Request = it.getArgument<Request>(0)
                        respBuilder.request(inReq).code(200).build()
                    }
                }

                val icpt = AuthInterceptor(mockUser, listOf("https://example.com"))
                val res = icpt.intercept(mockChainWithAuthHeader)
                res.request().header("Authorization") shouldNotBe "originalValue"
            }

            "add auth header if needed" {
                val req = Request.Builder().url("https://example.com")
                        .build()

                val mockChainWithoutAuthHeader: Interceptor.Chain = mock {
                    on { request() }.thenReturn(req)
                    on { proceed(any()) }.then {
                        val inReq: Request = it.getArgument<Request>(0)
                        respBuilder.request(inReq).code(200).build()
                    }
                }

                val icpt = AuthInterceptor(mockUser, listOf("https://example.com"))
                val res = icpt.intercept(mockChainWithoutAuthHeader)
                res.request().header("Authorization") shouldBe userToken.bearerAuthHeader()
            }

            "call refreshToken on a 401 response" {
                val req = Request.Builder().url("https://example.com")
                        .build()

                val mockChainWithoutAuthHeader: Interceptor.Chain = mock {
                    on { request() }.thenReturn(req)
                    on { proceed(any()) }.thenReturn(failedResp)
                }

                val mockUserForRefresh: User = mock {
                    on { refreshToken() }.thenReturn(true)
                    on { token }.thenReturn(userToken)
                }

                val icpt = AuthInterceptor(mockUserForRefresh, listOf("https://example.com"))
                icpt.intercept(mockChainWithoutAuthHeader)

                verify(mockUserForRefresh).refreshToken()
            }

            "not add token to a non-whitelisted domain, but allow the request" {
                val req = Request.Builder().url("https://example.com").build()

                val mockChainWithoutAuthHeader: Interceptor.Chain = mock {
                    on { request() }.thenReturn(req)
                    on { proceed(any()) }.then {
                        val inReq: Request = it.getArgument<Request>(0)
                        respBuilder.request(inReq).code(200).build()
                    }
                }

                val icpt = AuthInterceptor(mockUser, listOf(), allowNonWhitelistedDomains = true)
                val res = icpt.intercept(mockChainWithoutAuthHeader)
                res.request().header("Authorization") shouldBe null
                res.code() shouldBe 200
            }

            "strip the Authorization header for non-whitelisted domains, but allow the request" {
                val req = Request.Builder().url("https://example.com").build()

                val mockChainWithoutAuthHeader: Interceptor.Chain = mock {
                    on { request() }.thenReturn(req)
                    on { proceed(any()) }.then {
                        val inReq: Request = it.getArgument<Request>(0)
                        respBuilder.request(inReq)
                                .code(200)
                                .header("Authorization", "myAuthValue")
                                .build()
                    }
                }

                val icpt = AuthInterceptor(mockUser, listOf(), allowNonWhitelistedDomains = true)
                val res = icpt.intercept(mockChainWithoutAuthHeader)
                res.request().header("Authorization") shouldBe null
                res.code() shouldBe 200
            }
        }

        "refreshToken" should {
            "refresh token when no refresh is in progress" {
                val mockChain: Interceptor.Chain = mock {
                    on { proceed(any()) }.thenReturn(sucessfulResp)
                }

                val mockUser: User = mock {
                    on { refreshToken() }.thenReturn(true)
                    on { token }.thenReturn(userToken)
                }

                val icpt = AuthInterceptor(mockUser, listOf("https://example.com"))
                icpt.refreshToken(failedResp, mockChain, 1)

                verify(mockUser, times(1)).refreshToken()
                verify(mockChain, times(1)).proceed(argThat {
                    header("Authorization") == userToken.bearerAuthHeader()
                })
            }

            "await execution when there is an on-going refresh and only refresh once" {
                val mockChain: Interceptor.Chain = mock {
                    on { proceed(any()) }.thenReturn(sucessfulResp)
                }

                val mockUserWithSlowResponse: User = mock {
                    on { refreshToken() }.then {
                        Thread.sleep(1000)
                        true
                    }
                    on { token }.thenReturn(userToken)
                }

                val icpt = AuthInterceptor(mockUserWithSlowResponse, listOf("https://example.com"))
                val refreshes = (1..3).map { async(newFixedThreadPoolContext(4, "MyContext")) { icpt.refreshToken(failedResp, mockChain, it) } }

                runBlocking {
                    refreshes.map { it.await() }
                }

                verify(mockUserWithSlowResponse, times(1)).refreshToken()
                verify(mockChain, times(3)).proceed(argThat {
                    header("Authorization") == userToken.bearerAuthHeader()
                })
            }

            "return the original response if it times out" {
                val mockChain: Interceptor.Chain = mock {
                    on { proceed(any()) }.thenReturn(sucessfulResp)
                }

                val mockUserWithSlowResponse: User = mock {
                    on { refreshToken() }.then {
                        Thread.sleep(1000)
                        false
                    }
                    on { token }.thenReturn(userToken)
                }

                val originalResp = failedResp.newBuilder().header("Authorization", "authHeaderValue").build()

                val icpt = AuthInterceptor(mockUserWithSlowResponse, listOf("https://example.com"), timeout = 50)
                val refreshes = (1..3).map { async(newFixedThreadPoolContext(4, "MyContext")) { icpt.refreshToken(originalResp, mockChain, it) } }

                runBlocking {
                    val responses = refreshes.map { it.await() }

                    forAll(responses) { resp ->
                        resp.header("Authorization") shouldBe "authHeaderValue"
                    }
                }

                verify(mockUserWithSlowResponse, times(1)).refreshToken()
                verify(mockChain, never()).proceed(any())
            }
        }

        "whitelistCheck" should {
            "not throw an exception if url is whitelisted" {
                val req = Request.Builder().url("https://example.com").build()
                val check = checkUrlInWhitelist(listOf("https://example.com"), false)
                check.validate(req) shouldBe AuthCheck.AuthCheckResult.Passed
            }

            "throw an AuthException if url is not whitelisted" {
                val req = Request.Builder().url("https://another-example.com").build()
                val check = checkUrlInWhitelist(listOf("https://example.com"), false)
                check.validate(req) should beInstanceOf(AuthCheck.AuthCheckResult.Failed::class)
            }
        }

        "protocolCheck" should {
            "not throw an exception if url is using the HTTPS protocol" {
                val req = Request.Builder().url("https://example.com").build()
                protocolCheck(false).validate(req) shouldBe AuthCheck.AuthCheckResult.Passed
            }

            "throw an AuthException if url is not whitelisted" {
                val req = Request.Builder().url("http://example.com").build()
                protocolCheck(false).validate(req) should beInstanceOf(AuthCheck.AuthCheckResult.Failed::class)
            }
        }
    }
}

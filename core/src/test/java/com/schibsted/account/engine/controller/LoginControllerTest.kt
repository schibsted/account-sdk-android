/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.controller

import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import com.schibsted.account.AccountService
import com.schibsted.account.ClientConfiguration
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.integration.CallbackProvider
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.engine.integration.contract.LoginContract
import com.schibsted.account.engine.step.StepLoginIdentify
import com.schibsted.account.model.LoginResult
import com.schibsted.account.model.NoValue
import com.schibsted.account.model.UserId
import com.schibsted.account.model.UserToken
import com.schibsted.account.model.error.ClientError
import com.schibsted.account.network.NetworkCallback
import com.schibsted.account.network.OIDCScope
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.response.UserTokenResponse
import com.schibsted.account.network.service.authentication.OAuthService
import com.schibsted.account.session.User
import com.schibsted.account.test.TestUtil
import io.kotlintest.fail
import io.kotlintest.specs.WordSpec
import retrofit2.Call

class LoginControllerTest : WordSpec({
    ClientConfiguration.set(ClientConfiguration("https://example.com", "id", "secret"))
    AccountService.localBroadcastManager = null
    val userTokenResponse = Gson().fromJson(TestUtil.readResource("json/user_token.json"), UserTokenResponse::class.java)

    fun getMockContract(callback: ResultCallback<LoginResult>): LoginContract = mock {
        on { onCredentialsRequested(any()) }.then {
            it.getArgument<InputProvider<Credentials>>(0)
                    .provide(Credentials(Identifier(Identifier.IdentifierType.EMAIL, "someone@example.com"), "password", true),
                            object : ResultCallback<NoValue> {
                                override fun onSuccess(result: NoValue) {}
                                override fun onError(error: ClientError) {
                                    fail("An error occurred: $error")
                                }
                            })
        }

        on { onFlowReady(any()) }.then {
            val provider: CallbackProvider<LoginResult> = it.getArgument<CallbackProvider<LoginResult>>(0)
            provider.provide(callback)
            Unit
        }
    }

    val mockCall: Call<UserTokenResponse> = mock {
        on { enqueue(any()) }.then { it.getArgument<NetworkCallback<UserTokenResponse>>(0).onSuccess(userTokenResponse) }
    }
    val mockOAuthService: OAuthService = mock {
        on { tokenFromPassword(any(), any(), any(), any(), any()) }.thenReturn(mockCall)
    }
    ClientConfiguration.set(ClientConfiguration("https://example.com", "id", "secret"))
    ServiceHolder.oAuthService = mockOAuthService

    Logger.loggingEnabled = false

    "calling perform" should {
        "perform tasks resulting in a user" {
            val controller = LoginController(false)
            val mockCallback: ResultCallback<LoginResult> = mock()
            val mockContract = getMockContract(mockCallback)

            controller.evaluate(mockContract)

            verify(mockContract).onCredentialsRequested(any())
            verify(mockContract).onFlowReady(any())
            verify(mockCallback).onSuccess(argWhere { it.user.userId.legacyId == userTokenResponse.userId })
        }

        "skip all tasks when all information is available" {
            val controller = LoginController(false, arrayOf(OIDCScope.SCOPE_OPENID))
            val userToken = UserToken(userTokenResponse)
            val userId = UserId.fromUserTokenResponse(userTokenResponse)
            controller.navigation.push(StepLoginIdentify(
                    Credentials(Identifier(Identifier.IdentifierType.EMAIL, "some@some.com"), "password", true),
                    User(userToken, userId, isPersistable = true),
                    true,
                    setOf()
            ))

            val mockCallback: ResultCallback<LoginResult> = mock()
            val mockContract = getMockContract(mockCallback)
            controller.evaluate(mockContract)

            verify(mockContract, never()).onCredentialsRequested(any())
            verify(mockCallback).onSuccess(argWhere { it.user.userId.legacyId == userTokenResponse.userId })
        }

        "verify the user if specified" {
            val controller = LoginController(true, arrayOf(OIDCScope.SCOPE_OPENID))
            val userToken = UserToken(userTokenResponse)
            val userId = UserId.fromUserTokenResponse(userTokenResponse)
            controller.navigation.push(StepLoginIdentify(
                    Credentials(Identifier(Identifier.IdentifierType.EMAIL, "some@some.com"), "password", true),
                    User(userToken, userId, isPersistable = true),
                    false,
                    setOf(), mock()
            ))

            val mockContract = getMockContract(mock())
            controller.evaluate(mockContract)

            verify(mockContract, never()).onCredentialsRequested(any())
            verify(mockContract).onAgreementsRequested(any(), any())
        }
    }
})

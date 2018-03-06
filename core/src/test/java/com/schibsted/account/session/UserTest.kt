/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.session

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.schibsted.account.model.UserToken
import com.schibsted.account.network.ServiceHolder
import com.schibsted.account.network.service.authentication.OAuthService
import com.schibsted.account.network.service.user.UserService
import com.schibsted.account.test.MockedCall
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.WordSpec
import retrofit2.Response

class UserTest : WordSpec({
    val userToken = UserToken(null, "userId", "eyJ0eXAiOiJKV1MiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczpcL1wvaWRlbnRpdHktcHJlLnNjaGlic3RlZC5jb21cLyIsImNsYXNzIjoidG9rZW4uT0F1dGhVc2VyQWNjZXNzVG9rZW4iLCJleHAiOjE1MTA5MTgyNTMsImlhdCI6MTUxMDMxMzQ1MywianRpIjoiODI5Mzc2MDYtMjRiZS00OWMxLWJjZTktNzgzOTgwYWUyZDNiIiwic3ViIjoiZTA2MTYyNzAtMjA5Mi01ZTlkLTg1NmItNDhlMDY1ZDQ4OTlmIiwic2NvcGUiOiIiLCJ1c2VyX2lkIjoiMTEwOTk0NjQiLCJhenAiOiI1OGNmZjk4ZjE3ZTU5Njg2MTU4YjQ1NjciLCJjbGllbnRfaWQiOiI1OGNmZjk4ZjE3ZTU5Njg2MTU4YjQ1NjcifQ.gS0h44PX42hwv6P7TYjaR4Dskl3X0lT716-_iW_Wd2E",
            "eyJ0eXAiOiJKV1MiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczpcL1wvaWRlbnRpdHktcHJlLnNjaGlic3RlZC5jb21cLyIsImNsYXNzIjoidG9rZW4uT0F1dGhVc2VyUmVmcmVzaFRva2VuIiwiZXhwIjoxNTEyMTI3ODUzLCJpYXQiOjE1MTAzMTM0NTMsImp0aSI6Ijc2ODBjYTgwLTZjNzAtNGQ5YS04OTdlLTUzNmM0ZTQyYzM1ZCIsInN1YiI6ImUwNjE2MjcwLTIwOTItNWU5ZC04NTZiLTQ4ZTA2NWQ0ODk5ZiIsImFqdGkiOiI4MjkzNzYwNi0yNGJlLTQ5YzEtYmNlOS03ODM5ODBhZTJkM2IiLCJhenAiOiI1OGNmZjk4ZjE3ZTU5Njg2MTU4YjQ1NjciLCJjbGllbnRfaWQiOiI1OGNmZjk4ZjE3ZTU5Njg2MTU4YjQ1NjciLCJzY29wZSI6IiIsInVzZXJfaWQiOiIxMTA5OTQ2NCJ9.5nVYDsdJydFZRSawDWZ44SKJzHup0OUcAJZ8VmD_Hzw",
            "", "Bearer", 604800)

    "ServiceHolder's services" should {
        val user = User(userToken, true)

        "be reset when token is set to null" {
            val userService = mock<UserService> {
                on { logout(any()) }.thenReturn(MockedCall<Unit>(Response.success(Unit)))
            }

            ServiceHolder.overrideService(userService)
            ServiceHolder.userService(user) shouldBe userService

            user.logout(null)
            verify(userService).logout(any())

            ServiceHolder.clientService() shouldNotBe userService
        }

        "not be reset when token is not null" {
            val oAuthService = mock<OAuthService> {
                on { refreshToken(any(), any(), any()) }.thenReturn(MockedCall(Response.success(userToken)))
            }

            ServiceHolder.overrideService(oAuthService)

            user.refreshToken()
            verify(oAuthService).refreshToken(any(), any(), any())

            ServiceHolder.oAuthService() shouldBe oAuthService
        }
    }
})

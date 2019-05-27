/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.service.user

import com.schibsted.account.ListContainer
import com.schibsted.account.network.response.AcceptAgreementResponse
import com.schibsted.account.network.response.AgreementsResponse
import com.schibsted.account.network.response.ApiContainer
import com.schibsted.account.network.response.ProductAccess
import com.schibsted.account.network.response.ProfileData
import com.schibsted.account.network.response.RequiredFieldsResponse
import com.schibsted.account.network.response.Subscription
import com.schibsted.account.network.response.TokenResponse
import com.schibsted.account.network.response.DeviceFingerprint
import com.schibsted.account.network.service.BaseNetworkService
import okhttp3.OkHttpClient
import retrofit2.Call

class UserService(environment: String, okHttpClient: OkHttpClient) : BaseNetworkService(environment, okHttpClient) {
    private val userContract: UserContract = createService(UserContract::class.java)

    fun getUserAgreements(userId: String, userToken: TokenResponse): Call<ApiContainer<AgreementsResponse>> {
        return this.userContract.agreements(userToken.bearerAuthHeader(), userId)
    }

    fun acceptUserAgreements(userId: String, userToken: TokenResponse): Call<ApiContainer<AcceptAgreementResponse>> {
        return this.userContract.agreementAccept(userToken.bearerAuthHeader(), userId)
    }

    fun getMissingRequiredFields(userId: String, userToken: TokenResponse): Call<ApiContainer<RequiredFieldsResponse>> {
        return this.userContract.requiredFields(userToken.bearerAuthHeader(), userId)
    }

    fun getSubscriptions(userToken: TokenResponse, userId: String): Call<ListContainer<Subscription>> {
        return this.userContract.subscriptions(userToken.bearerAuthHeader(), userId)
    }

    /**
     * Updates the user's profile data
     * @param userId The user's ID, this must match the one in the user token
     * @param userToken The user's access token
     * @return Success if okay, failure otherwise
     */
    fun updateUserProfile(userId: String, userToken: TokenResponse, profileData: Map<String, Any>): Call<Unit> {
        return userContract.updateUserProfile(userToken.bearerAuthHeader(), userId, profileData)
    }

    /**
     * Retrieves the user data from Schibsted account
     * @param userId The user's ID, must match the one in the token
     * @param userToken The user's access token
     * @return On success it will return the profile data, failure if something went wrong
     */
    fun getUserProfile(userId: String, userToken: TokenResponse): Call<ApiContainer<ProfileData>> {
        return this.userContract.getUserProfile(userToken.serializedAccessToken, userId)
    }

    /**
     * Checks whether the user has access to the given product.
     * @param userId The user's ID, must match the one in the token
     * @param userToken The user's access token
     * @param productId The product's ID to check (e.g. specific newspaper)
     * @return On success it will return if the user has access, failure if something went wrong or the user doesn't have access
     */
    fun getProductAccess(userToken: TokenResponse, userId: String, productId: String): Call<ApiContainer<ProductAccess>> {
        return this.userContract.getProductAccess(userToken.serializedAccessToken, userId, productId)
    }

    /**
     * Create a device fingerprint for the user's current device.
     * @param userToken The user's access token
     * @return On Success it will return a new device fingerprint, failure otherwise
     */
    fun createDeviceFingerprint(userToken: TokenResponse, deviceData: Map<String, String>): Call<ApiContainer<DeviceFingerprint>> {
        return this.userContract.createDeviceFingerprint(userToken.bearerAuthHeader(), deviceData)
    }
}

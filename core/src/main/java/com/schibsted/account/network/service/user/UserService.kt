/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.service.user

import com.schibsted.account.ListContainer
import com.schibsted.account.network.response.*
import com.schibsted.account.network.service.BaseNetworkService
import okhttp3.OkHttpClient
import retrofit2.Call

class UserService(environment: String, okHttpClient: OkHttpClient) : BaseNetworkService(environment, okHttpClient) {
    private val userContract: UserContract = createService(UserContract::class.java)

    fun getUserAgreements(userId: String, bearerAuthHeader: String): Call<ApiContainer<AgreementsResponse>> {
        return this.userContract.agreements(bearerAuthHeader, userId)
    }

    fun acceptUserAgreements(userId: String, bearerAuthHeader: String): Call<ApiContainer<AcceptAgreementResponse>> {
        return this.userContract.agreementAccept(bearerAuthHeader, userId)
    }

    fun getMissingRequiredFields(userId: String, bearerAuthHeader: String): Call<ApiContainer<RequiredFieldsResponse>> {
        return this.userContract.requiredFields(bearerAuthHeader, userId)
    }

    fun getSubscriptions(bearerAuthHeader: String, userId: String): Call<ListContainer<Subscription>> {
        return this.userContract.subscriptions(bearerAuthHeader, userId)
    }

    /**
     * Updates the user's profile data
     * @param userId The user's ID, this must match the one in the user token
     * @param bearerAuthHeader The user's access token
     * @return Success if okay, failure otherwise
     */
    fun updateUserProfile(userId: String, bearerAuthHeader: String, profileData: Map<String, Any>): Call<Unit> {
        return userContract.updateUserProfile(bearerAuthHeader, userId, profileData)
    }

    /**
     * Retrieves the user data from Schibsted account
     * @param userId The user's ID, must match the one in the token
     * @param bearerAuthHeader The user's access token
     * @return On success it will return the profile data, failure if something went wrong
     */
    fun getUserProfile(userId: String, bearerAuthHeader: String): Call<ApiContainer<ProfileData>> {
        return this.userContract.getUserProfile(bearerAuthHeader, userId)
    }

    /**
     * Checks whether the user has access to the given product.
     * @param userId The user's ID, must match the one in the token
     * @param bearerAuthHeader The user's access token
     * @param productId The product's ID to check (e.g. specific newspaper)
     * @return On success it will return if the user has access, failure if something went wrong or the user doesn't have access
     */
    fun getProductAccess(bearerAuthHeader: String, userId: String, productId: String): Call<ApiContainer<ProductAccess>> {
        return this.userContract.getProductAccess(bearerAuthHeader, userId, productId)
    }

    /**
     * Create a device fingerprint for the user's current device.
     * @param bearerAuthHeader The user's access token
     * @return On Success it will return a new device fingerprint, failure otherwise
     */
    fun createDeviceFingerprint(bearerAuthHeader: String, deviceData: Map<String, String>): Call<ApiContainer<DeviceFingerprint>> {
        return this.userContract.createDeviceFingerprint(bearerAuthHeader, deviceData)
    }
}

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.account.smartlock

import android.content.IntentSender
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.auth.api.credentials.*
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.schibsted.account.common.smartlock.SmartLockCallback
import com.schibsted.account.common.smartlock.Smartlock
import com.schibsted.account.common.util.Logger
import com.schibsted.account.engine.input.Identifier

class SmartlockController(private val activity: AppCompatActivity, private val smartLockCallback: SmartLockCallback) : Smartlock {

    companion object {
        private const val TAG = "SmartlockController"
        /**
         * Request code to send when the user has to choose between multiple account the one to login with.
         */
        private const val RC_CHOOSE_ACCOUNT = 3
        /**
         * Request code to send when the user has to choose which account must be used to pre-fill the identifier field.
         */
        private const val RC_IDENTIFIER_ONLY = 4
        /**
         * Request code to send when the user has to decide if he wants to save his credentials with smartlock.
         */
        private const val RC_SAVE_CREDENTIALS = 5
    }

    var options = CredentialsOptions.Builder()
            .forceEnableSaveDialog()
            .build()
    private val credentialsClient = Credentials.getClient(activity, options)
    private val mCredentialRequest: CredentialRequest = CredentialRequest.Builder()
            .setPasswordLoginSupported(true)
            .setAccountTypes(IdentityProviders.GOOGLE)
            .build()

    private var currentSmartlockCredential: Credential? = null

    override fun requestCredentials() {
        requestCredentials(mCredentialRequest)
    }

    /**
     * Request Credentials from the Credentials API.
     */
    private fun requestCredentials(credentialRequest: CredentialRequest) {

        credentialsClient.request(credentialRequest).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                retrieveCredential(task.result.credential)
            } else {
                val exception = task.exception
                if (exception is ResolvableApiException) {
                    if (exception.statusCode == CommonStatusCodes.SIGN_IN_REQUIRED) {
                        Logger.info(TAG, "cannot silently log-in, providing the account identifier")
                        resolveException(exception, RC_IDENTIFIER_ONLY)
                    } else {
                        Logger.info(TAG, "multiple accounts are found, prompting the user to choose one")
                        resolveException(exception, RC_CHOOSE_ACCOUNT)
                    }
                } else {
                    Logger.info(TAG, "unable to get credentials from smartlock", exception)
                    smartLockCallback.onFailure()
                }
            }
        }
    }

    private fun retrieveCredential(credential: Parcelable?) {
        val cred = credential as? Credential
        cred?.let {
            val password = credential.password ?: ""
            if (password.isEmpty()) {
                smartLockCallback.onHintRetrieved(credential.id)
                Logger.info(TAG, "hint was successfully retrieved")
            } else {
                smartLockCallback.onCredentialRetrieved(credential.id, password, true)
                currentSmartlockCredential = cred
                Logger.info(TAG, "credentials were successfully retrieved")
            }
        } ?: Logger.info(TAG, "Failed to retrieve credentials after resolution")
    }

    /**
     * Attempt to resolve a non-successful result from an asynchronous request.
     * @param rae the ResolvableApiException to resolve.
     * @param requestCode the request code to use when starting an Activity for result,
     * this will be passed back to onActivityResult.
     */
    private fun resolveException(rae: ResolvableApiException, requestCode: Int) {
        Logger.info(TAG, "Resolving:  $rae.message with requestCode $requestCode")
        try {
            rae.startResolutionForResult(activity, requestCode)
        } catch (e: IntentSender.SendIntentException) {
            Logger.error(TAG, "Failed to resolve $rae", e)
        }
    }

    /**
     * Save the crendential to smartlock manager
     */
    override fun saveCredentials(username: String, password: String) {
        val credential = Credential.Builder(username)
                .setPassword(password)
                .build()

        credentialsClient.save(credential).addOnCompleteListener(
                OnCompleteListener<Void> { task ->
                    if (task.isSuccessful) {
                        Logger.info(TAG, "Credential saved.")
                        return@OnCompleteListener
                    }
                    val rae = task.exception
                    if (rae is ResolvableApiException) {
                        Logger.info(TAG, "Ask user agreement to save credentials")
                        resolveException(rae, RC_SAVE_CREDENTIALS)
                    } else {
                        Logger.error(TAG, "Failed attempt to save credentials", rae)
                    }
                })
    }

    /**
     * Delete stored credential
     */
    override fun deleteCredentials() {
        currentSmartlockCredential?.let {
            credentialsClient.delete(currentSmartlockCredential!!).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    smartLockCallback.onCredentialDeleted()
                    Log.e(TAG, "Credential were successfully deleted")
                } else {
                    smartLockCallback.onFailure()
                    Log.e(TAG, "Credential deletion failed", task.exception)
                }
            }
        }
    }

    /**
     * Convert a [Parcelable] object to [com.schibsted.account.engine.input.Credentials]
     * @return null if the mapping failed, [com.schibsted.account.engine.input.Credentials] otherwise
     */
    fun mapToIdentityCredentials(parcelable: Parcelable): com.schibsted.account.engine.input.Credentials? {
        val credential = parcelable as? Credential
        credential?.id?.let {
            val id = Identifier(Identifier.IdentifierType.EMAIL, it)
            credential.password?.let {
                return com.schibsted.account.engine.input.Credentials(id, it, true)
            }
        }
        return null
    }

    /**
     * Extract a [String] identifier and a [String] password from a [Parcelable] object
     * @return a [Pair] of [String] containing the identifier and the password if the extract was successful
     * null otherwise
     */
    fun extractCredentialData(parcelable: Parcelable): Pair<String?, String?>? {
        val credential = parcelable as? Credential
        credential?.let {
            return Pair(credential.id, credential.password)
        }
        return null
    }
}

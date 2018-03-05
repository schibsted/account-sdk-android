/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.schibsted.account.common.util.Logger;
import com.schibsted.account.common.util.SecurityUtil;
import com.schibsted.account.network.response.PasswordlessToken;
import com.schibsted.account.network.response.TokenResponse;
import com.schibsted.account.network.service.passwordless.PasswordlessService;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Locale;

/**
 * DAO for sdk-related shared preferences.
 */
public class KeyValueStore {
    @VisibleForTesting
    static final String KEY_PASSWORDLESS_TOKEN = "PasswordlessToken";
    @VisibleForTesting
    static final String KEY_JWT = "JSONWebToken";
    /**
     * @deprecated Use {@link #KEY_JWT} instead. In its beginnings, they sdk would keep
     * the token under the key RashomonJwt. When the Rashomon project was dropped, this left
     * the branded key in a compromised situation. For compatibility reasons, this is to be
     * kept for another year, which is the lifetime of the refresh token. After that, every
     * client will either have been updated to use a version of the sdk that doesn't brand
     * the keys under which data is stored, or they will be conceptually logged out because
     * the refresh token will no longer be valid. That means this field and its usages are
     * good for deletion starting 2018.
     */
    @Deprecated
    @VisibleForTesting
    static final String KEY_RASHOMON_JWT = "RashomonJwt";
    @VisibleForTesting
    static final String KEY_CONNECTION = "Connection";
    @VisibleForTesting
    static final String KEY_AUTH_CODE = "AuthCode";
    private static final String KEY_PREFERENCES_FILE = "preferences";
    private static final String KEY_CLIENT_CREDENTIALS = "ClientCredentials";
    private static final Gson GSON = new Gson();
    private final Context context;
    private final String preferencesFile;

    /**
     * Constructor.
     *
     * @param context The context.
     */
    public KeyValueStore(@NonNull final Context context) {
        Preconditions.checkNotNull(context);
        this.context = context.getApplicationContext();
        this.preferencesFile = KeyValueStore.KEY_PREFERENCES_FILE;
    }

    /**
     * Reads the stored access token, if a valid one is stored.
     *
     * @return The stored access token, if any. <code>null</code> otherwise.
     */
    @Nullable
    public TokenResponse readAccessToken() {
        return this.readAccessTokenCompat(null);
    }

    /**
     * Reads the stored access token, if a valid one is stored.
     *
     * @param clientSecret The client secret, used for the purpose of decrypting the token as it
     *                     was encrypted by the old one. If <code>null</code>, a check for
     *                     old sdk credentials is skipped, otherwise it is performed
     * @return The stored access token, if any. <code>null</code> otherwise.
     */
    @Nullable
    @SuppressWarnings("PMD")
    public TokenResponse readAccessTokenCompat(final String clientSecret) {
        if (clientSecret != null) {
            @SuppressWarnings("PrivateMemberAccessBetweenOuterAndInnerClass") final TokenResponse oldSdkTokenResponse = OldSdkCompatOperations.decryptAccessTokenFromOldSdkSharedPreferences(this.context, clientSecret);

            if (oldSdkTokenResponse != null && oldSdkTokenResponse.isValidToken()) {
                return oldSdkTokenResponse;
            }
        }

        String jwtJson = this.readString(KeyValueStore.KEY_JWT);
        if (TextUtils.isEmpty(jwtJson)) {
            jwtJson = this.readString(KeyValueStore.KEY_RASHOMON_JWT);
        }

        if (jwtJson != null) {
            final TokenResponse tokenResponse = KeyValueStore.GSON.fromJson(jwtJson, TokenResponse.class);
            if (tokenResponse != null && tokenResponse.isValidToken()) {
                return tokenResponse;
            }
        }
        
        return null;
    }

    /**
     * Reads the stored client token, if a valid one is stored.
     *
     * @return The stored client token, if any. <code>null</code> otherwise.
     */
    @Nullable
    public TokenResponse readClientAccessToken() {
        String jwtJson = this.readString(KeyValueStore.KEY_CLIENT_CREDENTIALS);
        return jwtJson != null ? KeyValueStore.GSON.fromJson(jwtJson, TokenResponse.class) : null;
    }

    /**
     * Stores an access token.
     *
     * @param jsonWebToken The access token to store.
     */
    public void writeAccessToken(final TokenResponse jsonWebToken) {
        this.writeString(KeyValueStore.KEY_JWT, jsonWebToken != null
                ? KeyValueStore.GSON.toJson(jsonWebToken) : null);
    }

    /**
     * Stores a token representing client credentials.
     *
     * @param clientCredentials The client credentials to store.
     */
    public void writeClientToken(final TokenResponse clientCredentials) {
        this.writeString(KeyValueStore.KEY_CLIENT_CREDENTIALS, clientCredentials != null
                ? KeyValueStore.GSON.toJson(clientCredentials) : null);
    }

    /**
     * Reads the stored passwordless token, if a valid one is stored.
     *
     * @return The stored passwordless token, if any. <code>null</code> otherwise.
     */
    @Nullable
    public PasswordlessToken readPasswordlessToken() {
        final String passwordlessToken = this.readString(KeyValueStore.KEY_PASSWORDLESS_TOKEN);
        return passwordlessToken != null
                ? KeyValueStore.GSON.fromJson(passwordlessToken, PasswordlessToken.class) : null;
    }

    /**
     * Stores a passwordless token.
     *
     * @param passwordlessToken The passwordless token to store.
     */
    public void writePasswordlessToken(final PasswordlessToken passwordlessToken) {
        this.writeString(KeyValueStore.KEY_PASSWORDLESS_TOKEN,
                passwordlessToken != null ? KeyValueStore.GSON.toJson(passwordlessToken) : null);
    }

    /**
     * Writes the currently active connection type
     *
     * @param connection The currently active connection type
     */
    public void writeConnection(@PasswordlessService.Connection final String connection) {
        Preconditions.checkNotNull(connection);
        this.writeString(KeyValueStore.KEY_CONNECTION, connection);
    }

    /**
     * Gets the currently active connection type
     *
     * @return The currently active connection type
     */
    @Nullable
    @PasswordlessService.Connection
    public String readConnection() {
        //noinspection WrongConstant
        return this.readString(KeyValueStore.KEY_CONNECTION);
    }

    /**
     * Sets the last auth code provided to the sdk
     *
     * @param authCode Writes the last auth code provided to the sdk
     */
    public void writeAuthCode(final String authCode) {
        this.writeString(KeyValueStore.KEY_AUTH_CODE, authCode);
    }

    /**
     * Gets the last auth code provided to the sdk
     *
     * @return The last auth code provided to the sdk
     */
    @Nullable
    @PasswordlessService.Connection
    public String readAuthCode() {
        return this.readString(KeyValueStore.KEY_AUTH_CODE);
    }

    /**
     * Clears all preferences in the file
     */
    public void clear() {
        if (!this.getPreferences().edit().clear().commit()) {
            throw new IllegalStateException(String.format(Locale.ENGLISH,
                    "Unable to clear preference file %s", KeyValueStore.KEY_PREFERENCES_FILE));
        }
    }

    /**
     * Clears the currently stored access token.
     */
    public void clearAccessToken() {
        this.writeAccessToken(null);
        this.writeString(KeyValueStore.KEY_RASHOMON_JWT, null);
        //noinspection PrivateMemberAccessBetweenOuterAndInnerClass
        OldSdkCompatOperations.clearAccessToken(this.context);
    }

    /**
     * Reads a {@link String} preference.
     *
     * @param preferenceKey The key for the desired preference.
     * @return The currently stored value of the desired preference, or <code>null</code> if the
     * requested key is not found.
     */
    @Nullable
    private String readString(@NonNull final String preferenceKey) {
        Preconditions.checkNotNull(preferenceKey);
        return this.getPreferences().getString(preferenceKey, null);
    }

    /**
     * Writes a {@link String} preference, creating it if it doesn't yet exist and overwriting it
     * otherwise.
     *
     * @param preferenceKey The key for the desired preference.
     * @param value         The value to write.
     */
    private void writeString(@NonNull final String preferenceKey, final String value) {
        Preconditions.checkNotNull(preferenceKey);
        final SharedPreferences sharedPref = this.getPreferences();
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(preferenceKey, value);
        this.commitString(editor, preferenceKey, value);
    }

    /**
     * Attempts to commit an editor, printing an error on failure.
     *
     * @param editor The editor to commit.
     * @param key    The key of the preference to write.
     * @param value  The value of the preference to write.
     */
    private void commitString(@NonNull SharedPreferences.Editor editor,
                              @NonNull final String key, final String value) {
        Preconditions.checkNotNull(editor, key);
        if (!editor.commit()) {
            String msg = String.format("Failed to commit editor for preference %s with value %s.", key, value);
            Logger.INSTANCE.error(Logger.getDEFAULT_TAG(), msg, null);
        }
    }

    /**
     * Provides the sdk shared preferences.
     *
     * @return The sdk shared preferences.
     */
    private SharedPreferences getPreferences() {
        return this.context.getSharedPreferences(this.preferencesFile, Context.MODE_PRIVATE);
    }

    /**
     * Wraps operations that are specific to the old sdk
     */
    @VisibleForTesting
    static class OldSdkCompatOperations {
        @VisibleForTesting
        static final String KEY_ACCESS_TOKEN = "access_token";
        @VisibleForTesting
        static final String KEY_REFRESH_TOKEN = "refresh_token";
        @VisibleForTesting
        static final String KEY_USER_ID = "user_id";
        @VisibleForTesting
        static final String KEY_EXPIRES_AT = "expires_at";

        /**
         * As extracted from the old sdk
         *
         * @param clientSecret The client secret the expected token was encrypted with
         * @return The token as retrieved from the shared preferences
         */
        @Nullable
        @SuppressWarnings("PMD")
        private static TokenResponse decryptAccessTokenFromOldSdkSharedPreferences(
                @NonNull final Context context, @NonNull final String clientSecret) {
            final SharedPreferences secure =
                    OldSdkCompatOperations.getOldSdkSecurePreferencesFile(context);
            if (secure != null) {
                try {
                    final JsonObject object = new JsonObject();
                    object.addProperty(OldSdkCompatOperations.KEY_ACCESS_TOKEN, SecurityUtil.decryptString(clientSecret, secure.getString(KEY_ACCESS_TOKEN, null)));
                    object.addProperty(OldSdkCompatOperations.KEY_REFRESH_TOKEN,
                            SecurityUtil.decryptString(clientSecret, secure.getString(KEY_REFRESH_TOKEN, null)));
                    object.addProperty(OldSdkCompatOperations.KEY_USER_ID,
                            SecurityUtil.decryptString(clientSecret, secure.getString(KEY_USER_ID, null)));
                    return new Gson().fromJson(object, TokenResponse.class);
                } catch (GeneralSecurityException | UnsupportedEncodingException ignored) {
                    // ignored
                }
            }
            return null;
        }

        /**
         * Removes the access token from the old shared preferences.
         *
         * @param context The context
         */
        private static void clearAccessToken(
                @NonNull final Context context) {
            final SharedPreferences sharedPreferences =
                    OldSdkCompatOperations.getOldSdkSecurePreferencesFile(context);
            if (sharedPreferences != null) {
                final SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(OldSdkCompatOperations.KEY_ACCESS_TOKEN);
                editor.remove(OldSdkCompatOperations.KEY_REFRESH_TOKEN);
                editor.remove(OldSdkCompatOperations.KEY_USER_ID);
                editor.remove(OldSdkCompatOperations.KEY_EXPIRES_AT);
                editor.apply();
            }
        }

        /**
         * Returns a reference to the shared preferences of old deprecated sdk
         *
         * @param context The context
         * @return A reference to the shared preferences of old deprecated sdk
         */
        @Nullable
        private static SharedPreferences getOldSdkSecurePreferencesFile(
                @NonNull final Context context) {
            return context.getSharedPreferences(
                    context.getPackageName() + ".sdk", Context.MODE_PRIVATE);
        }
    }
}

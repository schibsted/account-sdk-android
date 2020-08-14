/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.service.passwordless;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.schibsted.account.network.Environment;
import com.schibsted.account.network.response.PasswordlessToken;
import com.schibsted.account.network.service.BaseNetworkService;
import com.schibsted.account.util.Preconditions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;

public class PasswordlessService extends BaseNetworkService {

    public static final String PARAM_CONNECTION_EMAIL = "email";
    public static final String PARAM_CONNECTION_SMS = "sms";
    private static final String PARAM_LOCALE = "locale";
    private static final String PARAM_PHONE_NUMBER = "phone_number";
    private static final String PARAM_CONNECTION = "connection";

    private final PasswordlessContract passwordlessContract;

    public PasswordlessService(@Environment String environment, final OkHttpClient okHttpClient) {
        super(environment, okHttpClient);
        passwordlessContract = createService(PasswordlessContract.class);
    }

    /**
     * Requests a code to be sent to an identifier.
     *
     * @param clientId   The client id of the app.
     * @param identifier The identifier to sent the auth code to.
     * @param connection The connection type with the identifier.
     * @param locale     The locale to use for the message containing the code.
     */
    public Call<PasswordlessToken> sendValidationCode(@NonNull final String clientId,
                                                      @NonNull final String identifier, @NonNull final String connection,
                                                      @NonNull final Locale locale) {
        Preconditions.checkNotNull(clientId, identifier, connection);
        final Map<String, String> params = new HashMap<>();

        params.put(PARAM_CLIENT_ID, clientId);
        params.put(PARAM_CONNECTION, connection);
        params.put(PARAM_LOCALE, locale.getLanguage() + "-" + locale.getCountry());
        if (PARAM_CONNECTION_SMS.equalsIgnoreCase(connection)) {
            params.put(PARAM_PHONE_NUMBER, identifier);
        } else {
            params.put(PARAM_CONNECTION_EMAIL, identifier);
        }

        return this.passwordlessContract.requestCode(params);
    }

    /**
     * Request an auth code that had already been sent to an identifier previously to be resent.
     *
     * @param passwordlessToken The passwordless token corresponding to the last request for sending
     *                          (or resending) an auth code.
     */
    public Call<PasswordlessToken> resendCode(@NonNull final String clientId,
                                              @NonNull final PasswordlessToken passwordlessToken) {
        Preconditions.checkNotNull(clientId, passwordlessToken);
        final Map<String, String> params = new HashMap<>();
        params.put(PARAM_CLIENT_ID, clientId);
        params.put(PARAM_PASSWORDLESS_TOKEN, passwordlessToken.getValue());

        return this.passwordlessContract.resendCode(params);
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({PARAM_CONNECTION_EMAIL, PARAM_CONNECTION_SMS})
    public @interface Connection {
    }
}

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.network;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Groups together the scopes defined by OpenID Connect supported by the SDK. See
 * <a href="http://openid.net/specs/openid-connect-core-1_0.html#ScopeClaims">OpenID Connect Scope Claims</a>.
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({
        OIDCScope.SCOPE_READ_NAME, OIDCScope.SCOPE_WRITE_NAME,
        OIDCScope.SCOPE_READ_GIVEN_NAME, OIDCScope.SCOPE_WRITE_GIVEN_NAME,
        OIDCScope.SCOPE_READ_FAMILY_NAME, OIDCScope.SCOPE_WRITE_FAMILY_NAME,
        OIDCScope.SCOPE_READ_MIDDLE_NAME, OIDCScope.SCOPE_WRITE_MIDDLE_NAME,
        OIDCScope.SCOPE_READ_NICKNAME, OIDCScope.SCOPE_WRITE_NICKNAME,
        OIDCScope.SCOPE_READ_PREFERRED_USERNAME, OIDCScope.SCOPE_WRITE_PREFERRED_USERNAME,
        OIDCScope.SCOPE_READ_PICTURE, OIDCScope.SCOPE_WRITE_PICTURE,
        OIDCScope.SCOPE_READ_WEBSITE, OIDCScope.SCOPE_WRITE_WEBSITE,
        OIDCScope.SCOPE_READ_EMAIL, OIDCScope.SCOPE_WRITE_EMAIL,
        OIDCScope.SCOPE_READ_GENDER, OIDCScope.SCOPE_WRITE_GENDER,
        OIDCScope.SCOPE_READ_BIRTHDATE, OIDCScope.SCOPE_WRITE_BIRTHDATE,
        OIDCScope.SCOPE_READ_ZONE_INFO, OIDCScope.SCOPE_WRITE_ZONE_INFO,
        OIDCScope.SCOPE_READ_LOCALE, OIDCScope.SCOPE_WRITE_LOCALE,
        OIDCScope.SCOPE_READ_PHONE_NUMBER, OIDCScope.SCOPE_WRITE_PHONE_NUMBER,
        OIDCScope.SCOPE_READ_UPDATED_AT, OIDCScope.SCOPE_WRITE_UPDATED_AT,
})
public @interface OIDCScope {

    /**
     * This is a required scope. It will be added by the SDK if not already requested.
     */
    String SCOPE_OPENID = "openid";
    String SCOPE_READ_NAME = "name";
    String SCOPE_WRITE_NAME = "w_name";
    String SCOPE_READ_GIVEN_NAME = "given_name";
    String SCOPE_WRITE_GIVEN_NAME = "w_given_name";
    String SCOPE_READ_FAMILY_NAME = "family_name";
    String SCOPE_WRITE_FAMILY_NAME = "w_family_name";
    String SCOPE_READ_MIDDLE_NAME = "middle_name";
    String SCOPE_WRITE_MIDDLE_NAME = "w_middle_name";
    String SCOPE_READ_NICKNAME = "nickname";
    String SCOPE_WRITE_NICKNAME = "w_nickname";
    String SCOPE_READ_PREFERRED_USERNAME = "preferred_username";
    String SCOPE_WRITE_PREFERRED_USERNAME = "w_preferred_username";
    String SCOPE_READ_PICTURE = "picture";
    String SCOPE_WRITE_PICTURE = "w_picture";
    String SCOPE_READ_WEBSITE = "website";
    String SCOPE_WRITE_WEBSITE = "w_website";
    String SCOPE_READ_EMAIL = "email";
    String SCOPE_WRITE_EMAIL = "w_email";
    String SCOPE_READ_GENDER = "gender";
    String SCOPE_WRITE_GENDER = "w_gender";
    String SCOPE_READ_BIRTHDATE = "birthdate";
    String SCOPE_WRITE_BIRTHDATE = "w_birthdate";
    String SCOPE_READ_ZONE_INFO = "zoneinfo";
    String SCOPE_WRITE_ZONE_INFO = "w_zoneinfo";
    String SCOPE_READ_LOCALE = "locale";
    String SCOPE_WRITE_LOCALE = "w_locale";
    String SCOPE_READ_PHONE_NUMBER = "phone_number";
    String SCOPE_WRITE_PHONE_NUMBER = "w_phone_number";
    String SCOPE_READ_UPDATED_AT = "updated_at";
    String SCOPE_WRITE_UPDATED_AT = "w_updated_at";
}

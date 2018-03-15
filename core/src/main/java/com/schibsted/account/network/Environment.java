/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Contains the set of environments that can be used.
 *
 * @see #ENVIRONMENT_DEVELOPMENT
 * @see #ENVIRONMENT_PREPRODUCTION
 * @see #ENVIRONMENT_PRODUCTION
 * @see #ENVIRONMENT_PRODUCTION_NORWAY
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({Environment.ENVIRONMENT_DEVELOPMENT, Environment.ENVIRONMENT_PREPRODUCTION,
        Environment.ENVIRONMENT_PRODUCTION, Environment.ENVIRONMENT_PRODUCTION_NORWAY})
public @interface Environment {

    String ENVIRONMENT_DEVELOPMENT = "https://identity-dev.schibsted.com/";
    String ENVIRONMENT_PREPRODUCTION = "https://identity-pre.schibsted.com/";
    String ENVIRONMENT_PRODUCTION = "https://login.schibsted.com/";
    String ENVIRONMENT_PRODUCTION_NORWAY = "https://payment.schibsted.no/";
}

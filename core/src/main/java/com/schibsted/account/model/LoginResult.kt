/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.model

import com.schibsted.account.session.User

data class LoginResult(val user: User, val isNewUser: Boolean)

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.integration.contract

import com.schibsted.account.engine.input.Agreements
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.input.RequiredFields
import com.schibsted.account.engine.input.VerificationCode
import com.schibsted.account.model.LoginResult

/**
 * The contract containing all the required steps to log in using the
 * [com.schibsted.account.flowengine.controller.PasswordlessController]
 */
interface PasswordlessContract : Contract<LoginResult>, Identifier.Provider, VerificationCode.Provider,
        Agreements.Provider, RequiredFields.Provider

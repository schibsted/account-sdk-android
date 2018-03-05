/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.session.event

import com.schibsted.account.model.UserId

sealed class BroadcastEvent {
    class LogoutEvent(val userId: UserId) : BroadcastEvent()
    class RefreshEvent(val userId: UserId) : BroadcastEvent()
}

/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.persistence

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.WordSpec

class StorageDelegateTest : WordSpec({
    "Should write using the provided function to write to persistence" {
        var storedValue = -1

        val editor: SharedPreferences.Editor = mock {
            on { putInt(any(), any()) }.then {
                storedValue = it.arguments[1] as Int
                mock<SharedPreferences.Editor>()
            }
        }

        val prefs: SharedPreferences = mock {
            on { getInt(any(), any()) }.thenReturn(storedValue)
            on { edit() }.thenReturn(editor)
        }

        var delegated: Int by StorageDelegate(prefs, SharedPreferences::getInt, SharedPreferences.Editor::putInt, 0)
        delegated = 443

        storedValue shouldEqual 443
        verify(editor).putInt(any(), any())
    }

    "Should read using the provided function" {
        val storedValue = "none"

        val prefs: SharedPreferences = mock {
            on { getString(any(), any()) }.thenReturn(storedValue)
        }

        val delegated: String by StorageDelegate(prefs, SharedPreferences::getString, SharedPreferences.Editor::putString, "abc")

        delegated shouldEqual storedValue
        verify(prefs).getString(any(), any())
    }
})

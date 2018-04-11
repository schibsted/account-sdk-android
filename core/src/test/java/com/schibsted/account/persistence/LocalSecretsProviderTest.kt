package com.schibsted.account.persistence

import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.mock
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.properties.forAll
import io.kotlintest.properties.headers
import io.kotlintest.properties.row
import io.kotlintest.properties.table
import io.kotlintest.specs.StringSpec
import java.util.UUID

class LocalSecretsProviderTest : StringSpec({
    val data = mutableMapOf<String, String>()

    val mockEditor: SharedPreferences.Editor = mock {
        on { putString(any(), any()) }.then {
            val arg1 = it.getArgument(0) as String
            val arg2 = it.getArgument(1) as String

            println("PUTTING $arg1 : $arg2")
            data.put(arg1, arg2)
            null
        }
    }

    val mockedSharedPreferences: SharedPreferences = mock {
        on { getString(any(), anyOrNull()) }.then {
            data[it.getArgument(0) as String] ?: it.getArgument(1) as String?
        }

        on { edit() }.thenReturn(mockEditor)
    }

    val mockedContext: Context = mock {
        on { getSharedPreferences(any(), any()) }.thenReturn(mockedSharedPreferences)
    }

    "Retrieveing data which does not exist should not crash" {
        val lsp = LocalSecretsProvider(mockedContext)
        lsp.get("someKey") shouldBe null

    }

    "Storing data should return a secret key in the UUID format" {
        val lsp = LocalSecretsProvider(mockedContext)
        val res = lsp.put("somedata")
        UUID.fromString(res)
    }

    "Storing an existing data value should returnthe same key" {
        val lsp = LocalSecretsProvider(mockedContext)
        val firstRes = lsp.put("somedata")
        val secondRes = lsp.put("somedata")
        firstRes shouldEqual secondRes
    }

    "Stored data should be retrievable from the returned key" {
        val lsp = LocalSecretsProvider(mockedContext)
        val myData = "thisIsMydataThereAreManyLikeIt"
        val res = lsp.put(myData)
        lsp.get(res) shouldEqual myData
    }

    "When exceeding the max items, the oldest should be dropped" {
        val lsp = LocalSecretsProvider(mockedContext, 3)

        val table = table(
                headers("key", "expected value"),
                row(lsp.put("MyValue 1"), null as String?),
                row(lsp.put("MyValue 2"), null as String?),
                row(lsp.put("MyValue 3"), "MyValue 3" as String?),
                row(lsp.put("MyValue 4"), "MyValue 4" as String?),
                row(lsp.put("MyValue 5"), "MyValue 5" as String?)
        )

        forAll(table) { key, expVal ->
            lsp.get(key) shouldBe expVal
        }
    }
})

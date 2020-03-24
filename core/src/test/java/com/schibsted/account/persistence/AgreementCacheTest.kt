package com.schibsted.account.persistence

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.util.*

class AgreementCacheTest : StringSpec() {

    override fun isInstancePerTest() = true

    init {
        val storage: AgreementStorage = mock()
        val agreementCache = AgreementCache(storage)

        "There is no valid agreement when storage is empty" {
            agreementCache.hasValidAgreement("userId") shouldBe false
        }

        "There is no valid agreement when user id does not match" {
            val future = System.currentTimeMillis() + 1000L
            whenever(storage.getAgreement()).thenReturn(Pair("something else", Date(future)))

            agreementCache.hasValidAgreement("userId") shouldBe false
        }

        "There is no valid agreement when date is in past" {
            val past = System.currentTimeMillis() - 1000L
            whenever(storage.getAgreement()).thenReturn(Pair("userId", Date(past)))

            agreementCache.hasValidAgreement("userId") shouldBe false
        }

        "There is a valid agreement when user id matches and date is in future" {
            val future = System.currentTimeMillis() + 1000L
            whenever(storage.getAgreement()).thenReturn(Pair("userId", Date(future)))

            agreementCache.hasValidAgreement("userId") shouldBe true
        }
    }
}

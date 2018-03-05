/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.inbox
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.pressBack
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.ui.R
import com.schibsted.account.ui.UiConfiguration
import com.schibsted.account.ui.login.flow.password.PasswordActivity
import com.schibsted.spain.barista.assertion.BaristaImageViewAssertions.assertHasDrawable
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaSleepInteractions.sleep
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InboxFragmentTest {
    private val emailIdentifier = Identifier(Identifier.IdentifierType.EMAIL, "myTestEmail@gmail.com")

    @Rule
    @JvmField
    val rule = object : ActivityTestRule<PasswordActivity>(PasswordActivity::class.java) {
        override fun getActivityIntent(): Intent {
            val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

            val uiConfiguration = UiConfiguration.Builder
                    .fromManifest(targetContext)
                    .identifierType(Identifier.IdentifierType.EMAIL)
                    .build()

            return PasswordActivity.getCallingIntent(targetContext, uiConfiguration)
        }
    }

    @Before
    fun init() {
        rule.activity.navigationController.navigateToFragment(InboxFragment.newInstance(emailIdentifier))
    }

    @Test
    fun shouldDisplayIdentifier() {
        assertDisplayed(rule.activity.getString(R.string.schacc_inbox_information, emailIdentifier.identifier))
    }

    @Test
    fun shouldDisplayDrawable() {
        assertHasDrawable(R.id.inbox_image, R.drawable.schacc_ic_envelopes)
    }

    @Test
    fun backPressShouldNavigateToIdentificationScreen() {
        pressBack()
        sleep(1500)
        assertDisplayed(R.id.identification_container)
    }

    @Test
    fun clickOnLinkShouldNavigateToIdentificationScreen() {
        clickOn(R.id.inbox_change_identifier)
        sleep(1500)
        assertDisplayed(R.id.identification_container)
    }
}

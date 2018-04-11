/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui.login.screen.identification

import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.ui.R
import com.schibsted.account.ui.UiConfiguration
import com.schibsted.account.ui.login.flow.passwordless.PasswordlessActivity
import com.schibsted.spain.barista.assertion.BaristaEnabledAssertions.assertEnabled
import com.schibsted.spain.barista.assertion.BaristaFocusedAssertions.assertFocused
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertContains
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
import com.schibsted.spain.barista.interaction.BaristaKeyboardInteractions.closeKeyboard
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class EmailIdentificationFragmentTest {

    @Rule
    @JvmField
    val rule = object : ActivityTestRule<PasswordlessActivity>(PasswordlessActivity::class.java) {
        override fun getActivityIntent(): Intent {
            val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

            val uiConfiguration = UiConfiguration.Builder
                    .fromManifest(targetContext)
                    .identifierType(Identifier.IdentifierType.EMAIL)
                    .build()

            return PasswordlessActivity.getCallingIntent(targetContext, uiConfiguration)
        }
    }

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        val fragment = rule.activity.fragmentProvider.getOrCreateIdentificationFragment(
                null,
                null,
                Identifier.IdentifierType.EMAIL.value,
                null)
        rule.activity.navigationController.navigateToFragment(fragment)
    }

    @Test
    fun buttonShouldNotBeVisible() {
        assertNotDisplayed(R.id.identification_button_continue)
    }

    @Test
    fun fieldShouldBeFocused() {
        assertFocused(R.id.identification_input_view)
    }

    @Test
    fun buttonShouldBeVisible() {
        closeKeyboard()
        assertDisplayed(R.id.identification_button_continue)
        assertEnabled(R.id.identification_button_continue)
        assertContains(R.id.button, "Continue")
    }

    @Test
    fun shouldDisplayErrorField() {
        writeTo(R.id.input, "invalidEmail")
        closeKeyboard()
        clickOn(R.id.button)
        assertDisplayed(R.id.input_error_view)
        assertContains(R.id.input_error_view, "Please enter a valid email address")
    }

    @Test
    fun shouldHideErrorField() {
        shouldDisplayErrorField()
        writeTo(R.id.input, "")
        writeTo(R.id.input, "testidsdk@gmail.com")
        assertNotDisplayed(R.id.input_error_view)
    }
}

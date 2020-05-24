package nic.goi.aarogyasetu.views

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.utility.CorUtility
import nic.goi.aarogyasetu.utility.chooser
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeActivityTest {
    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(HomeActivity::class.java)

    @Test
    fun testShareAppButton() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val shareText = CorUtility.getShareText(appContext)

        Intents.init()
        Espresso.onView(ViewMatchers.withId(R.id.share)).perform(ViewActions.click())
        Intents.intended(
            chooser(
                CoreMatchers.allOf(
                    IntentMatchers.hasAction(Intent.ACTION_SEND),
                    IntentMatchers.hasType("text/plain"),
                    IntentMatchers.hasExtra(Intent.EXTRA_TEXT, shareText)
                )
            )
        )
        Intents.release()

        // dismiss the Share Dialog
        InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }

    @Test
    fun testClickLanguageChange() {
        Espresso.onView(ViewMatchers.withId(R.id.language_change))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.language_change)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.rv_select_language))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.btn_next))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}
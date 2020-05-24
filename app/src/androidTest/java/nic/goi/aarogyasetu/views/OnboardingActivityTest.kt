package nic.goi.aarogyasetu.views

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import nic.goi.aarogyasetu.R
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingActivityTest {
    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(OnboardingActivity::class.java)


    @Test
    fun testCLickNextButton() {
        Espresso.onView(withId(R.id.close))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.close)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.close))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.close)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.close))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.close)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.close))
            .check(ViewAssertions.matches(not(ViewMatchers.isDisplayed())))
    }

    @Test
    fun testSwipeViewPager() {
        Espresso.onView(withId(R.id.close))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.pager)).perform(ViewActions.swipeLeft())
        Espresso.onView(withId(R.id.close))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.pager)).perform(ViewActions.swipeLeft())
        Espresso.onView(withId(R.id.close))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.pager)).perform(ViewActions.swipeLeft())

        Espresso.onView(withId(R.id.close))
            .check(ViewAssertions.matches(not(ViewMatchers.isDisplayed())))
        Espresso.onView(withId(R.id.btn_register))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testClickLanguageChange() {
        Espresso.onView(withId(R.id.language_change))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.language_change)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.rv_select_language))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.btn_next))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}
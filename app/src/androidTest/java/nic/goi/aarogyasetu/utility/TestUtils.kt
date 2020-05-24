package nic.goi.aarogyasetu.utility

import android.content.Intent
import androidx.test.espresso.intent.matcher.IntentMatchers
import org.hamcrest.Matcher
import org.hamcrest.Matchers

/**
 * Simplify testing Intents with Chooser
 *
 * @param matcher the actual intent before wrapped by Chooser Intent
 */
fun chooser(matcher: Matcher<Intent>): Matcher<Intent> = Matchers.allOf(
    IntentMatchers.hasAction(Intent.ACTION_CHOOSER),
    IntentMatchers.hasExtra(Matchers.`is`(Intent.EXTRA_INTENT), matcher)
)
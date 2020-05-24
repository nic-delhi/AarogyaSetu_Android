package nic.goi.aarogyasetu.views

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_onboarding.*
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.analytics.EventNames
import nic.goi.aarogyasetu.utility.AnalyticsUtils
import nic.goi.aarogyasetu.utility.Constants
import nic.goi.aarogyasetu.utility.LocalizationUtil.getLocalisedString

class OnboardingActivity : AppCompatActivity(), SelectLanguageFragment.LanguageChangeListener {

    companion object {
        const val PAGE_COUNT = 4
        const val SCREEN_1 = 1
        const val SCREEN_2 = 2
        const val SCREEN_3 = 3
        const val SCREEN_4 = 4
    }

    var registrationFlow: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        updateStatusColor(R.color.onboarding_screen_1_bg_color)
        setContentView(R.layout.activity_onboarding)

        registrationFlow = !(intent.extras != null && intent.extras!!.containsKey(Constants.FINISH))

        if (!registrationFlow) {
            configureViews()
        } else {
            configureOnboardingViews()
        }
        configureLanguageChangeClick()
        configurePagerAdapter()
    }

    private fun configureLanguageChangeClick() {
        language_change.setOnClickListener {
            showLanguageSelectionDialog()
        }
    }

    private fun configurePagerAdapter() {
        pager.adapter = OnboardingAdapter(supportFragmentManager, registrationFlow)
        pageindicator.setViewPager(pager)

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                //do nothing
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                //do nothing
            }

            override fun onPageSelected(position: Int) {
                when (position + 1) {
                    SCREEN_1 -> updateStatusColor(R.color.onboarding_screen_1_bg_color)
                    SCREEN_2 -> updateStatusColor(R.color.onboarding_screen_2_bg_color)
                    SCREEN_3 -> updateStatusColor(R.color.onboarding_screen_3_bg_color)
                    SCREEN_4 -> updateStatusColor(R.color.onboarding_screen_4_bg_color)
                }
                if (registrationFlow) {
                    if ((pager.currentItem) < (PAGE_COUNT - 1)) {

                        close.visibility = View.VISIBLE
                    } else {
                        close.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun configureOnboardingViews() {
        close.visibility = View.VISIBLE
        close.text = getLocalisedString(this, R.string.next)
        close.setOnClickListener {

            if (pager.currentItem < (PAGE_COUNT - 1)) {
                pager.currentItem = (pager.currentItem + 1)
            }
        }
        AnalyticsUtils.sendEvent(EventNames.EVENT_OPEN_ONBOARDING)
    }

    private fun configureViews() {
        close.text = getLocalisedString(this, R.string.close)
        close.visibility = View.VISIBLE
        close.setOnClickListener {

            finish()
        }
        language_change.visibility = View.GONE
        AnalyticsUtils.sendEvent(EventNames.EVENT_OPEN_ONBOARDING_AS_INFO)
    }

    fun updateStatusColor(@ColorRes colorId: Int) {
        try {
            window.statusBarColor = ContextCompat.getColor(this, colorId)
        } catch (e: Exception) {
        }
    }

    private fun showLanguageSelectionDialog() {
        SelectLanguageFragment.showDialog(supportFragmentManager, true)
    }

    override fun languageChange() {
        if (registrationFlow) {
            close.text = getLocalisedString(this, R.string.next)
        } else {
            close.text = getLocalisedString(this, R.string.close)
        }
        pager.adapter?.notifyDataSetChanged()
    }
}

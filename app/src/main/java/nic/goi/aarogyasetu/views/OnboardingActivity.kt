package nic.goi.aarogyasetu.views

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.analytics.EventNames
import nic.goi.aarogyasetu.databinding.ActivityOnboardingBinding
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
    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        updateStatusColor(R.color.onboarding_screen_1_bg_color)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding)

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
        binding.languageChange.setOnClickListener {
            showLanguageSelectionDialog()
        }
    }

    private fun configurePagerAdapter() {
        binding.pager.adapter = OnboardingAdapter(supportFragmentManager, registrationFlow)
        binding.pageindicator.setViewPager(binding.pager)

        binding.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
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
                    if ((binding.pager.currentItem) < (PAGE_COUNT - 1)) {

                        binding.close.visibility = View.VISIBLE
                    } else {
                        binding.close.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun configureOnboardingViews() {
        binding.close.visibility = View.VISIBLE
        binding.close.text = getLocalisedString(this, R.string.next)
        binding.close.setOnClickListener {

            if (binding.pager.currentItem < (PAGE_COUNT - 1)) {
                binding.pager.currentItem = (binding.pager.currentItem + 1)
            }
        }
        AnalyticsUtils.sendEvent(EventNames.EVENT_OPEN_ONBOARDING)
    }

    private fun configureViews() {
        binding.close.text = getLocalisedString(this, R.string.close)
        binding.close.visibility = View.VISIBLE
        binding.close.setOnClickListener {

            finish()
        }
        binding.languageChange.visibility = View.GONE
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
            binding.close.text = getLocalisedString(this, R.string.next)
        } else {
            binding.close.text = getLocalisedString(this, R.string.close)
        }
        binding.pager.adapter?.notifyDataSetChanged()
    }
}

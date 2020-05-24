package nic.goi.aarogyasetu.views

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * @author Niharika.Arora
 */
class OnboardingAdapter(fm: FragmentManager, isRegistrationFlow: Boolean) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    companion object {
        const val ITEM_1 = 0
        const val ITEM_2 = 1
        const val ITEM_3 = 2
        const val ITEM_4 = 3
    }

    private var isRegistration: Boolean = isRegistrationFlow

    override fun getItem(item: Int): Fragment {
        return when (item) {
            ITEM_1 -> FirstOnBoardIntroFragment()
            ITEM_2 -> SecondOnBoardIntroFragment()
            ITEM_3 -> ThirdOnBoardIntroFragment()
            ITEM_4 -> ForthOnBoardIntroFragment.newInstance(isRegistration)
            else -> {
                FirstOnBoardIntroFragment()
            }

        }
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getCount(): Int = 4
}
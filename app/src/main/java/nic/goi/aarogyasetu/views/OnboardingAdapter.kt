/*
 * Copyright 2020 Government of India
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
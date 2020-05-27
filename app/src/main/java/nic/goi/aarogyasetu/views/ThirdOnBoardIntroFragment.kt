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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.utility.LocalizationUtil.getLocalisedString

/**
 * A simple [Fragment] subclass.
 */
class ThirdOnBoardIntroFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_third_on_board_intro, container, false)

        val youWillBeAlertedIfTY: TextView =
            view.findViewById(R.id.tv_you_will_be_alerted_if) as TextView
        youWillBeAlertedIfTY.text =
            getLocalisedString(view.context, R.string.you_will_be_alerted_if)

        val theAppAlertsAreAccompaniedTV: TextView =
            view.findViewById(R.id.tv_the_app_alerts_are_accompanied) as TextView
        theAppAlertsAreAccompaniedTV.text =
            getLocalisedString(view.context, R.string.the_app_alerts_are_accompanied)


        return view
    }
}

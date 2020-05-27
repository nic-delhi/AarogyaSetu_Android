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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import nic.goi.aarogyasetu.utility.LocalizationUtil.*


import nic.goi.aarogyasetu.R

class FirstOnBoardIntroFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first_introduction, container, false)

        val tvEachOneOfUs: TextView = view.findViewById(R.id.tv_each_one_of_us) as TextView
        tvEachOneOfUs.text = getLocalisedString(view.context, R.string.each_one_of_us)
        val wouldYouLikeTo: TextView = view.findViewById(R.id.tv_would_you_like_to) as TextView
        wouldYouLikeTo.text = getLocalisedString(view.context, R.string.would_you_like_to)


        return view
    }
}

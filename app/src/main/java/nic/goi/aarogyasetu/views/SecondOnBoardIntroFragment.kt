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
import androidx.core.text.HtmlCompat

import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.utility.LocalizationUtil.getLocalisedString
import nic.goi.aarogyasetu.utility.LocalizationUtil.getSpannableString

class SecondOnBoardIntroFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_second_on_board_intro, container, false)

        val tvAppTracksThrough: TextView =
            view.findViewById(R.id.tv_cowin_20_tracks_through) as TextView
        tvAppTracksThrough.text =
            HtmlCompat.fromHtml(
                getLocalisedString(view.context, R.string.cowin_20_tracks_through),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )

        val tvSimply1InstallTheApp: TextView =
            view.findViewById(R.id.tv_simply_1_install_the_app) as TextView

        val strArr = arrayOf(
            getLocalisedString(view.context, R.string.install),
            getLocalisedString(view.context, R.string.bluetooth_and_gps),
            getLocalisedString(view.context, R.string.location_sharing)
        )
        tvSimply1InstallTheApp.text =
            getSpannableString(view.context, R.string.simply_1_install_the_app, strArr)

        return view
    }
}

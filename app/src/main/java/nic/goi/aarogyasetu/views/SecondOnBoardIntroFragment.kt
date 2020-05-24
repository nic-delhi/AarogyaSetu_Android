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

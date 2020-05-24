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

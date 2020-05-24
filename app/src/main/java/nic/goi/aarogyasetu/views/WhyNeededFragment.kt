package nic.goi.aarogyasetu.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_why_needed.*
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.utility.LocalizationUtil.getLocalisedString
import nic.goi.aarogyasetu.viewmodel.OnBoardingViewModel

class WhyNeededFragment : BottomSheetDialogFragment() {
    private lateinit var onboardingViewModel: OnBoardingViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        onboardingViewModel = ViewModelProvider(this).get(OnBoardingViewModel::class.java)
        return inflater.inflate(R.layout.fragment_why_needed, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_mobile_no_required.text = getLocalisedString(
            view.context,
            R.string.your_mobile_number_is_required_to_know_your_identity
        )
        tv_say_you_met_someone.text = getLocalisedString(view.context, R.string.text_value)
        i_understand.text = getLocalisedString(view.context, R.string.i_understand)

        i_understand.setOnClickListener {
            onboardingViewModel.whyNeededshown.value = false
            dismiss()
        }

        close.setOnClickListener {
            onboardingViewModel.whyNeededshown.value = false
            dismiss()
        }
    }

}

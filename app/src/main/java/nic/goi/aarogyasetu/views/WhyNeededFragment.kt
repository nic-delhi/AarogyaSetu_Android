package nic.goi.aarogyasetu.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.databinding.FragmentWhyNeededBinding
import nic.goi.aarogyasetu.utility.LocalizationUtil.getLocalisedString
import nic.goi.aarogyasetu.viewmodel.OnBoardingViewModel

class WhyNeededFragment : BottomSheetDialogFragment() {
    private lateinit var onboardingViewModel: OnBoardingViewModel
    private lateinit var binding: FragmentWhyNeededBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        onboardingViewModel = ViewModelProvider(this).get(OnBoardingViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_why_needed, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvMobileNoRequired.text = getLocalisedString(
            view.context,
            R.string.your_mobile_number_is_required_to_know_your_identity
        )
        binding.tvSayYouMetSomeone.text = getLocalisedString(view.context, R.string.text_value)
        binding.iUnderstand.text = getLocalisedString(view.context, R.string.i_understand)

        binding.iUnderstand.setOnClickListener {
            onboardingViewModel.whyNeededshown.value = false
            dismiss()
        }

        binding.close.setOnClickListener {
            onboardingViewModel.whyNeededshown.value = false
            dismiss()
        }
    }

}

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

package nic.goi.aarogyasetu.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BottomSheetViewModel : ViewModel() {

    var otpSent = MutableLiveData(false)
    var phoneNumberValidation = MutableLiveData(false)

    var phoneNumber = ""
}

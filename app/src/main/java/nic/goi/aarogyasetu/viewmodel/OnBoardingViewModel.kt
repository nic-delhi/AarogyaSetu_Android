package nic.goi.aarogyasetu.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nic.goi.aarogyasetu.background.BluetoothScanningService
import nic.goi.aarogyasetu.utility.AuthUtility

class OnBoardingViewModel : ViewModel() {
    var signedInState = MutableLiveData(false)
    var whyNeededshown = MutableLiveData(false)

    var isSharingPossible = MutableLiveData(false)

    init {
        signedInState.value = AuthUtility.isSignedIn()
        isSharingPossible.value = true

    }
}

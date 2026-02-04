package nic.goi.aarogyasetu.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.utility.*
import nic.goi.aarogyasetu.utility.LocalizationUtil.getLocalisedString
import nic.goi.aarogyasetu.utility.LocalizationUtil.getSpannableString
import nic.goi.aarogyasetu.viewmodel.BottomSheetViewModel
import nic.goi.aarogyasetu.viewmodel.OnBoardingViewModel
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import nic.goi.aarogyasetu.analytics.EventNames
import nic.goi.aarogyasetu.analytics.ScreenNames
import nic.goi.aarogyasetu.databinding.FragmentLoginBottomSheetBinding
import java.util.concurrent.TimeUnit


class LoginBottomSheet : BottomSheetDialogFragment(), ViewTreeObserver.OnGlobalLayoutListener {

    private lateinit var contentView: FragmentLoginBottomSheetBinding
    private lateinit var onBoardingViewModel: OnBoardingViewModel
    private lateinit var phoneNumberValidationViewModel: BottomSheetViewModel
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var smsReceiver: SmsReceiver? = null

    private val timer: CountDownTimer by lazy {
        object : CountDownTimer(TimeUnit.MINUTES.toMillis(2), 1000) {
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
                if (!isAdded) {
                    timer.cancel()
                    return
                }
                val seconds =
                    TimeUnit.SECONDS.convert(millisUntilFinished, TimeUnit.MILLISECONDS).toInt()
                val retryOtp = contentView.otpValidationLayout.retryOtp
                retryOtp.isEnabled = (seconds <= 60)
                if (!retryOtp.isEnabled && (120 - seconds) < 60) {
                    val arr = arrayOf("00:" + (seconds - 60))
                    retryOtp.text = getSpannableString(context, R.string.resend_otp_in, arr)
                } else {
                    retryOtp.text = getLocalisedString(context, R.string.resend_otp)
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.MyDialogStyle)
    }

    /**
     * This method is used to verify user with OTP.
     */
    private fun signInWithPhoneAuthCredential(otp: String) {
        AnalyticsUtils.sendBasicEvent(EventNames.EVENT_VALIDATE_OTP, ScreenNames.SCREEN_LOGIN)
        contentView.otpValidationLayout.progressBarOtp.visibility = View.VISIBLE
        AuthUtility.verifyOtp(
            phoneNumberValidationViewModel.phoneNumber,
            otp,
            object : UserVerifyListener {
                override fun onUserVerified(token: String?) {
                    if (isAdded) {
                        contentView.otpValidationLayout.progressBarOtp.visibility =
                            View.GONE
                        if (CorUtility.isBluetoothAvailable()) {
                            val mBTA = BluetoothAdapter.getDefaultAdapter()
                            mBTA.startDiscovery()
                        }
                        dismissAllowingStateLoss()
                        onBoardingViewModel.signedInState.value = true
                    }
                }

                override fun onAuthError(e: java.lang.Exception?, authError: AuthError) {
                    if (isAdded) {
                        contentView.otpValidationLayout.progressBarOtp.visibility =
                            View.GONE
                        contentView.otpValidationLayout.otpLayout.isErrorEnabled = true
                        contentView.otpValidationLayout.otpLayout.error =
                            getLocalisedString(context, authError.errorMsg)
                        AnalyticsUtils.sendBasicEvent(
                            EventNames.EVENT_VALIDATE_OTP_FAILED, ScreenNames.SCREEN_LOGIN,
                            e?.localizedMessage ?: getString(authError.errorMsg)
                        )
                    }
                }
            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        onBoardingViewModel = ViewModelProvider(activity!!).get(OnBoardingViewModel::class.java)
        configureSmsRetriever()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    /**
     * This method is used to start receiver to auto read OTP.
     */
    private fun configureSmsRetriever() {
        val smsRetrieverClient: SmsRetrieverClient = SmsRetriever.getClient(context!!)
        val smsRetrieverTask: Task<Void> = smsRetrieverClient.startSmsRetriever()
        smsRetrieverTask.addOnSuccessListener { onSmsRetrieverInitSuccess() }
        smsRetrieverTask.addOnFailureListener { onSmsRetrieverInitFailed() }
    }

    private fun onSmsRetrieverInitSuccess() {
        if (smsReceiver == null) {
            smsReceiver = SmsReceiver()
            smsReceiver?.injectOTPListener(otpListener)
            context?.registerReceiver(
                smsReceiver,
                IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            )
        }
    }

    private var otpListener: SmsReceiver.OTPListener = object : SmsReceiver.OTPListener {
        override fun onOTPReceived(otp: String?) {
            if (!otp.isNullOrEmpty()) {
                try {
                    contentView.otpValidationLayout.otpEditText.setText(
                        otp
                    )
                    contentView.otpValidationLayout.otpEditText.setSelection(
                        otp.length
                    )
                } catch (ex: Exception) {
                    //do nothing(setSelection fails in some devices)
                }
                submitOtp(otp)
            }
        }

        override fun onOTPTimeOut() {
            //do nothing
        }

    }

    private fun onSmsRetrieverInitFailed() { // do nothing
    }

    private fun handleGlobalLayoutListener() {
        contentView.root.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        contentView = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.fragment_login_bottom_sheet,
            null,
            false
        )
        phoneNumberValidationViewModel =
            ViewModelProvider(this).get(BottomSheetViewModel::class.java)
        dialog.setContentView(contentView.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setCanceledOnTouchOutside(false)
        bottomSheetBehavior = BottomSheetBehavior.from(contentView.root.parent as View)
        handleGlobalLayoutListener()
        setViews()
    }

    private fun setViews() = contentView.apply {
        phoneNumberValidationLayout.title.text =
            getLocalisedString(context, R.string.enter_mobile_number)
        phoneNumberValidationLayout.phoneNumberLayout.hint =
            getLocalisedString(context, R.string.mobile_number)
        phoneNumberValidationLayout.phoneNumberLayout.prefixText =
            getLocalisedString(context, R.string.country_code)

        otpValidationLayout.otptitleView.text =
            getLocalisedString(context, R.string.enter_otp)
        otpValidationLayout.otpLayout.helperText =
            getLocalisedString(context, R.string.we_have_sent_otp)
        otpValidationLayout.otpLayout.hint =
            getLocalisedString(context, R.string.otp)
        phoneNumberValidationLayout.whyNeeded.text =
            getLocalisedString(context, R.string.why_is_it_needed)
        phoneNumberValidationLayout.whyNeeded.setOnClickListener {
            onBoardingViewModel.whyNeededshown.value = true
        }
        contentView.phoneNumberValidationLayout.phoneNum.requestFocus()
        otpValidationLayout.retryOtp.setOnClickListener {
            sendReValidationCode(
                "+91" + phoneNumberValidationLayout.phoneNum.text.toString().trim()
            )
            otpValidationLayout.otpLayout.error = null
            otpValidationLayout.otpLayout.helperText =
                getLocalisedString(context, R.string.we_have_resent_otp)
        }

        phoneNumberValidationLayout.validatePhone.text =
            getLocalisedString(context, R.string.submit)
        phoneNumberValidationLayout.validatePhone.setOnClickListener {

            if (android.util.Patterns.PHONE.matcher(
                    phoneNumberValidationLayout.phoneNum.text.toString().trim()
                ).matches() && phoneNumberValidationLayout.phoneNum.text.toString()
                    .trim().length == 10
            ) {
                if (CorUtility.isNetworkAvailable(context)) {
                    phoneNumberValidationLayout.progressBar.visibility =
                        View.VISIBLE
                    phoneNumberValidationViewModel.phoneNumberValidation.value = true
                    sendValidationCode(
                        "+91" + phoneNumberValidationLayout.phoneNum.text.toString().trim()
                    )
                    phoneNumberValidationLayout.phoneNumberLayout.error =
                        null
                } else {
                    Toast.makeText(
                        context,
                        getLocalisedString(context, R.string.error_network_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                phoneNumberValidationLayout.phoneNumberLayout.error =
                    getLocalisedString(context, R.string.please_enter_a_valid_number)
            }
        }

        //TODO: possible back press handling required
//        phoneNumberValidationLayout.root.back.setOnClickListener {
//            otpValidationLayout.progressBarOtp.visibility = View.GONE
//            otpValidationLayout.otpLayout.isErrorEnabled = true
//            otpValidationLayout.otpEditText.setText("")
//            otpValidationLayout.otpLayout.error = ""
//            phoneNumberValidationViewModel.otpSent.value = false
//        }

        otpValidationLayout.validateOtp.text =
            getLocalisedString(context, R.string.submit)
        otpValidationLayout.validateOtp.setOnClickListener {

            if (otpValidationLayout.otpLayout.editText?.text.isNullOrEmpty()) {
                otpValidationLayout.otpLayout.isErrorEnabled = true
                otpValidationLayout.otpLayout.error =
                    getLocalisedString(context, R.string.please_enter_a_valid_otp)
            } else {
                val otp =
                    contentView.otpValidationLayout.otpLayout.editText?.text.toString()
                        .trim()
                submitOtp(otp)
            }
        }


        phoneNumberValidationLayout.close.setOnClickListener {
            dismissAllowingStateLoss()
        }
        activity?.let { it ->
            phoneNumberValidationViewModel.otpSent.observe(it, Observer {
                phoneNumberValidationLayout.progressBar.visibility =
                    View.GONE
                if (it) {
                    otpValidationLayout.otpLayout.requestFocus()
                    otpValidationLayout.root.visibility =
                        View.VISIBLE
                    phoneNumberValidationLayout.root.visibility = View.GONE
                } else {
                    otpValidationLayout.root.visibility =
                        View.GONE
                    phoneNumberValidationLayout.root.visibility =
                        View.VISIBLE
                }
            })
        }
    }

    private fun submitOtp(otp: String?) {
        if (CorUtility.isNetworkAvailable(context)) {
            if (!otp.isNullOrEmpty()) {
                contentView.otpValidationLayout.otpLayout.isErrorEnabled =
                    true
                contentView.otpValidationLayout.otpLayout.error = ""
                signInWithPhoneAuthCredential(otp)
            } else {
                Toast.makeText(
                    context,
                    resources.getString(R.string.please_enter_a_valid_otp),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                getLocalisedString(context, R.string.error_network_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    /**
     * This method is used to start Login process on the server and send OTP on the mobile number.
     */
    private fun sendValidationCode(phoneNumber: String) {
        AnalyticsUtils.sendBasicEvent(EventNames.EVENT_GET_OTP, ScreenNames.SCREEN_LOGIN)
        AuthUtility.signIn(phoneNumber, object : UserSignInListener {
            override fun onAuthError(e: Exception?, authError: AuthError) {
                if (isAdded) {
                    contentView.phoneNumberValidationLayout.progressBar.visibility =
                        View.GONE
                    contentView.phoneNumberValidationLayout.phoneNumberLayout.error =
                        getLocalisedString(context, authError.errorMsg)
                    AnalyticsUtils.sendBasicEvent(
                        EventNames.EVENT_GET_OTP_FAILED, ScreenNames.SCREEN_LOGIN,
                        e?.localizedMessage ?: getString(authError.errorMsg)
                    )
                }
            }

            override fun onAskOtp() {
                if (isAdded) {
                    phoneNumberValidationViewModel.phoneNumber = phoneNumber
                    phoneNumberValidationViewModel.otpSent.value = true
                    phoneNumberValidationViewModel.phoneNumberValidation.value = false
                    timer.start()
                }
            }

        })
    }

    /**
     * This method is used to Resend OTP on the mobile number.
     */
    private fun sendReValidationCode(phoneNumber: String) {
        timer.cancel()
        AuthUtility.signIn(phoneNumber, object : UserSignInListener {
            override fun onAuthError(e: Exception?, authError: AuthError) {
                if (isAdded) {
                    contentView.phoneNumberValidationLayout.phoneNum.error =
                        getLocalisedString(context, authError.errorMsg)
                    AnalyticsUtils.sendBasicEvent(
                        EventNames.EVENT_GET_OTP_FAILED, ScreenNames.SCREEN_LOGIN,
                        e?.localizedMessage ?: getString(authError.errorMsg)
                    )
                }
            }

            override fun onAskOtp() {
                if (isAdded) {
                    timer.start()
                }
            }

        })
    }

    private fun unRegisterReceiver() {
        if (smsReceiver != null) {
            context?.unregisterReceiver(smsReceiver)
            smsReceiver = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer.cancel()
        unRegisterReceiver()
    }

    override fun onGlobalLayout() {
        val rect = Rect()
        contentView.root.getWindowVisibleDisplayFrame(rect)
        val screenHeight = contentView.root.height
        val heightDifference = screenHeight - (rect.bottom - rect.top)
        bottomSheetBehavior.peekHeight = screenHeight + heightDifference
        contentView.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }


}
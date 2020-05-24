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
import kotlinx.android.synthetic.main.fragment_login_bottom_sheet.view.*
import kotlinx.android.synthetic.main.otp_validation_layout.view.*
import kotlinx.android.synthetic.main.phone_number_layout.view.*
import nic.goi.aarogyasetu.analytics.EventNames
import nic.goi.aarogyasetu.analytics.ScreenNames
import java.util.concurrent.TimeUnit


class LoginBottomSheet : BottomSheetDialogFragment(), ViewTreeObserver.OnGlobalLayoutListener {

    private lateinit var contentView: View
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
                val retryOtp = contentView.otp_validation_layout?.rootView?.retry_otp
                retryOtp?.isEnabled = (seconds <= 60)
                if (retryOtp?.isEnabled == false && (120 - seconds) < 60) {
                    val arr = arrayOf("00:" + (seconds - 60))
                    retryOtp.text = getSpannableString(context, R.string.resend_otp_in, arr)
                } else {
                    retryOtp?.text = getLocalisedString(context, R.string.resend_otp)
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
        contentView.otp_validation_layout.rootView.progress_bar_otp?.visibility = View.VISIBLE
        AuthUtility.verifyOtp(
            phoneNumberValidationViewModel.phoneNumber,
            otp,
            object : UserVerifyListener {
                override fun onUserVerified(token: String?) {
                    if (isAdded) {
                        contentView.otp_validation_layout.rootView.progress_bar_otp?.visibility =
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
                        contentView.otp_validation_layout.rootView.progress_bar_otp?.visibility =
                            View.GONE
                        contentView.otp_validation_layout.rootView.otp_layout?.isErrorEnabled = true
                        contentView.otp_validation_layout.rootView.otp_layout?.error =
                            getLocalisedString(context, authError.errorMsg)
                        AnalyticsUtils.sendBasicEvent(EventNames.EVENT_VALIDATE_OTP_FAILED, ScreenNames.SCREEN_LOGIN,
                            e?.localizedMessage?:getString(authError.errorMsg))
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
                    contentView.rootView.otp_validation_layout.rootView.otp_layout.editText?.setText(
                        otp
                    )
                    contentView.rootView.otp_validation_layout.rootView.otp_layout.editText?.setSelection(
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
        contentView.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        contentView = View.inflate(context, R.layout.fragment_login_bottom_sheet, null)
        phoneNumberValidationViewModel =
            ViewModelProvider(this).get(BottomSheetViewModel::class.java)
        dialog.setContentView(contentView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setCanceledOnTouchOutside(false)
        bottomSheetBehavior = BottomSheetBehavior.from(contentView.parent as View)
        handleGlobalLayoutListener()
        setViews()
    }

    private fun setViews() {
        val rootView = contentView.rootView
        rootView.phone_number_validation_layout.rootView.title.text =
            getLocalisedString(context, R.string.enter_mobile_number)
        rootView.phone_number_validation_layout.rootView.phone_number_layout.hint =
            getLocalisedString(context, R.string.mobile_number)
        rootView.phone_number_validation_layout.rootView.phone_number_layout.prefixText =
            getLocalisedString(context, R.string.country_code)

        rootView.otp_validation_layout.rootView.otptitleView.text =
            getLocalisedString(context, R.string.enter_otp)
        rootView.otp_validation_layout.rootView.otp_layout.helperText =
            getLocalisedString(context, R.string.we_have_sent_otp)
        rootView.otp_validation_layout.rootView.otp_layout.hint =
            getLocalisedString(context, R.string.otp)
        rootView.phone_number_validation_layout.rootView.why_needed.text =
            getLocalisedString(context, R.string.why_is_it_needed)
        rootView.phone_number_validation_layout.rootView.why_needed.setOnClickListener {
            onBoardingViewModel.whyNeededshown.value = true
        }
        rootView.phone_number_validation_layout.rootView.phone_num.requestFocus()
        rootView.otp_validation_layout.rootView.retry_otp.setOnClickListener {
            sendReValidationCode(
                "+91" + rootView.phone_number_validation_layout.rootView.phone_num.text.toString().trim()
            )
            rootView.otp_validation_layout.rootView.otp_layout?.error = null
            rootView.otp_validation_layout.rootView.otp_layout?.helperText =
                getLocalisedString(context, R.string.we_have_resent_otp)
        }

        rootView.phone_number_validation_layout.rootView.validate_phone.text =
            getLocalisedString(context, R.string.submit)
        rootView.phone_number_validation_layout.rootView.validate_phone.setOnClickListener {

            if (android.util.Patterns.PHONE.matcher(rootView.phone_number_validation_layout.rootView.phone_num.text.toString().trim()).matches() && rootView.phone_number_validation_layout.rootView.phone_num.text.toString().trim().length == 10) {
                if (CorUtility.isNetworkAvailable(context)) {
                    rootView.phone_number_validation_layout.rootView.progress_bar?.visibility =
                        View.VISIBLE
                    phoneNumberValidationViewModel.phoneNumberValidation.value = true
                    sendValidationCode(
                        "+91" + rootView.phone_number_validation_layout.rootView.phone_num?.text.toString().trim()
                    )
                    rootView.phone_number_validation_layout.rootView.phone_number_layout?.error =
                        null
                } else {
                    Toast.makeText(
                        context,
                        getLocalisedString(context, R.string.error_network_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                rootView.phone_number_validation_layout.rootView.phone_number_layout?.error =
                    getLocalisedString(context, R.string.please_enter_a_valid_number)
            }
        }

        rootView.phone_number_validation_layout.rootView.back.setOnClickListener {
            rootView.otp_validation_layout.rootView.progress_bar?.visibility = View.GONE
            rootView.otp_validation_layout.rootView.otp_layout?.isErrorEnabled = true
            rootView.otp_validation_layout.rootView.otp_layout?.otp_edit_text?.setText("")
            rootView.otp_validation_layout.rootView.otp_layout?.error = ""
            phoneNumberValidationViewModel.otpSent.value = false
        }

        rootView.otp_validation_layout.rootView.validate_otp.text =
            getLocalisedString(context, R.string.submit)
        rootView.otp_validation_layout.rootView.validate_otp.setOnClickListener {

            if (rootView.otp_validation_layout.rootView.otp_layout?.editText?.text.isNullOrEmpty()) {
                rootView.otp_validation_layout.rootView.otp_layout?.isErrorEnabled = true
                rootView.otp_validation_layout.rootView.otp_layout?.error =
                    getLocalisedString(context, R.string.please_enter_a_valid_otp)
            } else {
                val otp =
                    contentView.rootView.otp_validation_layout.rootView.otp_layout.editText?.text.toString()
                        .trim()
                submitOtp(otp)
            }
        }


        rootView.phone_number_validation_layout.rootView.close.setOnClickListener {
            dismissAllowingStateLoss()
        }
        activity?.let { it ->
            phoneNumberValidationViewModel.otpSent.observe(it, Observer {
                rootView.phone_number_validation_layout.rootView.progress_bar?.visibility =
                    View.GONE
                if (it) {
                    rootView.otp_validation_layout.rootView.otp_layout.requestFocus()
                    rootView.otp_validation_layout.rootView.otp_validation_layout?.visibility =
                        View.VISIBLE
                    rootView.phone_number_validation_layout?.visibility = View.GONE
                } else {
                    rootView.otp_validation_layout.rootView.otp_validation_layout?.visibility =
                        View.GONE
                    rootView.phone_number_validation_layout.rootView.phone_number_validation_layout?.visibility =
                        View.VISIBLE
                }
            })
        }
    }

    private fun submitOtp(otp: String?) {
        if (CorUtility.isNetworkAvailable(context)) {
            if (!otp.isNullOrEmpty()) {
                contentView.rootView.otp_validation_layout.rootView.otp_layout?.isErrorEnabled =
                    true
                contentView.rootView.otp_validation_layout.rootView.otp_layout?.error = ""
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
                    contentView.rootView.phone_number_validation_layout.rootView.progress_bar?.visibility =
                        View.GONE
                    contentView.rootView.phone_number_validation_layout.rootView.phone_number_layout?.error =
                        getLocalisedString(context, authError.errorMsg)
                    AnalyticsUtils.sendBasicEvent(EventNames.EVENT_GET_OTP_FAILED, ScreenNames.SCREEN_LOGIN,
                        e?.localizedMessage?:getString(authError.errorMsg))
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
                    contentView.rootView.phone_number_validation_layout.rootView.otp_layout?.error =
                        getLocalisedString(context, authError.errorMsg)
                    AnalyticsUtils.sendBasicEvent(EventNames.EVENT_GET_OTP_FAILED, ScreenNames.SCREEN_LOGIN,
                        e?.localizedMessage?:getString(authError.errorMsg))
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
        contentView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = contentView.rootView.height
        val heightDifference = screenHeight - (rect.bottom - rect.top)
        bottomSheetBehavior.peekHeight = screenHeight + heightDifference
        contentView.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }


}
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

package nic.goi.aarogyasetu.utility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern

/**
 *@author Niharika
 */

private const val OTP_REGEX = "\\d{6}"

class SmsReceiver : BroadcastReceiver() {

    private var otpReceiver: OTPListener? = null

    fun injectOTPListener(receiver: OTPListener?) {
        this.otpReceiver = receiver
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            extras?.let {
                val status = extras.get(SmsRetriever.EXTRA_STATUS) as? Status
                when (status?.statusCode) {

                    CommonStatusCodes.SUCCESS -> {

                        val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String

                        val pattern = Pattern.compile(OTP_REGEX)
                        val matcher = pattern.matcher(message)

                        if (matcher.find()) {
                            otpReceiver?.onOTPReceived(matcher.group(0))
                            return
                        } else {
                            // do nothing
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> {
                        otpReceiver?.onOTPTimeOut()
                    }
                    else -> {
                    }
                }
            }
        }
    }

    interface OTPListener {

        fun onOTPReceived(otp: String?)

        fun onOTPTimeOut()
    }
}
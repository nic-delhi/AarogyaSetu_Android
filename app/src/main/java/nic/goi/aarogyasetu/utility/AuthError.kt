package nic.goi.aarogyasetu.utility

import androidx.annotation.StringRes
import nic.goi.aarogyasetu.R

sealed class AuthError(@StringRes val errorMsg: Int)
class AuthErrorUnknown : AuthError(R.string.auth_error_unknown)
class AuthErrorInvalidOtp : AuthError(R.string.auth_error_invalid_otp)
class AuthErrorUserDisabled : AuthError(R.string.auth_error_user_disabled)
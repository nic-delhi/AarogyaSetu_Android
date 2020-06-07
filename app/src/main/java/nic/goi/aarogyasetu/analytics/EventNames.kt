package nic.goi.aarogyasetu.analytics

object EventNames {

    const val EVENT_REGISTER_LOC = "registerWithLoc"
    const val EVENT_REGISTER_WITHOUT_LOC = "registerWithoutLoc"
    const val EVENT_SHARE_CLICKED = "shareClicked"
    const val EVENT_UPI_CLICKED = "upiClicked"
    const val EVENT_GET_OTP = "getOtp"
    const val EVENT_GET_OTP_FAILED = "getOtpFailed"
    const val EVENT_VALIDATE_OTP = "validateOtp"
    const val EVENT_VALIDATE_OTP_FAILED = "validateOtpFailed"
    const val EVENT_UPLOAD_CLICKED = "uploadClicked"
    const val EVENT_SUBMIT_UPLOAD_CONSENT = "submitUploadConsent"
    const val EVENT_CONSENT_CANCELLED = "consentCancelled"

    const val EVENT_USER_SESSION_EXPIRED = "sessionExpired"


    const val EVENT_OPEN_UPLOAD_CHOICE = "uploadChoiceScreen"
    const val EVENT_OPEN_UPLOAD_CONSENT_SCREEN = "uploadConsentScreen"
    const val EVENT_OPEN_SPLASH = "splashScreen"
    const val EVENT_OPEN_ONBOARDING = "OnboardingScreen"
    const val EVENT_OPEN_ONBOARDING_AS_INFO = "infoScreen"
    const val EVENT_OPEN_PERMISSION = "permissionScreen"
    const val ADVERTISING_LEGACY_ISSUE = "AdvertisingLegacyIssue"
    const val EVENT_OPEN_WEB_VIEW = "webviewScreen"
    const val EVENT_OPEN_LANGUAGE = "languageSelectionScreen"
    const val EVENT_PHONE_ROOTED = "phoneRooted"
    const val EVENT_GOOGLE_SERVICE_RESOLVABLE_ERROR = "googleServiceResolvableError"
    const val EVENT_GOOGLE_SERVICE_ERROR_RESOLUTION_CANCEL = "googleServiceErrorResolutionCanceled"
    const val EVENT_GOOGLE_SERVICE_NON_RESOLVABLE_ERROR = "googleServiceNonResolvableError"
    const val EVENT_UPLOAD_START = "uploadStart"
    const val EVENT_UPLOAD_SUCCESS = "uploadSuccess"
    const val EVENT_UPLOAD_FAILED = "uploadFailed"
    const val EVENT_SCAN_FAILED = "eventScanFailed"

}
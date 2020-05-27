# Aarogya Setu Android app

![alt text](./aarogya.png "AarogyaSetu Logo")

Aarogya Setu is a mobile application developed by the Government of India to connect essential health services with the people of India in our combined fight against COVID-19. The App is aimed at augmenting the initiatives of the Government of India, particularly the Department of Health, in proactively reaching out to and informing the users of the app regarding risks, best practices and relevant advisories pertaining to the containment of COVID-19.

## Features

Aarogya Setu mobile application provides the following features:

- Minimal and simple user interface, which user can get easily acquainted with
- Scan nearby Aarogya Setu user using BluetoothLE Scanner 
- Advertise to nearby Aarogya Setu user using BluetoothLE GATT Server
- Update user about nearby activity using Location Service
- Secure information transfer with SSL Pinning
- Encrypt any sensitive information
- Available in 12 different languages
- Nation wide COVID-19 Statistics
- Self-Assessment as per MoHFW and ICMR guidelines
- Emergency Helpline Contact
- List of ICMR approved labs with COVID-19 testing facilities
- e-Pass integration

The Aarogya Setu App is being widely used by more than 11 Crore Users. The App has been highly successful in identifying people with high risk of COVID-19 infection and has also played a major role in identifying potential COVID-19 hotspots. In the larger public interest and in order to help the international community in their COVID-19 efforts, the Government of India is opening the source code of this App under Apache License 2.0.

If you find any security issues or vulnerabilities in the code, then you can send the details to us at : as-bugbounty@nic.in

If you want to convey any other feedback regarding the App or Code, then you can send it to us at : support.aarogyasetu@nic.in



## Setup

### Requirements
- JDK 8
- Latest Android SDK tools
- Latest Android platform tools
- Android SDK 21 or newer
- AndroidX

### Configure
- ./keystore.properties
- Firebase - google-services.json

**keystore.properties**

Setup a keystore.properties at the root folder with following sample detail and your configurations
```
# Server SSL Keys
ssl_public_key=<Your Public Key>
ssl_backup_key=<Your Backup Key>
ssl_auth_key=<Your Auth Key>
ssl_auth_backup_key=<Your Auth Backup Key>

aws_api_key=<Your AWS Key>
platform_key=android_key

# Android Keystore details
android_alias=YourAndroidAlias
android_keystore=YourAndroidKeyStore
transformation=AES/GCM/NoPadding

# BLE UUIDs
service_uuid=YOURUUID-1234-ABCD-WXYZ-A12B34C56D78
did_uuid=YOURUUID-1234-ABCD-WXYZ-A12B34C56E78
pinger_uuid=YOURUUID-1234-ABCD-WXYZ-A12B34C56F78


# API URLs
webview_url = <Your Web URL>
webview_host = <Your Web Host>
app_host_url = <Your App Host>
auth_host_url = <Your Auth Host>

# API End Points
bulk_upload_api = /api/v1/end/point/1/
register_user_api = /api/v1/end/point/2/
update_fcm_token_api = /api/v1/end/point/3/
check_status_api = /api/v1/end/point/4/
fetch_config_api = /api/v1/end/point/5/
generate_otp_api = endPoint6
validate_otp_api = endPoint7
refresh_auth_token_api = endPoint8
qr_fetch_api = endPoint9
call_us_url=tel:1075
faq_url=<Your URL>
privacy_policy_url=<Your URL>
tnc_url=<Your URL>
verify_app_url=<Your URL>

# APK sign Keystore details:
key_store_cetificate = yourCertificate.jks
key_store_password = yourStorePassword
key_alias = yourAlias
key_password = yourPassword

```

**Firebase and google-services.json**

Setup Firebase for the different environment.
Download the google-services.json for each of the environments and put it in the corresponding folder.

Debug: ./app/src/debug/google-services.json

Production: ./app/src/google-services.json


### Build
    ./gradlew assembleDebug

## Download App

<p align="center">
<a href='https://play.google.com/store/apps/details?id=nic.goi.aarogyasetu'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="50%"/></a>
</p>


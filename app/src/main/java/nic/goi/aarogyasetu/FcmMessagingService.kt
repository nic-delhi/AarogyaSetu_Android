package nic.goi.aarogyasetu

import android.app.Notification
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import nic.goi.aarogyasetu.prefs.SharedPref
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants
import nic.goi.aarogyasetu.utility.Constants
import nic.goi.aarogyasetu.utility.CorUtility
import nic.goi.aarogyasetu.utility.UploadDataUtil
import nic.goi.aarogyasetu.views.HomeActivity

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * @author Chandrapal Yadav
 */
class FcmMessagingService : FirebaseMessagingService() {

    @Override
    fun onNewToken(@NonNull s: String) {
        super.onNewToken(s)
        CorUtility.Companion.refreshFCMToken(s)
    }

    @Override
    fun onMessageReceived(@NonNull remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        setBluetoothName()
        val remoteMessageData = remoteMessage.getData()
        if (!remoteMessageData.isEmpty()) {
            pushDataToServer(remoteMessageData)
            showNotification(remoteMessageData)
        }
    }

    /**
     * This method is used to check if contact data needed by server. If yes then push the data to server
     * @param remoteMessageData: The data received from the firebase notification
     */
    private fun pushDataToServer(remoteMessageData: Map<String, String>) {
        if (remoteMessageData.containsKey(Constants.PUSH_COVID_POSTIVE_P) && Constants.COVID_POSTIVE_PUSH_P_VALUE.equals(
                remoteMessageData[Constants.PUSH_COVID_POSTIVE_P]
            )
        ) {

            CorUtility.Companion.pushDataToServer(this)
            val mUploadDataUtil = UploadDataUtil()
            mUploadDataUtil.startInBackground()

        }
    }

    /**
     * This method is used to show data to user.
     * @param remoteMessageData: The data received from the firebase notification
     */
    private fun showNotification(remoteMessageData: Map<String, String>) {
        val target =
            if (remoteMessageData.containsKey(Constants.TARGET) && !TextUtils.isEmpty(remoteMessageData[Constants.TARGET]))
                remoteMessageData[Constants.TARGET]
            else
                BuildConfig.WEB_URL

        if (!TextUtils.isEmpty(target)) {
            val targetTitle =
                if (remoteMessageData.containsKey(Constants.PAGE_TITLE) && !TextUtils.isEmpty(remoteMessageData[Constants.PAGE_TITLE]))
                    remoteMessageData[Constants.PAGE_TITLE]
                else
                    Constants.EMPTY
            val notificationIntent = HomeActivity.getLaunchIntent(target, targetTitle, this)
            if (remoteMessageData.containsKey(Constants.PUSH) && "1".equals(remoteMessageData[Constants.PUSH])) {
                val uploadType =
                    if (remoteMessageData.containsKey(Constants.UPLOAD_TYPE) && !TextUtils.isEmpty(remoteMessageData[Constants.UPLOAD_TYPE])) remoteMessageData[Constants.UPLOAD_TYPE] else Constants.UPLOAD_TYPES.PUSH_CONSENT
                notificationIntent.putExtra(Constants.UPLOAD_TYPE, uploadType)
                notificationIntent.putExtra(Constants.PUSH, true)
            }
            val pendingIntent = PendingIntent.getActivities(
                this,
                0,
                arrayOf<Intent>(notificationIntent),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) CorUtility.Companion.getNotificationChannel() else ""
            val notificationBuilder = NotificationCompat.Builder(this, channelId)
            val notification = notificationBuilder
                .setStyle(NotificationCompat.BigTextStyle().bigText(getContentText(remoteMessageData)))
                .setContentTitle(getContentTitle(remoteMessageData))
                .setContentText(getContentText(remoteMessageData))
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setColor(getResources().getColor(R.color.colorPrimary)).setAutoCancel(true)
                .setSmallIcon(R.drawable.notification_icon)
                .build()
            val notificationManager = NotificationManagerCompat.from(this)

            notificationManager.notify(NOTIFICATION_ID, notification)

        }
    }

    private fun getContentText(remoteMessageData: Map<String, String>): CharSequence {
        return if (remoteMessageData.containsKey(Constants.NOT_BODY)) {
            remoteMessageData[Constants.NOT_BODY]
        } else Constants.FIREBASE_DEFAULT_MSG_TEXT
    }

    private fun getContentTitle(remoteMessageData: Map<String, String>): CharSequence {
        return if (remoteMessageData.containsKey(Constants.NOT_TITLE)) {
            remoteMessageData[Constants.NOT_TITLE]
        } else getResources().getString(R.string.app_name)
    }

    private fun setBluetoothName() {
        val defaultAdapter = BluetoothAdapter.getDefaultAdapter()
        val uniqueId = SharedPref.getStringParams(
            CoronaApplication.getInstance(),
            SharedPrefsConstants.UNIQUE_ID,
            ""
        )
        if (CorUtility.isBluetoothPermissionAvailable(CoronaApplication.instance) && defaultAdapter != null && !uniqueId.isEmpty()) {
            defaultAdapter!!.setName(uniqueId)
        }
    }

    companion object {
        val NOTIFICATION_ID = 888
    }
}

package nic.goi.aarogyasetu;

import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import nic.goi.aarogyasetu.prefs.SharedPref;
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants;
import nic.goi.aarogyasetu.utility.Constants;
import nic.goi.aarogyasetu.utility.CorUtility;
import nic.goi.aarogyasetu.utility.UploadDataUtil;
import nic.goi.aarogyasetu.views.SplashActivity;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * @author Chandrapal Yadav
 */
public class FcmMessagingService extends FirebaseMessagingService {
    public static final int NOTIFICATION_ID = 888;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        CorUtility.Companion.refreshFCMToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        setBluetoothName();
        Map<String, String> remoteMessageData = remoteMessage.getData();
        if (!remoteMessageData.isEmpty()) {
            pushDataToServer(remoteMessageData);
            if(remoteMessageData.containsKey(Constants.IS_SILENT_NOTIFICATION)&&remoteMessageData.get(Constants.IS_SILENT_NOTIFICATION).equals("1"))
            {
                return;
            }
            showNotification(remoteMessageData);
        }
    }

    /**
     * This method is used to check if contact data needed by server. If yes then push the data to server
     * @param remoteMessageData: The data received from the firebase notification
     */
    private void pushDataToServer(Map<String, String> remoteMessageData) {
        if (remoteMessageData.containsKey(Constants.PUSH_COVID_POSTIVE_P) && Constants.COVID_POSTIVE_PUSH_P_VALUE.equals(remoteMessageData.get(Constants.PUSH_COVID_POSTIVE_P)))
        {
            CorUtility.Companion.pushDataToServer(this);
            UploadDataUtil mUploadDataUtil = new UploadDataUtil();
            mUploadDataUtil.startInBackground();

        }
    }

    /**
     * This method is used to show data to user.
     * @param remoteMessageData: The data received from the firebase notification
     */
    private void showNotification(Map<String, String> remoteMessageData) {


        String target = (remoteMessageData.containsKey(Constants.TARGET) && !TextUtils.isEmpty(remoteMessageData.get(Constants.TARGET)))
                ? remoteMessageData.get(Constants.TARGET) : BuildConfig.WEB_URL;

        if (!TextUtils.isEmpty(target)) {
            Intent notificationIntent = new Intent(this, SplashActivity.class);
            notificationIntent.putExtra(Constants.TARGET,target);
            if (remoteMessageData.containsKey(Constants.PUSH) && "1".equals(remoteMessageData.get(Constants.PUSH))) {
                String uploadType = (remoteMessageData.containsKey(Constants.UPLOAD_TYPE) && !TextUtils.isEmpty(remoteMessageData.get(Constants.UPLOAD_TYPE))) ? remoteMessageData.get(Constants.UPLOAD_TYPE) : Constants.UPLOAD_TYPES.PUSH_CONSENT;
                notificationIntent.putExtra(Constants.UPLOAD_TYPE,uploadType);
                notificationIntent.putExtra(Constants.PUSH,true);
            }

            if(remoteMessageData.containsKey(Constants.DEEPLINK_TAG))
            {
                notificationIntent.putExtra(Constants.DEEPLINK_TAG,remoteMessageData.get(Constants.DEEPLINK_TAG));
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? CorUtility.Companion.getNotificationChannel() : "";
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
            Notification notification = notificationBuilder
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getContentText(remoteMessageData)))
                    .setContentTitle(getContentTitle(remoteMessageData))
                    .setContentText(getContentText(remoteMessageData))
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                    .setColor(getResources().getColor(R.color.colorPrimary)).setAutoCancel(true)
                    .setSmallIcon(R.drawable.notification_icon)
                    .build();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            notificationManager.notify(NOTIFICATION_ID, notification);

        }
    }

    private CharSequence getContentText(Map<String, String> remoteMessageData) {
        if (remoteMessageData.containsKey(Constants.NOT_BODY)) {
            return remoteMessageData.get(Constants.NOT_BODY);
        }
        return Constants.FIREBASE_DEFAULT_MSG_TEXT;
    }

    private CharSequence getContentTitle(Map<String, String> remoteMessageData) {
        if (remoteMessageData.containsKey(Constants.NOT_TITLE)) {
            return remoteMessageData.get(Constants.NOT_TITLE);
        }
        return getResources().getString(R.string.app_name);
    }

    private void setBluetoothName() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        String uniqueId = SharedPref.getStringParams(
                CoronaApplication.getInstance(),
                SharedPrefsConstants.UNIQUE_ID,
                "");
        if (CorUtility.isBluetoothPermissionAvailable(CoronaApplication.instance) && defaultAdapter != null && !uniqueId.isEmpty()) {
            defaultAdapter.setName(uniqueId);
        }
    }
}

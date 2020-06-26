package nic.goi.aarogyasetu.utility;

import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;

import nic.goi.aarogyasetu.CoronaApplication;
import nic.goi.aarogyasetu.R;
import nic.goi.aarogyasetu.db.DBManager;
import nic.goi.aarogyasetu.db.FightCovidDB;
import nic.goi.aarogyasetu.models.WhiteListData;
import nic.goi.aarogyasetu.views.SplashActivity;

/**
 * Helper implementation to detect and handle social distancing breach
 */
public class SocialDistancingHelper {
    private String TAG = this.getClass().getName();

    private List<String> whiteListDevices = new ArrayList<>();          //Id's for devices that are whitelisted
    private List<String> currentNearDevices = new ArrayList<>();        //Id's for current nearby available devices
    private AlertDialog breachDialog;
    private static final int BREACH_RSSI_THRESHOLD = -50;

    private Context context;

    public SocialDistancingHelper(Context context) {
        this.context = context;
        //fetch current list of whiteListed devices from DB
        ExecutorHelper.getThreadPoolExecutor().execute(() -> whiteListDevices = FightCovidDB.getInstance().getWhiteListDataDao().getAllWhiteListDevices());

    }

    /**
     * Check for social distancing breach.
     *
     * @param result the result
     */
    public void checkForSocialDistancingBreach(ScanResult result) {
        /*
         * Check if nearby device is within 2 Meter (rssi -50) radius
         * */
        if (result != null && result.getRssi() > BREACH_RSSI_THRESHOLD) {
            if (!whiteListDevices.contains(result.getDevice().getAddress()) && !currentNearDevices.contains(result.getDevice().getAddress())) {
                currentNearDevices.add(result.getDevice().getAddress());
                if (CoronaApplication.getInstance().isAppInBackground()) {
                    showBreachNotification(result);
                } else {
                    showBreachAlert(result);
                }
            } else {
                Logger.d(TAG, "Nearby whitelist device found: " + result.getDevice());
            }
        }
    }

    /**
     * Show device dialog.
     *
     * @param scanResult the scan result
     */
    private void showBreachAlert(ScanResult scanResult) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, "Error while playing sound " + e.toString());
        }
        if (breachDialog != null && breachDialog.isShowing()) {
            if (currentNearDevices.size() > 1) {
                breachDialog.setMessage(context.getString(R.string.multiple_breaches_message));
            } else {
                breachDialog.setMessage(context.getString(R.string.single_breache_message));
            }
        } else {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, R.style.MaterialDialogTheme);
            dialogBuilder.setTitle(context.getString(R.string.title_distance_breach));
            dialogBuilder.setMessage(context.getString(R.string.single_breache_message));
            dialogBuilder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
                dialog.dismiss();
                currentNearDevices.clear();
            });

            //Add this device to white list if it is my own device or someone from my family
            dialogBuilder.setNeutralButton(context.getString(R.string.btn_white_list_device), (dialog, which) -> {
                DBManager.insertWhiteListData(new WhiteListData(scanResult.getDevice().getName(), scanResult.getDevice().getAddress()));
                dialog.dismiss();
                currentNearDevices.clear();
            });
            breachDialog = dialogBuilder.create();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                breachDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY - 1);
            } else {
                breachDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
            breachDialog.show();
        }
    }

    /**
     * Show breach notification.
     *
     * @param scanResult the scan result
     */
    private void showBreachNotification(ScanResult scanResult) {
        Notification notification = createBreachNotification(scanResult);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(12, notification);
    }

    /**
     * Creates breach notification.
     *
     * @param scanResult the scan result
     * @return the breach notification
     */
    private Notification createBreachNotification(ScanResult scanResult) {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Intent notificationIntent = new Intent(context, SplashActivity.class);
        PendingIntent notificationClickIntent = PendingIntent.getActivities(context, 0, new Intent[]{notificationIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        String notificationDescText = context.getString(R.string.single_breache_message);

        // Add action button in the notification
        Intent snoozeIntent = new Intent(context, WhiteListBroadcastReceiver.class);
        snoozeIntent.setAction(Constants.ACTION_WHITELIST_DEVICE);
        snoozeIntent.putExtra(Constants.ARGS_DEVICE_ADDRESS, scanResult.getDevice().getAddress());
        snoozeIntent.putExtra(Constants.ARGS_DEVICE_NAME, scanResult.getDevice().getName());
        snoozeIntent.putExtra(Constants.ARGS_NOTIFICATION_ID, 12);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(context, 0, snoozeIntent, 0);

        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? CorUtility.Companion.getNotificationChannel() : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(context.getString(R.string.app_name));
        bigTextStyle.bigText(notificationDescText);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

        return notificationBuilder
                .setStyle(bigTextStyle)
                .setContentTitle(context.getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentText(notificationDescText)
                .setContentIntent(notificationClickIntent)
                .setOngoing(true)
                .setSound(soundUri)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setSmallIcon(R.drawable.notification_icon)
                .addAction(R.drawable.notification_icon, context.getString(R.string.btn_white_list_device),
                        snoozePendingIntent)
                .build();
    }

    /**
     * OnDestroy. to clear service references
     */
    public void onDestroy() {
        context = null;
    }
}

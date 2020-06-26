package nic.goi.aarogyasetu.utility;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import nic.goi.aarogyasetu.db.DBManager;
import nic.goi.aarogyasetu.models.WhiteListData;

/**
 * The WhiteListBroadcastReceiver will receive broadcast when user tap on "Add as family member" from notification
 */
public class WhiteListBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra(Constants.ARGS_NOTIFICATION_ID, 0);
        String deviceId = intent.getStringExtra(Constants.ARGS_DEVICE_ADDRESS);
        String deviceName = intent.getStringExtra(Constants.ARGS_DEVICE_NAME);
        //Insert device id into whitelist DB table
        DBManager.insertWhiteListData(new WhiteListData(deviceName!= null? deviceName: "", deviceId));
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }
}

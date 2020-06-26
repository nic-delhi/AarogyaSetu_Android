package nic.goi.aarogyasetu.db;


import nic.goi.aarogyasetu.models.BluetoothData;
import nic.goi.aarogyasetu.models.WhiteListData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Damanpreet.Singh
 */
public class DBManager {


    private static int numCores = Runtime.getRuntime().availableProcessors();
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(numCores * 2, numCores * 2,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    public static Task<List<Long>> insertNearbyDetectedDeviceInfo(List<BluetoothData> userDeviceInfoList) {

        return Tasks.call(executor, () -> FightCovidDB.getInstance().getBluetoothDataDao().insertAllNearbyUsers(userDeviceInfoList));
    }

    public static Task<Long> insertNearbyDetectedDeviceInfo(BluetoothData userDeviceInfo) {

        return Tasks.call(executor, () -> FightCovidDB.getInstance().getBluetoothDataDao().insertNearbyUser(userDeviceInfo));
    }
    public static Task<Long> insertWhiteListData(WhiteListData whiteListData) {
        return Tasks.call(executor, () -> FightCovidDB.getInstance().getWhiteListDataDao().insertWhiteListDevice(whiteListData));
    }

}

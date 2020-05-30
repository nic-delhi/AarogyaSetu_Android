package nic.goi.aarogyasetu.db


import nic.goi.aarogyasetu.models.BluetoothData

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author Damanpreet.Singh
 */
object DBManager {


    private val numCores = Runtime.getRuntime().availableProcessors()
    private val executor = ThreadPoolExecutor(
        numCores * 2, numCores * 2,
        60L, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>()
    )

    fun insertNearbyDetectedDeviceInfo(userDeviceInfoList: List<BluetoothData>): Task<List<Long>> {

        return Tasks.call(
            executor,
            { FightCovidDB.getInstance().getBluetoothDataDao().insertAllNearbyUsers(userDeviceInfoList) })
    }

    fun insertNearbyDetectedDeviceInfo(userDeviceInfo: BluetoothData): Task<Long> {

        return Tasks.call(
            executor,
            { FightCovidDB.getInstance().getBluetoothDataDao().insertNearbyUser(userDeviceInfo) })
    }
}

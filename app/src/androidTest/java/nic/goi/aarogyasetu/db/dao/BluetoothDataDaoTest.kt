package nic.goi.aarogyasetu.db.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import nic.goi.aarogyasetu.db.FightCovidDB
import nic.goi.aarogyasetu.models.BluetoothData
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BluetoothDataDaoTest {
    private lateinit var database: FightCovidDB
    private lateinit var bluetoothDataDao: BluetoothDataDao
    private val dataA = BluetoothData("abc1001", 20, "", "")
    private val dataB = BluetoothData("abc1002", 20, "", "")
    private val dataC = BluetoothData("abc1003", 20, "", "")
    private val dataD = BluetoothData("abc1004", 20, "", "")

//    @get:Rule
//    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FightCovidDB::class.java).build()
        bluetoothDataDao = database.bluetoothDataDao

        dataA.timeStamp = dataA.timeStamp - (10 * DAY)
        dataB.timeStamp = dataB.timeStamp - (30 * DAY)
        dataC.timeStamp = dataC.timeStamp - (20 * DAY)
        bluetoothDataDao.insertAllNearbyUsers(listOf(dataB, dataC, dataA))
        bluetoothDataDao.insertNearbyUser(dataD)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testAllNearbyDevices() {
        val bluetoothDataList = bluetoothDataDao.allNearbyDevices
        Assert.assertThat(bluetoothDataList.size, Matchers.equalTo(4))

        Assert.assertThat(
            bluetoothDataList[0].bluetoothMacAddress,
            Matchers.equalTo(dataB.bluetoothMacAddress)
        )
        Assert.assertThat(
            bluetoothDataList[1].bluetoothMacAddress,
            Matchers.equalTo(dataC.bluetoothMacAddress)
        )
        Assert.assertThat(
            bluetoothDataList[2].bluetoothMacAddress,
            Matchers.equalTo(dataA.bluetoothMacAddress)
        )
        Assert.assertThat(
            bluetoothDataList[3].bluetoothMacAddress,
            Matchers.equalTo(dataD.bluetoothMacAddress)
        )
    }

    @Test
    fun testRowCount() {
        val count = bluetoothDataDao.rowCount
        Assert.assertThat(count, Matchers.equalTo(4L))
    }

    @Test
    fun testGetFirstXNearbyDeviceInfo() {
        val bluetoothDataList = bluetoothDataDao.getFirstXNearbyDeviceInfo(3)
        Assert.assertThat(bluetoothDataList.size, Matchers.equalTo(3))

        Assert.assertThat(
            bluetoothDataList[0].bluetoothMacAddress,
            Matchers.equalTo(dataB.bluetoothMacAddress)
        )
        Assert.assertThat(
            bluetoothDataList[1].bluetoothMacAddress,
            Matchers.equalTo(dataC.bluetoothMacAddress)
        )
        Assert.assertThat(
            bluetoothDataList[2].bluetoothMacAddress,
            Matchers.equalTo(dataA.bluetoothMacAddress)
        )
    }

    @Test
    fun testGetXNearbyDeviceInfoWithOffset() {
        val bluetoothDataList = bluetoothDataDao.getXNearbyDeviceInfoWithOffset(3, 1)
        Assert.assertThat(bluetoothDataList.size, Matchers.equalTo(3))

        Assert.assertThat(
            bluetoothDataList[0].bluetoothMacAddress,
            Matchers.equalTo(dataC.bluetoothMacAddress)
        )
        Assert.assertThat(
            bluetoothDataList[1].bluetoothMacAddress,
            Matchers.equalTo(dataA.bluetoothMacAddress)
        )
        Assert.assertThat(
            bluetoothDataList[2].bluetoothMacAddress,
            Matchers.equalTo(dataD.bluetoothMacAddress)
        )
    }

    @Test
    fun testDeleteXDaysOldData() {
        val currentTimeInSec = (System.currentTimeMillis() / 1000).toInt()
        val count = bluetoothDataDao.deleteXDaysOldData(DAY, currentTimeInSec)
        Assert.assertThat(count, Matchers.equalTo(3))
    }

    @Test
    fun testDeleteAll() {
        val bluetoothDataList = bluetoothDataDao.allNearbyDevices
        bluetoothDataDao.deleteAll(bluetoothDataList)
        val count = bluetoothDataDao.rowCount
        Assert.assertThat(count, Matchers.equalTo(0L))
    }

    companion object {
        private const val DAY = 24 * 60 * 60
    }

}
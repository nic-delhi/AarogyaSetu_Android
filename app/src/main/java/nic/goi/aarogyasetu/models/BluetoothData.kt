package nic.goi.aarogyasetu.models

import android.os.Build

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

import nic.goi.aarogyasetu.utility.CorUtility
import nic.goi.aarogyasetu.utility.EncryptionUtil
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.SignatureException
import java.security.UnrecoverableEntryException
import java.security.cert.CertificateException
import java.util.Calendar

import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

@TypeConverters(Converters::class)
@Entity(tableName = "nearby_devices_info_table")
class BluetoothData(// This is not MAC Address. This is UNIQUE ID Assign to scanned device.
    @field:ColumnInfo(name = "bluetooth_mac_address")
    @field:SerializedName("d")
    @field:Expose
    var bluetoothMacAddress: String?, // This is the RSSI of the scanned device.
    @field:ColumnInfo(name = "distance")
    @field:SerializedName("dist")
    @field:Expose
    var distance: Integer?, @field:ColumnInfo(name = "tx_power")
    @field:SerializedName("tx_power")
    @field:Expose
    var txPower: String?, @field:ColumnInfo(name = "tx_power_level")
    @field:SerializedName("tx_power_level")
    @field:Expose
    var txPowerLevel: String?
) {


    @ColumnInfo(name = "id")
    @SerializedName("id")
    @Expose
    var id: Int = 0

    @Ignore
    var latitude: Double = 0.toDouble()
        set(latitude) = setEncLatitute(latitude)

    @Ignore
    var longitude: Double = 0.toDouble()
        set(longitude) = setEncLongitude(longitude)

    @ColumnInfo(name = "lat")
    @SerializedName("lat")
    @Expose
    var latitudeenc: EncryptedInfo? = null

    @ColumnInfo(name = "long")
    @SerializedName("long")
    @Expose
    var longitudeenc: EncryptedInfo? = null

    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    @SerializedName("ts")
    @Expose
    var timeStamp: Integer? = null


    init {
        timeStamp = CorUtility.Companion.getCurrentEpochTimeInSec()
    }

    private fun setEncLatitute(latitude: Double) {
        val encryptedInfo = EncryptedInfo()
        try {
            encryptedInfo.setData(EncryptionUtil.getInstance().encryptText(String.valueOf(latitude)))
            encryptedInfo.setIv(EncryptionUtil.getInstance().getIv())
        } catch (e: UnrecoverableEntryException) {
            //do nothing
        } catch (e: NoSuchAlgorithmException) {
        } catch (e: KeyStoreException) {
        } catch (e: NoSuchPaddingException) {
        } catch (e: InvalidKeyException) {
        } catch (e: InvalidAlgorithmParameterException) {
        } catch (e: IOException) {
        } catch (e: SignatureException) {
        } catch (e: BadPaddingException) {
        } catch (e: IllegalBlockSizeException) {
        } catch (e: CertificateException) {
        } catch (e: NoSuchProviderException) {
        }

        this.latitudeenc = encryptedInfo
    }

    private fun setEncLongitude(longitude: Double) {
        val encryptedInfo = EncryptedInfo()
        try {
            encryptedInfo.setData(EncryptionUtil.getInstance().encryptText(String.valueOf(longitude)))
            encryptedInfo.setIv(EncryptionUtil.getInstance().getIv())
        } catch (e: UnrecoverableEntryException) {
            //do nothing
        } catch (e: NoSuchAlgorithmException) {
        } catch (e: KeyStoreException) {
        } catch (e: NoSuchPaddingException) {
        } catch (e: InvalidKeyException) {
        } catch (e: InvalidAlgorithmParameterException) {
        } catch (e: IOException) {
        } catch (e: SignatureException) {
        } catch (e: BadPaddingException) {
        } catch (e: IllegalBlockSizeException) {
        } catch (e: CertificateException) {
        } catch (e: NoSuchProviderException) {
        }

        this.longitudeenc = encryptedInfo
    }
}

package nic.goi.aarogyasetu.models;

import android.os.Build;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import nic.goi.aarogyasetu.utility.CorUtility;
import nic.goi.aarogyasetu.utility.EncryptionUtil;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

@TypeConverters({Converters.class})
@Entity(tableName = "nearby_devices_info_table")
public class BluetoothData {


    @ColumnInfo(name = "id")
    @SerializedName("id")
    @Expose
    private int id;

    // This is not MAC Address. This is UNIQUE ID Assign to scanned device.
    @ColumnInfo(name = "bluetooth_mac_address")
    @SerializedName("d")
    @Expose
    private String bluetoothMacAddress;

    // This is the RSSI of the scanned device.
    @ColumnInfo(name = "distance")
    @SerializedName("dist")
    @Expose
    private Integer distance;

    @ColumnInfo(name = "tx_power")
    @SerializedName("tx_power")
    @Expose
    private String txPower;

    @ColumnInfo(name = "tx_power_level")
    @SerializedName("tx_power_level")
    @Expose
    private String txPowerLevel;

    @Ignore
    private double latitude;

    @Ignore
    private double longitude;

    @ColumnInfo(name = "lat")
    @SerializedName("lat")
    @Expose
    private EncryptedInfo latitudeenc;

    @ColumnInfo(name = "long")
    @SerializedName("long")
    @Expose
    private EncryptedInfo longitudeenc;

    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    @SerializedName("ts")
    @Expose
    private Integer timeStamp;


    public BluetoothData(String bluetoothMacAddress, Integer distance, String txPower, String txPowerLevel) {
        this.bluetoothMacAddress = bluetoothMacAddress;
        this.distance = distance;
        this.txPower = txPower;
        this.txPowerLevel = txPowerLevel;
        timeStamp = CorUtility.Companion.getCurrentEpochTimeInSec();
    }

    private void setEncLatitute(double latitude) {
        EncryptedInfo encryptedInfo = new EncryptedInfo();
        try {
            encryptedInfo.setData(EncryptionUtil.getInstance().encryptText(String.valueOf(latitude)));
            encryptedInfo.setIv(EncryptionUtil.getInstance().getIv());
        } catch (UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException | NoSuchPaddingException |
                InvalidKeyException | InvalidAlgorithmParameterException | IOException | SignatureException | BadPaddingException | IllegalBlockSizeException | CertificateException | NoSuchProviderException e) {
            //do nothing
        }
        this.latitudeenc = encryptedInfo;
    }

    private void setEncLongitude(double longitude) {
        EncryptedInfo encryptedInfo = new EncryptedInfo();
        try {
            encryptedInfo.setData(EncryptionUtil.getInstance().encryptText(String.valueOf(longitude)));
            encryptedInfo.setIv(EncryptionUtil.getInstance().getIv());
        } catch (UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException | NoSuchPaddingException |
                InvalidKeyException | InvalidAlgorithmParameterException | IOException | SignatureException | BadPaddingException | IllegalBlockSizeException | CertificateException | NoSuchProviderException e) {
            //do nothing
        }
        this.longitudeenc = encryptedInfo;
    }

    public String getBluetoothMacAddress() {
        return bluetoothMacAddress;
    }

    public void setBluetoothMacAddress(String bluetoothMacAddress) {
        this.bluetoothMacAddress = bluetoothMacAddress;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EncryptedInfo getLatitudeenc() {
        return latitudeenc;
    }

    public void setLatitudeenc(EncryptedInfo latitudeenc) {
        this.latitudeenc = latitudeenc;
    }

    public EncryptedInfo getLongitudeenc() {
        return longitudeenc;
    }

    public void setLongitudeenc(EncryptedInfo longitudeenc) {
        this.longitudeenc = longitudeenc;
    }

    public Integer getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Integer timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        setEncLatitute(latitude);
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        setEncLongitude(longitude);
    }

    public String getTxPower() {
        return txPower;
    }

    public void setTxPower(String txPower) {
        this.txPower = txPower;
    }

    public String getTxPowerLevel() {
        return txPowerLevel;
    }

    public void setTxPowerLevel(String txPowerLevel) {
        this.txPowerLevel = txPowerLevel;
    }
}

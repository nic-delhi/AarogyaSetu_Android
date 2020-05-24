package nic.goi.aarogyasetu.models


data class BluetoothModel(
    val name: String?,
    val address: String?,
    val rssi: Int? = 0,
    val txPower: String? = "",
    val txPowerLevel: String? = ""
)
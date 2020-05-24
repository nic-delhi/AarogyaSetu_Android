package nic.goi.aarogyasetu.utility.authsp

interface AuthSpHelper {

    fun getString(key: String, defaultValue: String?): String?
    fun putString(key: String, value: String?)
    fun removeKey(key: String)
}
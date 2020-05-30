package nic.goi.aarogyasetu.models

import androidx.room.TypeConverter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.lang.reflect.Type

/**
 * @author Niharika.Arora
 */
object Converters {
    @TypeConverter
    fun fromString(value: String): EncryptedInfo {
        val listType = object : TypeToken<EncryptedInfo>() {

        }.getType()
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: EncryptedInfo): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}
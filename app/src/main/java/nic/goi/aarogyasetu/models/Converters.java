package nic.goi.aarogyasetu.models;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * @author Niharika.Arora
 */
public class Converters {
    @TypeConverter
    public static EncryptedInfo fromString(String value) {
        Type listType = new TypeToken<EncryptedInfo>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(EncryptedInfo list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}
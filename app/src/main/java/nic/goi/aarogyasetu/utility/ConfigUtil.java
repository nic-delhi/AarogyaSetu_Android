package nic.goi.aarogyasetu.utility;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
    Util class to load value in properties file
 */
public class ConfigUtil {
    private static final String TAG = ConfigUtil.class.getSimpleName();

    public static String getProperty(String key, Context context) {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open("config.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            Logger.e(TAG, "Error in parsing config.properties file " + e.getMessage());
        }
        Logger.d(TAG, key + " :> " + properties.getProperty(key));
        return properties.getProperty(key);
    }
}
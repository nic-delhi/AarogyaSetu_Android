package nic.goi.aarogyasetu.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import nic.goi.aarogyasetu.CoronaApplication;
import nic.goi.aarogyasetu.db.dao.BluetoothDataDao;
import nic.goi.aarogyasetu.db.dao.WhiteListDataDao;
import nic.goi.aarogyasetu.models.BluetoothData;
import nic.goi.aarogyasetu.models.WhiteListData;


@Database(entities = {BluetoothData.class, WhiteListData.class}, version = 3, exportSchema = false)
public abstract class FightCovidDB extends RoomDatabase {

    private static FightCovidDB sInstance;

    private static final String DATABASE_NAME = "fight-covid-db";

    public static FightCovidDB getInstance() {
        Context context = CoronaApplication.getInstance().getApplicationContext();
        if (sInstance == null) {
            sInstance = buildDatabase(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Build the database. {@link Builder#build()} only sets up the database configuration and
     * creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time.
     */
    private static FightCovidDB buildDatabase(final Context appContext) {//}, final AppExecutors executors) {
        return Room.databaseBuilder(appContext, FightCovidDB.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                    }
                })
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .build();
    }

    /**
     * Migration to remove unused fields "latitude" and "longitude" from table "nearby_devices_info_table"
     * The table already has encrypted fields for the same therefore we got rid of the redundant, unencrypted, unused fields
     */
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            //"Migrating from database version 1 to database version 2");
            database.execSQL("BEGIN TRANSACTION");

            database.execSQL("CREATE TABLE nearby_devices_info_table_backup " +
                    "(id INTEGER NOT NULL, bluetooth_mac_address TEXT,distance INTEGER, lat TEXT,long TEXT,timestamp INTEGER, PRIMARY KEY(timestamp))");

            database.execSQL("INSERT INTO nearby_devices_info_table_backup SELECT id,bluetooth_mac_address,distance,lat,long,timestamp FROM nearby_devices_info_table");
            database.execSQL("DROP TABLE nearby_devices_info_table");
            database.execSQL("ALTER TABLE nearby_devices_info_table_backup RENAME TO nearby_devices_info_table");
            database.execSQL("COMMIT");
            //"Migrated from database version 1 to database version 2 completed.");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2,3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            //"Migrating from database version 2 to database version 3");
            database.execSQL("BEGIN TRANSACTION");
            database.execSQL("DROP TABLE user_device_info_table");
            database.execSQL("DROP TABLE user_location");
            database.execSQL("COMMIT");
            //"Migrated from database version 2 to database version 3 completed.");
        }
    };

    public abstract BluetoothDataDao getBluetoothDataDao();

    public abstract WhiteListDataDao getWhiteListDataDao();

}

package com.nexturninc.biyaheplus_driver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by imbisibol on 2/17/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "BiyaheOfflineData.db";

    public static DatabaseHelper sqlHelperInstance;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sqlHelperInstance == null) {
            sqlHelperInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sqlHelperInstance;
    }

    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(Database_UserProfileContract.SQL_CREATE_TABLE);
        db.execSQL(Database_VehicleContract.SQL_CREATE_TABLE);
        db.execSQL(Database_RideContract.SQL_CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(Database_UserProfileContract.SQL_DELETE_TABLE);
        db.execSQL(Database_VehicleContract.SQL_DELETE_TABLE);
        db.execSQL(Database_RideContract.SQL_DELETE_TABLE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}

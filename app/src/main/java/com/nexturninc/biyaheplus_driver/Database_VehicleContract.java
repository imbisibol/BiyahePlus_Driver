package com.nexturninc.biyaheplus_driver;

import android.provider.BaseColumns;

/**
 * Created by imbisibol on 2/17/2016.
 */
public class Database_VehicleContract {

    public Database_VehicleContract() {
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String DECIMAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + Vehicle.TABLE_NAME + " (" +
                    Vehicle._ID + " INTEGER PRIMARY KEY," +
                    Vehicle.COLUMN_NAME_Id + TEXT_TYPE + COMMA_SEP +
                    Vehicle.COLUMN_NAME_UserId + TEXT_TYPE + COMMA_SEP +
                    Vehicle.COLUMN_NAME_PlateNo + TEXT_TYPE + COMMA_SEP +
                    Vehicle.COLUMN_NAME_VehicleType + TEXT_TYPE + COMMA_SEP +
                    Vehicle.COLUMN_NAME_LocLong + DECIMAL_TYPE + COMMA_SEP +
                    Vehicle.COLUMN_NAME_LocLat + DECIMAL_TYPE + COMMA_SEP +
                    Vehicle.COLUMN_NAME_DriverName + TEXT_TYPE + COMMA_SEP +
                    Vehicle.COLUMN_NAME_DriverPhoto + TEXT_TYPE + COMMA_SEP +
                    Vehicle.COLUMN_NAME_Status + TEXT_TYPE +
                    " )";
    public static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + Vehicle.TABLE_NAME;

    /* Inner class that defines the table contents */
    public static abstract class Vehicle implements BaseColumns {
        public static final String TABLE_NAME = "Vehicle";
        public static final String COLUMN_NAME_Id = "Id";
        public static final String COLUMN_NAME_UserId = "UserId";
        public static final String COLUMN_NAME_PlateNo = "PlateNo";
        public static final String COLUMN_NAME_VehicleType = "VehicleType";
        public static final String COLUMN_NAME_LocLong = "LocLong";
        public static final String COLUMN_NAME_LocLat = "LocLat";
        public static final String COLUMN_NAME_DriverName = "DriverName";
        public static final String COLUMN_NAME_DriverPhoto = "DriverPhoto";
        public static final String COLUMN_NAME_Status = "Status";

    }

}

package com.nexturninc.biyaheplus_driver;

import android.provider.BaseColumns;


public class Database_RideContract {

    public Database_RideContract() {
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String DATE_TYPE = " DATETIME";
    private static final String DECIMAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + Ride.TABLE_NAME + " (" +
                    Ride._ID + " INTEGER PRIMARY KEY," +
                    Ride.COLUMN_NAME_Id + TEXT_TYPE + COMMA_SEP +
                    Ride.COLUMN_NAME_CommuterId + TEXT_TYPE + COMMA_SEP +
                    Ride.COLUMN_NAME_CommuterName + TEXT_TYPE + COMMA_SEP +
                    Ride.COLUMN_NAME_CommuterPhoto + TEXT_TYPE + COMMA_SEP +
                    Ride.COLUMN_NAME_VehicleId + TEXT_TYPE + COMMA_SEP +
                    Ride.COLUMN_NAME_OrigLocation + TEXT_TYPE + COMMA_SEP +
                    Ride.COLUMN_NAME_OrigLong + DECIMAL_TYPE + COMMA_SEP +
                    Ride.COLUMN_NAME_OrigLat + DECIMAL_TYPE + COMMA_SEP +
                    Ride.COLUMN_NAME_DestLocation + TEXT_TYPE + COMMA_SEP +
                    Ride.COLUMN_NAME_DestLong + DECIMAL_TYPE + COMMA_SEP +
                    Ride.COLUMN_NAME_DestLat + DECIMAL_TYPE + COMMA_SEP +
                    Ride.COLUMN_NAME_Status + TEXT_TYPE + COMMA_SEP +
                    Ride.COLUMN_NAME_LastUpdated + DATE_TYPE +
                    " )";
    public static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + Ride.TABLE_NAME;

    /* Inner class that defines the table contents */
    public static abstract class Ride implements BaseColumns {
        public static final String TABLE_NAME = "Ride";
        public static final String COLUMN_NAME_Id = "Id";
        public static final String COLUMN_NAME_CommuterId = "CommuterId";
        public static final String COLUMN_NAME_CommuterName = "CommuterName";
        public static final String COLUMN_NAME_CommuterPhoto = "CommuterPhoto";
        public static final String COLUMN_NAME_VehicleId = "VehicleId";
        public static final String COLUMN_NAME_OrigLocation = "OrigLocation";
        public static final String COLUMN_NAME_OrigLong = "OrigLong";
        public static final String COLUMN_NAME_OrigLat = "OrigLat";
        public static final String COLUMN_NAME_DestLocation = "DestLocation";
        public static final String COLUMN_NAME_DestLong = "DestLong";
        public static final String COLUMN_NAME_DestLat = "DestLat";
        public static final String COLUMN_NAME_Status = "Status";
        public static final String COLUMN_NAME_LastUpdated = "DateModified";
    }

}

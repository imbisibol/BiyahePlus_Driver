package com.nexturninc.biyaheplus_driver;

import android.provider.BaseColumns;

/**
 * Created by imbisibol on 2/17/2016.
 */
public class Database_UserProfileContract {

    public Database_UserProfileContract() {
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String DATE_TYPE = " DATETIME";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + UserProfile.TABLE_NAME + " (" +
                    UserProfile._ID + " INTEGER PRIMARY KEY," +
                    UserProfile.COLUMN_NAME_UserId + TEXT_TYPE + COMMA_SEP +
                    UserProfile.COLUMN_NAME_Username + TEXT_TYPE + COMMA_SEP +
                    UserProfile.COLUMN_NAME_Email + TEXT_TYPE + COMMA_SEP +
                    UserProfile.COLUMN_NAME_FirstName + TEXT_TYPE + COMMA_SEP +
                    UserProfile.COLUMN_NAME_LastName + TEXT_TYPE + COMMA_SEP +
                    UserProfile.COLUMN_NAME_AvatarURL + TEXT_TYPE +
                    " )";
    public static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + UserProfile.TABLE_NAME;

    /* Inner class that defines the table contents */
    public static abstract class UserProfile implements BaseColumns {
        public static final String TABLE_NAME = "UserProfile";
        public static final String COLUMN_NAME_UserId = "UserId";
        public static final String COLUMN_NAME_Username = "UserName";
        public static final String COLUMN_NAME_FirstName = "FirstName";
        public static final String COLUMN_NAME_LastName = "LastName";
        public static final String COLUMN_NAME_Email = "Email";
        public static final String COLUMN_NAME_AvatarURL = "AvatarURL";

    }
}

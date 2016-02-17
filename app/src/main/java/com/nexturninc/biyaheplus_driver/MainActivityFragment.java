package com.nexturninc.biyaheplus_driver;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ProgressBar mProgressBar;
    private View mStampListBody;
    String UserID;

    ImageView imgAvatarURL;
    TextView lblFirstName;
    TextView lblLastName;

    ImageView imgCommuterAvatar;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //SHARED PREFERENCES
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(getContext());
        UserID = mSettings.getString(getString(R.string.SHARE_PREF_UserId), null);

        //INITIALIZE CONTROLS
        imgAvatarURL = (ImageView)rootView.findViewById(R.id.imgAvatarURL);
        lblFirstName = (TextView)rootView.findViewById(R.id.lblFirstName);
        lblLastName = (TextView)rootView.findViewById(R.id.lblLastName);

        imgCommuterAvatar = (ImageView)rootView.findViewById(R.id.imgCommuterAvatar);

        LoadProfileData(UserID);

        return rootView;
    }


    private void LoadProfileData(String id) {

        DatabaseHelper helper = DatabaseHelper.getInstance(getContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        //GET DATA FROM DB
        String[] projection = {
                Database_UserProfileContract.UserProfile.COLUMN_NAME_AvatarURL,
                Database_UserProfileContract.UserProfile.COLUMN_NAME_Email,
                Database_UserProfileContract.UserProfile.COLUMN_NAME_FirstName,
                Database_UserProfileContract.UserProfile.COLUMN_NAME_LastName,
                Database_UserProfileContract.UserProfile.COLUMN_NAME_UserId,
                Database_UserProfileContract.UserProfile.COLUMN_NAME_Username,
        };

        Cursor c = db.query(
                Database_UserProfileContract.UserProfile.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                Database_UserProfileContract.UserProfile.COLUMN_NAME_UserId + " = ?",                                // The columns for the WHERE clause
                new String[]{id},                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        if(c.moveToNext()) {

            Common.getImageLoader(null).displayImage(c.getString(c.getColumnIndexOrThrow(Database_UserProfileContract.UserProfile.COLUMN_NAME_AvatarURL)), imgAvatarURL);
            lblFirstName.setText(c.getString(c.getColumnIndexOrThrow(Database_UserProfileContract.UserProfile.COLUMN_NAME_FirstName)));
            lblLastName.setText(c.getString(c.getColumnIndexOrThrow(Database_UserProfileContract.UserProfile.COLUMN_NAME_LastName)) + ",");

            //temp
            Common.getImageLoader(null).displayImage(c.getString(c.getColumnIndexOrThrow(Database_UserProfileContract.UserProfile.COLUMN_NAME_AvatarURL)), imgCommuterAvatar);
        }

        /*
        //CONTROLS
        TextView lblLoyaltyTitle = (TextView)findViewById(R.id.lblLoyaltyTitle);
        TextView lblVolume = (TextView)findViewById(R.id.lblVolume);
        TextView lblCardPrice = (TextView) findViewById(R.id.lblCardPrice);
        TextView lblExpiryDate = (TextView) findViewById(R.id.lblExpiryDate);
        ImageView imgCard = (ImageView) findViewById(R.id.imgCard);
        ImageView imgQRCode = (ImageView) findViewById(R.id.imgQRCode);

        if(c.moveToNext()) {
            lblLoyaltyTitle.setText(c.getString(c.getColumnIndexOrThrow(MerchantLoyaltyContract.MerchantLoyaltyInformation.COLUMN_NAME_Title)));
            lblVolume.setText("AVAILABLE: " + c.getString(c.getColumnIndexOrThrow(MerchantLoyaltyContract.MerchantLoyaltyInformation.COLUMN_NAME_Volume)));
            lblCardPrice.setText(getString(R.string.Currency) + c.getString(c.getColumnIndexOrThrow(MerchantLoyaltyContract.MerchantLoyaltyInformation.COLUMN_NAME_Card_Price)));
            lblExpiryDate.setText(c.getString(c.getColumnIndexOrThrow(MerchantLoyaltyContract.MerchantLoyaltyInformation.COLUMN_NAME_Date_Expiration)));
            lblExpiryDate.setText(lblExpiryDate.getText().toString().substring(0, lblExpiryDate.getText().toString().indexOf("T")));
            Common.getImageLoader(null).displayImage(c.getString(c.getColumnIndexOrThrow(MerchantLoyaltyContract.MerchantLoyaltyInformation.COLUMN_NAME_Loyalty_Card_Image)), imgCard);Common.getImageLoader(null).displayImage(c.getString(c.getColumnIndexOrThrow(MerchantLoyaltyContract.MerchantLoyaltyInformation.COLUMN_NAME_Loyalty_Card_QR)), imgQRCode);

        }
        */

    }

}

package com.nexturninc.biyaheplus_driver;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ProgressBar mProgressBar;
    private View mStampListBody;
    String UserID;
    String VehicleId;
    Integer RideId;
    Timer timer;
    MyTimerTask myTimerTask;

    private CommuterRequestTask mAuthTask = null;
    private AcceptBookingTask mBookTask = null;

    //CONTROLS
    LinearLayout dvOperatorRequest = null;
    ImageView imgAvatarURL;
    TextView lblFirstName;
    TextView lblLastName;

    TextView lblNoRequestLabel;

    TextView lblRideId;
    TextView lblRideDate;
    ImageView imgCommuterAvatar;
    TextView lblCommuterName;
    TextView lblCommuterOrigin;
    TextView lblCommuterDestination;

    ImageButton ibtnAccept;

    TextView lblUpdating;


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
        timer = new Timer();
        myTimerTask = new MyTimerTask();
        lblUpdating = (TextView)rootView.findViewById(R.id.lblUpdating);
        timer.schedule(myTimerTask, 60000, 60000);

        dvOperatorRequest = (LinearLayout) rootView.findViewById(R.id.dvOperatorRequest);
        imgAvatarURL = (ImageView) rootView.findViewById(R.id.imgAvatarURL);
        lblFirstName = (TextView) rootView.findViewById(R.id.lblFirstName);
        lblLastName = (TextView) rootView.findViewById(R.id.lblLastName);

        lblNoRequestLabel = (TextView)rootView.findViewById(R.id.lblNoRequestLabel);

        lblRideId = (TextView)rootView.findViewById(R.id.lblRideId);
        lblRideDate = (TextView)rootView.findViewById(R.id.lblRideDate);
        imgCommuterAvatar = (ImageView) rootView.findViewById(R.id.imgCommuterAvatar);
        lblCommuterName = (TextView) rootView.findViewById(R.id.lblCommuterName);
        lblCommuterOrigin = (TextView)rootView.findViewById(R.id.lblCommuterOrigin);
        lblCommuterDestination = (TextView)rootView.findViewById(R.id.lblCommuterDestination);

        ibtnAccept = (ImageButton)rootView.findViewById(R.id.ibtnAccept);
        ibtnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Common.GetInternetConnectivity((ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE))) {
                    Toast toast = Toast.makeText(getContext(), R.string.InternetConnectionMessage, Toast.LENGTH_SHORT);
                    toast.show();
                } else {

                    AcceptBooking(RideId);

                }
            }
        });

        LoadProfileData(UserID);
        LoadVehicleData(UserID);
        LoadCommuterRequests(VehicleId);


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

        if (c.moveToNext()) {

            Common.getImageLoader(null).displayImage(c.getString(c.getColumnIndexOrThrow(Database_UserProfileContract.UserProfile.COLUMN_NAME_AvatarURL)), imgAvatarURL);
            lblFirstName.setText(c.getString(c.getColumnIndexOrThrow(Database_UserProfileContract.UserProfile.COLUMN_NAME_FirstName)) + " " + c.getString(c.getColumnIndexOrThrow(Database_UserProfileContract.UserProfile.COLUMN_NAME_LastName)));
            //lblLastName.setText(c.getString(c.getColumnIndexOrThrow(Database_UserProfileContract.UserProfile.COLUMN_NAME_LastName)) + ",");

        }
    }

    private void LoadVehicleData(String id) {

        DatabaseHelper helper = DatabaseHelper.getInstance(getContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        //GET DATA FROM DB
        String[] projection = {
                Database_VehicleContract.Vehicle.COLUMN_NAME_Id,
                Database_VehicleContract.Vehicle.COLUMN_NAME_UserId,
                Database_VehicleContract.Vehicle.COLUMN_NAME_DriverName,
                Database_VehicleContract.Vehicle.COLUMN_NAME_DriverPhoto,
                Database_VehicleContract.Vehicle.COLUMN_NAME_PlateNo,
                Database_VehicleContract.Vehicle.COLUMN_NAME_VehicleType
        };

        Cursor c = db.query(
                Database_VehicleContract.Vehicle.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                Database_VehicleContract.Vehicle.COLUMN_NAME_UserId + " = ?",                                // The columns for the WHERE clause
                new String[]{id},                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        if (c.moveToNext()) {

            VehicleId = c.getString(c.getColumnIndexOrThrow(Database_VehicleContract.Vehicle.COLUMN_NAME_Id));

        }
    }

    private void LoadBooking (String rideId){

        DatabaseHelper helper = DatabaseHelper.getInstance(getContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        //GET DATA FROM DB
        String[] projection = {
                Database_RideContract.Ride.COLUMN_NAME_Id,
                Database_RideContract.Ride.COLUMN_NAME_VehicleId,
                Database_RideContract.Ride.COLUMN_NAME_CommuterId,
                Database_RideContract.Ride.COLUMN_NAME_CommuterName,
                Database_RideContract.Ride.COLUMN_NAME_CommuterPhoto,
                Database_RideContract.Ride.COLUMN_NAME_OrigLocation,
                Database_RideContract.Ride.COLUMN_NAME_DestLocation,
                Database_RideContract.Ride.COLUMN_NAME_LastUpdated,
                Database_RideContract.Ride.COLUMN_NAME_Status
        };

        Cursor c = db.query(
                Database_RideContract.Ride.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                Database_RideContract.Ride.COLUMN_NAME_Id + " = ?",                                // The columns for the WHERE clause
                new String[]{rideId},                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        if(c.getCount() > 0) {
            lblNoRequestLabel.setVisibility(View.GONE);
            dvOperatorRequest.setVisibility(View.VISIBLE);
            if (c.moveToNext()) {

                Common.getImageLoader(null).displayImage(c.getString(c.getColumnIndexOrThrow(Database_RideContract.Ride.COLUMN_NAME_CommuterPhoto)), imgCommuterAvatar);

                lblRideId.setText(c.getString(c.getColumnIndexOrThrow(Database_RideContract.Ride.COLUMN_NAME_Id)));
                lblCommuterName.setText(c.getString(c.getColumnIndexOrThrow(Database_RideContract.Ride.COLUMN_NAME_CommuterName)));
                lblCommuterOrigin.setText(c.getString(c.getColumnIndexOrThrow(Database_RideContract.Ride.COLUMN_NAME_OrigLocation)));
                lblCommuterDestination.setText(c.getString(c.getColumnIndexOrThrow(Database_RideContract.Ride.COLUMN_NAME_DestLocation)));
                lblRideDate.setText(c.getString(c.getColumnIndexOrThrow(Database_RideContract.Ride.COLUMN_NAME_LastUpdated)));

                if(c.getString(c.getColumnIndexOrThrow(Database_RideContract.Ride.COLUMN_NAME_Status)).equals("Open"))
                {
                    ibtnAccept.setVisibility(View.VISIBLE);
                }
                else
                {
                    ibtnAccept.setVisibility(View.GONE);
                }

            }
        }
        else {
            lblNoRequestLabel.setVisibility(View.VISIBLE);
            dvOperatorRequest.setVisibility(View.GONE);
        }

    }


    private void LoadCommuterRequests(String vehicleId) {

        if (mAuthTask != null) {
            return;
        }

        if (Common.GetInternetConnectivity((ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE))) {
            showProgress(true);
            mAuthTask = new CommuterRequestTask(vehicleId);
            mAuthTask.execute((Void) null);
        }
        else
        {
            Toast toast = Toast.makeText(getContext(), R.string.InternetConnectionMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void AcceptBooking(Integer rideId) {

        if (mAuthTask != null) {
            return;
        }

        if (Common.GetInternetConnectivity((ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE))) {
            showProgress(true);
            mBookTask = new AcceptBookingTask(rideId, "Assigned", UserID);
            mBookTask.execute((Void) null);
        }
        else
        {
            Toast toast = Toast.makeText(getContext(), R.string.InternetConnectionMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    public class CommuterRequestTask extends AsyncTask<Void, Void, Boolean> {

        private final String mVehicleId;

        CommuterRequestTask(String vehicleId) {
            mVehicleId = vehicleId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            Boolean loginSuccess = false;
            JSONObject jsonResponse = null;

            try {

                Common comm = new Common();
                comm.setAPIURL(getString(R.string.APIURL));
                jsonResponse = comm.GetAPI("api/RideView?commuterId=&vehicleId=" + mVehicleId + "&status=&appId=" + getString(R.string.app_id));


                if (jsonResponse != null) {

                    String strSuccess = jsonResponse.getString("Success");

                    if (strSuccess == "true") {

                        JSONObject apiResponseData = jsonResponse.getJSONObject("ResponseData");
                        JSONArray rides = apiResponseData.getJSONArray("Rides");

                        if(rides != null && rides.length() > 0) {

                            for(int ctr=0;ctr < rides.length(); ctr++) {

                                String status = rides.getJSONObject(ctr).getString(Database_RideContract.Ride.COLUMN_NAME_Status);
                                String vehicleId = rides.getJSONObject(ctr).getString(Database_RideContract.Ride.COLUMN_NAME_VehicleId);

                                if(status.equals("Open")
                                        || (vehicleId.equals(VehicleId))) {

                                    RideId = rides.getJSONObject(ctr).getInt(Database_RideContract.Ride.COLUMN_NAME_Id);

                                    // Gets the data repository in write mode
                                    DatabaseHelper helper = DatabaseHelper.getInstance(getContext());
                                    SQLiteDatabase db = helper.getWritableDatabase();

                                    // Create a new map of values, where column names are the keys
                                    ContentValues values = new ContentValues();
                                    values.put(Database_RideContract.Ride.COLUMN_NAME_Id, rides.getJSONObject(ctr).getInt(Database_RideContract.Ride.COLUMN_NAME_Id));
                                    values.put(Database_RideContract.Ride.COLUMN_NAME_CommuterId, rides.getJSONObject(ctr).getString(Database_RideContract.Ride.COLUMN_NAME_CommuterId));
                                    values.put(Database_RideContract.Ride.COLUMN_NAME_CommuterName, rides.getJSONObject(ctr).getString(Database_RideContract.Ride.COLUMN_NAME_CommuterName));
                                    values.put(Database_RideContract.Ride.COLUMN_NAME_CommuterPhoto, rides.getJSONObject(ctr).getString(Database_RideContract.Ride.COLUMN_NAME_CommuterPhoto));
                                    values.put(Database_RideContract.Ride.COLUMN_NAME_OrigLocation, rides.getJSONObject(ctr).getString(Database_RideContract.Ride.COLUMN_NAME_OrigLocation));
                                    values.put(Database_RideContract.Ride.COLUMN_NAME_OrigLong, rides.getJSONObject(ctr).getDouble(Database_RideContract.Ride.COLUMN_NAME_OrigLong));
                                    values.put(Database_RideContract.Ride.COLUMN_NAME_OrigLat, rides.getJSONObject(ctr).getDouble(Database_RideContract.Ride.COLUMN_NAME_OrigLat));
                                    values.put(Database_RideContract.Ride.COLUMN_NAME_DestLocation, rides.getJSONObject(ctr).getString(Database_RideContract.Ride.COLUMN_NAME_DestLocation));
                                    values.put(Database_RideContract.Ride.COLUMN_NAME_DestLong, rides.getJSONObject(ctr).getDouble(Database_RideContract.Ride.COLUMN_NAME_DestLong));
                                    values.put(Database_RideContract.Ride.COLUMN_NAME_DestLat, rides.getJSONObject(ctr).getDouble(Database_RideContract.Ride.COLUMN_NAME_DestLat));
                                    values.put(Database_RideContract.Ride.COLUMN_NAME_LastUpdated, rides.getJSONObject(ctr).getString(Database_RideContract.Ride.COLUMN_NAME_LastUpdated));
                                    values.put(Database_RideContract.Ride.COLUMN_NAME_Status, rides.getJSONObject(ctr).getString(Database_RideContract.Ride.COLUMN_NAME_Status));
                                    values.put(Database_RideContract.Ride.COLUMN_NAME_VehicleId, rides.getJSONObject(ctr).getString(Database_RideContract.Ride.COLUMN_NAME_VehicleId));

                                    long newRowId;
                                    db.delete(Database_RideContract.Ride.TABLE_NAME, null, null);

                                    newRowId = db.insert(
                                            Database_RideContract.Ride.TABLE_NAME,
                                            Database_RideContract.Ride.COLUMN_NAME_Id,
                                            values);


                                    db.close(); //SAVE TO DB
                                }

                            }
                        }
                        else {

                            RideId = 0;

                            // Gets the data repository in write mode
                            DatabaseHelper helper = DatabaseHelper.getInstance(getContext());
                            SQLiteDatabase db = helper.getWritableDatabase();

                            long newRowId;
                            db.delete(Database_RideContract.Ride.TABLE_NAME, null, null);
                            db.close(); //SAVE TO DB
                        }


                        loginSuccess = true;
                    }
                }

            }
            catch (Exception ex) {
                String message = ex.getMessage();
            }


            // TODO: register the new account here.
            return loginSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                if(RideId != null) {
                    LoadBooking(RideId.toString());
                }
            }
            else {

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class AcceptBookingTask extends AsyncTask<Void, Void, Boolean> {

        private final Integer mRideId;
        private final String mStatus;
        private final String mUserId;

        AcceptBookingTask(Integer rideId, String status, String userId)
        {
            mRideId = rideId;
            mStatus = status;
            mUserId = userId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            Boolean loginSuccess = false;
            JSONObject jsonResponse = null;

            try {

                JSONObject jsonParam = new JSONObject();

                Common comm = new Common();
                comm.setAPIURL(getString(R.string.APIURL));
                jsonResponse = comm.PostAPI(jsonParam, "api/Ride?id=" + mRideId + "&status=" + mStatus + "&userId=" + mUserId + "&appId=" + getString(R.string.app_id));

                if (jsonResponse != null) {

                    String strSuccess = jsonResponse.getString("Success");

                    if (strSuccess == "true") {

                        loginSuccess = true;
                    }
                }

            }
            catch (Exception ex) {
                String message = ex.getMessage();
            }


            // TODO: register the new account here.
            return loginSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {

                ibtnAccept.setVisibility(View.GONE);

                Toast toast = Toast.makeText(getContext(), R.string.BookingAcceptedMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
            else {

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LoadCommuterRequests(VehicleId);
                }
            });
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        if(show)
        {
            lblUpdating.setVisibility(View.VISIBLE);
        }
        else{
            lblUpdating.setVisibility(View.INVISIBLE);
        }

        /*
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            dvOperatorRequest.setVisibility(show ? View.GONE : View.VISIBLE);
            dvOperatorRequest.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    dvOperatorRequest.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            login_progress.setVisibility(show ? View.VISIBLE : View.GONE);
            login_progress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    login_progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.setVisibility(show ? View.VISIBLE : View.GONE);
            dvOperatorRequest.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        */
    }
}

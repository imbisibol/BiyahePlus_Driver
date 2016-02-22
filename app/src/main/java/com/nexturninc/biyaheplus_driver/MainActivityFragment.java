package com.nexturninc.biyaheplus_driver;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
    Integer TimerSeconds;
    Timer timer;
    MyTimerTask myTimerTask;

    private CommuterRequestTask mAuthTask = null;
    private AcceptBookingTask mBookTask = null;
    private UpdateCurrentLocationTask mLocationTask = null;

    //LOCATION MANAGER
    private LocationManager locManager;
    private LocationListener locListener = new MyLocationListener();
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    private boolean locationChanged = false;
    private Double latitude;
    private Double longitude;

    //CONTROLS
    LinearLayout dvOperatorRequest = null;
    ImageView imgAvatarURL;
    TextView lblFirstName;
    TextView lblLastName;

    TextView lblNoRequestLabel;
    TextView lblDriverInactive;

    TextView lblRideId;
    TextView lblRideDate;
    ImageView imgCommuterAvatar;
    TextView lblCommuterName;
    TextView lblCommuterOrigin;
    TextView lblCommuterDestination;
    TextView lblCommuterMobileNo;

    ImageButton ibtnAccept;
    ImageButton ibtnCallCommuter;

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
        TimerSeconds = 0;
        timer = new Timer();
        myTimerTask = new MyTimerTask();
        lblUpdating = (TextView)rootView.findViewById(R.id.lblUpdating);
        timer.schedule(myTimerTask, 10000, 10000);

        dvOperatorRequest = (LinearLayout) rootView.findViewById(R.id.dvOperatorRequest);
        imgAvatarURL = (ImageView) rootView.findViewById(R.id.imgAvatarURL);
        lblFirstName = (TextView) rootView.findViewById(R.id.lblFirstName);
        lblLastName = (TextView) rootView.findViewById(R.id.lblLastName);

        lblNoRequestLabel = (TextView)rootView.findViewById(R.id.lblNoRequestLabel);
        lblDriverInactive = (TextView)rootView.findViewById(R.id.lblDriverInactive);

        lblRideId = (TextView)rootView.findViewById(R.id.lblRideId);
        lblRideDate = (TextView)rootView.findViewById(R.id.lblRideDate);
        imgCommuterAvatar = (ImageView) rootView.findViewById(R.id.imgCommuterAvatar);
        lblCommuterName = (TextView) rootView.findViewById(R.id.lblCommuterName);
        lblCommuterOrigin = (TextView)rootView.findViewById(R.id.lblCommuterOrigin);
        lblCommuterDestination = (TextView)rootView.findViewById(R.id.lblCommuterDestination);
        lblCommuterMobileNo = (TextView)rootView.findViewById(R.id.lblCommuterMobileNo);

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

        ibtnCallCommuter = (ImageButton)rootView.findViewById(R.id.ibtnCallCommuter);
        ibtnCallCommuter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lblCommuterMobileNo.getText().length() == 0)
                {
                    Toast toast = Toast.makeText(getContext(), R.string.CommuterNoMobile, Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {

                    Intent in = new Intent(Intent.ACTION_CALL,Uri.parse("tel:" + lblCommuterMobileNo.getText().toString()));
                    try{
                        getContext().startActivity(in);
                    }
                    catch (android.content.ActivityNotFoundException ex){
                        Toast.makeText(getContext(), R.string.ErrorMessage,Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        //GPS
        locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch (Exception ex) {
        }

        try {
            network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        catch (Exception ex) {
        }

        // don't start listeners if no provider is enabled
        if (!gps_enabled && !network_enabled) {
            Toast toast = Toast.makeText(getContext(), "Unable to track location. Please ensure that GPS is enabled on your phone.", Toast.LENGTH_SHORT);
            toast.show();
        }

        if (getActivity().checkCallingPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (gps_enabled) {
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
            }
            if (network_enabled) {
                locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
            }
        }

        LoadProfileData(UserID);
        LoadVehicleData(UserID);
        LoadCommuterRequests(VehicleId);


        return rootView;
    }

    //METHODS
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
                Database_RideContract.Ride.COLUMN_NAME_CommuterMobileNo,
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

            if(!dvOperatorRequest.isShown()) {
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getContext().getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            lblNoRequestLabel.setVisibility(View.GONE);
            dvOperatorRequest.setVisibility(View.VISIBLE);
            if (c.moveToNext()) {

                Common.getImageLoader(null).displayImage(c.getString(c.getColumnIndexOrThrow(Database_RideContract.Ride.COLUMN_NAME_CommuterPhoto)), imgCommuterAvatar);

                lblRideId.setText(c.getString(c.getColumnIndexOrThrow(Database_RideContract.Ride.COLUMN_NAME_Id)));
                lblCommuterName.setText(c.getString(c.getColumnIndexOrThrow(Database_RideContract.Ride.COLUMN_NAME_CommuterName)));
                lblCommuterMobileNo.setText(c.getString(c.getColumnIndexOrThrow(Database_RideContract.Ride.COLUMN_NAME_CommuterMobileNo)));
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

    //TASK LAUNCHERS
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

    private void UpdateLocationRequests(String vehicleId) {

        if (mLocationTask != null) {
            return;
        }

        if (Common.GetInternetConnectivity((ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE))) {
            showProgress(true);
            mLocationTask = new UpdateCurrentLocationTask(vehicleId);
            mLocationTask.execute((Void) null);
        }
        else
        {
            Toast toast = Toast.makeText(getContext(), R.string.InternetConnectionMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }



    //TASKS / LISTENERS
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

                        int ridesRetrieved = 0;
                        if(rides != null && rides.length() > 0) {

                            for(int ctr=0;ctr < rides.length(); ctr++) {

                                String status = rides.getJSONObject(ctr).getString(Database_RideContract.Ride.COLUMN_NAME_Status);
                                String vehicleId = rides.getJSONObject(ctr).getString(Database_RideContract.Ride.COLUMN_NAME_VehicleId);

                                if(status.equals("Open")
                                   || status.equals("Assigned")) {

                                    ridesRetrieved = ridesRetrieved + 1;

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
                                    values.put(Database_RideContract.Ride.COLUMN_NAME_CommuterMobileNo, rides.getJSONObject(ctr).getString(Database_RideContract.Ride.COLUMN_NAME_CommuterMobileNo ));
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

                        if(ridesRetrieved == 0)
                        {
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

    public class UpdateCurrentLocationTask extends AsyncTask<Void, Void, Boolean> {

        private final String mVehicleId;

        UpdateCurrentLocationTask(String vehicleId)
        {
            mVehicleId = vehicleId;
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
                jsonResponse = comm.PostAPI(jsonParam, "api/Vehicle?id=" + mVehicleId + "&longitude=" + longitude + "&latitude=" + latitude + "&appId=" + getString(R.string.app_id));

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
                locationChanged = false;
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
                    TimerSeconds += 10;

                    //DRIVER STATUS
                    if (((MainActivity) getActivity()).driverStatus.equals(getString(R.string.VehicleStatus_Inactive))
                            && lblDriverInactive.getVisibility() == View.GONE)
                    {
                        lblNoRequestLabel.setVisibility(View.GONE);
                        dvOperatorRequest.setVisibility(View.GONE);
                        lblDriverInactive.setVisibility(View.VISIBLE);
                    }
                    else if(!((MainActivity) getActivity()).driverStatus.equals(getString(R.string.VehicleStatus_Inactive))
                            && lblDriverInactive.getVisibility() == View.VISIBLE)
                    {
                        lblDriverInactive.setVisibility(View.GONE);
                        LoadCommuterRequests(VehicleId);
                    }

                    //MAIN OPERATION
                    if(TimerSeconds >= 60) {

                        TimerSeconds = 0;

                        if (((MainActivity) getActivity()).driverStatus.equals(getString(R.string.VehicleStatus_Active))) {

                            LoadCommuterRequests(VehicleId);

                            if (locationChanged) {
                                UpdateLocationRequests(VehicleId);
                            }
                        }
                    }
                }
            });
        }

    }

    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null
                && getActivity().checkCallingPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                // This needs to stop getting the location data and save the battery power.
                locManager.removeUpdates(locListener);
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                locationChanged = true;
            }
        }
        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
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

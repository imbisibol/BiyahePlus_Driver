package com.nexturninc.biyaheplus_driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    public String driverStatus;
    private String vehicleId;

    private Menu mainmenu;
    private UpdateVehicleStatusTask mVehicleStatusTask = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mainmenu = menu;

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        driverStatus = mSettings.getString(getString(R.string.SHARE_PREF_VehicleStatus), null);
        vehicleId = mSettings.getString(getString(R.string.SHARE_PREF_VehicleId), null);
        UpdateVehicleStatusMenu(driverStatus);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_startRequests) {

            UpdateVehicleStatus(vehicleId, getString(R.string.VehicleStatus_Active));

            return true;
        }
        else if (id == R.id.action_stopRequests) {

            UpdateVehicleStatus(vehicleId, getString(R.string.VehicleStatus_Inactive));

            return true;
        }
        else  if (id == R.id.action_logout) {

            DatabaseHelper helper = DatabaseHelper.getInstance(getBaseContext());
            SQLiteDatabase db = helper.getWritableDatabase();

            db.delete(Database_UserProfileContract.UserProfile.TABLE_NAME, null, null);
            db.delete(Database_VehicleContract.Vehicle.TABLE_NAME, null, null);
            db.delete(Database_RideContract.Ride.TABLE_NAME, null, null);

            SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = mSettings.edit();
            editor.clear();
            editor.commit();

            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }


    //TASKS
    public class UpdateVehicleStatusTask extends AsyncTask<Void, Void, Boolean> {

        private final String mVehicleId;
        private final String mStatus;

        UpdateVehicleStatusTask(String vehicleId, String status)
        {
            mVehicleId = vehicleId;
            mStatus = status;
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
                jsonResponse = comm.PostAPI(jsonParam, "api/Vehicle?id=" + mVehicleId + "&status=" + mStatus + "&appId=" + getString(R.string.app_id));

                if (jsonResponse != null) {

                    String strSuccess = jsonResponse.getString("Success");

                    if (strSuccess == "true") {

                        loginSuccess = true;

                        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putString(getString(R.string.SHARE_PREF_VehicleStatus), mStatus);
                        editor.commit();

                        driverStatus = mStatus;

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
            mVehicleStatusTask = null;
            //showProgress(false);

            UpdateVehicleStatusMenu(driverStatus);

            if (success) {
                Toast toast = Toast.makeText(getBaseContext(), getString(R.string.DriverStatusUpdatedMessage), Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                Toast toast = Toast.makeText(getBaseContext(), getString(R.string.ErrorMessage), Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        @Override
        protected void onCancelled() {
            mVehicleStatusTask = null;
            //showProgress(false);
        }
    }

    //METHODS
    private void UpdateVehicleStatus(String vehicleId, String status) {

        if (mVehicleStatusTask != null) {
            return;
        }

        if (Common.GetInternetConnectivity((ConnectivityManager)getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE))) {
            mVehicleStatusTask = new UpdateVehicleStatusTask(vehicleId, status);
            mVehicleStatusTask.execute((Void) null);
        }
        else
        {
            Toast toast = Toast.makeText(getBaseContext(), R.string.InternetConnectionMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void UpdateVehicleStatusMenu(String vehicleStatus)
    {
        if(vehicleStatus.equals(getString(R.string.VehicleStatus_Active)))
        {
            mainmenu.findItem(R.id.action_startRequests).setVisible(false);
            mainmenu.findItem(R.id.action_stopRequests).setVisible(true);
        }
        else if(vehicleStatus.equals(getString(R.string.VehicleStatus_Inactive)))
        {
            mainmenu.findItem(R.id.action_startRequests).setVisible(true);
            mainmenu.findItem(R.id.action_stopRequests).setVisible(false);
        }
    }
}

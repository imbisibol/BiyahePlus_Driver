package com.nexturninc.biyaheplus_driver;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imbisibol on 2/17/2016.
 */
public class LoginActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (Common.GetInternetConnectivity((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
                    if (id == R.id.login || id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                    }
                } else {
                    mPasswordView.setError(getString(R.string.message_Internet_Required));
                    mPasswordView.requestFocus();
                }

                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.GetInternetConnectivity((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE))) {
                    attemptLogin();
                }
                else {
                    mEmailView.setError("Internet connection is required to log in.");
                    mEmailView.requestFocus();
                }
            }
        });


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        Common.getImageLoader(this);

        //GO RIGHT IN, BECAUSE YOU ALREADY LOGGED IN BEFORE
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
        String UserID = mSettings.getString(this.getString(R.string.SHARE_PREF_UserId), null);
        String UserDisplay = mSettings.getString(this.getString(R.string.SHARE_PREF_UserName), null);
        if(UserID != null && !UserID.isEmpty())
        {
            Toast toast = Toast.makeText(getBaseContext(), "Logged in as " + UserDisplay, Toast.LENGTH_SHORT);
            toast.show();

            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            Boolean loginSuccess = false;
            String userId = "";
            String userDisplay = "";
            JSONObject jsonResponse = null;

            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("userName", mEmail);
                jsonParam.put("password", mPassword);



                Common comm = new Common();
                comm.setAPIURL(getString(R.string.APIURL));
                jsonResponse = comm.PostAPI(jsonParam, "api/login?appId=" + getString(R.string.app_id));


                if (jsonResponse != null) {

                    String strSuccess = jsonResponse.getString("Success");

                    if (strSuccess == "true") {

                        JSONObject apiResponseData = jsonResponse.getJSONObject("ResponseData");
                        JSONObject customerInfo =apiResponseData.getJSONObject("UserProfile");

                        if(customerInfo != null) {
                            userId = customerInfo.getString("UserId");
                            userDisplay = customerInfo.getString("FirstName")  +
                                    " " +
                                    customerInfo.getString("LastName")
                            ;

                            //SAVE TO SHARED PREFERENCES
                            SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = mSettings.edit();
                            editor.putString(getString(R.string.SHARE_PREF_UserId), userId);
                            editor.putString(getString(R.string.SHARE_PREF_UserName), userDisplay);
                            editor.commit();


                            // Gets the data repository in write mode
                            DatabaseHelper helper = DatabaseHelper.getInstance(getBaseContext());
                            SQLiteDatabase db = helper.getWritableDatabase();

                            // Create a new map of values, where column names are the keys
                            ContentValues values = new ContentValues();
                            values.put(Database_UserProfileContract.UserProfile.COLUMN_NAME_UserId, customerInfo.getString("UserId"));
                            values.put(Database_UserProfileContract.UserProfile.COLUMN_NAME_Username, customerInfo.getString("UserName"));
                            values.put(Database_UserProfileContract.UserProfile.COLUMN_NAME_Email, customerInfo.getString("Email"));
                            values.put(Database_UserProfileContract.UserProfile.COLUMN_NAME_FirstName, customerInfo.getString("FirstName"));
                            values.put(Database_UserProfileContract.UserProfile.COLUMN_NAME_LastName, customerInfo.getString("LastName"));
                            values.put(Database_UserProfileContract.UserProfile.COLUMN_NAME_AvatarURL, customerInfo.getString("AvatarURL"));



                            //GET VEHICLE INFO
                            comm = new Common();
                            comm.setAPIURL(getString(R.string.APIURL));
                            jsonParam = new JSONObject();
                            jsonResponse = comm.GetAPI("api/VehicleView?vehicleId=&driverId=" + customerInfo.getString("UserId") + "&appId=" + getString(R.string.app_id));

                            if (jsonResponse != null) {
                                strSuccess = jsonResponse.getString("Success");

                                if (strSuccess == "true") {

                                    apiResponseData = jsonResponse.getJSONObject("ResponseData");
                                    JSONObject vehicleInfo = apiResponseData.getJSONObject("Vehicle");

                                    // Create a new map of values, where column names are the keys
                                    ContentValues values2 = new ContentValues();
                                    values2.put(Database_VehicleContract.Vehicle.COLUMN_NAME_Id, vehicleInfo.getString("Id"));
                                    values2.put(Database_VehicleContract.Vehicle.COLUMN_NAME_UserId, vehicleInfo.getString("UserId"));
                                    values2.put(Database_VehicleContract.Vehicle.COLUMN_NAME_PlateNo, vehicleInfo.getString("PlateNo"));
                                    values2.put(Database_VehicleContract.Vehicle.COLUMN_NAME_VehicleType, vehicleInfo.getString("VehicleType"));
                                    values2.put(Database_VehicleContract.Vehicle.COLUMN_NAME_LocLong, vehicleInfo.getDouble("LocLong"));
                                    values2.put(Database_VehicleContract.Vehicle.COLUMN_NAME_LocLat, vehicleInfo.getDouble("LocLat"));
                                    values2.put(Database_VehicleContract.Vehicle.COLUMN_NAME_DriverName, vehicleInfo.getString("DriverName"));
                                    values2.put(Database_VehicleContract.Vehicle.COLUMN_NAME_DriverPhoto, vehicleInfo.getString("DriverPhoto"));


                                    //SAVE TO DB
                                    long newRowId;
                                    db.delete(Database_UserProfileContract.UserProfile.TABLE_NAME, null, null);
                                    db.delete(Database_VehicleContract.Vehicle.TABLE_NAME, null, null);

                                    newRowId = db.insert(
                                            Database_UserProfileContract.UserProfile.TABLE_NAME,
                                            Database_UserProfileContract.UserProfile.COLUMN_NAME_UserId,
                                            values);


                                    newRowId = db.insert(
                                            Database_VehicleContract.Vehicle.TABLE_NAME,
                                            Database_VehicleContract.Vehicle.COLUMN_NAME_UserId,
                                            values2);
                                }
                            }


                            db.close();

                        }


                        loginSuccess = true;
                    }
                }

            }
            catch (Exception ex) {
                String message = ex.getMessage();
            }

            if(loginSuccess) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }


            // TODO: register the new account here.
            return loginSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            }
            else if (!Common.GetInternetConnectivity((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE))){
                mPasswordView.setError("Internet connection is required to log in.");
                mPasswordView.requestFocus();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}

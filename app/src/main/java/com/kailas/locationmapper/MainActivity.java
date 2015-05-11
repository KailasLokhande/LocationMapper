package com.kailas.locationmapper;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
    import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.kailas.frienzo.adapter.FrienzoAdapter;
import com.kailas.frienzo.model.User;
import com.kailas.frienzo.model.UserResponse;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener {
    private static final int RC_SIGN_IN = 0;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap map;
    private Location locationUp;
    private LocationRequest mLocationRequest = new LocationRequest();
    private boolean requestingLocationUpdate = true;
    private GoogleMap googleMap;
    private Map<String, Marker> markers = new HashMap<String, Marker>();
    private SignInButton btnSignIn;
    private Button btnSignOut;
    private ViewGroup authentication_bar;
    Logger logger = Logger.getLogger(MainActivity.class.toString());

    private User loggedInUser;


    //TODO: Move to Res folder
    // Sync Adapter Settings
    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.kailas.locationmapper.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "com.kailas";

    // Instance fields
    Account account;


    /**
     * True if the sign-in button was clicked.  When true, we know to resolve all
     * issues preventing sign-in without waiting.
     */
    private boolean mSignInClicked;

    /**
     * True if we are in the process of resolving a ConnectionResult
     */
    private boolean mIntentInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        btnSignIn = (SignInButton) findViewById(R.id.sign_in_button);
        btnSignIn.setOnClickListener(this);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);
        btnSignOut.setOnClickListener(this);
        authentication_bar = (ViewGroup) btnSignIn.getParent();
        showSignOutBtn(false);
        //Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else {
            // Getting reference to the SupportMapFragment of activity_main.xml
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            googleMap = fm.getMap();

            // Enabling MyLocation Layer of Google Map
            googleMap.setMyLocationEnabled(true);

            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(Plus.API)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .build();

            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        }
    }


    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public  Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                loggedInUser.getId(), ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager = AccountManager.get(context);
                /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }

        return newAccount;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();//be connected with GoogleApiClient
    }

    @Override
    public void onConnected(Bundle bundle) {
        mSignInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
        showSignInBtn(false);
        showSignOutBtn(true);
        if (requestingLocationUpdate) {
            startLocationUpdate();//trying to get the update location
        }

        // Add user to database if not already present
        try {
            new FrienzoAdapter(this).updateUser(getLoggedInUser());
            account = CreateSyncAccount(this);
            ContentResolver.addPeriodicSync(account, AUTHORITY, Bundle.EMPTY, 5);
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true /*sync*/);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private User getLoggedInUser() {
        if (loggedInUser == null && Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String personName = currentPerson.getDisplayName();
            Person.Image photo = currentPerson.getImage();
            String personGooglePlusProfile = currentPerson.getUrl();
            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
            String id = currentPerson.getId();
            loggedInUser = new User();
            loggedInUser.setEmail( email);
            loggedInUser.setId( id);
            loggedInUser.setPhotoUrlGoogle( photo.getUrl());
            loggedInUser.setName(personName);
            loggedInUser.setgPlusProfileUrl( personGooglePlusProfile);
        }
        return loggedInUser;
    }

    private void startLocationUpdate() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private Location getLocation() {
        Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        return lastKnownLocation;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("result:", "connection has been suspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        Log.d("result:", "not connected with GoogleApiClient");
        if (!mIntentInProgress) {
            if (mSignInClicked && result.hasResolution()) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                try {
                    result.startResolutionForResult(this, RC_SIGN_IN);
                    mIntentInProgress = true;
                } catch (IntentSender.SendIntentException e) {
                    // The intent was canceled before it was sent.  Return to the default
                    // state and attempt to connect to get an updated ConnectionResult.
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdate();//when activity is on pause, need to stop the location update since we
//do not need that any more
    }

    private void stopLocationUpdate() {

        if( mGoogleApiClient.isConnected() )
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);//stopping the update location
    }

    //no need to use googleplayservices
    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();//disconnect the GoogleApiClient from the google play services
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdate();// since we need to get the update location continuously like gps
//when activity is in foreground(resume), then we have to request to get update location
//here alos
        }
    }

    //to get the location change
    @Override
    public void onLocationChanged(Location location) {

        locationUp = location;//this location is the update location,


    if(!LocationSyncAdapter.isSyncOn()) {
        try {
            new FrienzoAdapter().updateUserLocation(account.name, locationUp.getLatitude(), locationUp.getLongitude());

        } catch (JSONException e) {
            logger.severe("Location update failing for account : "+ account + "Reason: "+ e.getMessage());
        }
    }
// came from the requestLocationUpdate() method’s interface
        updateView();//put your value in the text or whatever you want to do
    }

    private void updateView() {

        LatLng latLng = new LatLng(locationUp.getLatitude(), locationUp.getLongitude());

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        if(loggedInUser == null)
            return;


        List<UserResponse> tempSet = new ArrayList<UserResponse>();
        new FrienzoAdapter().updateFriendLocations(markers, googleMap, loggedInUser.getId());

       //  marker = googleMap.addMarker(new MarkerOptions().position(latLng).visible(true).title(loggedInUser.getName()).snippet("Email: "+ loggedInUser.getEmail()).draggable(false));
        //marker.showInfoWindow();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.sign_in_button:
                // Signin button clicked
                signInWithGplus();
                break;
            case R.id.btn_sign_out:
                // Signout button clicked
                signOutFromGplus();
                break;
        }
    }

    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            mGoogleApiClient.connect();
//            resolveSignInError();
        }
    }

//    /**
//     * Method to resolve any signin errors
//     * */
//    private void resolveSignInError() {
//        if (mConnectionResult.hasResolution()) {
//            try {
//                mIntentInProgress = true;
//                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
//            } catch (SendIntentException e) {
//                mIntentInProgress = false;
//                mGoogleApiClient.connect();
//            }
//        }
//    }
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.reconnect();
            }
        }
    }

    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            showSignInBtn(true);
            showSignOutBtn(false);
        }
    }

    private void showSignOutBtn(boolean show) {
        if(authentication_bar == null)
            return;
        if( show )
        {
//            btnSignOut = (Button) findViewById(R.id.btn_sign_out);
//            btnSignOut.setOnClickListener(this);
//            authentication_bar.addView(btnSignOut);
            btnSignOut.setVisibility(View.VISIBLE);
        } else {
            btnSignOut.setVisibility(View.INVISIBLE);
//            authentication_bar.removeView(btnSignOut);
        }
    }

    private void showSignInBtn(boolean show) {
        if(authentication_bar == null)
           return;
        if( show )
        {
            btnSignIn.setVisibility(View.VISIBLE);
        } else {
             btnSignIn.setVisibility(View.INVISIBLE);        }
    }
}
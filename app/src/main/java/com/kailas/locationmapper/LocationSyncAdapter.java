package com.kailas.locationmapper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentSender;
import android.content.SyncResult;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.kailas.frienzo.adapter.FrienzoAdapter;

import org.json.JSONException;

import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * Class wraps data transfer code in an interface compatible
 * with the sync adapter framework.
 *
 * Created by kailasl on 5/10/2015.
 */
public class LocationSyncAdapter extends AbstractThreadedSyncAdapter implements LocationListener , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private boolean mIntentInProgress;
    private static final int RC_SIGN_IN = 0;
    private FrienzoAdapter frienzoAdapter = new FrienzoAdapter();

    private Location lastKnownLocation;
    private Location lastUpdatedLocation;
    private static long lastSyncTime = 0;

    public static boolean isSyncOn() {
        long diff = new Date().getTime() - lastSyncTime;
        if( diff > 180000)
            return false;
        return true;
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d("result:", "not connected with GoogleApiClient");
        if (!mIntentInProgress) {
            if ( result.hasResolution()) {

                    locationApiClient.connect();

            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(locationApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        logger.info("Location changed" + location.getLatitude());
        lastKnownLocation = location;
    }

    private GoogleApiClient locationApiClient;
    private LocationRequest locationRequest = new LocationRequest();
    void setupLocationApiClient() {
        logger.info("Creating location api client");
        locationApiClient = new GoogleApiClient.Builder(ApplicationController.getInstance().getApplicationContext())
                .addConnectionCallbacks(this)
                .addApi(Plus.API)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(4000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    // Global variable
    // Define a variable to contain a content resolver instance --- WHY?
    ContentResolver contentResolver;

    private final AccountManager accountManager;

    Logger logger = Logger.getLogger(LocationSyncAdapter.class.toString());
    public LocationSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        accountManager = AccountManager.get(context);
        /*
        If your app uses a content resolver, get an instance of it from the incoming context
        TODO: Does our app use anything like that? WHY?
        */
        contentResolver = context.getContentResolver();
        setupLocationApiClient();
    }

    /**
     * Setup the sync adapter. This form of the constructor maintains compatibility with Android 3.0 and later platform versions.
     */
    public LocationSyncAdapter( Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        accountManager = AccountManager.get(context);
        contentResolver = context.getContentResolver();
        setupLocationApiClient();
    }


    /**
     * The sync adapter component does not automatically do data transfer. Instead, it encapsulates your data transfer code, so that the sync adapter framework can run the data transfer in the background, without involvement from your app. When the framework is ready to sync your application's data, it invokes your implementation of the method onPerformSync().

     To facilitate the transfer of data from your main app code to the sync adapter component, the sync adapter framework calls onPerformSync() with the following arguments:

     Account
     An Account object associated with the event that triggered the sync adapter. If your server doesn't use accounts, you don't need to use the information in this object.
     Extras
     A Bundle containing flags sent by the event that triggered the sync adapter.
     Authority
     The authority of a content provider in the system. Your app has to have access to this provider. Usually, the authority corresponds to a content provider in your own app.
     Content provider client
     A ContentProviderClient for the content provider pointed to by the authority argument. A ContentProviderClient is a lightweight public interface to a content provider. It has the same basic functionality as a ContentResolver. If you're using a content provider to store data for your app, you can connect to the provider with this object. Otherwise, you can ignore it.
     Sync result
     A SyncResult object that you use to send information to the sync adapter framework.
     */
    /*
     * Specify the code you want to run in the sync adapter. The entire
     * sync adapter runs in a background thread, so you don't have to set
     * up your own background processing.
     */
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        lastSyncTime = new Date().getTime();
        logger.info("Last sync time: "+ lastSyncTime);
        /*
         * Put the data transfer code here.
         */
        if(!locationApiClient.isConnected()) {
            locationApiClient.connect();
        }

        if(lastKnownLocation == null)
            lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(locationApiClient);
        if( lastKnownLocation == null )
            return;
        if(lastUpdatedLocation != null && lastKnownLocation.getLongitude() == lastUpdatedLocation.getLongitude() && lastKnownLocation.getLatitude() == lastUpdatedLocation.getLatitude())
            return;
        logger.info("Sync data" + account.name + " LT: "+ lastKnownLocation.getLatitude());


        try {
            frienzoAdapter.updateUserLocation(account.name, lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

        } catch (JSONException e) {
            logger.severe("Location update failing for account : "+ account + "Reason: "+ e.getMessage());
        }


    }

}

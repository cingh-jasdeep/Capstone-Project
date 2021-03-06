package com.example.android.gurudwaratime.location_updates;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

import static com.example.android.gurudwaratime.data.Constants.KEY_LOCATION_UPDATES_REQUEST_STATUS;

/**
 * Helper class
 * helper methods related to creating location updates for fetching nearby Gurudwaras
 * <p>
 * nearby Gurudwaras should only be fetched every 15 minutes
 * with fastest time to be 7.5 minutes
 * <p>
 * only if displacement from last location is more than 20 kilometers
 * <p>
 * source: https://github.com/googlecodelabs/background-location-updates-android-o
 */
public class LocationRequestHelper {

    private static final long UPDATE_INTERVAL_IN_MINUTES = 8;
    private static final long SMALLEST_DISPLACEMENT_IN_KILOMETERS = 10;
    static final float SMALLEST_DISPLACEMENT_IN_METERS =
            SMALLEST_DISPLACEMENT_IN_KILOMETERS * 1000;
    private static final long UPDATE_INTERVAL_IN_MILLIS =
            TimeUnit.MINUTES.toMillis(UPDATE_INTERVAL_IN_MINUTES);//in millis
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLIS =
            UPDATE_INTERVAL_IN_MILLIS / 2;
    private static final String TAG = LocationRequestHelper.class.getSimpleName();

    /**
     * Sets up the location request.
     *
     * @return locationRequest location request object to be used for location updates
     */
    private static LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();

        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLIS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLIS);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_IN_METERS);

        return locationRequest;
    }

    /**
     * Sets up the pending intent for a location request.
     *
     * @return locationRequest location request object to be used for location updates
     */
    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void startLocationUpdates(Context context, boolean forceStart) {

        //if not already requesting updates or force start
        if (!getRequestingStatus(context) || forceStart) {
            FusedLocationProviderClient fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(context);
            try {
                fusedLocationClient.requestLocationUpdates(
                        getLocationRequest(),
                        getPendingIntent(context));

                //set location updates request status to true
                setRequestingStatus(context, true);
                Log.i(TAG, "startLocationUpdates: location updates started");

            } catch (SecurityException e) {
                Log.e(TAG, "startLocationUpdates: ", e);

                //set location updates request status to false
                setRequestingStatus(context, false);

            }
        }


    }

    public static void stopLocationUpdates(Context context) {
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context);
        fusedLocationClient.removeLocationUpdates(getPendingIntent(context));

        //set location updates request status to false
        setRequestingStatus(context, false);
        Log.i(TAG, "stopLocationUpdates: stopping location updates");
    }

    public static void setRequestingStatus(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUEST_STATUS, value)
                .apply();
    }

    public static boolean getRequestingStatus(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_UPDATES_REQUEST_STATUS, false);
    }
}

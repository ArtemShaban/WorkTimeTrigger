package com.shaban.worktimetrigger.service;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.shaban.worktimetrigger.R;
import com.shaban.worktimetrigger.StartActivity;

import java.util.Calendar;

/**
 * Created by Artem on 18.03.2015.
 */
public class TriggerLocationService extends Service implements LocationListener {
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private SharedPreferences sharedPreferences;

    private Location workPoint;
    private Location hostelPoint;
    private static final int ACCURACY = 30; //in meters
    public static final String SHARED_PREFERENCES_NAME = "work_trigger";
    private static final String ENTER = "enter";
    public static final String DAY_DURATION_SUFFIX = "_duration";

    private String TAG = "@@@";


    //53.910119, 27.572614  - киселева 5

//    53.891212, 27.567065 - октябрьская 10а

    @Override
    public void onCreate() {
        super.onCreate();
        workPoint = new Location((String) null);
        workPoint.setLatitude(53.910119);
        workPoint.setLongitude(27.572614);

        hostelPoint = new Location((String) null);
        hostelPoint.setLatitude(53.891212);
        hostelPoint.setLongitude(27.567065);

        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i(TAG, "onConnected");
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, TriggerLocationService.this);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i(TAG, "onConnectionSuspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.i(TAG, "onConnectionFailed; " + connectionResult);
                    }
                })
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "On location changed; Location: " + location + "  location.distanceTo(hostelPoint) = " + location.distanceTo(hostelPoint));

        if (location.distanceTo(workPoint) <= ACCURACY && sharedPreferences.getLong(ENTER, -1) == -1) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(ENTER, System.currentTimeMillis());
            editor.commit();

            notify("Entered");
        } else {
            long now = System.currentTimeMillis();
            long enter = sharedPreferences.getLong(ENTER, now);
            long duration = now - enter;
            long previousDuration = sharedPreferences.getLong(getTodayTimestamp() + DAY_DURATION_SUFFIX, 0);
            long fullDuration = previousDuration + duration;

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(ENTER);
            editor.putLong(getTodayTimestamp() + DAY_DURATION_SUFFIX, fullDuration);
            editor.commit();

            notify("Exit");
        }
    }

    private void notify(String action) {
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setContentTitle(action)
                        .setContentText("We are " + action + " to/from work")
                        .setSmallIcon(R.drawable.ic_launcher);
        Intent resultIntent = new Intent(this, StartActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent, 0);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    public static long getTodayTimestamp() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}

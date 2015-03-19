package com.shaban.worktimetrigger.service;

import android.app.Service;
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
import com.shaban.worktimetrigger.notification.NotificationHelper;
import com.shaban.worktimetrigger.util.DateUtils;

/**
 * Created by Artem on 18.03.2015.
 */
public class TriggerLocationService extends Service implements LocationListener
{
    public static final String SHARED_PREFERENCES_NAME = "work_trigger";
    public static final String DAY_DURATION_SUFFIX = "_duration";
    private static final int ACCURACY = 50; //in meters
    private static final String ENTER = "enter";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private SharedPreferences sharedPreferences;
    private Location workPoint;
    private Location hostelPoint;
    private String TAG = "@@@";
    private NotificationHelper notificationHelper;


    //53.910119, 27.572614  - киселева 5

//    53.891212, 27.567065 - октябрьская 10а

    @Override
    public void onCreate()
    {
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
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        notificationHelper = new NotificationHelper(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks()
                {
                    @Override
                    public void onConnected(Bundle bundle)
                    {
                        Log.i(TAG, "onConnected");
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, TriggerLocationService.this);
                    }

                    @Override
                    public void onConnectionSuspended(int i)
                    {
                        Log.i(TAG, "onConnectionSuspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener()
                {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult)
                    {
                        Log.i(TAG, "onConnectionFailed; " + connectionResult);
                    }
                })
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Log.i(TAG, "On location changed; Location: " + location + "  location.distanceTo(hostelPoint) = " + location.distanceTo(hostelPoint));

        if (location.distanceTo(workPoint) <= ACCURACY)
        {
            if (sharedPreferences.getLong(ENTER, -1) == -1)
            {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(ENTER, System.currentTimeMillis());
                editor.commit();

                notificationHelper.notify("Entered to work");
            }
        } else
        {
            long now = System.currentTimeMillis();
            long enter = sharedPreferences.getLong(ENTER, now);
            long duration = now - enter;
            if (duration > 0)
            {
                notificationHelper.notify("Exited from work");
            }
            long previousDuration = sharedPreferences.getLong(DateUtils.getTodayTimestamp() + DAY_DURATION_SUFFIX, 0);
            long fullDuration = previousDuration + duration;

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(ENTER);
            editor.putLong(DateUtils.getTodayTimestamp() + DAY_DURATION_SUFFIX, fullDuration);
            editor.commit();
        }
    }
}

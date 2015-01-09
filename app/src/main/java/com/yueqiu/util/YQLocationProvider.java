package com.yueqiu.util;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.awt.font.TextAttribute;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by scguo on 15/1/8.
 *
 * 当我们在SearchActivity当中的Fragment向Server端请求一定范围内的用户数据时，我们需要
 * 提供本地用户的位置才可以供服务器端进行判断。
 * 我们需要提供的只是Latitude和Longitude
 *
 * 这个类设计的目的是Speed > Accuracy,当然这个设计准则在后期还可能需要同服务器端进行协商来进一步改进
 *
 * 这个类暂时还在开发当中，不确定是否使用。
 *
 */
// TODO: 建议换成Baidu地图的SDK来获取当前用户的位置，因为普通方式的获取太慢了，MDMSDK当中的也是挺慢的
public class YQLocationProvider extends Service implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final String TAG = "YQLocationProvider";

    private static final String KEY_GPS_CHECK_INTERVAL = "gps_check_interval";
    private static final String KEY_GPS_MIN_DISTANCE = "gps_min_distance";

    private LocationManager mLocationManager;
    private boolean mServiceRunning = false;

    private Location mLastLocation = null;
    private ScheduledThreadPoolExecutor mExecutor = new ScheduledThreadPoolExecutor(5);

    private long mLastProviderTimestamp = 0;
    private boolean mGpsRecorderRunning = false;

//    private final IBinder mBinder = new Loca

    private Location getBestLocation()
    {
        Location gpsLocation = getLocationByProvider(LocationManager.GPS_PROVIDER);
        Location networkLocation = getLocationByProvider(LocationManager.NETWORK_PROVIDER);

        if (networkLocation == null && gpsLocation == null)
            Toast.makeText(this, "Please turn on the GPS or the network or the data interface", Toast.LENGTH_LONG).show();

        if (networkLocation == null)
            return gpsLocation;
        if (gpsLocation == null)
            return networkLocation;

        long old = System.currentTimeMillis() - getGPSCheckMinutesFromPrefs();
        boolean gpsIsOld = gpsLocation.getTime() < old;
        boolean networkIsOld = networkLocation.getTime() < old;

        if (!gpsIsOld)
        {
            return gpsLocation;
        }

        if (!networkIsOld)
        {
            return networkLocation;
        }

        if (gpsLocation.getTime() > networkLocation.getTime())
        {
            return gpsLocation;
        } else
        {
            return networkLocation;
        }
    }

    private void forceSingleProviderUpdate(String provider)
    {
        LocationManager locationManager = getLocationManager();
        if (locationManager.isProviderEnabled(provider))
        {
            locationManager.requestLocationUpdates(provider, 0, 0, new SingleUpdateLocationListener());
        }
    }

    private void forceLocationUpdate()
    {
        Log.d(TAG, "updating the current location ");
        forceSingleProviderUpdate(LocationManager.GPS_PROVIDER);
        forceSingleProviderUpdate(LocationManager.NETWORK_PROVIDER);
    }

    public void doLocationUpdates(Location location, boolean force)
    {
        long minDistance = getMinDistanceFromPrefs();

        if (location == null)
        {
            if (force)
            {
                return ;
            }
        }
    }

    private long getGPSCheckMinutesFromPrefs()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int checkMinutes = 15;
        try
        {
            checkMinutes = Integer.parseInt(prefs.getString(KEY_GPS_CHECK_INTERVAL, "15"));
        } catch (NumberFormatException e)
        {
            Log.d(TAG, " the number format exception happened");
        }
        return checkMinutes;
    }

    private long getMinDistanceFromPrefs()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int minDist = 1000;
        try
        {
            minDist = Integer.parseInt(preferences.getString(KEY_GPS_MIN_DISTANCE, "1000"));
        } catch (NumberFormatException e)
        {
            Log.d(TAG, " exception happened in parsing the min distance ");
        }
        return minDist;

    }

    public boolean checkIfGPSTurnOn()
    {
        LocationManager locationManager = getLocationManager();
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    private Location getLocationByProvider(String providerName)
    {
        Location location = null;
        if (!isProviderSupported(providerName))
            return null;
        LocationManager locationManager = getLocationManager();

        try
        {
            if (locationManager.isProviderEnabled(providerName))
            {
                location = locationManager.getLastKnownLocation(providerName);
            }
        } catch (Exception e)
        {
            Log.d(TAG, " the expcetion happened in getting the location provider and the error are : " + e.toString());
        }

        return location;
    }

    private boolean isProviderSupported(String provider)
    {
        if (TextUtils.isEmpty(provider))
            throw new IllegalArgumentException(" the provide must be specified ");
        LocationManager locationManager = getLocationManager();
        int locationProviderNum;
        List<String> locationProviderList;

        try
        {
            locationProviderList = locationManager.getAllProviders();
        } catch (Throwable e)
        {
            return false;
        }
        final int size = locationProviderList.size();

        for (locationProviderNum = 0; locationProviderNum < size; ++locationProviderNum)
        {
            if (provider.equals(String.valueOf(locationProviderList.get(locationProviderNum))))
            {
                return true;
            }
        }
        return false;
    }


    private LocationManager getLocationManager()
    {
        if (null == mLocationManager)
        {
            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        }

        return mLocationManager;
    }

    private class YQLocationListener implements LocationListener
    {
        private String provider;

        @Override
        public void onLocationChanged(Location location)
        {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        @Override
        public void onProviderEnabled(String provider)
        {

        }

        @Override
        public void onProviderDisabled(String provider)
        {

        }
    }

    private class SingleUpdateLocationListener implements LocationListener
    {

        @Override
        public void onLocationChanged(Location location)
        {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        @Override
        public void onProviderEnabled(String provider)
        {

        }

        @Override
        public void onProviderDisabled(String provider)
        {

        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {

    }
}

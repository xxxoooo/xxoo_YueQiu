package com.yueqiu.util;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.yueqiu.R;

/**
 * Created by doushuqi on 15/1/20.
 * 优先取GPS定位其次是网络获得位置，当timeout时取lastlocation
 * 若依然为null，弹出toast警告用户，这时候的isTimeout变量将设为true
 * 并返回给接收器，接收端需先判断isTimeout，若为true说明未获得位置信息
 */
public class LocationUtil extends Service {
    private static final String TAG = "LocationUtil";
    private Location mLocation;
    private int TIMEOUT_SEC = 5;    //Timeout Sec
    private boolean flagGetGPSDone = false;
    private boolean flagNetworkDone = false;
    private boolean flagGPSEnable = true;
    private boolean flagNetworkEnable = true;
    private LocationManager myLocationManager;
    private Location culocationGPS = null;
    private Location culocationNetwork = null;
    private Location bestLocation = null;
    private Handler handler = new Handler();
    private int counts = 0;
    public static final String LOCATION_KEY = "com.yueqiu.util.LocationUtil.location_key";
    public static final String ISTIMEOUT_KEY = "com.yueqiu.util.LocationUtil.is_timeout_key";
    public static final String BROADCAST_FILTER = "com.yueqiu.util.LocationUtil.local_broadcast";
    private boolean isTimeout;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initGPS();
        return super.onStartCommand(intent, flags, startId);

    }

    private Runnable showTime = new Runnable() {
        public void run() {
            counts++;
            if (counts > TIMEOUT_SEC) {
                flagGetGPSDone = true;
                flagNetworkDone = true;
            }
            bestLocation = getCurrentLocation();

            if (bestLocation == null) {
                //如果 bestLocation == null 表示未取得最新位置, 等待 continue wait......
                //若已经超时还取得null，则抛timeout提示
                if (counts > TIMEOUT_SEC) {
                    Utils.showToast(LocationUtil.this, getString(R.string.location_request_time_out));
                    isTimeout = true;
                    sendBroadcast();
                } else
                    handler.postDelayed(showTime, 1000);
            } else {
                mLocation = bestLocation;
                //返回最优的位置信息，通过本地广播返回给接收器！
                sendBroadcast();
            }
        }
    };

    private void sendBroadcast() {
        Intent intent = new Intent(BROADCAST_FILTER);
        Bundle bundle = new Bundle();
        bundle.putBoolean(ISTIMEOUT_KEY, isTimeout);
        bundle.putParcelable(LOCATION_KEY, mLocation);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
        LocationUtil.this.stopSelf();
    }

    private Location getCurrentLocation() {
        Location retLocation = null;
        if ((flagGetGPSDone || flagNetworkDone)) {
            culocationGPS = myLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            culocationNetwork = myLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (culocationGPS == null && culocationNetwork == null) {
                retLocation = myLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                if (retLocation == null) {
                    retLocation = new Location(LocationManager.PASSIVE_PROVIDER);
                }
            } else {
                retLocation = culocationGPS != null ? culocationGPS : culocationNetwork;
            }
            //取得最佳LOCATION后 停止所有定位
            stopAllUpdate();
        }
        return retLocation;
    }

    private void initGPS() {
        myLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        flagGPSEnable = myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        flagNetworkEnable = myLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        flagGetGPSDone = !flagGPSEnable;
        flagNetworkDone = !flagNetworkEnable;
        bestLocation = null;
        counts = 0;
        startAllUpdate();
        handler.postDelayed(showTime, 1000);
    }

    //Turn on the  GPS NETWORK update
    public void startAllUpdate() {
        if (flagGPSEnable)
            myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener01);
        if (flagNetworkEnable)
            myLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener02);
    }

    //Turn of  GPS NETWORK update
    public void stopAllUpdate() {
        myLocationManager.removeUpdates(mLocationListener01);
        myLocationManager.removeUpdates(mLocationListener02);
    }

    public final LocationListener mLocationListener01 = new LocationListener() {
        public void onLocationChanged(Location location) {
            OnGPSChange();
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    public final LocationListener mLocationListener02 = new LocationListener() {
        public void onLocationChanged(Location location) {
            OnNetworkChange();
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void OnGPSChange() {
        flagGetGPSDone = true;
        flagNetworkDone = true;
        stopAllUpdate();
    }

    private void OnNetworkChange() {
        flagNetworkDone = true;
        myLocationManager.removeUpdates(mLocationListener02);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}

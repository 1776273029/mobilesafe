package com.test.android.mobilesafe.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;

import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.SpUtil;

public class LocationService extends Service {
    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //以最优方式获取经纬度坐标
        Criteria criteria = new Criteria();
        //允许花费
        criteria.setCostAllowed(true);
        //指定获取经纬度的精确度
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestProvider = lm.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //在一定的时间间隔，移动一定的距离后获取经纬度坐标
        MyLocationListener listener = new MyLocationListener();
        lm.requestLocationUpdates(bestProvider, 0, 0, listener);
    }

    class MyLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            //发送短信
            SmsManager manager = SmsManager.getDefault();
            String address = SpUtil.getString(getApplicationContext(), ConstantValue.CONTACT_PHONE,"");
            manager.sendTextMessage(address,null,latitude +","+ longitude,null,null);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

package com.test.android.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.test.android.mobilesafe.domain.ProcessInfo;
import com.test.android.mobilesafe.engine.ProcessInfoProvider;

public class LockClearService extends Service {

    private IntentFilter filter;
    private InnerReceiver receiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        receiver = new InnerReceiver();
        registerReceiver(receiver,filter);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (receiver != null){
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    private class InnerReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            ProcessInfoProvider.killAll(context);
        }
    }
}

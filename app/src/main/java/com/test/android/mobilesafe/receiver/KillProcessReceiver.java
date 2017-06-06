package com.test.android.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.test.android.mobilesafe.engine.ProcessInfoProvider;

public class KillProcessReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        ProcessInfoProvider.killAll(context);
    }
}

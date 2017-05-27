package com.test.android.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import com.test.android.mobilesafe.activity.Setup2Activity;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.SpUtil;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String sim_num = SpUtil.getString(context, ConstantValue.SIM_NUMBER,"");
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String simSerialNumber = manager.getSimSerialNumber() + "xxx";//获取序列号
        if (!sim_num.equals(simSerialNumber)){
            SmsManager sm = SmsManager.getDefault();
            String num = SpUtil.getString(context,ConstantValue.CONTACT_PHONE,"");
            sm.sendTextMessage(num,null,"sim change!!!",null,null);
        }
    }
}

package com.test.android.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.test.android.mobilesafe.db.dao.BlackNumberDao;
import com.test.android.mobilesafe.domain.BlackNumberInfo;

import java.lang.reflect.Method;
import java.util.List;

public class BlackNumberService extends Service {

    private InnerSmsReceiver smsReceiver;
    private BlackNumberDao dao;
    private List<BlackNumberInfo> list;
    private TelephonyManager mTM;
    private MyPhoneStateListener listener;
    private MyContentObserver observer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        dao = BlackNumberDao.getInstance(getApplicationContext());

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(Integer.MAX_VALUE);//设置优先级
        smsReceiver = new InnerSmsReceiver();
        registerReceiver(smsReceiver,filter);

        mTM = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        listener = new MyPhoneStateListener();
        mTM.listen(listener,PhoneStateListener.LISTEN_CALL_STATE);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (smsReceiver != null){
            unregisterReceiver(smsReceiver);
        }
        if (mTM != null && listener != null){
            //取消对电话状态的监听（开启服务的时候监听电话的对象）
            mTM.listen(listener,PhoneStateListener.LISTEN_NONE);
        }
        //注销内容观察者
        if (observer != null){
            getContentResolver().unregisterContentObserver(observer);
        }
        super.onDestroy();
    }

    private class InnerSmsReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            //获取短信内容
            Object[] objects = (Object[]) intent.getExtras().get("pdus");
            //循环遍历短信
            for (Object object : objects) {
                //获取短信对象
                SmsMessage msg = SmsMessage.createFromPdu((byte[]) object);
                //获取短信对象的基本信息
                String address = msg.getOriginatingAddress();
                int mode = dao.getMode(address);
                if (mode == 0 || mode == 2){
                    abortBroadcast();//拦截广播，Android 4.4之后不能中断广播
                }
            }
        }
    }

    class MyPhoneStateListener extends PhoneStateListener{
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state){
                case TelephonyManager.CALL_STATE_IDLE://空闲状态，没有任何活动
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK://摘机（拨打或通话）
                    break;
                case TelephonyManager.CALL_STATE_RINGING://响铃
                    endCall(incomingNumber);
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    private void endCall(String phone) {
        int mode = dao.getMode(phone);
        if (mode == 1 || mode == 2){
            try {
//                ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                //获取ServiceManager的字节码文件
                Class<?> clazz = Class.forName("android.os.ServiceManager");
                //获取方法
                Method method = clazz.getMethod("getService",String.class);
                //反射调用此方法
                IBinder binder = (IBinder) method.invoke(null,Context.TELEPHONY_SERVICE);
                //调用获取aidl文件对象的方法
                ITelephony iTelephony = ITelephony.Stub.asInterface(binder);
                //调用在aidl中隐藏的endCall（）方法
                iTelephony.endCall();
            }catch (Exception e){
                e.printStackTrace();
            }
            //在内容解析器上注册内容观察者，观察数据库的变化
            observer = new MyContentObserver(new Handler(),phone);
            getContentResolver().registerContentObserver(
                    Uri.parse("content://call_log/calls"),true, observer);
        }
    }


    private class MyContentObserver extends ContentObserver{

        private String phone = null;
        public MyContentObserver(Handler handler,String phone) {
            super(handler);
            this.phone = phone;
        }

        @Override//数据发生改变时会调用这个方法
        public void onChange(boolean selfChange) {
            //删除记录
            getContentResolver().delete(Uri.parse("content://call_log/calls"),
                    "number = ?",new String[]{phone});
            super.onChange(selfChange);
        }
    }

}

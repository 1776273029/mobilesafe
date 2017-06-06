package com.test.android.mobilesafe.service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.RemoteViews;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.engine.ProcessInfoProvider;
import com.test.android.mobilesafe.receiver.MyAppWidgetProvider;

import java.util.Timer;
import java.util.TimerTask;

import static com.jaredrummler.android.processes.AndroidProcesses.TAG;

public class UpdateWidgetService extends Service {

    private Timer mTimer;
    private InnerReceiver receiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //管理进程总数和可用内存（定时器）
        startTimer();
        //注册开锁，解锁广播接收者
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.setPriority(800);
        receiver = new InnerReceiver();
        registerReceiver(receiver,filter);
        super.onCreate();
    }

    private void startTimer() {
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //UI定时更新
                UpdateAppWidget();
                Log.i(TAG, "run: 更新数据......");
            }
        }, 0, 10000);
    }

    private void UpdateAppWidget() {
        //获取AppWidget对象
        AppWidgetManager mAWM = AppWidgetManager.getInstance(this);
        //获取窗体小部件布局转换成的View对象（定位应用的包名，相应的布局文件）
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.my_app_widget);
        //给内部控件赋值
        remoteViews.setTextViewText(R.id.process_count,"进程总数：" + ProcessInfoProvider.getProcessCount(this));
        String availSpace = Formatter.formatFileSize(this,ProcessInfoProvider.getAvailSpace(this));
        remoteViews.setTextViewText(R.id.process_memory,"可用内存：" + availSpace);
        //点击窗体小部件，进入应用
        Intent intent = new Intent("mobilesafe.action.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,
                PendingIntent.FLAG_CANCEL_CURRENT);//点击完成后
        //在哪个控件上响应点击事件，延期意图
        remoteViews.setOnClickPendingIntent(R.id.ll_widget_root,pendingIntent);
        //给窗体小部件上按钮添加点击事件
        Intent broadCastIntent = new Intent("mobilesafe.action.KILL_BACKGROUND_PROCESS");
        PendingIntent bt_pendingIntent = PendingIntent.getBroadcast(this,0,broadCastIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        //通过延期意图发送广播，在广播接收者中杀死后台进程
        remoteViews.setOnClickPendingIntent(R.id.btn_clear,bt_pendingIntent);

        //上下文环境，窗体小部件对应的广播接收者的字节码文件
        ComponentName componentName = new ComponentName(this,MyAppWidgetProvider.class);
        //更新窗体小部件
        mAWM.updateAppWidget(componentName,remoteViews);
    }

    @Override
    public void onDestroy() {
        if (receiver != null){
            unregisterReceiver(receiver);
        }
        if (mTimer != null){
            mTimer.cancel();
        }
        super.onDestroy();
    }

    private class InnerReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                startTimer();
            }else {
                cancelTimerTask();
            }
        }
    }

    private void cancelTimerTask() {
        if (mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }
}

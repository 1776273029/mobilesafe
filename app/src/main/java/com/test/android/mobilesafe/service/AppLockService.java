package com.test.android.mobilesafe.service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;

import com.test.android.mobilesafe.activity.EnterPsdActivity;
import com.test.android.mobilesafe.activity.HomeActivity;
import com.test.android.mobilesafe.db.dao.AppLockDao;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppLockService extends Service {

    private boolean isWatch;
    private AppLockDao dao;
    private List<String> lockApps;
    private UsageStatsManager mUsageStatsManager;
    private long time;
    private ActivityManager mAM;
    private List<ActivityManager.RunningTaskInfo> runningTasks;
    private ActivityManager.RunningTaskInfo runningTaskInfo;
    private List<UsageStats> stats;
    private String packageName;
    private InnerReceiver receiver;
    private IntentFilter filter;
    private String skipPackageName;
    private MyContentObserver observer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        dao = AppLockDao.getInstance(this);
        isWatch = true;
        watch(isWatch);
        filter = new IntentFilter();
        filter.addAction("mobilesafe.intent.action.SKIP");
        receiver = new InnerReceiver();
        registerReceiver(receiver,filter);
        //注册一个内容观察者观察数据库的变化
        observer = new MyContentObserver(new Handler());
        getContentResolver().registerContentObserver(
                Uri.parse("content://applock/change"),true, observer);
        super.onCreate();
    }

    private void watch(final boolean isWatch) {
        new Thread(){
            public void run(){
                lockApps = dao.findAll();
                packageName = null;
                while (isWatch) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                        //检测正在开启的应用（任务栈）
                        mAM = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                        runningTasks = mAM.getRunningTasks(1);
                        runningTaskInfo = runningTasks.get(0);
                        packageName = runningTaskInfo.topActivity.getPackageName();
                    }else {
                        mUsageStatsManager = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
                        time = System.currentTimeMillis();
                        stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000*2, time);
                        if(stats != null) {
                            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                            for (UsageStats usageStats : stats) {
                                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                            }
                            if (mySortedMap != null && !mySortedMap.isEmpty()) {
                                packageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                            }
                        }
                    }
                    if (lockApps.contains(packageName)){
                        if (!packageName.equals(skipPackageName)){
                            //弹出界面
                            Intent intent = new Intent(getApplicationContext(), EnterPsdActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("packagename", packageName);
                            startActivity(intent);
                        }
                    }
                    try {
                        //睡眠一下，时间片轮转
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        isWatch = false;
        if (receiver != null){
            unregisterReceiver(receiver);
        }
        if (observer != null){
            getContentResolver().unregisterContentObserver(observer);
        }
        super.onDestroy();
    }

    private class InnerReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            skipPackageName = intent.getStringExtra("packagename");
        }
    }

    private class MyContentObserver extends ContentObserver{

        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            //一旦数据库发生改变时调用方法
            new Thread(){
                public void run(){
                    lockApps = dao.findAll();
                }
            }.start();
            super.onChange(selfChange);
        }
    }
}



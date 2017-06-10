package com.test.android.mobilesafe.activity;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.test.android.mobilesafe.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CacheClearActivity extends AppCompatActivity {

    private static final int UPDATE_CACHE_APP = 1;
    private static final int CHECK_CACHE_APP = 2;
    private static final int SCAN_FINISH = 3;
    private static final int CLEAR_CACHE = 4;
    private Button bt_clear;
    private ProgressBar pb_bar;
    private TextView tv_name;
    private LinearLayout ll_add_item;
    private int index = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_clear);
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ll_add_item.removeAllViews();
        initData();
    }

    private void initData() {
        new Thread(){
            public void run(){
                PackageManager mPM = getPackageManager();
                List<PackageInfo> installedPackages = mPM.getInstalledPackages(0);
                pb_bar.setMax(installedPackages.size());
                for (PackageInfo packageinfo : installedPackages) {
                    String packageName = packageinfo.packageName;
                    Drawable icon = packageinfo.applicationInfo.loadIcon(mPM);
                    getPackageCache(packageName);
                    try {
                        Thread.sleep(50 + new Random().nextInt(100));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    index ++;
                    pb_bar.setProgress(index);
                    Message msg = Message.obtain();
                    msg.what = CHECK_CACHE_APP;
                    String name = null;
                    try {
                        name = mPM.getApplicationInfo(packageinfo.packageName,0).loadLabel(mPM).toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    msg.obj = name;
                    mHandler.sendMessage(msg);

                }
                Message msg = Message.obtain();
                msg.what = SCAN_FINISH;
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    private void initUI() {
        bt_clear = (Button) findViewById(R.id.bt_clear);
        pb_bar = (ProgressBar) findViewById(R.id.pb_bar);
        tv_name = (TextView) findViewById(R.id.tv_name);
        ll_add_item = (LinearLayout) findViewById(R.id.ll_add_item);
    }

    
    class CacheInfo{
        public String name;
        public Drawable icon;
        public String packageName;
        public String cacheSize;
    }
    
    public void getPackageCache(final String packageName){
        final PackageManager mPM = getPackageManager();
        IPackageStatsObserver.Stub mStatsobserver = new IPackageStatsObserver.Stub() {
            @Override
            public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                //运行在子线程
                long cacheSize = pStats.cacheSize;
                if (Build.VERSION.SDK_INT == 22 && Formatter.formatFileSize(getApplicationContext(), cacheSize).equals("12.00 KB")) {
                    return;
                } else if (cacheSize > 0) {
                    Message msg = Message.obtain();
                    msg.what = UPDATE_CACHE_APP;
                    CacheInfo cacheInfo = null;
                    try {
                        cacheInfo = new CacheInfo();
                        cacheInfo.name = mPM.getApplicationInfo(pStats.packageName, 0).loadLabel(mPM).toString();
                        cacheInfo.icon = mPM.getApplicationInfo(pStats.packageName, 0).loadIcon(mPM);
                        cacheInfo.packageName = pStats.packageName;
                        cacheInfo.cacheSize = Formatter.formatFileSize(getApplicationContext(), cacheSize);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    msg.obj = cacheInfo;
                    mHandler.sendMessage(msg);
                }
            }
        };
        //mPM.getPackageSizeInfo("com.android.browser",mStatsobserver);
        //反射调用系统隐藏方法
        try {
            //获取指定类的字节码文件
            Class<?> clazz = Class.forName("android.content.pm.PackageManager");
            //获取调用方法对象
            Method method = clazz.getMethod("getPackageSizeInfo",String.class,IPackageStatsObserver.class);
            //获取对象调用方法
            method.invoke(mPM,packageName,mStatsobserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_CACHE_APP:
                    View view = View.inflate(getApplicationContext(),R.layout.listview_app_cache_item,null);
                    ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
                    TextView tv_item_name = (TextView) view.findViewById(R.id.tv_app_name);
                    TextView tv_cache = (TextView) view.findViewById(R.id.tv_cache_size);
                    ImageView iv_clear = (ImageView) view.findViewById(R.id.iv_clear);
                    final CacheInfo cacheInfo = (CacheInfo) msg.obj;
                    iv_icon.setBackgroundDrawable(cacheInfo.icon);
                    tv_item_name.setText(cacheInfo.name);
                    tv_cache.setText(cacheInfo.cacheSize);
                    iv_clear.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //需要系统权限无法实现
//                            try {
//                                PackageManager mPM = getPackageManager();
//                                Class<?> clazz = Class.forName("android.content.pm.PackageManager");
//                                Method method = clazz.getMethod("deleteApplicationCacheFiles",String.class, IPackageDataObserver.class);
//                                method.invoke(mPM, cacheInfo.packageName, new IPackageDataObserver.Stub(){
//                                    @Override//清除缓存完成后调用方法
//                                    public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
//                                        Log.d("MyLog:","ok");
//                                        if (succeeded){
//                                            Log.d("MyLog:","ok!!!");
////                                            Message msg = Message.obtain();
////                                            msg.what = CLEAR_CACHE;
////                                            mHandler.sendMessage(msg);
//                                        }
//                                    }
//                                });
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
                            //通过查看系统日志，获取开启系统清理缓存activity的action和data
                            //调用系统清除缓存的界面
                            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                            intent.setData(Uri.parse("package:" + cacheInfo.packageName));
                            startActivityForResult(intent,0);
                        }
                    });
                    ll_add_item.addView(view,0);
                    break;
                case CHECK_CACHE_APP:
                    tv_name.setText(msg.obj.toString());
                    break;
                case SCAN_FINISH:
                    tv_name.setText("扫描完成");
                    final PackageManager mPM = getPackageManager();
                    bt_clear.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Class<?> clazz = Class.forName("android.content.pm.PackageManager");
                                Method method = clazz.getMethod("freeStorageAndNotify",long.class, IPackageDataObserver.class);
                                method.invoke(mPM, Long.MAX_VALUE, new IPackageDataObserver.Stub(){
                                    @Override//清除缓存完成后调用方法
                                    public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
                                        if (succeeded){
                                            Message msg = Message.obtain();
                                            msg.what = CLEAR_CACHE;
                                            mHandler.sendMessage(msg);
                                        }
                                    }
                                });
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                case CLEAR_CACHE:
                    ll_add_item.removeAllViews();
                    tv_name.setText("清理完成");
                    break;
            }
        }
    };
    
}

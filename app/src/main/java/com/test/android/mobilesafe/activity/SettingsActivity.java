package com.test.android.mobilesafe.activity;

import android.Manifest;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.SQLException;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.service.AddressService;
import com.test.android.mobilesafe.service.AppLockService;
import com.test.android.mobilesafe.service.BlackNumberService;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.ServiceUtil;
import com.test.android.mobilesafe.util.SpUtil;
import com.test.android.mobilesafe.view.SettingClickView;
import com.test.android.mobilesafe.view.SettingItemView;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private SettingClickView scv_toast_style;
    private String[] mToastStyleDes;
    private int mToastStyle;
    private SettingClickView scv_toast_location;
    private SettingItemView siv_blacknumber;
    private SettingItemView siv_appLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initUpdate();
        initAddress();
        initToastStyle();
        initLocation();
        initBlackNumber();
        initAppLock();
    }

    private void initAppLock() {
        siv_appLock = (SettingItemView) findViewById(R.id.siv_app_lock);
        boolean isRunning = ServiceUtil.isRunning(this,
                "com.test.android.mobilesafe.service.AppLockService");
        siv_appLock.setCheck(isRunning);
        siv_appLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = siv_appLock.isCheck();
                siv_appLock.setCheck(!isCheck);
                if (!isCheck) {
                    if (isNoOptions()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (!isNoSwitch()) {
                                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                startActivity(intent);
                            }
                        }
                        startService(new Intent(getApplicationContext(), AppLockService.class));
                    } else {
                        stopService(new Intent(getApplicationContext(), AppLockService.class));
                    }
                }
            }
        });
    }
                //判断是否拥有查看权限
    private boolean isNoOptions() {
        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    //判断是否选中
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean isNoSwitch() {
        long dujinyang = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService("usagestats");
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST, 0, dujinyang );
        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
            return false;
        }
        return true;
    }

    private void initBlackNumber() {

        siv_blacknumber = (SettingItemView) findViewById(R.id.siv_blacknumber);
        final boolean isRunning = ServiceUtil.isRunning(this,
                "com.test.android.mobilesafe.service.BlackNumberService");
        siv_blacknumber.setCheck(isRunning);
        siv_blacknumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = siv_blacknumber.isCheck();
                siv_blacknumber.setCheck(!isCheck);
                if (!isCheck){
                    startService(new Intent(getApplicationContext(), BlackNumberService.class));
                }else {
                    stopService(new Intent(getApplicationContext(),BlackNumberService.class));
                }
            }
        });
    }

    private void initLocation() {
        scv_toast_location = (SettingClickView) findViewById(R.id.scv_toast_location);
        scv_toast_location.setTitle("归属地提示框的位置");
        scv_toast_location.setDes("设置归属地提示框的位置");
        scv_toast_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ToastLocationActivity.class));
            }
        });
    }

    private void initToastStyle() {
        scv_toast_style = (SettingClickView) findViewById(R.id.scv_toast_style);
        scv_toast_style.setTitle("设置归属地显示风格");
        mToastStyleDes = new String[]{"透明","橙色","蓝色","灰色","绿色"};
        mToastStyle = SpUtil.getInt(this, ConstantValue.TOAST_STYLE,0);
        scv_toast_style.setDes(mToastStyleDes[mToastStyle]);
        scv_toast_style.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToastStyleDialog();
            }
        });
    }

    private void showToastStyleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.main_icon);
        builder.setTitle("请选择归属地样式");
        mToastStyle = SpUtil.getInt(this,ConstantValue.TOAST_STYLE,0);
        builder.setSingleChoiceItems(mToastStyleDes, mToastStyle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SpUtil.putInt(getApplicationContext(),ConstantValue.TOAST_STYLE,which);
                dialog.dismiss();
                scv_toast_style.setDes(mToastStyleDes[which]);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void initAddress() {
        final SettingItemView siv_address = (SettingItemView) findViewById(R.id.siv_address);
        //通过与服务绑定获取当前状态
        boolean isRunning = ServiceUtil.isRunning(this,
                "com.test.android.mobilesafe.service.AddressService");
        siv_address.setCheck(isRunning);
        siv_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = siv_address.isCheck();
                siv_address.setCheck(!isCheck);
                if (!isCheck){
                    startService(new Intent(getApplicationContext(), AddressService.class));
                }else {
                    stopService(new Intent(getApplicationContext(), AddressService.class));
                }
            }
        });
    }

    private void initUpdate() {
        final SettingItemView siv_update = (SettingItemView) findViewById(R.id.siv_update);
        boolean open_update = SpUtil.getBoolean(this, ConstantValue.OPEN_UPDATE,false);
        siv_update.setCheck(open_update);
        siv_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = siv_update.isCheck();
                siv_update.setCheck(!isCheck);
                SpUtil.putBoolean(getApplicationContext(),ConstantValue.OPEN_UPDATE,!isCheck);
            }
        });
    }

}
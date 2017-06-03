package com.test.android.mobilesafe.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.support.annotation.NonNull;
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
import com.test.android.mobilesafe.service.BlackNumberService;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.ServiceUtil;
import com.test.android.mobilesafe.util.SpUtil;
import com.test.android.mobilesafe.view.SettingClickView;
import com.test.android.mobilesafe.view.SettingItemView;

public class SettingsActivity extends AppCompatActivity {

    private SettingClickView scv_toast_style;
    private String[] mToastStyleDes;
    private int mToastStyle;
    private SettingClickView scv_toast_location;
    private SettingItemView siv_blacknumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initUpdate();
        initAddress();
        initToastStyle();
        initLocation();
        initBlackNumber();
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
        builder.setIcon(R.mipmap.ic_launcher);
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
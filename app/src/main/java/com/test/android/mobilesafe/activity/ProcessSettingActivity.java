package com.test.android.mobilesafe.activity;

import android.app.Service;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.service.LockClearService;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.ServiceUtil;
import com.test.android.mobilesafe.util.SpUtil;

public class ProcessSettingActivity extends AppCompatActivity {

    private CheckBox cb_show_system;
    private CheckBox cb_lock_clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_setting);
        initSystemShow();
        initLockScreenClear();
    }

    private void initLockScreenClear() {
        boolean isRunning = ServiceUtil.isRunning(this,"com.test.android.mobilesafe.service.LockClearService");
        cb_lock_clear = (CheckBox) findViewById(R.id.cb_lock_clear);
        cb_lock_clear.setChecked(isRunning);
        if (isRunning){
            cb_lock_clear.setText("锁屏清理已开启");
        }else {
            cb_lock_clear.setText("锁屏清理已关闭");
        }
        cb_lock_clear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //isChecked作为是否选中的状态
                if (isChecked){
                    cb_lock_clear.setText("锁屏清理已开启");
                    startService(new Intent(getApplicationContext(), LockClearService.class));
                }else {
                    cb_lock_clear.setText("锁屏清理已关闭");
                    stopService(new Intent(getApplicationContext(),LockClearService.class));
                }
            }
        });
    }

    private void initSystemShow() {
        boolean isCheck = SpUtil.getBoolean(this,ConstantValue.SHOW_SYSTEM,false);
        cb_show_system = (CheckBox) findViewById(R.id.cb_show_system);
        cb_show_system.setChecked(isCheck);
        if (isCheck){
            cb_show_system.setText("显示系统进程");
        }else {
            cb_show_system.setText("隐藏系统进程");
        }
        cb_show_system.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //isChecked作为是否选中的状态
                if (isChecked){
                    cb_show_system.setText("显示系统进程");
                }else {
                    cb_show_system.setText("隐藏系统进程");
                }
                SpUtil.putBoolean(getApplicationContext(), ConstantValue.SHOW_SYSTEM,isChecked);
            }
        });
    }
}

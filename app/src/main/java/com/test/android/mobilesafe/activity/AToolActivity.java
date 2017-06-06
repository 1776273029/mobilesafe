package com.test.android.mobilesafe.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.engine.SmsBackup;

import java.io.File;

public class AToolActivity extends AppCompatActivity {

    private TextView tv_query_address;
    private TextView tv_backup_sms;
    private TextView tv_number_query;
    private TextView tv_app_lock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atool);
        initPhoneAddress();
        initBackupSms();
        initCommonNumberQuery();
        initAppLock();
    }

    private void initAppLock() {
        tv_app_lock = (TextView) findViewById(R.id.tv_app_lock);
        tv_app_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),AppLockActivity.class));
            }
        });
    }

    private void initCommonNumberQuery() {
        tv_number_query = (TextView) findViewById(R.id.tv_commonnumber_query);
        tv_number_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),CommonNumberQueryActivity.class));
            }
        });
    }

    private void initBackupSms() {
        tv_backup_sms = (TextView) findViewById(R.id.tv_backup_sms);
        tv_backup_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBackupSmsDialog();
            }
        });
    }

    private void showBackupSmsDialog() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIcon(R.drawable.main_icon);
        dialog.setTitle("短信备份");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();
        new Thread(){
            public void run(){
                String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator + "sms.xml";
                SmsBackup.backup(AToolActivity.this, path, new SmsBackup.CallBack() {
                    @Override
                    public void setMaX(int max) {
                        dialog.setMax(max);
                    }

                    @Override
                    public void setProgress(int index) {
                        dialog.setProgress(index);
                    }
                });
                dialog.dismiss();
            }
        }.start();
    }

    private void initPhoneAddress() {
        tv_query_address = (TextView) findViewById(R.id.tv_query_phone_address);
        tv_query_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),QueryAddressActivity.class));
            }
        });
    }
}

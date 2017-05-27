package com.test.android.mobilesafe.deviceadmin;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.test.android.mobilesafe.R;

public class MyDeviceAdminActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bt_start;
    private Button bt_lock;
    private Button bt_wipedata;
    private Button bt_uninstall;
    private ComponentName mDeviceAdmin;
    private DevicePolicyManager mDPM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_device_admin);
        bt_start = (Button) findViewById(R.id.bt_start);
        bt_lock = (Button) findViewById(R.id.bt_lock);
        bt_wipedata = (Button) findViewById(R.id.bt_wipedata);
        bt_uninstall = (Button) findViewById(R.id.bt_uninstall);
        bt_start.setOnClickListener(this);
        bt_lock.setOnClickListener(this);
        bt_wipedata.setOnClickListener(this);
        bt_uninstall.setOnClickListener(this);
        //上下文环境，广播接收者所对应的字节码文件
        //组件对象可以作为是否激活的判断标志
        mDeviceAdmin = new ComponentName(this, DeviceAdmin.class);
        //获取设备的管理者对象
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_start:
                // 开启应用管理器激活界面的activity
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"设备管理器");
                startActivity(intent);
                break;
            case R.id.bt_lock:
                //判断是否激活
                if (mDPM.isAdminActive(mDeviceAdmin)){
                    //锁屏
                    mDPM.lockNow();
                    //设置密码
                    mDPM.resetPassword("123",0);
                }else {
                    Toast.makeText(getApplicationContext(),
                            "没有激活设备管理器",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_wipedata:
                //清除数据
                mDPM.wipeData(0);//清除手机数据
                mDPM.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);//清除SD卡数据
                break;
            case R.id.bt_uninstall:
                //卸载
                // 在设备管理器中没有激活的应用可以卸载，
                // 作为设备管理器的应用不可以卸载，系统会提示取消在设备管理器中激活，然后才可以卸载
                Intent intent1 = new Intent("android.intent.action.DELETE");
                intent1.addCategory("android.intent.category.DEFAULT");
                intent1.setData(Uri.parse("package:"+getPackageName()));
                startActivity(intent1);
                break;
        }
    }
}

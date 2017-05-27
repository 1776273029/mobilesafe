package com.test.android.mobilesafe.receiver;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.deviceadmin.DeviceAdmin;
import com.test.android.mobilesafe.service.LocationService;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.SpUtil;

public class SmsReceiver extends BroadcastReceiver {

    private ComponentName mDeviceAdmin;
    private DevicePolicyManager mDPM;
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean open_security = SpUtil.getBoolean(context, ConstantValue.OPEN_SECURITY, false);
        //上下文环境，广播接收者所对应的字节码文件
        //组件对象可以作为是否激活的判断标志
        mDeviceAdmin = new ComponentName(context, DeviceAdmin.class);
        //获取设备的管理者对象
        mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (open_security) {
            //获取短信内容
            Object[] objects = (Object[]) intent.getExtras().get("pdus");
            //循环遍历短信
            for (Object object : objects) {
                //获取短信对象
                SmsMessage msg = SmsMessage.createFromPdu((byte[]) object);
                //获取短信对象的基本信息
                String address = msg.getOriginatingAddress();
                String msgBody = msg.getMessageBody();
                //判断是否包含关键字
                if (msgBody.contains("#*alarm*#")) {
                    //播放音乐
                    MediaPlayer player = MediaPlayer.create(context, R.raw.ylzs);
                    player.setLooping(true);
                    player.start();
                } else if (msgBody.contains("#*location*#")) {
                    //开启定位服务
                    context.startService(new Intent(context,LocationService.class));
                } else if (msgBody.contains("#*lockscreen*#")){
                    //判断是否激活
                    if (mDPM.isAdminActive(mDeviceAdmin)){
                        //锁屏
                        mDPM.lockNow();
                        //设置密码
                        mDPM.resetPassword("123",0);
                    }else {
                        Toast.makeText(context,
                                "没有激活设备管理器",Toast.LENGTH_SHORT).show();
                    }
                } else if (msgBody.contains("#*wipedata*#")){
                    mDPM.wipeData(0);//清除手机数据
                    mDPM.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);//清除SD卡数据
                }
            }
        }
    }
}

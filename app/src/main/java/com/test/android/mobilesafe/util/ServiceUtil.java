package com.test.android.mobilesafe.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

import java.util.List;

/**
 * Created by Administrator on 2017/5/27.
 */

public class ServiceUtil {

    public static boolean isRunning(Context context, String serviceName){
        //获取activityManager管理者对象，可以去获取当前手机正在运行的所有服务
        ActivityManager mAM = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //获取服务的最大个数
        List<RunningServiceInfo> runningServices = mAM.getRunningServices(1000);
        //遍历获取所有正在运行的服务集合，拿到每一个服务的类名，和传递进来的类名做比对，
        // 如果一致，说明服务正在运行
        for (RunningServiceInfo serviceInfo : runningServices) {
            //获取每一个正在运行的服务的名称
            if(serviceName.equals(serviceInfo.service.getClassName())){
                return true;
            }
        }
        return false;
    }
}

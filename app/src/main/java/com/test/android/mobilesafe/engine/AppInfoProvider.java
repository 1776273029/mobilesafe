package com.test.android.mobilesafe.engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.test.android.mobilesafe.domain.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/1.
 */

public class AppInfoProvider {

    //获取当前手机的所有应用的相关信息（名称，包名，图标，（内存，SD卡），（系统，用户））
    public static List<AppInfo> getAppInfoList(Context context){
        //包的管理者对象
        PackageManager pm = context.getPackageManager();
        //获取手机上安装应用相关信息的集合
        List<PackageInfo> packageInfoList = pm.getInstalledPackages(0);
        List<AppInfo> appList = new ArrayList<AppInfo>();
        //遍历
        for (PackageInfo packageInfo : packageInfoList) {
            AppInfo app = new AppInfo();
            app.packageName = packageInfo.packageName;
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            app.uid = applicationInfo.uid;//获取每一个应用的唯一性标识
            app.name = applicationInfo.loadLabel(pm).toString();
            app.icon = applicationInfo.loadIcon(pm);
            //判断是否为系统应用（每一个手机上的应用对应的flag都不一致）
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                    == ApplicationInfo.FLAG_SYSTEM){//系统应用
                app.isSystem = true;
            }else {
                app.isSystem = false;
            }
            //判断应用安装位置（手机内存或SD卡）
            if ((applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE)
                    == ApplicationInfo.FLAG_EXTERNAL_STORAGE){//SD卡应用
                app.isSDCard = true;
            }else {
                app.isSDCard = false;
            }
            appList.add(app);
        }
        return appList;
    }
}

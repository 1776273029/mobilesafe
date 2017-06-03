package com.test.android.mobilesafe.engine;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Debug;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jaredrummler.android.processes.models.Stat;
import com.jaredrummler.android.processes.models.Statm;
import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.domain.ProcessInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.ActivityManager.MemoryInfo;
import static android.app.ActivityManager.RunningAppProcessInfo;

/**
 * Created by Administrator on 2017/6/2.
 */

public class ProcessInfoProvider {

    public static int getProcessCount(Context context){
        ActivityManager mAM = (ActivityManager) context.
                getSystemService(Context.ACTIVITY_SERVICE);
        //获取正在运行内存的集合
        if (Build.VERSION.SDK_INT >= 21){
            List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
            return processes.size();
        }else {
            List<RunningAppProcessInfo> runningAppProcesses = mAM.getRunningAppProcesses();
            return runningAppProcesses.size();
        }

    }

    public static long getAvailSpace(Context context){
        ActivityManager mAM = (ActivityManager) context.
                getSystemService(Context.ACTIVITY_SERVICE);
        //构建存储可用内存的对象
        MemoryInfo memoryInfo = new MemoryInfo();
        //给MemoryInfo对象赋（可用内存）值
        mAM.getMemoryInfo(memoryInfo);
        //获取MemoryInfo中相应可用内存大小
        long availMem = memoryInfo.availMem;
        return availMem;
    }

    public static long getTotalSpace(Context context) {
//        ActivityManager mAM = (ActivityManager) context.
//                getSystemService(Context.ACTIVITY_SERVICE);
//        //构建存储可用内存的对象
//        MemoryInfo memoryInfo = new MemoryInfo();
//        //给MemoryInfo对象赋（可用内存）值
//        mAM.getMemoryInfo(memoryInfo);
//        //获取MemoryInfo中相应总内存大小
//        long totalMem = memoryInfo.totalMem;
//        return totalMem;

        //API 16以前不能用上面的方法
        //内存大小写入文件中，读取proc/meminfo文件，adb shell  cat 小文件
        // 读取第一行，获取数字字符（kb），转化成bytes返回
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader("proc/meminfo");
            bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            char[] charArray = line.toCharArray();
            StringBuffer stringBuffer = new StringBuffer();
            for (char c: charArray) {
                if (c >= '0' && c <= '9'){
                    stringBuffer.append(c);
                }
            }
            return  Long.parseLong(stringBuffer.toString()) * 1024;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (fileReader != null && bufferedReader != null){
                try {
                    fileReader.close();
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    public static List<ProcessInfo> getProcessInfo(Context context){
        //获取进程相关信息
        ActivityManager mAM = (ActivityManager) context.
                getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager mPM = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= 21){
            //List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
            List<ActivityManager.RunningAppProcessInfo> processesInfo = AndroidProcesses.getRunningAppProcessInfo(context);
            List<ProcessInfo> processInfoList = new ArrayList<ProcessInfo>();
            for (RunningAppProcessInfo info : processesInfo) {
                try {
                    ProcessInfo process = new ProcessInfo();
                    //获取进程的名称 == 应用的包名
                    process.packageName = info.processName;
                    //获取进程占用内存的大小（传递一个进程对应的pid数组）
                    Debug.MemoryInfo[] procMemInfo  = mAM.getProcessMemoryInfo(new int[]{info.pid});
                    //返回数组中索引位置为0的对象，为当前进程的内存信息的对象
                    Debug.MemoryInfo memInfo = procMemInfo[0];
                    //获取当前进程已使用内存的大小
                    process.memSize = memInfo.getTotalPrivateDirty() * 1024;
                    try {
                        ApplicationInfo appInfo = mPM.getApplicationInfo(process.packageName,0);
                        //获取应用的名称
                        process.name = appInfo.loadLabel(mPM).toString();
                        //获取应用的图标
                        process.icon = appInfo.loadIcon(mPM);
                        //判断是否为系统进程
                        if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                                == ApplicationInfo.FLAG_SYSTEM){
                            process.isSystem = true;
                        }else {
                            process.isSystem = false;
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        process.name = info.processName;
                        process.icon = context.getResources().getDrawable(R.mipmap.ic_launcher);
                        process.isSystem = true;
                        e.printStackTrace();
                    }
                    processInfoList.add(process);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return processInfoList;
        }else {
            //获取正在运行的进程的集合
            List<RunningAppProcessInfo> runningAppProcesses = mAM.getRunningAppProcesses();
            List<ProcessInfo> processInfoList = new ArrayList<ProcessInfo>();
            for (RunningAppProcessInfo processInfo : runningAppProcesses) {
                ProcessInfo process = new ProcessInfo();
                //获取进程的名称 == 应用的包名
                process.packageName = processInfo.processName;
                //获取进程占用内存的大小（传递一个进程对应的pid数组）
                Debug.MemoryInfo[] procMemInfo  = mAM.getProcessMemoryInfo(new int[]{processInfo.pid});
                //返回数组中索引位置为0的对象，为当前进程的内存信息的对象
                Debug.MemoryInfo memInfo = procMemInfo[0];
                //获取当前进程已使用内存的大小
                process.memSize = memInfo.getTotalPrivateDirty() * 1024;
                try {
                    ApplicationInfo appInfo = mPM.getApplicationInfo(process.packageName,0);
                    //获取应用的名称
                    process.name = appInfo.loadLabel(mPM).toString();
                    //获取应用的图标
                    process.icon = appInfo.loadIcon(mPM);
                    //判断是否为系统进程
                    if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                            == ApplicationInfo.FLAG_SYSTEM){
                        process.isSystem = true;
                    }else {
                        process.isSystem = false;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    process.name = processInfo.processName;
                    process.icon = context.getResources().getDrawable(R.mipmap.ic_launcher);
                    process.isSystem = true;
                    e.printStackTrace();
                }
                processInfoList.add(process);
            }
            return processInfoList;
        }
    }

    public static void killProcess(Context context,ProcessInfo processInfo){
        ActivityManager mAM = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //杀死指定包名进程（需要权限）
        mAM.killBackgroundProcesses(processInfo.packageName);
    }

    public static void killAll(Context context){
        ActivityManager mAM = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ProcessInfo> processes = getProcessInfo(context);
        //杀死指定包名进程（需要权限）
        for (ProcessInfo process : processes) {
            if (process.packageName.equals(context.getPackageName())){
                continue;
            }
            mAM.killBackgroundProcesses(process.packageName);
        }
    }
}

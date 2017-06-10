package com.test.android.mobilesafe.global;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by zhang on 2017/6/10 21:42.
 * Project: mobilesafe.
 * Package: com.test.android.mobilesafe.global.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //捕获全局（任意模块）异常
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override//在获取到未捕获的异常后，处理的方法
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                Log.d("MyLog:","捕获到了一个程序的异常");
                String path = Environment.getExternalStorageDirectory().getAbsoluteFile() + 
                        File.separator + "error.log";
                File file = new File(path);
                try {
                    PrintWriter printWriter = new PrintWriter(file);
                    e.printStackTrace(printWriter);
                    printWriter.close();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
                //将错误日志文件上传到服务器......
                //结束应用
                System.exit(0);
            }
        });
    }
}

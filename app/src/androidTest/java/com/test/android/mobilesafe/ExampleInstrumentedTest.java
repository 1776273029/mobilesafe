package com.test.android.mobilesafe;

import android.content.Context;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.text.LoginFilter;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.test.android.mobilesafe.db.dao.BlackNumberDao;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static final String TAG = "Test";
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.test.android.mobilesafe", appContext.getPackageName());
    }

    @Test
    public void insertTest(){
        Context context = InstrumentationRegistry.getTargetContext();
        assertEquals("com.test.android.mobilesafe", context.getPackageName());
        BlackNumberDao dao = BlackNumberDao.getInstance(context);
        int mode = 0;
        for (int i = 0;i < 100;i ++){
            mode = new Random().nextInt(3);
            if (i < 10){
                dao.insert("1860000000" + i,mode+"");
            }else {
                dao.insert("186000000"+i,mode+"");
            }
        }
    }

    @Test
    public void getCacheTest(){
        final Context context = InstrumentationRegistry.getTargetContext();
        assertEquals("com.test.android.mobilesafe", context.getPackageName());
        PackageManager mPM = context.getPackageManager();
        IPackageStatsObserver.Stub mStatsobserver = new IPackageStatsObserver.Stub() {
            @Override
            public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                String cacheSize = Formatter.formatFileSize(context,pStats.cacheSize);
                Log.i(TAG , "cache:" + cacheSize);
            }
        };
        //mPM.getPackageSizeInfo("com.android.browser",mStatsobserver);
        //反射调用系统隐藏方法
        try {
            //获取指定类的字节码文件
            Class<?> clazz = Class.forName("android.content.pm.PackageManager");
            //获取调用方法对象
            Method method = clazz.getMethod("getPackageSizeInfo",String.class,IPackageStatsObserver.class);
            //获取对象调用方法
            method.invoke(mPM,context.getPackageName(),mStatsobserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

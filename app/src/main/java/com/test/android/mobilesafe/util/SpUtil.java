package com.test.android.mobilesafe.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.test.android.mobilesafe.activity.Setup2Activity;

/**
 * Created by Administrator on 2017/5/24.
 */

public class SpUtil {

    private static SharedPreferences sp;
    public static void putBoolean(Context context,String key,boolean value){
        if (sp == null){
            sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        }
        sp.edit().putBoolean(key,value).commit();
    }
    public static boolean getBoolean(Context context,String key,boolean defValue){
        if (sp == null){
            sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        }
        return sp.getBoolean(key,defValue);
    }
    public static void putString(Context context,String key,String value){
        if (sp == null){
            sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        }
        sp.edit().putString(key,value).commit();
    }
    public static String getString(Context context,String key,String defValue){
        if (sp == null){
            sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        }
        return sp.getString(key,defValue);
    }

    public static void romove(Context context, String key) {
        if (sp == null){
            context.getSharedPreferences("config",Context.MODE_PRIVATE);
        }
        sp.edit().remove(key).commit();
    }
}

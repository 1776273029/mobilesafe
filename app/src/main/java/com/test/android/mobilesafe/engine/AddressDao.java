package com.test.android.mobilesafe.engine;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Administrator on 2017/5/27.
 */

public class AddressDao {

    //数据库地址
    public static String path = "/data/data/com.test.android.mobilesafe/files/address.db";
    private static String location = "未知号码";

    public static String getAddress(String phone){
        location = "未知号码";
        //以只读方式打开数据库
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY);
        //正则表达式匹配手机号码
        //手机号码结构分析：第一位为1：^1，第二位为3-8：[3-8]，后面九位数字：\d{9}
        //对应正则表达式："^1[3-8]\\d{9}"
        String regex = "^1[3-8]\\d{9}";
        if (phone.matches(regex)){
            phone = phone.substring(0,7);
            Cursor cursor = db.query("data1",new String[]{"outkey"},
                    "id = ?",new String[]{phone},null,null,null);
            if (cursor.moveToNext()){
                String outkey = cursor.getString(0);
                Log.d("outkey", "outkey:" + outkey);
                Cursor indexCursor = db.query("data2",new String[]{"location"},"id = ?",new String[]{outkey},null,null,null);
                if (indexCursor.moveToNext()){
                    location = indexCursor.getString(0);
                    Log.d("location", "Address: " + location);
                }
            }else {
                location = "未知号码";
            }
        }else {
            int length = phone.length();
            switch (length){
                case 3:
                    location = "报警电话";
                    break;
                case 4:
                    location = "模拟器";
                    break;
                case 5:
                    location = "服务电话";
                    break;
                case 7:
                case 8:
                    location = "本地电话";
                    break;
                case 11://3+8
                    String area = phone.substring(1,3);
                    Cursor cursor = db.query("data2",new String[]{"location"},"area = ?",new String[]{area},null,null,null);
                    if (cursor.moveToNext()){
                        location = cursor.getString(0);
                    }else {
                        String area1 = phone.substring(1,4);
                        Cursor cursor1 = db.query("data2",new String[]{"location"},"area = ?",new String[]{area1},null,null,null);
                        if (cursor1.moveToNext()){
                            location = cursor1.getString(0);
                        }else {
                            location = "未知号码";
                        }
                    }
                    break;
                case 12:
                    String area1 = phone.substring(1,4);
                    Cursor cursor1 = db.query("data2",new String[]{"location"},"area = ?",new String[]{area1},null,null,null);
                    if (cursor1.moveToNext()){
                        location = cursor1.getString(0);
                    }else {
                        location = "未知号码";
                    }
                    break;
            }
        }
        return location;
    }
}

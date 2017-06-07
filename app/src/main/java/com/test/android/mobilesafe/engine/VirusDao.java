package com.test.android.mobilesafe.engine;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/27.
 */

public class VirusDao {

    //数据库地址
    public static String path = "/data/data/com.test.android.mobilesafe/files/antivirus.db";

    public static List<String> getVirusList(){
        //以只读方式打开数据库
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.query("datable",new String[]{"md5"},null,null,null,null,null);
        List<String> virusList = new ArrayList<String>();
        while (cursor.moveToNext()){
            virusList.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return virusList;
    }
}

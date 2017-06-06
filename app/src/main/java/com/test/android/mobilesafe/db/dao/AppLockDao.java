package com.test.android.mobilesafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.test.android.mobilesafe.db.AppLockOpenHelper;
import com.test.android.mobilesafe.util.ConstantValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/5.
 */

public class AppLockDao {

    private final AppLockOpenHelper mOpenHelper;
    private static AppLockDao appLockDao = null;
    private Context context;

    private AppLockDao(Context context){
        this.context = context;
        mOpenHelper = new AppLockOpenHelper(context);
    }

    public static AppLockDao getInstance(Context context){
        if (appLockDao == null){
            appLockDao = new AppLockDao(context);
        }
        return appLockDao;
    }

    public void insert(String packageName){
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("packagename",packageName);
        db.insert("applock",null,values);
        db.close();
        context.getContentResolver().notifyChange(Uri.parse("content://applock/change"),null);
    }

    public void delete(String packageName){
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete("applock","packagename = ?",new String[]{packageName});
        db.close();
        context.getContentResolver().notifyChange(Uri.parse("content://applock/change"),null);
    }

    public List<String> findAll(){
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor cursor = db.query("applock",new String[]{"packagename"},null,null,null,null,null);
        List<String> lockList = new ArrayList<String>();
        while (cursor.moveToNext()){
            lockList.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return lockList;
    }

}

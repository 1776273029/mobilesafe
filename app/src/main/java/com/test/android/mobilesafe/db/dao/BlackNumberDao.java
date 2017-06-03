package com.test.android.mobilesafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.test.android.mobilesafe.db.BlackNumberOpenHelper;
import com.test.android.mobilesafe.domain.BlackNumberInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/30.
 */

// 在方法上面输入"/**"回车自动添加注释
public class BlackNumberDao {

    private final BlackNumberOpenHelper openHelper;
    private int count;

    //BlackNumberDao单例模式
    // 1私有化构造方法
    private BlackNumberDao(Context context){
        //创建数据库以及其表结构
        openHelper = new BlackNumberOpenHelper(context);
    }
    // 2.声明一个当前类的对象
    private static BlackNumberDao blackNumberDao = null;
    // 3.提供一个静态方法，如果当前类的对象为空，创建一个新的
    public static BlackNumberDao getInstance(Context context){
        if (blackNumberDao == null){
            blackNumberDao = new BlackNumberDao(context);
        }
        return blackNumberDao;
    }

    public void insert(String phone,String mode){
        //开启数据库
        SQLiteDatabase db = openHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("phone",phone);
        values.put("mode",mode);
        db.insert("blacknumber",null,values);
        db.close();
    }

    public void delete(String phone){
        SQLiteDatabase db = openHelper.getWritableDatabase();
        db.delete("blacknumber","phone = ?",new String[]{phone});
        db.close();
    }

    public void update(String phone,String mode){
        SQLiteDatabase db = openHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("mode",mode);
        db.update("blacknumber",values,"phone = ?",new String[]{phone});
        db.close();
    }

    public List<BlackNumberInfo> findAll(){
        SQLiteDatabase db = openHelper.getWritableDatabase();
        Cursor cursor = db.query("blacknumber",new String[]{"phone","mode"},null,null,null,null,"_id desc");
        List<BlackNumberInfo> list = new ArrayList<BlackNumberInfo>();
        while (cursor.moveToNext()){
            BlackNumberInfo info = new BlackNumberInfo();
            info.phone = cursor.getString(0);
            info.mode = cursor.getString(1);
            list.add(info);
        }
        cursor.close();
        db.close();
        return list;
    }

    //每次查询20条数据（逆序）
    public List<BlackNumberInfo> find(int index){
        SQLiteDatabase db = openHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select phone,mode from blacknumber " +
                "order by _id desc limit ?,20",new String[]{index+""});
        List<BlackNumberInfo> list = new ArrayList<BlackNumberInfo>();
        while (cursor.moveToNext()){
            BlackNumberInfo info = new BlackNumberInfo();
            info.phone = cursor.getString(0);
            info.mode = cursor.getString(1);
            list.add(info);
        }
        cursor.close();
        db.close();
        return list;
    }

    //获取数据库中数据的条目
    public int getCount(){
        SQLiteDatabase db = openHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from blacknumber",null);
        int count = 0;
        if (cursor.moveToNext()){
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public int getMode(String phone){
        SQLiteDatabase db = openHelper.getWritableDatabase();
        Cursor cursor = db.query("blacknumber",new String[]{"mode"},
                "phone = ?",new String[]{phone},null,null,null);
        int mode = -1;
        if (cursor.moveToNext()){
            mode = Integer.parseInt(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return mode;
    }
}

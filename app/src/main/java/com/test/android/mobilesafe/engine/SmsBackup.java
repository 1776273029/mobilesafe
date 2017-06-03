package com.test.android.mobilesafe.engine;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2017/6/1.
 */

public class SmsBackup {

    public static void backup(Context context, String path, CallBack callBack){

        int index = 0;
        FileOutputStream fos = null;
        Cursor cursor = null;
        //上下文环境，备份文件夹的路径，进度条所在的对话框对象（更新进度）
        try {
            //创建文件
            File file = new File(path);
            cursor = context.getContentResolver().query(Uri.parse("content://sms/"),
                    new String[]{"address","date","type","body"},null,null,null);
            //设置备份短信的总数
            if (callBack != null){
                callBack.setMaX(cursor.getCount());
            }
            //文件相应的输出流
            fos = new FileOutputStream(file);
            //序列化数据库中读取的数据，放置到xml中
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos,"utf-8");
            serializer.startDocument("utf-8",true);//是否独立存在
            serializer.startTag(null,"smss");
            //读取数据库中每一行的数据写入到xml文件中
            while (cursor.moveToNext()){

                serializer.startTag(null,"sms");

                serializer.startTag(null,"address");
                serializer.text(cursor.getString(0));
                serializer.endTag(null,"address");

                serializer.startTag(null,"date");
                serializer.text(cursor.getString(1));
                serializer.endTag(null,"date");

                serializer.startTag(null,"type");
                serializer.text(cursor.getString(2));
                serializer.endTag(null,"type");

                serializer.startTag(null,"body");
                serializer.text(cursor.getString(3));
                serializer.endTag(null,"body");

                serializer.endTag(null,"sms");

                index ++;
                Thread.sleep(500);
                if (callBack != null){
                    //ProgressDialog可以在子线程中更新进度
                    callBack.setProgress(index);
                }
            }
            serializer.endTag(null,"smss");
            serializer.endDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (cursor != null){
                cursor.close();
            }
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //回调
    // 1.定义一个接口
    // 2.定义接口中未实现的业务逻辑方法
    // 3.传递一个实现了此接口的类的对象
    // 4.获取传递进来的对象，在合适的地方做方法的调用
    public interface CallBack{
        public void setMaX(int max);
        public void setProgress(int index);
    }
}


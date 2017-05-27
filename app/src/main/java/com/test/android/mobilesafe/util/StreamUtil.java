package com.test.android.mobilesafe.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2017/5/23.
 */

public class StreamUtil {

    public static String stream2String(InputStream in){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int temp = -1;
        try {
            while ((temp = in.read(buffer)) != -1){
                bos.write(buffer,0,temp);
            }
            return bos.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                in.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

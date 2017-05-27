package com.test.android.mobilesafe.util;

import java.security.MessageDigest;

/**
 * Created by Administrator on 2017/5/24.
 */

public class MD5Util {
    public static String encoder(String psd) {
        try {
            //加盐处理
            psd = psd + "mobilesafe";
            //指定加密算法类型
            MessageDigest digest = MessageDigest.getInstance("MD5");
            //将需要加密的字符串转换成byte数组，然后进行随机哈希过程
            byte[] bs = digest.digest(psd.getBytes());
            //循环遍历bs，然后让其生成32位字符串，固定写法
            StringBuffer buffer = new StringBuffer();
            for (byte b : bs) {
                int i = b & 0xff;
                //int类型的i需要转换成16进制的字符
                String hexString = Integer.toHexString(i);
                if (hexString.length() < 2) {
                    //补0
                    hexString = "0" + hexString;
                }
                //拼接
                buffer.append(hexString);
            }
            return buffer.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

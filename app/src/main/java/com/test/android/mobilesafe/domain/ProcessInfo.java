package com.test.android.mobilesafe.domain;

import android.graphics.drawable.Drawable;

/**
 * Created by Administrator on 2017/6/2.
 */

public class ProcessInfo {

    public Drawable icon;
    public String name;
    public String packageName;//如果进程没有名称，则将其所在应用的包名作为名称
    public long memSize;
    public boolean isCheck;
    public boolean isSystem;
}

package com.test.android.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.util.ConstantValue;

/**
 * Created by Administrator on 2017/5/24.
 */

public class SettingClickView extends RelativeLayout {

    private TextView tv_des;
    private TextView tv_title;

    public SettingClickView(Context context) {
        this(context,null);
    }

    public SettingClickView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    //只调用这个方法
    public SettingClickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //直接添加到了SettingItemView对应的View中
        View view = View.inflate(context, R.layout.settings_click_item,this);
        tv_title = (TextView) findViewById(R.id.tv_title_c);
        tv_des = (TextView) findViewById(R.id.tv_des_c);
        //获取自定义以及原生属性的操作
    }

    public void setTitle(String title) {
        tv_title.setText(title);
    }

    public void setDes(String des) {
        tv_des.setText(des);
    }

}

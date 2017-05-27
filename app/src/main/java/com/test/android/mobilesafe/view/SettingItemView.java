package com.test.android.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.util.ConstantValue;

/**
 * Created by Administrator on 2017/5/24.
 */

public class SettingItemView extends RelativeLayout {

    private CheckBox cb_box;
    private TextView tv_des;
    private TextView tv_title;
    private String destitle;
    private String deson;
    private String desoff;

    public SettingItemView(Context context) {
        this(context,null);
    }

    public SettingItemView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    //只调用这个方法
    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //直接添加到了SettingItemView对应的View中
        View view = View.inflate(context, R.layout.settings_item,this);
//        View view = View.inflate(context, R.layout.settings_item,null);
//        this.addView(view);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_des = (TextView) findViewById(R.id.tv_des);
        cb_box = (CheckBox) findViewById(R.id.cb_box);

        //获取自定义以及原生属性的操作
        initAttrs(attrs);
    }

    private void initAttrs(AttributeSet attrs) {
//        attrs.getAttributeCount();//属性的个数
        destitle = attrs.getAttributeValue(ConstantValue.MINE,"destitle");
        deson = attrs.getAttributeValue(ConstantValue.MINE,"deson");
        desoff = attrs.getAttributeValue(ConstantValue.MINE,"desoff");
        tv_title.setText(destitle);
        if (isCheck()){
            tv_des.setText(deson);
        }else {
            tv_des.setText(desoff);
        }

    }

    public boolean isCheck(){
        return cb_box.isChecked();
    }

    public void setCheck(boolean isCheck){
        cb_box.setChecked(isCheck);
        if (isCheck){
            tv_des.setText(deson);
        }else{
            tv_des.setText(desoff);
        }
    }

}

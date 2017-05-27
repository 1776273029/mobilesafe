package com.test.android.mobilesafe.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.SpUtil;
import com.test.android.mobilesafe.view.SettingItemView;

public class Setup2Activity extends AppCompatActivity {

    private SettingItemView siv_bound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup2);
        initUI();
    }

    private void initUI() {
        siv_bound = (SettingItemView) findViewById(R.id.siv_sim_bound);
        String simNum = SpUtil.getString(this, ConstantValue.SIM_NUMBER,"");
        if (TextUtils.isEmpty(simNum)){
            siv_bound.setCheck(false);
        }else {
            siv_bound.setCheck(true);
        }
        siv_bound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取原有状态
                boolean isCheck = siv_bound.isCheck();
                //将原有状态取反，设置给当前条目
                siv_bound.setCheck(!isCheck);
                if (!isCheck){
                    //绑定SIM卡
                    //获取SIM卡序列号 TelephonyManager
                    TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String simSerialNumber = manager.getSimSerialNumber();//获取序列号
                    SpUtil.putString(Setup2Activity.this,ConstantValue.SIM_NUMBER,simSerialNumber);
                }else {
                    SpUtil.romove(Setup2Activity.this,ConstantValue.SIM_NUMBER);
                }
            }
        });

    }

    public void nextPage(View view){
        String sim_num = SpUtil.getString(this,ConstantValue.SIM_NUMBER,"");
        if (!TextUtils.isEmpty(sim_num)){
            Intent intent = new Intent(this,Setup3Activity.class);
            startActivity(intent);
            finish();
            //开启平移动画
            overridePendingTransition(R.anim.next_in_anim,R.anim.next_out_anim);
        }else {
            Toast.makeText(this,"请绑定SIM卡",Toast.LENGTH_SHORT).show();
        }
    }

    public void prePage(View view){
        Intent intent = new Intent(this,Setup1Activity.class);
        startActivity(intent);
        finish();
        //开启平移动画
        overridePendingTransition(R.anim.pre_in_anim,R.anim.pre_out_anim);
    }
}

package com.test.android.mobilesafe.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.SpUtil;

public class Setup4Activity extends AppCompatActivity {

    private CheckBox cb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup4);
        initUI();
    }

    private void initUI() {
        cb = (CheckBox) findViewById(R.id.cb_open);
        boolean open_security = SpUtil.getBoolean(this,ConstantValue.OPEN_SECURITY,false);
        if (open_security){
            cb.setText("安全设置已开启");
        }else {
            cb.setText("安全设置已关闭");
        }
        cb.setChecked(open_security);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtil.putBoolean(Setup4Activity.this,ConstantValue.OPEN_SECURITY,
                        isChecked);
                if (isChecked){
                    cb.setText("安全设置已开启");
                }else {
                    cb.setText("安全设置已关闭");
                }
            }
        });
    }

    public void nextPage(View view){
        boolean isOpen = SpUtil.getBoolean(this,ConstantValue.OPEN_SECURITY,false);
        if (isOpen){
            Intent intent = new Intent(this,SetupOverActivity.class);
            startActivity(intent);
            SpUtil.putBoolean(this, ConstantValue.SETUP_OVER,true);
            finish();
            //开启平移动画
            overridePendingTransition(R.anim.next_in_anim,R.anim.next_out_anim);
        }else {
            Toast.makeText(this,"安全设置未开启",Toast.LENGTH_SHORT).show();
        }

    }

    public void prePage(View view){
        Intent intent = new Intent(this,Setup3Activity.class);
        startActivity(intent);
        finish();
        //开启平移动画
        overridePendingTransition(R.anim.pre_in_anim,R.anim.pre_out_anim);
    }

}

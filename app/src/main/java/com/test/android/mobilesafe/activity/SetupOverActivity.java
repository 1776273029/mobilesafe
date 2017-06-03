package com.test.android.mobilesafe.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.SpUtil;

public class SetupOverActivity extends AppCompatActivity {

    private TextView tv_safe_num;
    private ImageView iv_isLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_over);
        boolean setup_over = SpUtil.getBoolean(this, ConstantValue.SETUP_OVER,false);
        if (setup_over){

        }else {
            Intent intent = new Intent(this,Setup1Activity.class);
            startActivity(intent);
            finish();
        }
        initUI();
    }

    private void initUI() {
        tv_safe_num = (TextView) findViewById(R.id.tv_safe_num);
        iv_isLock = (ImageView) findViewById(R.id.iv_islock);
        String phone = SpUtil.getString(this,ConstantValue.CONTACT_PHONE,"");
        tv_safe_num.setText(phone);
        boolean isLock = SpUtil.getBoolean(this,ConstantValue.OPEN_SECURITY,false);
        TextView tv_reset = (TextView) findViewById(R.id.tv_reset_setup);
        //让TextView具备可点击的操作
        tv_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupOverActivity.this,Setup1Activity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}

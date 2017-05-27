package com.test.android.mobilesafe.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.SpUtil;

public class Setup3Activity extends AppCompatActivity {

    private EditText et_phone_number;
    private Button bt_select_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup3);
        initUI();
    }

    private void initUI() {
        et_phone_number = (EditText) findViewById(R.id.et_phone_number);
        String phone = SpUtil.getString(this,ConstantValue.CONTACT_PHONE,"");
        et_phone_number.setText(phone);
        bt_select_number = (Button) findViewById(R.id.bt_select_number);
        bt_select_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Setup3Activity.this,ContactListActivity.class);
                startActivityForResult(intent,0);
            }
        });
    }

    public void nextPage(View view){
        String phone = et_phone_number.getText().toString();
        if (!TextUtils.isEmpty(phone)){
            Intent intent = new Intent(this,Setup4Activity.class);
            startActivity(intent);
            SpUtil.putString(this,ConstantValue.CONTACT_PHONE,phone);
            finish();
            //开启平移动画
            overridePendingTransition(R.anim.next_in_anim,R.anim.next_out_anim);
        }else {
            Toast.makeText(this,"请输入安全号码",Toast.LENGTH_SHORT).show();
        }
    }

    public void prePage(View view){
        Intent intent = new Intent(this,Setup2Activity.class);
        startActivity(intent);
        finish();
        //开启平移动画
        overridePendingTransition(R.anim.pre_in_anim,R.anim.pre_out_anim);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null){
            String phone = data.getStringExtra("phone");
            //特殊字符过滤
            phone = phone.replace("-","").replace(" ","").trim();
            et_phone_number.setText(phone);
            SpUtil.putString(getApplicationContext(), ConstantValue.CONTACT_PHONE,phone);
        }
    }
}

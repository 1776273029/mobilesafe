package com.test.android.mobilesafe.activity;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.engine.AddressDao;

public class QueryAddressActivity extends AppCompatActivity {

    private EditText et_phone;
    private Button bt_query;
    private TextView tv_address;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_address);
        //测试代码，查询引擎是否成功
//        AddressDao.getAddress("");
//        Log.d("address", "");
        initUI();
    }

    private void initUI() {
        et_phone = (EditText) findViewById(R.id.et_query_phone);
        bt_query = (Button) findViewById(R.id.bt_query_address);
        tv_address = (TextView) findViewById(R.id.tv_address);
        bt_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = et_phone.getText().toString();
                if (!TextUtils.isEmpty(phone)){
                    queryAddress(phone);
                }else {
                    //控件抖动
                    Animation shake = AnimationUtils.loadAnimation(
                            QueryAddressActivity.this,R.anim.shake);
                    //自定义插补器（数学函数）
//                    shake.setInterpolator(new Interpolator() {
//                        @Override//y = ax + b;
//                        public float getInterpolation(float input) {
//                            return 0;
//                        }
//                    });
                    et_phone.startAnimation(shake);
                    //手机震动（需要添加权限）
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(2000);//震动2秒
                    //不震动时间，震动时间...
                    long[] pattern = {500,500,200,800,};
                    //震动规则,重复次数（传入-1表示不重复）
                    vibrator.vibrate(pattern,2);
                }
            }
        });
        //实时查询（监听输入框中文本的变化）
        et_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String phone = et_phone.getText().toString();
                queryAddress(phone);
            }
        });
    }

    private void queryAddress(final String phone) {
        new Thread(){
            public void run(){
                address = AddressDao.getAddress(phone);
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tv_address.setText(address);
        }
    };

}

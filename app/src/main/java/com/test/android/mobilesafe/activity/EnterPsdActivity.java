package com.test.android.mobilesafe.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.test.android.mobilesafe.R;

public class EnterPsdActivity extends AppCompatActivity {

    private String packageName;
    private TextView tv_name;
    private ImageView iv_icon;
    private EditText et_psd;
    private Button bt_submit;
    private ApplicationInfo applicationInfo;
    private Drawable icon;
    private String label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_psd);
        packageName = getIntent().getStringExtra("packagename");
        initUI();
        initData();
    }

    private void initData() {
        PackageManager mPM = getPackageManager();
        try {
            applicationInfo = mPM.getApplicationInfo(packageName,0);
            icon = applicationInfo.loadIcon(mPM);
            label = applicationInfo.loadLabel(mPM).toString();
            iv_icon.setBackgroundDrawable(icon);
            tv_name.setText(label);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String psd = et_psd.getText().toString();
                if (!TextUtils.isEmpty(psd)){
                    if (psd.equals("123")){
                        //解锁，进入应用，告知看门狗不再监听已解锁的应用
                        Intent intent = new Intent("mobilesafe.intent.action.SKIP");
                        intent.putExtra("packagename",packageName);
                        sendBroadcast(intent);
                        finish();
                    }else {
                        Toast.makeText(getApplicationContext(),"密码错误",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"请输入密码",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initUI() {
        tv_name = (TextView) findViewById(R.id.tv_appName);
        iv_icon = (ImageView) findViewById(R.id.iv_appIcon);
        et_psd = (EditText) findViewById(R.id.et_lock_psd);
        bt_submit = (Button) findViewById(R.id.bt_submit_psd);
    }

    @Override
    public void onBackPressed() {
        //重写返回键，通过隐式意图跳转桌面
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        super.onBackPressed();
    }

}

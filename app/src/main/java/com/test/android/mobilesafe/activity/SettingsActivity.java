package com.test.android.mobilesafe.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.SpUtil;
import com.test.android.mobilesafe.view.SettingItemView;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initUpdate();
    }

    private void initUpdate() {
        final SettingItemView siv_update = (SettingItemView) findViewById(R.id.siv_update);
        boolean open_update = SpUtil.getBoolean(this, ConstantValue.OPEN_UPDATE,false);
        siv_update.setCheck(open_update);
        siv_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = siv_update.isCheck();
                siv_update.setCheck(!isCheck);
                SpUtil.putBoolean(getApplicationContext(),ConstantValue.OPEN_UPDATE,!isCheck);
            }
        });
    }
}

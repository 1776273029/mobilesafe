package com.test.android.mobilesafe.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TabHost;

import com.test.android.mobilesafe.R;

public class BaseCacheClearActivity extends TabActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_cache_clear);
        //生成选项卡
        TabHost.TabSpec tab1 = getTabHost().newTabSpec("clear_cache").setIndicator("缓存清理");
        TabHost.TabSpec tab2 = getTabHost().newTabSpec("sd_cache_clear").setIndicator("SD卡清理");
        //告知点中选项卡后续操作
        tab1.setContent(new Intent(this,CacheClearActivity.class));
        tab2.setContent(new Intent(this,SDCacheClearActivity.class));
        //将选项卡维护到host（选项卡宿主）中
        getTabHost().addTab(tab1);
        getTabHost().addTab(tab2);
        
    }
}

package com.test.android.mobilesafe.rocketman;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.test.android.mobilesafe.R;

public class RocketManActivity extends AppCompatActivity {

    private Button bt_start;
    private Button bt_stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rocket_man);
        bt_start = (Button) findViewById(R.id.bt_start_rocket);
        bt_stop = (Button) findViewById(R.id.bt_stop_rocket);
        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(getApplicationContext(),RocketService.class));
                finish();
            }
        });
        bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(getApplicationContext(),RocketService.class));
                finish();
            }
        });
    }
}

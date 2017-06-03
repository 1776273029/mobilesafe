package com.test.android.mobilesafe.rocketman;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.test.android.mobilesafe.R;

public class BackgroundActivity extends Activity {

    private AlphaAnimation alphaAnimation;
    private ImageView iv_smoke_t;
    private ImageView iv_smoke_m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background);
        iv_smoke_t = (ImageView) findViewById(R.id.iv_smoke_t);
        iv_smoke_m = (ImageView) findViewById(R.id.iv_smoke_m);
        alphaAnimation = new AlphaAnimation(0,1);
        alphaAnimation.setDuration(500);
        iv_smoke_m.setImageAlpha(0);
        iv_smoke_m.setVisibility(View.VISIBLE);
        iv_smoke_m.startAnimation(alphaAnimation);
        mHandler.sendEmptyMessageDelayed(1,300);
        mHandler.sendEmptyMessageDelayed(0,1200);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    finish();
                    break;
                case 1:
                    iv_smoke_t.setImageAlpha(0);
                    iv_smoke_t.setVisibility(View.VISIBLE);
                    iv_smoke_t.startAnimation(alphaAnimation);
                    break;
            }
        }
    };
}

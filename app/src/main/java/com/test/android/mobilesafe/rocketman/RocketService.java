package com.test.android.mobilesafe.rocketman;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.SpUtil;

public class RocketService extends Service {

    private WindowManager mWM;
    private int mScreenHeight;
    private int mScreenWidth;
    private View mRocketView;
    private ImageView iv_rocket;
    private WindowManager.LayoutParams params;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mWM = (WindowManager) getSystemService(WINDOW_SERVICE);
        mScreenHeight = mWM.getDefaultDisplay().getHeight();
        mScreenWidth = mWM.getDefaultDisplay().getWidth();
        showRocket();
        super.onCreate();
    }

    private void showRocket() {
        params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.setTitle("Toast");
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.LEFT + Gravity.TOP;
        params.format = PixelFormat.TRANSLUCENT;
        mRocketView = View.inflate(this, R.layout.rocket_view,null);
        iv_rocket = (ImageView) mRocketView.findViewById(R.id.iv_rocket);
        AnimationDrawable animationDrawable = (AnimationDrawable) iv_rocket.getBackground();
        animationDrawable.start();
        mRocketView.setOnTouchListener(new View.OnTouchListener() {
            private int startX;
            private int startY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int moveX = (int) event.getRawX();
                        int moveY = (int) event.getRawY();
                        int disX = moveX - startX;
                        int disY = moveY - startY;
                        params.x += disX;
                        params.y += disY;
                        //容错处理
                        if (params.x < 0){
                            params.x = 0;
                        }
                        if (params.y < 0){
                            params.y = 0;
                        }
                        if (params.x > mScreenWidth - mRocketView.getWidth()){
                            params.x = mScreenWidth - mRocketView.getWidth();
                        }
                        if (params.y > mScreenHeight - mRocketView.getHeight() - 35){
                            params.y = mScreenHeight - mRocketView.getHeight() - 35;
                        }
                        //告知窗体，需按照手势的移动更新位置
                        mWM.updateViewLayout(mRocketView, params);
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (params.x+mRocketView.getWidth()/2 > mScreenWidth/2 -50
                                && params.x+mRocketView.getWidth()/2 < mScreenWidth/2+ 50
                                && params.y+mRocketView.getHeight() > mScreenHeight - 100){
                            sendRocket();
                            //开启产生尾气的activity
                            Intent intent = new Intent(getApplicationContext(),BackgroundActivity.class);
                            //在服务中（没有活动界面）开启一个activity，需另开一个任务栈
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        SpUtil.putInt(getApplicationContext(), ConstantValue.ROCKET_X, params.x);
                        SpUtil.putInt(getApplicationContext(),ConstantValue.ROCKET_Y, params.y);
                        break;
                }
                return true;
            }
        });
        params.x = SpUtil.getInt(this,ConstantValue.ROCKET_X,0);
        params.y = SpUtil.getInt(this,ConstantValue.ROCKET_Y,0);
        mWM.addView(mRocketView, params);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mWM.updateViewLayout(mRocketView,params);
            SpUtil.putInt(getApplicationContext(), ConstantValue.ROCKET_X, params.x);
            SpUtil.putInt(getApplicationContext(),ConstantValue.ROCKET_Y, params.y);
        }
    };

    private void sendRocket() {
        //主线程不能睡眠，以免造成阻塞
        new Thread(){
            public void run(){
                int i = 0;
                while (params.y > 2*i*i){
                    params.y = params.y - 2*i*i;
                    params.x = mScreenWidth/2-mRocketView.getWidth()/2;
                    i += 1;
                    mHandler.sendEmptyMessage(0);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                params.y = 0;
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWM != null && mRocketView != null){
            mWM.removeView(mRocketView);
        }
    }
}

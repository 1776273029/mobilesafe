package com.test.android.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.engine.AddressDao;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.SpUtil;

public class AddressService extends Service {

    private TelephonyManager mTM;
    private MyPhoneStateListener listener;
    private View mViewToast;
    private WindowManager mWM;
    private String mAddress;
    private TextView tv_toast_text;
    private int[] mDrawableId;
    private int mTotastStyle;
    private int mScreenHeight;
    private int mScreenWidth;
    private InnerOutCallReceiver mOutCallReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mTM = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        listener = new MyPhoneStateListener();
        mTM.listen(listener,PhoneStateListener.LISTEN_CALL_STATE);
        //获取窗体对象
        mWM = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mScreenHeight = mWM.getDefaultDisplay().getHeight();
        mScreenWidth = mWM.getDefaultDisplay().getWidth();
        //监听拨出电话的广播过滤条件
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        mOutCallReceiver = new InnerOutCallReceiver();
        registerReceiver(mOutCallReceiver,filter);//注册服务
        super.onCreate();
    }

    class InnerOutCallReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String phone = getResultData();//获取拨出电话号码的字符串
            showToast(phone);
        }
    }

    @Override
    public void onDestroy() {
        if (mTM != null && listener != null){
            //取消对电话状态的监听（开启服务的时候监听电话的对象）
            mTM.listen(listener,PhoneStateListener.LISTEN_NONE);
        }
        if (mOutCallReceiver != null){
            unregisterReceiver(mOutCallReceiver);
        }
        super.onDestroy();
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        @Override//电话状态发生改变时调用,incomingNumber:来电号码
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state){
                case TelephonyManager.CALL_STATE_IDLE://空闲状态，没有任何活动
                    //隐藏Toast
                    if (mWM != null && mViewToast != null){
                        mWM.removeView(mViewToast);
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK://摘机（拨打或通话）
                    break;
                case TelephonyManager.CALL_STATE_RINGING://响铃
                    //展示Toast
                    showToast(incomingNumber);
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();

    public void showToast(String value){
        //自定义Toast
        final WindowManager.LayoutParams params = mParams;
        //指定大小
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        //指定格式
        params.format = PixelFormat.TRANSLUCENT;
        params.type = WindowManager.LayoutParams.TYPE_TOAST;//级别（与电话类型一致，7.0后不可用）
        params.setTitle("Toast");//标题
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//不能获取焦点
//                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;// 不能触摸，默认能够被触摸
        //指定所在位置
        params.gravity = Gravity.LEFT + Gravity.TOP;
        //指定显示效果（自定义布局文件）
        mViewToast = View.inflate(this, R.layout.toast_view,null);
        mViewToast.setOnTouchListener(new View.OnTouchListener() {
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
                        if (params.x > mScreenWidth - mViewToast.getWidth()){
                            params.x = mScreenWidth - mViewToast.getWidth();
                        }
                        if (params.y > mScreenHeight - mViewToast.getHeight() - 35){
                            params.y = mScreenHeight - mViewToast.getHeight() - 35;
                        }
                        //告知窗体，需按照手势的移动更新位置
                        mWM.updateViewLayout(mViewToast,params);
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        SpUtil.putInt(getApplicationContext(),ConstantValue.LOCATION_X,params.x);
                        SpUtil.putInt(getApplicationContext(),ConstantValue.LOCATION_Y,params.y);
                        break;
                }
                return true;
            }
        });
        tv_toast_text = (TextView) mViewToast.findViewById(R.id.tv_toast_text);
        //读取sp中存储位置的x，y坐标值，params.x，params.y 左上角坐标
        params.x = SpUtil.getInt(this,ConstantValue.LOCATION_X,0);
        params.y = SpUtil.getInt(this,ConstantValue.LOCATION_Y,0);
        //从sp中获取色值文字的索引，匹配图片，用作展示
        mTotastStyle = SpUtil.getInt(this, ConstantValue.TOAST_STYLE,0);
        mDrawableId = new int[]{R.drawable.call_locate_white,
                R.drawable.call_locate_orange, R.drawable.call_locate_blue,
                R.drawable.call_locate_gray,R.drawable.call_locate_green};
        tv_toast_text.setBackgroundResource(mDrawableId[mTotastStyle]);

        // 将自定义View挂载到WindowManager窗体上（需要权限）
        mWM.addView(mViewToast,params);
        query(value);//查询归属地
    }

    private void query(final String value){
        new Thread(){
            public void run(){
                mAddress = AddressDao.getAddress(value);
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tv_toast_text.setText(mAddress);
        }
    };
}

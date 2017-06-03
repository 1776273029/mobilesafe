package com.test.android.mobilesafe.activity;

import android.app.Activity;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.SpUtil;

import static android.R.attr.left;

public class ToastLocationActivity extends Activity {

    private ImageView iv_darg;
    private Button bt_top;
    private Button bt_bottom;
    private WindowManager mWM;
    private int mScreenWidth;
    private int mScreenHeight;
    private long[] mHits = new long[2];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast_location);
        initUI();
    }

    private void initUI() {
        iv_darg = (ImageView) findViewById(R.id.iv_drag);
        bt_top = (Button) findViewById(R.id.bt_top);
        bt_bottom = (Button) findViewById(R.id.bt_bottom);
        //获取屏幕宽高
        mWM = (WindowManager) getSystemService(WINDOW_SERVICE);
        mScreenHeight = mWM.getDefaultDisplay().getHeight();
        mScreenWidth = mWM.getDefaultDisplay().getWidth();

        //获取上一次的坐标（左上角）
        int locationX = SpUtil.getInt(this,ConstantValue.LOCATION_X,0);
        int locationY = SpUtil.getInt(this,ConstantValue.LOCATION_Y,0);
        if (locationY > mScreenHeight / 2){
            bt_top.setVisibility(View.VISIBLE);
            bt_bottom.setVisibility(View.INVISIBLE);
        }else {
            bt_top.setVisibility(View.INVISIBLE);
            bt_bottom.setVisibility(View.VISIBLE);
        }
        //ImageView在相对布局中，所以其所在位置的规则需要由相对布局提供
        //指定宽高
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        //指定位置（将左上角的坐标作用在iv_drag对应规则参数上）
        layoutParams.leftMargin = locationX;
        layoutParams.topMargin = locationY;
        //将以上规则作用在iv_drag上
        iv_darg.setLayoutParams(layoutParams);
        //监听多击事件
        iv_darg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.arraycopy(mHits,1,mHits,0,mHits.length-1);
                mHits[mHits.length-1] = SystemClock.uptimeMillis();
                if (mHits[mHits.length-1] - mHits[0] < 500){
                    int left = mScreenWidth/2 - iv_darg.getWidth()/2;
                    int top = mScreenHeight/2 - iv_darg.getHeight()/2;
                    int right = mScreenWidth/2 + iv_darg.getWidth()/2;
                    int bottom = mScreenHeight/2 + iv_darg.getHeight()/2;
                    iv_darg.layout(left,top,right,bottom);
                    //存储结果
                    SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_X,iv_darg.getLeft());
                    SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_Y,iv_darg.getTop());
                }
            }
        });
        //监听某一个控件的触摸事件
        iv_darg.setOnTouchListener(new View.OnTouchListener() {
            private int startX;
            private int startY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        //float startX = event.getX();//相对控件本身的位置
                        startX = (int) event.getRawX();//相对原点的位置
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int moveX = (int) event.getRawX();
                        int moveY = (int) event.getRawY();
                        int disX = moveX - startX;
                        int disY = moveY - startY;
                        //当前控件所在屏幕的（左，上）角的位置（Activity可显示区域）
                        //iv_darg.getLeft();当前控件左边缘与屏幕（左，上）边缘的间距
                        int left = iv_darg.getLeft() + disX;//左侧坐标
                        int top = iv_darg.getTop() + disY;//顶端坐标
                        int right = iv_darg.getRight() + disX;//右侧坐标
                        int bottom = iv_darg.getBottom() + disY;//底部坐标
                        //容错处理
                        if (left < 0){
                            return false;
                        }
                        if (right > mScreenWidth){
                            return false;
                        }
                        if (top < 0){
                            return false;
                        }
                        if (bottom > mScreenHeight - 35){//预留通知栏高度
                            return false;
                        }
                        if (top > mScreenHeight / 2){
                            bt_top.setVisibility(View.VISIBLE);
                            bt_bottom.setVisibility(View.INVISIBLE);
                        }else if (top < mScreenHeight / 2){
                            bt_top.setVisibility(View.INVISIBLE);
                            bt_bottom.setVisibility(View.VISIBLE);
                        }
                        //告知被移动的控件，按计算出来的坐标去做展示
                        iv_darg.layout(left,top,right,bottom);
                        //重置起点坐标
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        //存储移动后的位置
                        SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_X,iv_darg.getLeft());
                        SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_Y,iv_darg.getTop());
                        break;
                }
                //在当前情况下返回false不响应事件，返回true才会响应事件
                //return true;
                //既要响应点击事件，又要响应拖拽过程，则此返回值结果需修改为false
                return false;
            }
        });
    }
}

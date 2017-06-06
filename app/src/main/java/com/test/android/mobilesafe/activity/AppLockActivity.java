package com.test.android.mobilesafe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.db.dao.AppLockDao;
import com.test.android.mobilesafe.domain.AppInfo;
import com.test.android.mobilesafe.engine.AppInfoProvider;

import java.util.ArrayList;
import java.util.List;

public class AppLockActivity extends AppCompatActivity {

    private LinearLayout ll_unlock;
    private LinearLayout ll_lock;
    private Button bt_lock;
    private Button bt_unlock;
    private ListView lv_unlock;
    private ListView lv_lock;
    private List<AppInfo> appList;
    private List<AppInfo> mLockList;
    private List<AppInfo> mUnLockList;
    private MyAdapter mUnLockAdapter;
    private MyAdapter mLockAdapter;
    private TextView tv_lock;
    private TextView tv_unlock;
    private TranslateAnimation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);
        initUI();
        initData();
        initAnimation();
    }

    private void initAnimation() {
        animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,1,
                Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0);
        animation.setDuration(300);
    }

    private void initData() {
        new Thread(){
            public void run(){
                appList = AppInfoProvider.getAppInfoList(getApplicationContext());
                mLockList = new ArrayList<AppInfo>();
                mUnLockList = new ArrayList<AppInfo>();
                List<String> lockList = AppLockDao.getInstance(getApplicationContext()).findAll();
                for (AppInfo app : appList) {
                    if (lockList.contains(app.packageName)){
                        mLockList.add(app);
                    }else {
                        mUnLockList.add(app);
                    }
                }
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void initUI() {
        ll_unlock = (LinearLayout) findViewById(R.id.ll_unlock);
        ll_lock = (LinearLayout) findViewById(R.id.ll_lock);
        bt_unlock = (Button) findViewById(R.id.bt_unlock);
        bt_lock = (Button) findViewById(R.id.bt_lock);
        lv_unlock = (ListView) findViewById(R.id.lv_unlock);
        lv_lock = (ListView) findViewById(R.id.lv_lock);
        tv_lock = (TextView) findViewById(R.id.tv_lock);
        tv_unlock = (TextView) findViewById(R.id.tv_unlock);
        bt_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_unlock.setVisibility(View.GONE);
                ll_lock.setVisibility(View.VISIBLE);
                bt_lock.setBackgroundResource(R.drawable.tab_right_pressed);
                bt_unlock.setBackgroundResource(R.drawable.tab_left_default);
            }
        });
        bt_unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_lock.setVisibility(View.GONE);
                ll_unlock.setVisibility(View.VISIBLE);
                bt_unlock.setBackgroundResource(R.drawable.tab_left_pressed);
                bt_lock.setBackgroundResource(R.drawable.tab_right_default);
            }
        });
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
                mUnLockAdapter = new MyAdapter(false);
                lv_unlock.setAdapter(mUnLockAdapter);
                mLockAdapter = new MyAdapter(true);
                lv_lock.setAdapter(mLockAdapter);
        }
    };

    private class MyAdapter extends BaseAdapter{

        private boolean isLock = false;
        private MyAdapter(boolean isLock){
            this.isLock = isLock;
        }
        @Override
        public int getCount() {
            if (isLock){
                tv_lock.setText("已加锁应用：" + mLockList.size());
                return mLockList.size();
            }else {
                tv_unlock.setText("未加锁应用：" + mUnLockList.size());
                return mUnLockList.size();
            }
        }

        @Override
        public AppInfo getItem(int position) {
            if (isLock){
                return mLockList.get(position);
            }else {
                return mUnLockList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null){
                convertView = View.inflate(getApplicationContext(),
                        R.layout.listview_lock_item,null);
                holder = new ViewHolder();
                holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_app_icon);
                holder.tv_name = (TextView) convertView.findViewById(R.id.tv_app_name);
                holder.iv_isLock = (ImageView) convertView.findViewById(R.id.iv_isLock);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.iv_icon.setBackgroundDrawable(getItem(position).icon);
            holder.tv_name.setText(getItem(position).packageName);
            if (isLock){
                holder.iv_isLock.setBackgroundResource(R.drawable.lock);
            }else {
                holder.iv_isLock.setBackgroundResource(R.drawable.unlock);
            }
            final View finalConvertView = convertView;
            holder.iv_isLock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //对动画执行的过程做事件监听，监听到动画执行完成后，再移除数据，刷新界面
                    finalConvertView.startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if (isLock){
                                AppLockDao.getInstance(getApplicationContext()).delete(getItem(position).packageName);
                                mUnLockList.add(0,getItem(position));
                                mLockList.remove(getItem(position));
                                mLockAdapter.notifyDataSetChanged();
                                mUnLockAdapter.notifyDataSetChanged();
                            }else {
                                mLockList.add(0,getItem(position));
                                AppLockDao.getInstance(getApplicationContext()).insert(getItem(position).packageName);
                                mUnLockList.remove(getItem(position));
                                mUnLockAdapter.notifyDataSetChanged();
                                mLockAdapter.notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                }
            });
            return convertView;
        }
    }

    static class ViewHolder{
        ImageView iv_icon;
        TextView tv_name;
        ImageView iv_isLock;
    }

}

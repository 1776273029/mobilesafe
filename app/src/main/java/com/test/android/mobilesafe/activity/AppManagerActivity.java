package com.test.android.mobilesafe.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.domain.AppInfo;
import com.test.android.mobilesafe.engine.AppInfoProvider;

import java.util.ArrayList;
import java.util.List;

public class AppManagerActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView lv_app_list;
    private List<AppInfo> appInfoList;
    private MyAdapter adapter;
    private List<AppInfo> systemList;
    private List<AppInfo> customerList;
    private TextView tv_des;
    private AppInfo mApp;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);
        initTitle();
        initList();
    }

    @Override
    protected void onResume() {
        //获取数据
        getData();
        super.onResume();
    }

    private void getData() {
        new Thread(){
            public void run(){
                appInfoList = AppInfoProvider.getAppInfoList(getApplicationContext());
                systemList = new ArrayList<AppInfo>();
                customerList = new ArrayList<AppInfo>();
                for (AppInfo appInfo : appInfoList) {
                    if (appInfo.isSystem){
                        systemList.add(appInfo);
                    }else {
                        customerList.add(appInfo);
                    }
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void initList() {
        lv_app_list = (ListView) findViewById(R.id.lv_app_list);
        tv_des = (TextView) findViewById(R.id.tv_app_des);
        lv_app_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                //AbsListView中view就是ListView对象
                //firstVisibleItem第一个可见条目索引值
                //visibleItemCount当前一个屏幕的可见条目数
                //totalItemCount总共条目总数
                if (customerList != null && systemList != null){
                    if (firstVisibleItem >= customerList.size() + 1){
                        //滚动到了系统条目
                        tv_des.setText("系统应用：" + systemList.size());
                    }else {
                        tv_des.setText("用户应用：" + customerList.size());
                    }
                }
            }
        });
        lv_app_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override//view 点中条目指向的View对象
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 || position == customerList.size() + 1){
                    return;
                }else {
                    if (position < customerList.size() + 1){
                        mApp = customerList.get(position - 1);
                    }else {
                        mApp = systemList.get(position - customerList.size() - 2);
                    }
                    showPopupWindow(view);
                }
            }
        });
    }

    private void showPopupWindow(View anchor) {
        View popupView = View.inflate(this,R.layout.popupwindow_layout,null);
        TextView tv_uninstall = (TextView) popupView.findViewById(R.id.tv_uninstall);
        TextView tv_start = (TextView) popupView.findViewById(R.id.tv_start);
        TextView tv_share = (TextView) popupView.findViewById(R.id.tv_share);
        tv_uninstall.setOnClickListener(this);
        tv_start.setOnClickListener(this);
        tv_share.setOnClickListener(this);

        //透明动画
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);
        //缩放动画
        ScaleAnimation scaleAnimation = new ScaleAnimation(0,1,0,1,
                Animation.RELATIVE_TO_SELF,0,
                Animation.RELATIVE_TO_SELF,0.5f);
        scaleAnimation.setDuration(500);
        scaleAnimation.setFillAfter(true);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(alphaAnimation);
        set.addAnimation(scaleAnimation);

        //弹出窗体
        //true  获取焦点
        popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        //设置一个透明背景
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        //指定窗体位置
        popupWindow.showAsDropDown(anchor,120, -anchor.getHeight());

        popupView.startAnimation(set);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            adapter = new MyAdapter();
            lv_app_list.setAdapter(adapter);
            if (tv_des != null && customerList != null){
                tv_des.setText("用户应用：" + customerList.size());
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_uninstall:
                if (mApp.isSystem){
                    Toast.makeText(this,"系统应用不能卸载",Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent("android.intent.action.DELETE");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse("package:" + mApp.packageName));
                    startActivity(intent);
                }
                break;
            case R.id.tv_start:
                PackageManager pm = getPackageManager();
                //通过Launch开启指定包名的意图，去开启应用
                Intent intent = pm.getLaunchIntentForPackage(mApp.packageName);
                if (intent != null){
                    startActivity(intent);
                }else {
                    Toast.makeText(this,"此应用不能被开启",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_share:
                //通过短信应用，向外发送短信
                Intent intent1 = new Intent(Intent.ACTION_SEND);
                intent1.putExtra(Intent.EXTRA_TEXT,"分享一个应用，应用名称为" + mApp.name);
                intent1.setType("text/plain");
                startActivity(intent1);
                break;
        }
        if (popupWindow != null){
            popupWindow.dismiss();
        }
    }

    class MyAdapter extends BaseAdapter{

        @Override//条目类型总数
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1;
        }

        @Override//指定索引指向的条目类型，条目类型状态码指定（0（复用系统），1）
        public int getItemViewType(int position) {
            if (position == 0 || position == customerList.size() + 1){
                return 0;//代表纯文本条目的状态码
            }else {
                return 1;//代表图片+文本条目的状态码
            }
        }

        @Override
        public int getCount() {
            return customerList.size() + systemList.size() + 2;
        }

        @Override
        public AppInfo getItem(int position) {
            if (position == 0 || position == customerList.size() + 1){
                return null;
            }else {
                if (position < customerList.size() + 1){
                    return customerList.get(position - 1);
                }else {
                    return systemList.get(position - customerList.size() - 2);
                }
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);
            if (type == 0){
                ViewTitleHolder holder = null;
                if (convertView == null){
                    convertView = View.inflate(getApplicationContext(),R.layout.listview_app_item_title,null);
                    holder = new ViewTitleHolder();
                    holder.tv_title = (TextView) convertView.findViewById(R.id.tv_app_title);
                    convertView.setTag(holder);
                }else {
                    holder = (ViewTitleHolder) convertView.getTag();
                }
                if (position == 0){
                    holder.tv_title.setText("用户应用：" + customerList.size());
                }else {
                    holder.tv_title.setText("系统应用：" + systemList.size());
                }
                return convertView;
            }else {
                ViewHolder holder = null;
                if (convertView == null){
                    convertView = View.inflate(getApplicationContext(),R.layout.listview_app_item,null);
                    holder = new ViewHolder();
                    holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_appinfo_icon);
                    holder.tv_name = (TextView) convertView.findViewById(R.id.tv_app_name);
                    holder.tv_path = (TextView) convertView.findViewById(R.id.tv_app_path);
                    convertView.setTag(holder);
                }else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.iv_icon.setBackground(getItem(position).icon);
                holder.tv_name.setText(getItem(position).name);
                if (getItem(position).isSDCard){
                    holder.tv_path.setText("SD卡应用");
                }else {
                    holder.tv_path.setText("手机应用");
                }
                return convertView;
            }
        }
    }

    static class ViewHolder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_path;
    }

    static class ViewTitleHolder{
        TextView tv_title;
    }

    private void initTitle() {
        //获取磁盘（内存）可用大小，磁盘路径
        String path = Environment.getDataDirectory().getAbsolutePath();
        //获取SD卡可用大小，SD卡路径
        String sd_path = Environment.getExternalStorageDirectory().getAbsolutePath();
        //获取以上两个路径下文件夹的可用大小
        String memoryAvailSpace = Formatter.formatFileSize(this,getAvailSpace(path));
        String sdAvailSpace = Formatter.formatFileSize(this,getAvailSpace(sd_path));
        TextView tv_memory = (TextView) findViewById(R.id.tv_memory);
        TextView tv_sd_memory = (TextView) findViewById(R.id.tv_sd_memory);
        tv_memory.setText("磁盘可用：" + memoryAvailSpace);
        tv_sd_memory.setText("SD卡可用：" + sdAvailSpace);

    }

    //int类型最大值代表2G
    private long getAvailSpace(String path) {
        //获取可用磁盘大小
        StatFs statFs = new StatFs(path);
        //获取可用区块的个数
        long count = statFs.getAvailableBlocks();
        //获取区块的大小
        long size = statFs.getBlockSize();
        return count * size;//返回值结果为byte=8bit
    }


}

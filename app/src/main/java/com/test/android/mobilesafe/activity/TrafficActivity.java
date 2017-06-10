package com.test.android.mobilesafe.activity;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.domain.AppInfo;
import com.test.android.mobilesafe.engine.AppInfoProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TrafficActivity extends AppCompatActivity {

    private ListView lv_app_list;
    private List<AppInfo> appInfoList;
    private MyAdapter adapter;
    private List<AppInfo> systemList;
    private List<AppInfo> customerList;
    private TextView tv_des;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic);
        initdata();
        initList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }

    private void initdata() {
        //获取手机（2G/3G/4G）下载流量
        long mRB = TrafficStats.getMobileRxBytes();
        //获取手机（2G/3G/4G）上传流量
        long mTB = TrafficStats.getMobileTxBytes();
        //下载流量总和（手机+wifi）
        long tRB = TrafficStats.getTotalRxBytes();
        //上传流量（手机+wifi）
        long tTB = TrafficStats.getTotalTxBytes();
        TextView tv_content = (TextView)findViewById(R.id.content);
        String str_mRB = Formatter.formatFileSize(this,mRB);
        String str_mTB = Formatter.formatFileSize(this,mTB);
        String str_tRB = Formatter.formatFileSize(this,tRB);
        String str_tTB = Formatter.formatFileSize(this,tTB);
        Formatter.formatFileSize(this,tTB);
        tv_content.setText("手机下载：" + str_mRB + "\n" + "手机上传：" + str_mTB + "\n" 
                + "下载流量：" + str_tRB + "\n" + "上传流量：" + str_tTB);
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
                if (customerList != null && systemList != null) {
                    if (firstVisibleItem >= customerList.size() + 1) {
                        //滚动到了系统条目
                        tv_des.setText("系统应用：" + systemList.size());
                    } else {
                        tv_des.setText("用户应用：" + customerList.size());
                    }
                }
            }
        });
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

    class MyAdapter extends BaseAdapter {

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
                String uRB = Formatter.formatFileSize(getApplicationContext(),TrafficStats.getUidRxBytes(getItem(position).uid));
                String uTB = Formatter.formatFileSize(getApplicationContext(),TrafficStats.getUidTxBytes(getItem(position).uid));
                if (uRB.equals("-1 B") && uTB.equals("-1 B")){
                    holder.tv_path.setText("暂无统计数据");
                }else {
                    holder.tv_path.setText("上传：" + uTB + "  " + "下载：" + uRB);
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
    
}

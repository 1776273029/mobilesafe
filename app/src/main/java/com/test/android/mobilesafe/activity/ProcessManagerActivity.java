package com.test.android.mobilesafe.activity;

import android.app.ActivityManager;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.domain.AppInfo;
import com.test.android.mobilesafe.domain.ProcessInfo;
import com.test.android.mobilesafe.engine.AppInfoProvider;
import com.test.android.mobilesafe.engine.ProcessInfoProvider;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.SpUtil;

import java.util.ArrayList;
import java.util.List;

public class ProcessManagerActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_processCount;
    private TextView tv_memory_info;
    private Button bt_selector_all;
    private Button bt_selector_reverse;
    private Button bt_clear;
    private Button bt_setting;
    private ListView lv_process;
    private int processCount;
    private long availSpace;
    private long totalSpace;
    private List<ProcessInfo> systemList;
    private List<ProcessInfo> customerList;
    private List<ProcessInfo> processInfoList;
    private MyAdapter myAdapter = null;
    private TextView tv_des;
    private ProcessInfo mProcess;
    private long startAvailSpace;
    private int startProcessCount;
    private int killedProcessCount;
    private String releaseSpace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_manager);
        initUI();
        initTitleData();
        initListData();
    }

    private void initListData() {
        getData();
    }

    @Override
    protected void onResume() {
        getData();
        super.onResume();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            myAdapter = new MyAdapter();
            lv_process.setAdapter(myAdapter);
            if (tv_des != null && customerList != null){
                tv_des.setText("用户进程：" + customerList.size());
            }
        }
    };

    private void getData() {
        new Thread(){
            public void run(){
                processInfoList = ProcessInfoProvider.getProcessInfo(getApplicationContext());
                systemList = new ArrayList<ProcessInfo>();
                customerList = new ArrayList<ProcessInfo>();
                for (ProcessInfo processInfo : processInfoList) {
                    if (processInfo.isSystem){
                        systemList.add(processInfo);
                    }else {
                        customerList.add(processInfo);
                    }
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void initTitleData() {
        updateProcessCount();
        upateMemoryInfo();
        tv_des = (TextView) findViewById(R.id.tv_process_des);
    }

    private void upateMemoryInfo() {
        //获取内存信息并格式化
        availSpace = ProcessInfoProvider.getAvailSpace(this);
        totalSpace = ProcessInfoProvider.getTotalSpace(this);
        String avail = Formatter.formatFileSize(this,availSpace);
        String total = Formatter.formatFileSize(this,totalSpace);
        tv_memory_info.setText(avail + "/" + total);
    }

    private void initUI() {
        tv_processCount = (TextView) findViewById(R.id.tv_processCount);
        tv_memory_info = (TextView) findViewById(R.id.tv_memory_info);
        lv_process = (ListView) findViewById(R.id.lv_process_info);
        bt_selector_all = (Button) findViewById(R.id.bt_selector_all);
        bt_selector_reverse = (Button) findViewById(R.id.bt_selector_reverse);
        bt_clear = (Button) findViewById(R.id.bt_clear);
        bt_setting = (Button) findViewById(R.id.bt_setting);
        bt_selector_all.setOnClickListener(this);
        bt_selector_reverse.setOnClickListener(this);
        bt_clear.setOnClickListener(this);
        bt_setting.setOnClickListener(this);
        lv_process.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (customerList != null && systemList != null){
                    if (firstVisibleItem >= customerList.size() + 1){
                        //滚动到了系统条目
                        tv_des.setText("系统进程：" + systemList.size());
                    }else {
                        tv_des.setText("用户进程：" + customerList.size());
                    }
                }
            }
        });
        lv_process.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override//view 点中条目指向的View对象
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 || position == customerList.size() + 1){
                    return;
                }else {
                    if (position < customerList.size() + 1){
                        mProcess = customerList.get(position - 1);
                    }else {
                        mProcess = systemList.get(position - customerList.size() - 2);
                    }
                    if (mProcess != null){
                        if (!mProcess.packageName.equals(getPackageName())){
                            mProcess.isCheck = !mProcess.isCheck;
                            CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_process);
                            checkBox.setChecked(mProcess.isCheck);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_selector_all:
                selectAll();
                break;
            case R.id.bt_selector_reverse:
                selectReverse();
                break;
            case R.id.bt_clear:
                clearAll();
                break;
            case R.id.bt_setting:
                setting();
                break;
        }
    }

    private void setting() {
        Intent intent = new Intent(this,ProcessSettingActivity.class);
        startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (myAdapter != null){
            myAdapter.notifyDataSetChanged();
        }
    }

    private void clearAll() {
        startAvailSpace = availSpace;
        startProcessCount = processCount;
        List<ProcessInfo> killProcessList = new ArrayList<ProcessInfo>();
        for (ProcessInfo process : customerList) {
            if (process.packageName.equals(getPackageName())){
                continue;
            }
            if (process.isCheck){
                //不能在集合循环的过程中去移除集合中的对象
                //customerList.remove(process);
                //记录需要杀死的进程
                killProcessList.add(process);
            }
        }
        for (ProcessInfo process : systemList) {
            if (process.isCheck){
                killProcessList.add(process);
            }
        }
        //循环遍历killProcessList，然后移除customerList和systemList中相应的对象
        for (ProcessInfo process : killProcessList) {
//            if (customerList.contains(process)){
//                customerList.remove(process);
//            }
//            if (systemList.contains(process)){
//                systemList.remove(process);
//            }
            //杀死记录在killProcessList中的进程
            ProcessInfoProvider.killProcess(this,process);
        }
        //更新数据
        getData();
        //在集合改变后，通知数据适配器刷新
        if (myAdapter != null){
            myAdapter.notifyDataSetChanged();
        }
        //进程总数更新
        updateProcessCount();
        //获取内存信息并格式化
        upateMemoryInfo();
        killedProcessCount = startProcessCount - processCount;
        releaseSpace = Formatter.formatFileSize(this,availSpace - startAvailSpace);
//        Toast.makeText(this,"杀死了" + killedProcessCount + "个进程，" +
//                "释放了" + releaseSpace + "内存",Toast.LENGTH_SHORT).show();
        //占位符指定数据
        String totalStr = String.format("杀死了%d个进程，释放了%s内存",
                killedProcessCount,releaseSpace);
        Toast.makeText(this,totalStr,Toast.LENGTH_SHORT).show();
    }

    private void updateProcessCount() {
        processCount = ProcessInfoProvider.getProcessCount(this);
        tv_processCount.setText("进程总数：" + processCount);
    }

    private void selectReverse() {
        for (ProcessInfo process : customerList) {
            if (process.packageName.equals(getPackageName())){
                continue;
            }
            process.isCheck = !process.isCheck;
        }
        for (ProcessInfo process : systemList) {
            process.isCheck = !process.isCheck;
        }
        if (myAdapter != null){
            myAdapter.notifyDataSetChanged();
        }
    }

    private void selectAll() {
        for (ProcessInfo process : customerList) {
            if (process.packageName.equals(getPackageName())){
                continue;
            }
            process.isCheck = true;
        }
        for (ProcessInfo process : systemList) {
            process.isCheck = true;
        }
        if (myAdapter != null){
            myAdapter.notifyDataSetChanged();
        }
    }

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
            if (SpUtil.getBoolean(getApplicationContext(), ConstantValue.SHOW_SYSTEM,false)){
                return customerList.size() + systemList.size() + 2;
            }else {
                return customerList.size() + 1;
            }
        }

        @Override
        public ProcessInfo getItem(int position) {
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
                    convertView = View.inflate(getApplicationContext(),R.layout.listview_process_item_title,null);
                    holder = new ViewTitleHolder();
                    holder.tv_title = (TextView) convertView.findViewById(R.id.tv_process_title);
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
                    convertView = View.inflate(getApplicationContext(),
                            R.layout.listview_process_item,null);
                    holder = new ViewHolder();
                    holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_process_icon);
                    holder.tv_name = (TextView) convertView.findViewById(R.id.tv_process_name);
                    holder.tv_memSize = (TextView) convertView.findViewById(R.id.tv_process_mem_size);
                    holder.cb_isCheck = (CheckBox) convertView.findViewById(R.id.cb_process);
                    convertView.setTag(holder);
                }else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.iv_icon.setBackground(getItem(position).icon);
                holder.tv_name.setText(getItem(position).name);
                holder.tv_memSize.setText("内存占用：" + Formatter.
                        formatFileSize(getApplicationContext(),getItem(position).memSize));
                if (getItem(position).packageName.equals(getPackageName())){
                    holder.cb_isCheck.setVisibility(View.GONE);
                }else {
                    holder.cb_isCheck.setVisibility(View.VISIBLE);
                }
                holder.cb_isCheck.setChecked(getItem(position).isCheck);
                return convertView;
            }
        }
    }

    static class ViewHolder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_memSize;
        CheckBox cb_isCheck;
    }

    static class ViewTitleHolder{
        TextView tv_title;
    }
}

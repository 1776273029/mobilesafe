package com.test.android.mobilesafe.activity;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.db.dao.BlackNumberDao;
import com.test.android.mobilesafe.domain.BlackNumberInfo;

import java.util.ArrayList;
import java.util.List;
// 复用convertView
// 对findViewById次数进行优化,使用ViewHolder
// 将ViewHolder定义成静态，避免创建多个对象
// listView有多个条目的时候，可以做分页算法，每一次加载20条，逆序返回
public class BlackNumberActivity extends AppCompatActivity {

    private Button bt_add;
    private ListView lv_black_number;
    private BlackNumberDao dao;
    private List<BlackNumberInfo> list;
    private int mode = 0;
    private boolean isLoad = false;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (adapter == null){
                adapter = new MyAdapter();
                lv_black_number.setAdapter(adapter);
            }else {
                adapter.notifyDataSetChanged();
            }
        }
    };
    private MyAdapter adapter;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private EditText et_phone;
    private RadioGroup rg_group;
    private Button bt_submit;
    private Button bt_cancel;
    private List<BlackNumberInfo> moreData;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_number);
        initUI();
        initData();
    }

    private void initData() {
        new Thread() {
            public void run() {
                dao = BlackNumberDao.getInstance(getApplicationContext());
                count = dao.getCount();
                if (count >= 20){
                    list = dao.find(0);
                }else {
                    list = dao.findAll();
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void loadData(final int index) {
        new Thread(){
            public void run(){
                dao = BlackNumberDao.getInstance(getApplicationContext());
                moreData = dao.find(index);
                list.addAll(moreData);
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void initUI() {
        bt_add = (Button) findViewById(R.id.bt_add);
        lv_black_number = (ListView) findViewById(R.id.lv_black_number);
        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        //监听滚动状态
        lv_black_number.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override//状态改变
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //AbsListView.OnScrollListener.SCROLL_STATE_FLING   飞速滚动
                //AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL    触摸滚动
                //AbsListView.OnScrollListener.SCROLL_STATE_IDLE    空闲状态
                if (list != null){
                    //触底且空闲
                    if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                            && lv_black_number.getLastVisiblePosition() >= adapter.getCount() -1
                            && !isLoad){//防止重复加载
                        if (count > list.size()){
                            //加载下一页数据
                            loadData(list.size());
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void showDialog(){
        builder = new AlertDialog.Builder(this);
        dialog = builder.create();
        View view = View.inflate(this,R.layout.dialog_add_blacknumber,null);
        dialog.setView(view);
        et_phone = (EditText) view.findViewById(R.id.et_black_number);
        rg_group = (RadioGroup) view.findViewById(R.id.rg_group);
        bt_submit = (Button) view.findViewById(R.id.bt_submit_b);
        bt_cancel = (Button) view.findViewById(R.id.bt_cancel_b);
        rg_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId){
                    case R.id.rb_sms:
                        mode = 0;
                        break;
                    case R.id.rb_phone:
                        mode = 1;
                        break;
                    case R.id.rb_all:
                        mode = 2;
                        break;
                }
            }
        });
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = et_phone.getText().toString();
                if (!TextUtils.isEmpty(phone)){
                    dao.insert(phone,mode+"");
                    BlackNumberInfo info = new BlackNumberInfo();
                    info.phone = phone;
                    info.mode = mode + "";
                    list.add(0,info);//往集合顶部插入数据
                    if (adapter != null){
                        adapter.notifyDataSetChanged();
                    }
                    dialog.dismiss();
                }else {
                    Toast.makeText(getApplicationContext(),"请输入电话号码",Toast.LENGTH_SHORT).show();
                }
            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.listview_blacknumber_item, null);
                holder = new ViewHolder();
                holder.tv_phone = (TextView) convertView.findViewById(R.id.tv_phone);
                holder.tv_mode = (TextView) convertView.findViewById(R.id.tv_mode);
                holder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dao.delete(list.get(position).phone);
                    list.remove(position);
                    if (adapter != null){
                        adapter.notifyDataSetChanged();
                    }
                }
            });
            holder.tv_phone.setText(list.get(position).phone);
            int mode = Integer.parseInt(list.get(position).mode);
            switch (mode){
                case 0:
                    holder.tv_mode.setText("拦截短信");
                    break;
                case 1:
                    holder.tv_mode.setText("拦截电话");
                    break;
                case 2:
                    holder.tv_mode.setText("拦截所有");
                    break;
            }
            return convertView;
        }
    }

    static class ViewHolder {
        TextView tv_phone;
        TextView tv_mode;
        ImageView iv_delete;
    }

}

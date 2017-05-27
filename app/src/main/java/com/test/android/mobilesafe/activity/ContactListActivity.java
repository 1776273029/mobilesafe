package com.test.android.mobilesafe.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.test.android.mobilesafe.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactListActivity extends AppCompatActivity {

    private ListView lv_contacts;
    private List<HashMap<String,String>> contactsList = new ArrayList<HashMap<String, String>>();


    private MyAdapter adapter;
    private EditText et_phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        initUI();
        initData();
    }


    private void initData() {

        new Thread(){
            public void run(){
                ContentResolver resolver = getContentResolver();
                Cursor cursor = resolver.query(Uri.parse(
                        "content://com.android.contacts/raw_contacts"),
                        new String[]{"contact_id"},null,null,null);
                contactsList.clear();
                while (cursor.moveToNext()){
                    String id = cursor.getString(0);
                    if (id != null) {
                        Cursor indexCursor = resolver.query(Uri.parse(
                                "content://com.android.contacts/data"),
                                new String[]{"data1", "mimetype"},
                                "raw_contact_id = ?", new String[]{id}, null);
                        HashMap<String, String> map = new HashMap<String, String>();
                        while (indexCursor.moveToNext()) {
                            String data = indexCursor.getString(0);
                            String type = indexCursor.getString(1);
                            if (type.equals("vnd.android.cursor.item/phone_v2")) {
                                if (!TextUtils.isEmpty(data)) {
                                    map.put("phone", data);
                                }
                            } else if (type.equals("vnd.android.cursor.item/name")) {
                                if (!TextUtils.isEmpty(data)) {
                                    map.put("name", data);
                                }
                            }
                        }
                        indexCursor.close();
                        contactsList.add(map);
                        Log.d("cursor", "cursorId:" + id);
                    }
                }
                cursor.close();
                Message msg = Message.obtain();
                msg.obj = contactsList;
                mHandler.sendMessage(msg);
                //发送一个空的消息，告诉主线程可以去使用子线程已经填充好的数据集合
                mHandler.sendEmptyMessage(0);
            };
        }.start();


    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            adapter = new MyAdapter();
            lv_contacts.setAdapter(adapter);
        }
    };

    private void initUI() {
        lv_contacts = (ListView) findViewById(R.id.lv_contacts);
        lv_contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter != null){
                    HashMap<String,String> map = adapter.getItem(position);
                    String phone = map.get("phone");
                    Intent intent = new Intent();
                    intent.putExtra("phone",phone);
                    setResult(0,intent);//返回数据
                    finish();
                }
            }
        });
    }

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return contactsList.size();
        }

        @Override
        public HashMap<String, String> getItem(int position) {
            return contactsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(getApplicationContext(),
                    R.layout.contact_list_item,null);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_contact_name);
            TextView tv_phone = (TextView) view.findViewById(R.id.tv_contact_phone);
            tv_name.setText(getItem(position).get("name"));
            tv_phone.setText(getItem(position).get("phone"));
            return view;
        }
    }

}

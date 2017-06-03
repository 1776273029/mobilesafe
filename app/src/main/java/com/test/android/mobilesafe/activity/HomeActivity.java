package com.test.android.mobilesafe.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.MD5Util;
import com.test.android.mobilesafe.util.SpUtil;

public class HomeActivity extends AppCompatActivity {

    private GridView gv_home;
    private String[] myTitleStr;
    private int[] mDrawableIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        gv_home = (GridView) findViewById(R.id.gv_home);
        initData();
    }

    private void initData() {
        myTitleStr = new String[]{
                "手机防盗","通讯卫士","软件管理","进程管理","流量统计",
                "手机杀毒","缓存清理","高级工具","设置中心"
        };
        mDrawableIds = new int[]{
                R.drawable.home_safe,R.drawable.home_callmsgsafe,
                R.drawable.home_apps,R.drawable.home_taskmanager,
                R.drawable.home_netmanager,R.drawable.home_trojan,
                R.drawable.home_sysoptimize,R.drawable.home_tools,
                R.drawable.home_settings
        };

        gv_home.setAdapter(new MyAdapter());

        gv_home.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        showDialog();
                        break;
                    case 1:
                        startActivity(new Intent(getApplicationContext(),BlackNumberActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(getApplicationContext(),AppManagerActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(getApplicationContext(),ProcessManagerActivity.class));
                        break;
                    case 7:
                        startActivity(new Intent(getApplicationContext(),AToolActivity.class));
                        break;
                    case 8:
                        Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });

    }

    private void showDialog(){
        String password = SpUtil.getString(this, ConstantValue.MOBILE_SAFE_PSD,"");
        if (TextUtils.isEmpty(password)){
            showSetPsdDialog();
        }else {
            showConfirmPsdDialog();
        }
    }

    private void showConfirmPsdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        final View view = View.inflate(this,R.layout.dialog_confirm_psd,null);
        dialog.setView(view);
        dialog.show();

        Button bt_submit = (Button) view.findViewById(R.id.bt_submit);
        Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_input_psd = (EditText) view.findViewById(R.id.et_input_psd);
                String psd = et_input_psd.getText().toString();
                if (!TextUtils.isEmpty(psd)){
                    String password = SpUtil.getString(getApplicationContext(),ConstantValue.MOBILE_SAFE_PSD,"");
                    if (MD5Util.encoder(psd).equals(password)){
                        Intent intent = new Intent(getApplicationContext(),SetupOverActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    }else {
                        Toast.makeText(getApplicationContext(),"密码错误",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"请输入密码",Toast.LENGTH_SHORT).show();
                }
            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void showSetPsdDialog() {
        //自定义对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        final View view = View.inflate(this,R.layout.dialog_set_psd,null);
        dialog.setView(view);
        dialog.show();

        Button bt_submit = (Button) view.findViewById(R.id.bt_submit);
        Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_set_psd = (EditText) view.findViewById(R.id.et_set_psd);
                EditText et_confirm_psd = (EditText) view.findViewById(R.id.et_confirm_psd);
                String psd = et_set_psd.getText().toString();
                String confirm_psd = et_confirm_psd.getText().toString();
                if (!TextUtils.isEmpty(psd) && !TextUtils.isEmpty(confirm_psd)){
                    if (psd.equals(confirm_psd)){
                        Intent intent = new Intent(getApplicationContext(),SetupOverActivity.class);
                        startActivity(intent);
                        SpUtil.putString(getApplicationContext(),ConstantValue.MOBILE_SAFE_PSD, MD5Util.encoder(psd));
                        dialog.dismiss();
                    }else {
                        Toast.makeText(getApplicationContext(),"确认密码错误",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"请输入密码",Toast.LENGTH_SHORT).show();
                }
            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    public class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return myTitleStr.length;
        }

        @Override
        public Object getItem(int position) {
            return myTitleStr[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = View.inflate(getApplicationContext(),R.layout.gridview_item,null);
            TextView itemName = (TextView) view.findViewById(R.id.tv_item_name);
            ImageView itemIcon = (ImageView) view.findViewById(R.id.iv_icon);
            itemName.setText(myTitleStr[position]);
            itemIcon.setBackgroundResource(mDrawableIds[position]);

            return view;
        }
    }
}

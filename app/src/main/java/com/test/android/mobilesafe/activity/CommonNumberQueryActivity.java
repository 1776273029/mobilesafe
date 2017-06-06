package com.test.android.mobilesafe.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.engine.CommonNumDao;

import java.util.List;

public class CommonNumberQueryActivity extends AppCompatActivity {

    private ExpandableListView elv_common_number;
    private List<CommonNumDao.Group> groupList;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_number_query);
        initUI();
        initData();
    }

    private void initData() {
        CommonNumDao dao = new CommonNumDao();
        groupList = dao.getGroup();
        adapter = new MyAdapter();
        elv_common_number.setAdapter(adapter);
        elv_common_number.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                startCall(adapter.getChild(groupPosition, childPosition).number);
                return false;
            }
        });
    }

    private void startCall(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

    private void initUI() {
        elv_common_number = (ExpandableListView) findViewById(R.id.elv_common_number);
    }

    class MyAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return groupList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return groupList.get(groupPosition).childList.size();
        }

        @Override
        public CommonNumDao.Group getGroup(int groupPosition) {
            return groupList.get(groupPosition);
        }

        @Override
        public CommonNumDao.Child getChild(int groupPosition, int childPosition) {
            return groupList.get(groupPosition).childList.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            TextView textView = new TextView(getApplicationContext());
            textView.setText("      " + getGroup(groupPosition).name);
            textView.setTextColor(Color.RED);
            //dip == dp；dpi == ppi（像素密度，每一个英寸上分布的像素点的个数）
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,22);
            return textView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View view = View.inflate(getApplicationContext(),R.layout.elv_child_item,null);
            TextView tv_common_name = (TextView) view.findViewById(R.id.tv_commonnum_name);
            TextView tv_common_num = (TextView) view.findViewById(R.id.tv_common_num);
            tv_common_name.setText(getChild(groupPosition,childPosition).name);
            tv_common_num.setText(getChild(groupPosition,childPosition).number);
            return view;
        }

        @Override//是否响应点击事件
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}

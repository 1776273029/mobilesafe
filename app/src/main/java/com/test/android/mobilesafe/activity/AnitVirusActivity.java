package com.test.android.mobilesafe.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.engine.VirusDao;
import com.test.android.mobilesafe.util.MD5Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnitVirusActivity extends AppCompatActivity {

    private static final int SCANNING = 1;
    private static final int SCAN_FINISH = 2;
    private ImageView iv_scanning;
    private TextView tv_name;
    private LinearLayout ll_add_text;
    private ProgressBar pb_bar;
    private RotateAnimation mAnimation;
    private int index = 0;
    private List<ScanInfo> virusScanInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anit_virus);
        initUI();
        initAnimation();
        checkVirus();
    }

    private void checkVirus() {
        new Thread(){
            public void run(){
                List<String> virusList = VirusDao.getVirusList();
                PackageManager mPM = getPackageManager();
                //获取所有应用程序签名文件（PackageManager.GET_SIGNATURES：已安装应用的签名文件）
                // PackageManager.GET_UNINSTALLED_PACKAGES 卸载残余文件
                List<PackageInfo> packageInfoList = mPM.getInstalledPackages(
                        PackageManager.GET_SIGNATURES
                                + PackageManager.GET_UNINSTALLED_PACKAGES);
                //记录病毒集合
                virusScanInfoList = new ArrayList<ScanInfo>();
                //记录所有应用集合
                List<ScanInfo> scanInfoList = new ArrayList<ScanInfo>();
                //设置进度条的最大值
                pb_bar.setMax(packageInfoList.size());

                //遍历应用集合
                for (PackageInfo packageInfo: packageInfoList){
                    ScanInfo scanInfo = new ScanInfo();
                    //获取签名文件的数组
                    Signature[] signatures = packageInfo.signatures;
                    //获取签名文件数组的第一位，然后进行md5，将此md5和数据库中的md5进行比对
                    Signature signature = signatures[0];
                    String sig = signature.toCharsString();
                    String md5 = MD5Util.encoder(sig);
                    Log.d("md5",packageInfo.packageName + ":" + md5);
                    if (virusList.contains(md5)){
                        scanInfo.isVirus = true;
                        virusScanInfoList.add(scanInfo);
                    }else {
                        scanInfo.isVirus = false;
                    }
                    scanInfo.packageName = packageInfo.packageName;
                    scanInfo.name = packageInfo.applicationInfo.loadLabel(mPM).toString();
                    scanInfoList.add(scanInfo);
                    index ++;
                    pb_bar.setProgress(index);

                    try {
                        Thread.sleep(50 + new Random().nextInt(100));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Message msg = Message.obtain();
                    msg.what = SCANNING;
                    msg.obj = scanInfo;
                    mHandler.sendMessage(msg);
                }
                Message msg = Message.obtain();
                msg.what = SCAN_FINISH;
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SCANNING:
                    ScanInfo info = (ScanInfo) msg.obj;
                    tv_name.setText(info.name);
                    //在线性布局中添加一个正在扫描应用的TextView
                    TextView textView = new TextView(getApplicationContext());
                    if (info.isVirus){
                        textView.setTextColor(Color.RED);
                        textView.setText("发现病毒：" + info.name);
                    }else {
                        textView.setTextColor(Color.BLACK);
                        textView.setText("扫描安全：" + info.name);
                    }
                    ll_add_text.addView(textView,0);
                    break;
                case SCAN_FINISH:
                    tv_name.setText("扫描完成");
                    //停止动画
                    iv_scanning.clearAnimation();
                    //unInstallVirus();
                    break;
            }
        }
    };

    private void unInstallVirus() {
        for (ScanInfo virus : virusScanInfoList){
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + virus.packageName));
            startActivity(intent);
        }
    }

    class ScanInfo{
        public boolean isVirus;
        public String name;
        public String packageName;
    }

    private void initAnimation() {
        mAnimation = new RotateAnimation(0,360,
                Animation.RELATIVE_TO_SELF,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f);
        mAnimation.setDuration(1000);
        mAnimation.setRepeatCount(Animation.INFINITE);//一直重复
        mAnimation.setFillAfter(true);//保持动画执行结束后的状态
        iv_scanning.startAnimation(mAnimation);//开启动画
    }

    private void initUI() {
        iv_scanning = (ImageView) findViewById(R.id.iv_scanning);
        tv_name = (TextView) findViewById(R.id.tv_name);
        pb_bar = (ProgressBar) findViewById(R.id.pb_bar);
        ll_add_text = (LinearLayout) findViewById(R.id.ll_add_text);
    }
}

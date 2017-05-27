package com.test.android.mobilesafe.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.test.android.mobilesafe.R;
import com.test.android.mobilesafe.util.ConstantValue;
import com.test.android.mobilesafe.util.SpUtil;
import com.test.android.mobilesafe.util.StreamUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashActivity extends Activity {


    private int versionCode_l;
    private static final int UPDATE_VERSION = 1;
    private static final int ENTER_HOME = 2;
    private static final int REQUEST_ERROR = 3;
    private String versionDes;
    private String downloadUrl;
    private RelativeLayout rl_root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);//去除标题栏
        setContentView(R.layout.activity_splash);
        rl_root = (RelativeLayout) findViewById(R.id.rl_root);
        TextView tv = (TextView)findViewById(R.id.tv_version_name);
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(),0);
            String versionName = info.versionName;
            versionCode_l = info.versionCode;
            tv.setText("版本名称："+ versionName);
            getVersionInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initAnimation();
    }

    //添加淡入的动画效果
    private void initAnimation() {
        //F4查看类的继承关系
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
        alphaAnimation.setDuration(3000);
        rl_root.startAnimation(alphaAnimation);
    }

    private void getVersionInfo(){
        new Thread(){
            public void run(){
                long startTime = System.currentTimeMillis();
                Message msg = Message.obtain();
                try {
                    URL url = new URL("http://192.168.191.1/update.json");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(2000);
                    connection.setReadTimeout(2000);
                    if (connection.getResponseCode() == 200){
                        InputStream is = connection.getInputStream();
                        String json = StreamUtil.stream2String(is);
                        Log.i("json:", json);
                        JSONObject jsonObject = new JSONObject(json);
                        String versionName = jsonObject.getString("versionName");
                        versionDes = jsonObject.getString("versionDes");
                        String versionCode = jsonObject.getString("versionCode");
                        downloadUrl = jsonObject.getString("downloadUrl");
                        Log.i("info",versionName + versionDes + versionCode + downloadUrl);
                        if (versionCode_l < Integer.parseInt(versionCode)){
                            msg.what = UPDATE_VERSION;
                        }else {
                            msg.what = ENTER_HOME;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = REQUEST_ERROR;
                }finally {
                    long endTime = System.currentTimeMillis();
                    long timeLong = endTime - startTime;
                    if (timeLong < 4000){
                        try {
                            Thread.sleep(4000 - timeLong);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    private void showUpdateDialog(){
        //对话框是依赖于activity实现的
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);//左上角图标
        builder.setTitle("版本更新");
        builder.setMessage(versionDes);
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                            + File.separator+"mobilesafe.apk";
                    HttpUtils httpUtils = new HttpUtils();
                    httpUtils.download(downloadUrl, path, new RequestCallBack<File>() {
                        @Override//下载成功
                        public void onSuccess(ResponseInfo<File> responseInfo) {
                            File file = responseInfo.result;
                            installApk(file);
                        }

                        @Override//下载失败
                        public void onFailure(HttpException e, String s) {

                        }

                        @Override//刚开始下载
                        public void onStart() {
                            super.onStart();
                        }

                        @Override//下载总大小，当前下载的位置，是否正在下载
                        public void onLoading(long total, long current, boolean isUploading) {
                            super.onLoading(total, current, isUploading);
                        }

                        @Override//取消下载
                        public void onCancelled() {
                            super.onCancelled();
                            enterHome();
                        }
                    });
                }

            }
        });
        builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enterHome();
            }
        });
        //取消事件监听
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    private void enterHome() {
        Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void installApk(File file) {
        Intent intent = new Intent("android.intent.action.View");
        intent.addCategory("android.intent.category.DEFAULT");
//        intent.setDataAndType()
        intent.setData(Uri.fromFile(file));//文件作为数据源
        intent.setType("application/vnd.android.package-archive");//设置安装的类型
        startActivityForResult(intent,1);
        finish();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_VERSION:
                    if(SpUtil.getBoolean(getApplicationContext(), ConstantValue.OPEN_UPDATE,false)){
                        showUpdateDialog();
                    }else {
                        enterHome();
                    }
                    break;
                case ENTER_HOME:
                    enterHome();
                    break;
                case REQUEST_ERROR:
                    Toast.makeText(SplashActivity.this,"请求数据出错",Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        enterHome();
        super.onActivityResult(requestCode, resultCode, data);
    }
}

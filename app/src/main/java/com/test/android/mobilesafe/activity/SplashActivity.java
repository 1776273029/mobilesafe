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
import java.io.FileOutputStream;
import java.io.IOException;
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
        initDB();
        if (!SpUtil.getBoolean(this,ConstantValue.HAS_SHORTCUT,false)){
            initShortCut();
        }
    }

    //生成快捷方式
    private void initShortCut() {
        Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        //维护图标
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, android.graphics.
                BitmapFactory.decodeResource(getResources(),R.drawable.main_icon));
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,"黑马卫士");//快捷方式名称
        //点击快捷方式后跳转的activity
        //维护开启的意图对象
        Intent shortCutIntent = new Intent("mobilesafe.action.HOME");
        shortCutIntent.addCategory("android.intent.category.DEFAULT");

        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,shortCutIntent);
        //发送广播
        sendBroadcast(intent);
        SpUtil.putBoolean(this,ConstantValue.HAS_SHORTCUT,true);
    }

    private void initDB() {
        //归属地数据库拷贝过程
        initLocalDB("address.db");
        initLocalDB("commonnum.db");
        initLocalDB("antivirus.db");
    }

    private void initLocalDB(String dbName) {
//        getCacheDir();
//        Environment.getExternalStorageDirectory().getAbsolutePath();
        //在files文件夹下创建同名数据库文件
        File files = getFilesDir();
        File file = new File(files,dbName);
        if (file.exists()){
            return;
        }
        InputStream stream = null;
        FileOutputStream fos = null;
        try {
            //输入流读取第三方资产目录下的文件
            stream = getAssets().open(dbName);
            //将读取的内容写入到指定文件中
            fos = new FileOutputStream(file);
            //每次读取内容大小
            byte[] bytes = new byte[1024];
            int temp = -1;
            while ((temp = stream.read(bytes)) != -1){
                fos.write(bytes,0,temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (stream != null){
                    stream.close();
                }
                if (fos != null){
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
        builder.setIcon(R.drawable.main_icon);//左上角图标
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

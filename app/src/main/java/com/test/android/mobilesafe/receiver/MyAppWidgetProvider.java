package com.test.android.mobilesafe.receiver;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.test.android.mobilesafe.service.UpdateWidgetService;

/**
 * Created by Administrator on 2017/6/4.
 */

public class MyAppWidgetProvider extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override//创建第一个窗体小部件时调用（开启服务）
    public void onEnabled(Context context) {
        context.startService(new Intent(context, UpdateWidgetService.class));
        super.onEnabled(context);
    }

    @Override//创建多一个窗体小部件时调用
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, UpdateWidgetService.class));
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override//当窗体小部件宽高发生改变时调用，创建窗体小部件时也调用这个方法（API 16以上）
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, UpdateWidgetService.class));
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override//删除一个窗体小部件时调用
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override//删除最后一个窗体小部件时调用（关闭服务）
    public void onDisabled(Context context) {
        context.stopService(new Intent(context, UpdateWidgetService.class));
        super.onDisabled(context);
    }
}

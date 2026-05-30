package com.root.utils;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

// BatteryWidgetProvider.java
public class BatteryWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // 启动服务来更新小部件
        context.startService(new Intent(context, BatteryWidgetService.class));
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // 删除小部件时停止服务
        context.stopService(new Intent(context, BatteryWidgetService.class));
    }
}

package com.root.utils;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class InfoWidgetService extends Service {
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateWidgets();
            handler.postDelayed(this, 3000); // 每3秒更新一次
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        startPeriodicUpdate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPeriodicUpdate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startPeriodicUpdate() {
        handler.post(runnable);
    }

    private void stopPeriodicUpdate() {
        handler.removeCallbacks(runnable);
    }

    private void updateWidgets() {
        Context context = this;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, InfoWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int appWidgetId : appWidgetIds) {
            InfoWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}

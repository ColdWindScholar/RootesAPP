package com.root.utils;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;
import com.root.system.R;

public class BatteryWidgetService extends Service {

    private static final long UPDATE_INTERVAL = 3000; // 更新间隔，3秒钟
    private Handler mHandler = new Handler();
    private Runnable mUpdateTask = new Runnable() {
        @Override
        public void run() {
            updateWidget();
            mHandler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // 第一次启动服务时立即更新小部件
        updateWidget();
        // 启动定时任务定期更新小部件
        mHandler.postDelayed(mUpdateTask, UPDATE_INTERVAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止定时任务
        mHandler.removeCallbacks(mUpdateTask);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateWidget() {
        // 获取电池信息
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName thisWidget = new ComponentName(this, BatteryWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, intentFilter);

        if (batteryStatus != null) {
            // 提取电池信息
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN);
            String technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
            int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0); // 获取电池温度
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

            // 计算电池剩余百分比
            int batteryPct = (int) ((level / (float) scale) * 100);

            // 更新每个小部件
            for (int appWidgetId : appWidgetIds) {
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_battery_info);
                views.setTextViewText(R.id.text_battery_status, "电池状态: " + getBatteryStatusString(status));
                views.setTextViewText(R.id.text_battery_level, "电池电量: " + batteryPct + "%");
                views.setTextViewText(R.id.text_battery_health, "电池健康: " + getBatteryHealthString(health));
                views.setTextViewText(R.id.text_battery_technology, "电池技术: " + technology);

                // 将整数温度转换为带有一位小数的字符串形式
                String temperatureString = String.format("%.1f", temperature / 10.0f);
                views.setTextViewText(R.id.text_battery_current, "电池温度: " + temperatureString + "℃");

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }

    // 获取电池状态字符串
    private String getBatteryStatusString(int status) {
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                return getString(R.string.battery_status_charging);
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                return getString(R.string.battery_status_discharging);
            case BatteryManager.BATTERY_STATUS_FULL:
                return getString(R.string.battery_status_full);
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                return getString(R.string.battery_status_not_charging);
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
            default:
                return getString(R.string.battery_status_unknown);
        }
    }

    // 获取电池健康状态字符串
    private String getBatteryHealthString(int health) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                return "良好";
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return "过热";
            case BatteryManager.BATTERY_HEALTH_DEAD:
                return "损坏";
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                return "过压";
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                return "未指定故障";
            case BatteryManager.BATTERY_HEALTH_COLD:
                return "过冷";
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
            default:
                return "未知";
        }
    }
}

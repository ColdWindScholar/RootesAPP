package com.root.utils;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.widget.RemoteViews;
import android.app.ActivityManager;

import com.root.library.shell.CpuFrequencyUtils;
import com.root.library.shell.GpuUtils;
import com.root.system.R;

import java.util.Locale;

public class InfoWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        // 启动服务更新小部件
        context.startService(new Intent(context, InfoWidgetService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // 获取 GPU 频率和负载
        String gpuFreq = GpuUtils.getGpuFreq();
        int gpuLoad = GpuUtils.getGpuLoad();
        views.setTextViewText(R.id.gpu_freq, "GPU 频率: " + gpuFreq + " MHz");
        views.setTextViewText(R.id.gpu_load, "GPU 负载: " + gpuLoad + "%");

        // 获取 CPU 频率
        CpuFrequencyUtils cpuFrequencyUtils = new CpuFrequencyUtils();
        String cpuFreq = cpuFrequencyUtils.getCurrentFrequency(0);  // 获取第一个集群的当前频率
        views.setTextViewText(R.id.cpu_freq, "CPU 频率: " + cpuFreq + " MHz");

        // 获取运行内存
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long totalMem = memoryInfo.totalMem / (1024 * 1024);
        long freeMem = memoryInfo.availMem / (1024 * 1024);
        views.setTextViewText(R.id.memory_usage, "运行内存: " + freeMem + "MB / " + totalMem + "MB");

        // 获取存储内存
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        long totalStorage = (statFs.getTotalBytes() / (1024 * 1024));
        long freeStorage = (statFs.getAvailableBytes() / (1024 * 1024));
        views.setTextViewText(R.id.storage_usage, "存储内存: " + freeStorage + "MB / " + totalStorage + "MB");

        // 获取开机时间
        long uptimeMillis = SystemClock.elapsedRealtime();
        String bootTime = formatDuration(uptimeMillis);
        views.setTextViewText(R.id.uptime, "开机时间: " + bootTime);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;

        return String.format(Locale.getDefault(), "%d天 %02d:%02d:%02d", days, hours, minutes, seconds);
    }
}

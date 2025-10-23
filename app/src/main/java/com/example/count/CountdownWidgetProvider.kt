package com.example.count

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class CountdownWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 为每个小组件实例进行更新
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        // 注册AlarmManager定时刷新
        registerAlarm(context)
    }

    override fun onEnabled(context: Context) {
        // 第一个小组件被添加时，注册AlarmManager定时刷新
        registerAlarm(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // 当小组件被删除时，清理保存的设置
        for (appWidgetId in appWidgetIds) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }
        // 检查是否还有小组件，无则取消AlarmManager
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, CountdownWidgetProvider::class.java))
        if (ids.isEmpty()) {
            cancelAlarm(context)
        }
    }

    override fun onDisabled(context: Context) {
        // 最后一个小组件被删除时，取消AlarmManager
        cancelAlarm(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, CountdownWidgetProvider::class.java))
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}

private val handler = Handler(Looper.getMainLooper())
private var updateRunnable: Runnable? = null

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // 构造 RemoteViews 对象
    val views = RemoteViews(context.packageName, R.layout.countdown_widget)

    // 设置齿轮按钮的点击事件，打开配置 Activity
    val configIntent = Intent(context, CountdownWidgetConfigureActivity::class.java)
    configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    // PendingIntent唯一性：用appWidgetId做requestCode，FLAG_UPDATE_CURRENT保证可用
    val configPendingIntent = PendingIntent.getActivity(
        context, appWidgetId, configIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.settings_button, configPendingIntent)

    // 更新时钟、日期和星期
    val calendar = Calendar.getInstance()
    views.setTextViewText(R.id.date_text, SimpleDateFormat("MMMM d", Locale.getDefault()).format(calendar.time))
    views.setTextViewText(R.id.weekday_text, SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time))
    views.setTextViewText(R.id.clock_text, SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time))

    // 加载目标时间
    val targetTime = loadTargetTime(context, appWidgetId)

    if (targetTime > 0) {
        val currentTime = System.currentTimeMillis()
        var diff = targetTime - currentTime

        if (diff < 0) diff = 0

        val days = TimeUnit.MILLISECONDS.toDays(diff)

        views.setTextViewText(R.id.days_text, days.toString())
    } else {
        // 如果没有设置时间，显示默认文本
        views.setTextViewText(R.id.days_text, "N/A")
    }

    // 指示小组件管理器更新小组件
    appWidgetManager.updateAppWidget(appWidgetId, views)

    // Handler每秒刷新（进程活时流畅）
    if (updateRunnable == null) {
        updateRunnable = object : Runnable {
            override fun run() {
                val componentName = ComponentName(context, CountdownWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (id in allWidgetIds) {
                    updateAppWidget(context, appWidgetManager, id)
                }
                handler.postDelayed(this, 1000) // 每秒刷新一次
            }
        }
        handler.post(updateRunnable!!)
    }
}

private const val ACTION_UPDATE_WIDGET = "com.example.count.action.UPDATE_WIDGET"

private fun registerAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, CountdownWidgetProvider::class.java).apply {
        action = ACTION_UPDATE_WIDGET
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    // 每分钟触发一次（可改为每秒，但为省电建议每分钟）
    val intervalMillis = 60 * 1000L
    val triggerAtMillis = System.currentTimeMillis() + intervalMillis
    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        triggerAtMillis,
        intervalMillis,
        pendingIntent
    )
}

private fun cancelAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, CountdownWidgetProvider::class.java).apply {
        action = ACTION_UPDATE_WIDGET
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
}

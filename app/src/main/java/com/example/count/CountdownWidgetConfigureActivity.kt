package com.example.count

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.util.Calendar

class CountdownWidgetConfigureActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 从 Intent 中找出 App Widget ID
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // 如果没有有效的 ID，直接结束
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // 显示日期和时间选择器
        showDateTimePicker()
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedTime = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 23, 59, 59)
                }
                saveTargetTime(this, appWidgetId, selectedTime.timeInMillis)

                // 更新小组件
                val appWidgetManager = AppWidgetManager.getInstance(this)
                updateAppWidget(this, appWidgetManager, appWidgetId)

                // 创建返回结果并结束 Activity
                val resultValue = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(RESULT_OK, resultValue)
                finish()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.setOnCancelListener {
            // 当用户取消对话框时，也结束这个 Activity
            finish()
        }
        dialog.show()
    }
}

internal const val PREFS_NAME = "com.example.count.CountdownWidget"
internal const val PREF_PREFIX_KEY = "target_time_"

// 保存截止时间
internal fun saveTargetTime(context: Context, appWidgetId: Int, time: Long) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putLong(PREF_PREFIX_KEY + appWidgetId, time)
    prefs.apply()
}

// 加载截止时间
internal fun loadTargetTime(context: Context, appWidgetId: Int): Long {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    return prefs.getLong(PREF_PREFIX_KEY + appWidgetId, -1L)
}


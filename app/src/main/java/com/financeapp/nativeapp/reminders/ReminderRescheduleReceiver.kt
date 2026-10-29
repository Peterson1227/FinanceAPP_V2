package com.financeapp.nativeapp.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.AlarmManager

/** Recupera agendamentos futuros depois do boot e depois da atualização do APK. */
class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> ReminderScheduler.restorePending(context, deviceRebooted = true)
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> ReminderScheduler.restorePending(context)
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {
                if (ReminderScheduler.canUseExactAlarms(context)) {
                    ReminderScheduler.restorePending(context)
                }
            }
        }
    }
}

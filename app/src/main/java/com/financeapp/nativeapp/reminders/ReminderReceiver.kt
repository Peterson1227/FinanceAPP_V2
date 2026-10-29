package com.financeapp.nativeapp.reminders

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.financeapp.nativeapp.MainActivity
import com.financeapp.nativeapp.R

/** O BroadcastReceiver é chamado pelo sistema mesmo sem uma Activity aberta. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1)
        if (id < 0) return
        val reminder = ReminderStore.find(context, id) ?: return
        if (reminder.status != ReminderStatus.SCHEDULED) return
        // Evita disparar um conteúdo antigo, por exemplo, após o usuário editar o horário.
        if (reminder.scheduledAt > System.currentTimeMillis()) {
            ReminderScheduler.schedule(context, reminder)
            return
        }

        ReminderScheduler.createChannel(context)
        if (!ReminderScheduler.canNotify(context)) {
            ReminderStore.setStatus(context, id, ReminderStatus.BLOCKED)
            return
        }

        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openIntent = PendingIntent.getActivity(
            context, id, openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val privacySafeVersion = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_finance)
            .setContentTitle("FinanceAPP · lembrete")
            .setContentText("Abra o aplicativo para visualizar seu compromisso.")
            .build()
        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_finance)
            .setContentTitle(reminder.title)
            .setContentText(reminder.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminder.message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(privacySafeVersion)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
            ReminderStore.setStatus(context, id, ReminderStatus.DELIVERED)
        } catch (_: SecurityException) {
            ReminderStore.setStatus(context, id, ReminderStatus.BLOCKED)
        } catch (_: RuntimeException) {
            // Não registrar "Disparado" caso não tenha sido possível publicar.
            ReminderStore.setStatus(context, id, ReminderStatus.BLOCKED)
        }
    }
}

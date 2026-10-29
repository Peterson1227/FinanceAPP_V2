package com.financeapp.nativeapp.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Notificações 100% locais. AlarmManager + PendingIntent explícito permanecem válidos
 * mesmo quando a Activity não está aberta (exceto após Forçar parada do aplicativo).
 * Sem a autorização opcional de alarmes exatos, o disparo continua aproximado.
 * Alarmes não sobrevivem ao estado "Forçar parada" imposto pelo Android.
 */
object ReminderScheduler {
    // Um novo ID permite que instalações anteriores recebam um canal de alerta visível;
    // a importância de um canal Android já criado não pode ser elevada por código.
    const val CHANNEL_ID = "financeapp_financial_alerts_v2"
    const val EXTRA_REMINDER_ID = "financeapp_reminder_id"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Alertas financeiros",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Avisos locais sobre pagamentos e compromissos agendados"
                    enableVibration(true)
                }
            )
        }
    }

    fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        ) return false
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = context.getSystemService(NotificationManager::class.java)
                .getNotificationChannel(CHANNEL_ID)
            if (channel?.importance == NotificationManager.IMPORTANCE_NONE) return false
        }
        return true
    }

    /** Android 12+: só é possível agendar com exatidão quando o usuário permite.
     * Em versões anteriores o sistema permite o agendamento exato diretamente.
     */
    fun canUseExactAlarms(context: Context): Boolean {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarm.canScheduleExactAlarms()
    }

    fun schedule(context: Context, reminder: FinanceReminder): Boolean {
        if (reminder.scheduledAt <= System.currentTimeMillis()) return false
        createChannel(context)
        if (!canNotify(context)) return false
        return try {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = pendingIntent(context, reminder.id, PendingIntent.FLAG_UPDATE_CURRENT)
            // Prioriza o alarme exato SOMENTE quando permitido. Caso contrário,
            // preserva o agendamento aproximado da Aula 3 (sem bloquear o usuário).
            val exactScheduled = if (canUseExactAlarms(context)) {
                try {
                    alarm.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, reminder.scheduledAt, intent
                    )
                    true
                } catch (_: SecurityException) {
                    // A autorização pode ser retirada nas Configurações entre a consulta
                    // e o agendamento. Tentamos o modo aproximado como alternativa.
                    false
                }
            } else false
            if (!exactScheduled) {
                alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.scheduledAt, intent)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun cancel(context: Context, id: Int): Boolean {
        return try {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = pendingIntent(context, id, PendingIntent.FLAG_NO_CREATE)
            if (intent != null) {
                alarm.cancel(intent)
                intent.cancel()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    /** Reagendar somente os itens futuros: atualização do app ou reinício do Android. */
    fun restorePending(context: Context, deviceRebooted: Boolean = false) {
        createChannel(context)
        val now = System.currentTimeMillis()
        ReminderStore.list(context)
            .filter { it.status == ReminderStatus.SCHEDULED }
            .forEach { reminder ->
                if (reminder.scheduledAt > now) {
                    // Mesmo ID substitui o PendingIntent: sem agendamentos duplicados.
                    schedule(context, reminder)
                } else if (deviceRebooted) {
                    // Um alarme vencido durante o desligamento NÃO foi entregue.
                    ReminderStore.setStatus(context, reminder.id, ReminderStatus.MISSED)
                }
            }
    }

    private fun pendingIntent(context: Context, id: Int, flags: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, id)
        }
        return PendingIntent.getBroadcast(
            context, id, intent, flags or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

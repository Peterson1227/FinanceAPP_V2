package com.financeapp.nativeapp.reminders

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Histórico local separado das transações financeiras armazenadas no Room/Firebase. */
data class FinanceReminder(
    val id: Int,
    val title: String,
    val message: String,
    val scheduledAt: Long,
    val status: ReminderStatus = ReminderStatus.SCHEDULED,
    val lastEventAt: Long? = null
)

enum class ReminderStatus { SCHEDULED, DELIVERED, CANCELLED, BLOCKED, MISSED }

object ReminderStore {
    // Manter o mesmo nome e as chaves da Aula 3 preserva os lembretes já gravados.
    private const val PREFS = "financeapp_local_reminders"
    private const val REMINDERS = "reminders"
    private const val NEXT_ID = "next_id"

    @Synchronized
    fun createId(context: Context): Int? {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val id = prefs.getInt(NEXT_ID, 1)
        if (id <= 0 || id == Int.MAX_VALUE) return null
        return if (prefs.edit().putInt(NEXT_ID, id + 1).commit()) id else null
    }

    @Synchronized
    fun list(context: Context): List<FinanceReminder> = read(context)
        .sortedWith(compareBy<FinanceReminder> { it.status != ReminderStatus.SCHEDULED }
            .thenBy { if (it.status == ReminderStatus.SCHEDULED) it.scheduledAt else -it.scheduledAt })

    @Synchronized
    fun find(context: Context, id: Int): FinanceReminder? = read(context).firstOrNull { it.id == id }

    @Synchronized
    fun add(context: Context, reminder: FinanceReminder): Boolean {
        val current = read(context)
        if (current.any { it.id == reminder.id }) return false
        return write(context, current + reminder)
    }

    /** Atualização preservando o ID e os demais lembretes. Serve também para rollback. */
    @Synchronized
    fun replace(context: Context, reminder: FinanceReminder): Boolean {
        val current = read(context)
        if (current.none { it.id == reminder.id }) return false
        return write(context, current.map { if (it.id == reminder.id) reminder else it })
    }

    /** Exclusão usada somente para reverter uma criação malsucedida, nunca no histórico. */
    @Synchronized
    fun rollbackNew(context: Context, id: Int): Boolean {
        val current = read(context)
        return write(context, current.filterNot { it.id == id })
    }

    /** Exclusão definitiva solicitada pelo usuário, distinta do cancelamento com histórico.
     * Removemos do armazenamento antes de invalidar o PendingIntent, para que um
     * receiver eventualmente disparado não encontre o item e não publique o alerta.
     */
    @Synchronized
    fun delete(context: Context, id: Int): Boolean {
        val current = read(context)
        if (current.none { it.id == id }) return false
        return write(context, current.filterNot { it.id == id })
    }

    @Synchronized
    fun setStatus(context: Context, id: Int, status: ReminderStatus): Boolean {
        val current = read(context)
        val entry = current.firstOrNull { it.id == id } ?: return false
        if (entry.status != ReminderStatus.SCHEDULED) return false
        return write(context, current.map {
            if (it.id == id) it.copy(status = status, lastEventAt = System.currentTimeMillis()) else it
        })
    }

    private fun read(context: Context): List<FinanceReminder> {
        val data = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(REMINDERS, "[]") ?: "[]"
        return try {
            val array = JSONArray(data)
            buildList {
                for (i in 0 until array.length()) {
                    val json = array.getJSONObject(i)
                    val status = try {
                        ReminderStatus.valueOf(json.getString("status"))
                    } catch (_: Exception) {
                        ReminderStatus.CANCELLED
                    }
                    add(FinanceReminder(
                        id = json.getInt("id"),
                        title = json.getString("title"),
                        message = json.getString("message"),
                        scheduledAt = json.getLong("scheduledAt"),
                        status = status,
                        // Os registros anteriores não possuem este campo, e continuam legíveis.
                        lastEventAt = if (json.isNull("lastEventAt")) null
                            else json.optLong("lastEventAt").takeIf { it > 0L }
                    ))
                }
            }
        } catch (_: Exception) {
            // Um histórico corrompido não pode impedir a abertura do app financeiro.
            emptyList()
        }
    }

    private fun write(context: Context, reminders: List<FinanceReminder>): Boolean {
        val array = JSONArray()
        for (reminder in reminders) {
            array.put(JSONObject().apply {
                put("id", reminder.id)
                put("title", reminder.title)
                put("message", reminder.message)
                put("scheduledAt", reminder.scheduledAt)
                put("status", reminder.status.name)
                put("lastEventAt", reminder.lastEventAt ?: JSONObject.NULL)
            })
        }
        return context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(REMINDERS, array.toString()).commit()
    }
}

package com.financeapp.nativeapp.presentation.reminders

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.financeapp.nativeapp.reminders.FinanceReminder
import com.financeapp.nativeapp.reminders.ReminderScheduler
import com.financeapp.nativeapp.reminders.ReminderStatus
import com.financeapp.nativeapp.reminders.ReminderStore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

private enum class HistoryFilter(val label: String) {
    ALL("Todos"), SCHEDULED("Agendados"), DELIVERED("Disparados"),
    CANCELLED("Cancelados"), ISSUES("Não enviados")
}

private fun defaultReminderTime(): Long = Calendar.getInstance().apply {
    add(Calendar.HOUR_OF_DAY, 1)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

@Composable
fun ReminderScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var reminders by remember { mutableStateOf(ReminderStore.list(context)) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var scheduledAt by rememberSaveable { mutableStateOf(defaultReminderTime()) }
    var editingId by rememberSaveable { mutableStateOf(-1) }
    var feedback by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(HistoryFilter.ALL.name) }
    var deletingId by rememberSaveable { mutableStateOf(-1) }
    var exactAlarmsAllowed by remember { mutableStateOf(ReminderScheduler.canUseExactAlarms(context)) }
    val brazilianLocale = remember { Locale("pt", "BR") }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", brazilianLocale) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", brazilianLocale) }
    val dateTimeFormat = remember { SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", brazilianLocale) }

    fun refresh() { reminders = ReminderStore.list(context) }
    fun resetForm() {
        editingId = -1
        title = ""
        message = ""
        scheduledAt = defaultReminderTime()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                exactAlarmsAllowed = ReminderScheduler.canUseExactAlarms(context)
                // Ao voltar da tela de acesso especial, migra avisos futuros para
                // alarme exato, se permitido. Não cria lembretes duplicados.
                ReminderScheduler.restorePending(context)
                refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        feedback = if (granted) {
            "Permissão concedida. Toque novamente em Salvar/Agendar para confirmar."
        } else {
            "Notificações negadas. Ative-as nas configurações do FinanceAPP."
        }
    }

    fun submitReminder() {
        val cleanTitle = title.trim()
        val cleanMessage = message.trim()
        when {
            cleanTitle.isEmpty() || cleanMessage.isEmpty() ->
                feedback = "Preencha título e mensagem."
            scheduledAt <= System.currentTimeMillis() ->
                feedback = "Escolha uma data e hora futuras para agendar o aviso."
            !ReminderScheduler.canNotify(context) -> {
                feedback = "Ative as notificações do FinanceAPP nas configurações do Android."
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED
                ) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            editingId < 0 -> {
                val id = ReminderStore.createId(context)
                if (id == null) {
                    feedback = "Não foi possível gerar um identificador para o lembrete."
                    return
                }
                val reminder = FinanceReminder(id, cleanTitle, cleanMessage, scheduledAt)
                if (!ReminderStore.add(context, reminder)) {
                    feedback = "Não foi possível salvar o lembrete."
                    return
                }
                if (ReminderScheduler.schedule(context, reminder)) {
                    refresh()
                    resetForm()
                    feedback = "Lembrete agendado para notificações do Android (horário aproximado)."
                } else {
                    ReminderStore.rollbackNew(context, id)
                    refresh()
                    feedback = "Não foi possível agendar. Nenhum novo lembrete foi salvo."
                }
            }
            else -> {
                val previous = ReminderStore.find(context, editingId)
                if (previous == null) {
                    feedback = "O lembrete não foi encontrado."
                    resetForm()
                    refresh()
                    return
                }
                // Editar um item Disparado/Cancelado faz um NOVO agendamento para o mesmo ID;
                // nos outros itens históricos permanece o status até o usuário optar por editar.
                val updated = previous.copy(
                    title = cleanTitle,
                    message = cleanMessage,
                    scheduledAt = scheduledAt,
                    status = ReminderStatus.SCHEDULED,
                    lastEventAt = null
                )
                if (!ReminderStore.replace(context, updated)) {
                    feedback = "Não foi possível salvar a edição."
                    return
                }
                if (ReminderScheduler.schedule(context, updated)) {
                    refresh()
                    resetForm()
                    feedback = "Lembrete atualizado e agendado novamente."
                } else {
                    // Restaura os dados antigos caso o Android recuse o reagendamento.
                    ReminderStore.replace(context, previous)
                    if (previous.status == ReminderStatus.SCHEDULED &&
                        previous.scheduledAt > System.currentTimeMillis()
                    ) ReminderScheduler.schedule(context, previous)
                    refresh()
                    feedback = "Falha ao reagendar. Os dados anteriores foram mantidos."
                }
            }
        }
    }

    val reminderToDelete = reminders.firstOrNull { it.id == deletingId }
    if (reminderToDelete != null) {
        AlertDialog(
            onDismissRequest = { deletingId = -1 },
            title = { Text("Excluir lembrete?") },
            text = {
                Text("\"${reminderToDelete.title}\" será excluído definitivamente, " +
                    "inclusive do histórico. Esta ação não pode ser desfeita.")
            },
            confirmButton = {
                TextButton(onClick = {
                    val reminderId = reminderToDelete.id
                    if (ReminderStore.delete(context, reminderId)) {
                        // Não haverá notificação se o broadcast ocorrer durante a exclusão,
                        // pois o receiver consulta o ReminderStore antes de publicar.
                        ReminderScheduler.cancel(context, reminderId)
                        NotificationManagerCompat.from(context).cancel(reminderId)
                        if (editingId == reminderId) resetForm()
                        feedback = "Lembrete excluído definitivamente."
                    } else {
                        feedback = "Falha ao excluir. Tente novamente."
                    }
                    deletingId = -1
                    refresh()
                }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingId = -1 }) { Text("Voltar") }
            }
        )
    }

    val selectedFilter = HistoryFilter.entries.firstOrNull { it.name == filter } ?: HistoryFilter.ALL
    if (showSettings) {
        ReminderNotificationSettingsScreen(
            exactAlarmsAllowed = exactAlarmsAllowed,
            onBack = { showSettings = false }
        )
        return
    }
    val visibleReminders = reminders.filter {
        when (selectedFilter) {
            HistoryFilter.ALL -> true
            HistoryFilter.SCHEDULED -> it.status == ReminderStatus.SCHEDULED
            HistoryFilter.DELIVERED -> it.status == ReminderStatus.DELIVERED
            HistoryFilter.CANCELLED -> it.status == ReminderStatus.CANCELLED
            HistoryFilter.ISSUES -> it.status == ReminderStatus.BLOCKED || it.status == ReminderStatus.MISSED
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Lembretes financeiros", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Configurações")
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (editingId >= 0) "Editar lembrete" else "Novo lembrete",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (editingId >= 0) {
                        Text("Ao salvar, o lembrete será agendado novamente com a nova data e hora.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { if (it.length <= 80) title = it },
                        label = { Text("Título (ex.: Conta de energia)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { if (it.length <= 180) message = it },
                        label = { Text("Mensagem (ex.: Pagar até o vencimento)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = scheduledAt }
                                DatePickerDialog(context, { _, year, month, day ->
                                    val updated = Calendar.getInstance().apply {
                                        timeInMillis = scheduledAt
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, day)
                                    }
                                    scheduledAt = updated.timeInMillis
                                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                            }
                        ) { Text(dateFormat.format(scheduledAt)) }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = scheduledAt }
                                TimePickerDialog(context, { _, hour, minute ->
                                    val updated = Calendar.getInstance().apply {
                                        timeInMillis = scheduledAt
                                        set(Calendar.HOUR_OF_DAY, hour)
                                        set(Calendar.MINUTE, minute)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    scheduledAt = updated.timeInMillis
                                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                            }
                        ) { Text(timeFormat.format(scheduledAt)) }
                    }
                    Button(onClick = ::submitReminder, modifier = Modifier.fillMaxWidth()) {
                        Text(if (editingId >= 0) "Salvar alterações e reagendar" else "Agendar lembrete")
                    }
                    if (editingId >= 0) {
                        OutlinedButton(onClick = {
                            resetForm()
                            feedback = "Edição descartada."
                        }, modifier = Modifier.fillMaxWidth()) { Text("Voltar para novo lembrete") }
                    }
                    if (feedback.isNotEmpty()) {
                        Text(feedback, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
        item {
            Text("Histórico de lembretes (${reminders.size})", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryFilter.entries.forEach { option ->
                    val count = reminders.count {
                        when (option) {
                            HistoryFilter.ALL -> true
                            HistoryFilter.SCHEDULED -> it.status == ReminderStatus.SCHEDULED
                            HistoryFilter.DELIVERED -> it.status == ReminderStatus.DELIVERED
                            HistoryFilter.CANCELLED -> it.status == ReminderStatus.CANCELLED
                            HistoryFilter.ISSUES -> it.status == ReminderStatus.BLOCKED || it.status == ReminderStatus.MISSED
                        }
                    }
                    FilterChip(
                        selected = selectedFilter == option,
                        onClick = { filter = option.name },
                        label = { Text("${option.label} ($count)") }
                    )
                }
            }
        }
        if (visibleReminders.isEmpty()) {
            item {
                Text(
                    if (reminders.isEmpty()) "Nenhum lembrete criado."
                    else "Nenhum lembrete nesta categoria.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(visibleReminders, key = { it.id }) { reminder ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                        Text(reminder.message, style = MaterialTheme.typography.bodyMedium)
                        Text("Previsto: ${dateTimeFormat.format(reminder.scheduledAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(when (reminder.status) {
                            ReminderStatus.SCHEDULED -> "Status: Agendado"
                            ReminderStatus.DELIVERED -> "Status: Disparado"
                            ReminderStatus.CANCELLED -> "Status: Cancelado"
                            ReminderStatus.BLOCKED -> "Status: Não notificado (verifique a permissão)"
                            ReminderStatus.MISSED -> "Status: Não disparou (celular desligado no horário)"
                        }, style = MaterialTheme.typography.labelMedium)
                        reminder.lastEventAt?.let { eventAt ->
                            Text("Última alteração de status: ${dateTimeFormat.format(eventAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = {
                                editingId = reminder.id
                                title = reminder.title
                                message = reminder.message
                                // Um aviso já vencido precisa de uma nova data futura.
                                scheduledAt = if (reminder.scheduledAt > System.currentTimeMillis())
                                    reminder.scheduledAt else defaultReminderTime()
                                feedback = "Editando '${reminder.title}'."
                                coroutineScope.launch { lazyListState.animateScrollToItem(1) }
                            }) { Text(if (reminder.status == ReminderStatus.SCHEDULED) "Editar" else "Editar / Reagendar") }
                            if (reminder.status == ReminderStatus.SCHEDULED) {
                                TextButton(onClick = {
                                    // Primeiro preservamos o cancelamento no armazenamento:
                                    // se o alarme chegar neste intervalo, o receiver não publicará.
                                    val cancelled = ReminderStore.setStatus(
                                        context, reminder.id, ReminderStatus.CANCELLED
                                    )
                                    if (cancelled) {
                                        ReminderScheduler.cancel(context, reminder.id)
                                        feedback = "Lembrete cancelado; histórico preservado."
                                    } else feedback = "Falha ao cancelar. Tente novamente."
                                    if (editingId == reminder.id) resetForm()
                                    refresh()
                                }) { Text("Cancelar") }
                            }
                            TextButton(onClick = { deletingId = reminder.id }) {
                                Text("Excluir", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ReminderNotificationSettingsScreen(
    exactAlarmsAllowed: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
                Column {
                    Text(
                        "Configurações",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("Notificações", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Notificações com o app fechado", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Para receber avisos após sair do FinanceAPP, ative as notificações e permita a execução em segundo plano nas configurações do Android.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Text(
                            if (exactAlarmsAllowed) "Alarmes precisos: permitidos."
                            else "Alarmes precisos: não autorizados (os avisos continuam aproximados).",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (!exactAlarmsAllowed) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        context.startActivity(
                                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                        )
                                    } catch (_: Exception) {
                                        context.startActivity(
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Permitir alarmes precisos (opcional)") }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Abrir configurações de notificações e bateria") }
                    Text(
                        "Fechar pela tela de recentes é diferente de usar 'Forçar parada'. Após 'Forçar parada', o Android impede o envio até abrir o app novamente. Alguns fabricantes também restringem o app ao removê-lo dos recentes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

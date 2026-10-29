package com.financeapp.nativeapp.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SyncManager(
    private val context: Context,
    private val networkObserver: NetworkObserver
) {
    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Observa mudanças na rede e tenta sincronizar imediatamente se ficar online
        networkObserver.observe()
            .onEach { status ->
                if (status == NetworkObserver.Status.Available) {
                    enqueueSync()
                }
            }
            .launchIn(scope)
    }

    /**
     * Enfileira uma requisição de sincronização no WorkManager.
     * O WorkManager garante que o SyncWorker só será executado quando houver internet.
     */
    fun enqueueSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        // KEEP: Se já houver uma sincronização na fila, mantém a atual e ignora a nova
        workManager.enqueueUniqueWork(
            SyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }
}

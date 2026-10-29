package com.financeapp.nativeapp.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financeapp.nativeapp.data.local.AppDatabase
import com.financeapp.nativeapp.data.remote.FirebaseRemoteDataSourceImpl
import com.financeapp.nativeapp.data.repository.TransactionRepositoryImpl

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Inicialização manual simplificada para evitar complexidade excessiva de DI (Hilt) nesta etapa.
        val database = AppDatabase.getDatabase(applicationContext)
        val remoteSource = FirebaseRemoteDataSourceImpl()
        val repository = TransactionRepositoryImpl(database.transactionDao(), remoteSource)

        return try {
            val syncResult = repository.syncPending()
            if (syncResult.isSuccess) {
                // Sincronização concluída com sucesso ou nada pendente
                Result.success()
            } else {
                // Falha ao tentar sincronizar (ex: servidor instável)
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "FinanceAppSyncWorker"
    }
}

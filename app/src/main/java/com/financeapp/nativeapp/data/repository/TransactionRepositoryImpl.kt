package com.financeapp.nativeapp.data.repository

import com.financeapp.nativeapp.data.local.dao.TransactionDao
import com.financeapp.nativeapp.data.local.entity.toDomain
import com.financeapp.nativeapp.data.local.entity.toEntity
import com.financeapp.nativeapp.data.remote.RemoteTransactionDataSource
import com.financeapp.nativeapp.data.remote.dto.toDto
import com.financeapp.nativeapp.domain.model.SyncStatus
import com.financeapp.nativeapp.domain.model.Transaction
import com.financeapp.nativeapp.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TransactionRepositoryImpl(
    private val dao: TransactionDao,
    private val remoteSource: RemoteTransactionDataSource
) : TransactionRepository {

    override fun getTransactions(): Flow<List<Transaction>> {
        return dao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Regra Offline-First: Salvar imediatamente no banco de dados local com status PENDENTE
            val pendingTransaction = transaction.copy(syncStatus = SyncStatus.PENDENTE)
            dao.insertOrUpdate(pendingTransaction.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pendingTransaction = transaction.copy(
                syncStatus = SyncStatus.PENDENTE,
                lastModifiedAt = System.currentTimeMillis()
            )
            dao.insertOrUpdate(pendingTransaction.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.deleteById(id)
            // Em uma implementação avançada, manteríamos uma tabela de 'Deletes Pendentes' 
            // para enviar ao servidor. Para simplificação, removemos apenas do banco local.
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncPending(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val pendings = dao.getPendingTransactions(SyncStatus.PENDENTE)
            if (pendings.isEmpty()) return@withContext Result.success(0)

            var successCount = 0
            for (entity in pendings) {
                // Marca como sincronizando
                dao.updateSyncStatus(entity.id, SyncStatus.SINCRONIZANDO)

                // Tenta enviar para o servidor remoto
                val result = remoteSource.sendTransaction(entity.toDomain().toDto())
                
                if (result.isSuccess) {
                    dao.updateSyncStatus(entity.id, SyncStatus.SINCRONIZADO)
                    successCount++
                } else {
                    dao.updateSyncStatus(entity.id, SyncStatus.PENDENTE)
                }
            }
            Result.success(successCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPendingCount(): Flow<Int> {
        // Implementação simplificada usando Flow
        return dao.getAllTransactions().map { list ->
            list.count { it.syncStatus == SyncStatus.PENDENTE }
        }
    }
}

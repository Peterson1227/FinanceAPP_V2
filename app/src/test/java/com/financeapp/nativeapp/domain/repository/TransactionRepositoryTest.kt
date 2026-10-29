package com.financeapp.nativeapp.domain.repository

import com.financeapp.nativeapp.domain.model.SyncStatus
import com.financeapp.nativeapp.domain.model.Transaction
import com.financeapp.nativeapp.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Fake Repository para simular o comportamento Offline-First em Testes Unitários.
 * Em um cenário completo usaríamos MockK ou Mockito para mockar o DAO real.
 */
class FakeTransactionRepository : TransactionRepository {
    val localDb = mutableListOf<Transaction>()

    override fun getTransactions(): Flow<List<Transaction>> = flowOf(localDb)

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        // Regra testada: Salvar localmente garante status PENDENTE
        val pendingTx = transaction.copy(syncStatus = SyncStatus.PENDENTE)
        localDb.add(pendingTx)
        return Result.success(Unit)
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        val index = localDb.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            localDb[index] = transaction.copy(syncStatus = SyncStatus.PENDENTE)
            return Result.success(Unit)
        }
        return Result.failure(Exception("Not found"))
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> {
        localDb.removeAll { it.id == id }
        return Result.success(Unit)
    }

    override suspend fun syncPending(): Result<Int> {
        var count = 0
        localDb.forEachIndexed { index, transaction ->
            if (transaction.syncStatus == SyncStatus.PENDENTE) {
                localDb[index] = transaction.copy(syncStatus = SyncStatus.SINCRONIZADO)
                count++
            }
        }
        return Result.success(count)
    }

    override fun getPendingCount(): Flow<Int> = flowOf(localDb.count { it.syncStatus == SyncStatus.PENDENTE })
}

class TransactionRepositoryTest {

    @Test
    fun `Ao adicionar transacao, status DEVE ser PENDENTE`() = runBlocking {
        // Arrange
        val fakeRepo = FakeTransactionRepository()
        val novaTransacao = Transaction(
            id = UUID.randomUUID().toString(),
            userId = "user_1",
            title = "Teste de Software",
            amount = 150.0,
            type = TransactionType.DESPESA,
            category = "Educação",
            date = System.currentTimeMillis()
        )

        // Act
        val resultado = fakeRepo.addTransaction(novaTransacao)

        // Assert
        assertTrue(resultado.isSuccess)
        val transacaoSalva = fakeRepo.localDb.first()
        assertEquals(SyncStatus.PENDENTE, transacaoSalva.syncStatus)
    }

    @Test
    fun `Ao sincronizar pendentes, status DEVE atualizar para SINCRONIZADO`() = runBlocking {
        // Arrange
        val fakeRepo = FakeTransactionRepository()
        val txPendente = Transaction(
            id = UUID.randomUUID().toString(),
            userId = "user_1",
            title = "Teste Sync",
            amount = 50.0,
            type = TransactionType.RECEITA,
            category = "Vendas",
            date = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDENTE
        )
        fakeRepo.localDb.add(txPendente)

        // Act
        val qtdSincronizados = fakeRepo.syncPending().getOrNull()

        // Assert
        assertEquals(1, qtdSincronizados)
        assertEquals(SyncStatus.SINCRONIZADO, fakeRepo.localDb.first().syncStatus)
    }
}

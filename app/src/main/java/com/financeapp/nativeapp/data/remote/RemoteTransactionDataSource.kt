package com.financeapp.nativeapp.data.remote

import com.financeapp.nativeapp.data.remote.dto.TransactionDto
import kotlinx.coroutines.delay
import kotlin.random.Random

interface RemoteTransactionDataSource {
    suspend fun sendTransaction(dto: TransactionDto): Result<Unit>
}

/**
 * Simulação de um backend remoto (Mock API).
 * Em um cenário real de produção, substituiríamos esta implementação
 * por chamadas ao Firebase Firestore ou uma API REST via Retrofit.
 */
class MockRemoteDataSourceImpl : RemoteTransactionDataSource {

    override suspend fun sendTransaction(dto: TransactionDto): Result<Unit> {
        // Simula latência de rede entre 500ms e 1500ms
        delay(Random.nextLong(500, 1500))

        // Em nosso mock, consideramos sempre sucesso (200 OK) para focar na
        // mecânica do Modo Avião (quando está offline, a chamada nem chega aqui,
        // ela falharia antes ou nem seria agendada pelo WorkManager/SyncManager).
        // Se precisarmos testar resiliência com API instável (500 Error),
        // poderíamos lançar exceções esporádicas.
        
        return Result.success(Unit)
    }
}

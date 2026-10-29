package com.financeapp.nativeapp.domain.repository

import com.financeapp.nativeapp.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    
    /**
     * Retorna todas as transações de forma reativa, 
     * sempre observando o banco de dados local (Single Source of Truth).
     */
    fun getTransactions(): Flow<List<Transaction>>
    
    /**
     * Adiciona uma nova transação. A persistência inicial deve ser SEMPRE local.
     */
    suspend fun addTransaction(transaction: Transaction): Result<Unit>
    
    /**
     * Atualiza uma transação existente.
     */
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    
    /**
     * Remove uma transação.
     */
    suspend fun deleteTransaction(id: String): Result<Unit>
    
    /**
     * Dispara o processo de sincronização para todos os registros com status PENDENTE.
     * @return Result com a quantidade de itens sincronizados com sucesso.
     */
    suspend fun syncPending(): Result<Int>
    
    /**
     * Retorna de forma reativa a quantidade de transações locais aguardando sincronização.
     */
    fun getPendingCount(): Flow<Int>
}

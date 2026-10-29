package com.financeapp.nativeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.financeapp.nativeapp.data.local.entity.TransactionEntity
import com.financeapp.nativeapp.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE syncStatus = :status ORDER BY lastModifiedAt ASC")
    suspend fun getPendingTransactions(status: SyncStatus = SyncStatus.PENDENTE): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: TransactionEntity)

    @Query("UPDATE transactions SET syncStatus = :status, lastModifiedAt = :timestamp WHERE id = :id")
    suspend fun updateSyncStatus(
        id: String,
        status: SyncStatus,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)
}

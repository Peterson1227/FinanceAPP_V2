package com.financeapp.nativeapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.financeapp.nativeapp.domain.model.SyncStatus
import com.financeapp.nativeapp.domain.model.Transaction
import com.financeapp.nativeapp.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["syncStatus"])]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: Long,
    val location: String? = null,
    val receiptLocalUri: String? = null,
    val receiptRemoteUrl: String? = null,
    val syncStatus: SyncStatus,
    val lastModifiedAt: Long
)

// Extension functions for mapping between Entity and Domain model
fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        type = type,
        category = category,
        date = date,
        location = location,
        receiptLocalUri = receiptLocalUri,
        receiptRemoteUrl = receiptRemoteUrl,
        syncStatus = syncStatus,
        lastModifiedAt = lastModifiedAt
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        type = type,
        category = category,
        date = date,
        location = location,
        receiptLocalUri = receiptLocalUri,
        receiptRemoteUrl = receiptRemoteUrl,
        syncStatus = syncStatus,
        lastModifiedAt = lastModifiedAt
    )
}

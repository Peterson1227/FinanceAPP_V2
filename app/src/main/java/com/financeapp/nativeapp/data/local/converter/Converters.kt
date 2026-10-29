package com.financeapp.nativeapp.data.local.converter

import androidx.room.TypeConverter
import com.financeapp.nativeapp.domain.model.SyncStatus
import com.financeapp.nativeapp.domain.model.TransactionType

class Converters {

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String {
        return value.name
    }

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return try {
            SyncStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            SyncStatus.PENDENTE // Fallback seguro
        }
    }

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return try {
            TransactionType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            TransactionType.DESPESA // Fallback seguro
        }
    }
}

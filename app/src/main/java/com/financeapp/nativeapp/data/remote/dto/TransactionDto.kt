package com.financeapp.nativeapp.data.remote.dto

import com.financeapp.nativeapp.domain.model.Transaction
import com.financeapp.nativeapp.domain.model.TransactionType

data class TransactionDto(
    val id: String,
    val userId: String,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: Long,
    val location: String? = null,
    val receiptRemoteUrl: String? = null
)

fun Transaction.toDto(): TransactionDto {
    return TransactionDto(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        type = type.name,
        category = category,
        date = date,
        location = location,
        receiptRemoteUrl = receiptRemoteUrl
    )
}

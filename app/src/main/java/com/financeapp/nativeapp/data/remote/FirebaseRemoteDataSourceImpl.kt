package com.financeapp.nativeapp.data.remote

import com.financeapp.nativeapp.data.remote.dto.TransactionDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseRemoteDataSourceImpl : RemoteTransactionDataSource {

    private val db: FirebaseFirestore = Firebase.firestore
    
    override suspend fun sendTransaction(dto: TransactionDto): Result<Unit> {
        return try {
            val transactionMap = hashMapOf(
                "id" to dto.id,
                "userId" to dto.userId,
                "title" to dto.title,
                "amount" to dto.amount,
                "type" to dto.type,
                "category" to dto.category,
                "date" to dto.date,
                "location" to dto.location,
                "receiptRemoteUrl" to dto.receiptRemoteUrl
            )
            
            // Usamos .set com o ID da transação para garantir idempotência,
            // ou seja, se for sincronizado mais de uma vez não vai duplicar no banco
            db.collection("transactions").document(dto.id).set(transactionMap).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

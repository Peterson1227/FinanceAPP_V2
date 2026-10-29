package com.financeapp.nativeapp.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.financeapp.nativeapp.domain.model.SyncStatus
import com.financeapp.nativeapp.domain.model.Transaction
import com.financeapp.nativeapp.domain.model.TransactionType
import com.financeapp.nativeapp.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AddTransactionUiState(
    val title: String = "",
    val amount: String = "",
    val type: TransactionType = TransactionType.DESPESA,
    val category: String = "",
    val date: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
) {
    val dateFormatted: String
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(date))
}

class AddViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, errorMessage = null) }
    }

    fun updateAmount(amount: String) {
        // Validação básica para aceitar apenas números e ponto
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(amount = amount, errorMessage = null) }
        }
    }

    fun updateType(type: TransactionType) {
        // Ao trocar tipo, reseta a categoria (categorias são diferentes por tipo)
        _uiState.update { it.copy(type = type, category = "") }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category, errorMessage = null) }
    }

    fun updateDate(dateMillis: Long) {
        _uiState.update { it.copy(date = dateMillis) }
    }

    fun saveTransaction() {
        val currentState = _uiState.value
        val amountValue = currentState.amount.toDoubleOrNull()

        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Informe a descrição da transação.") }
            return
        }
        if (amountValue == null || amountValue <= 0) {
            _uiState.update { it.copy(errorMessage = "Informe um valor válido maior que zero.") }
            return
        }
        if (currentState.category.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Selecione uma categoria.") }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                userId = "user_default_123", // Num app real, viria da autenticação
                title = currentState.title.trim(),
                amount = amountValue,
                type = currentState.type,
                category = currentState.category.trim(),
                date = currentState.date,
                syncStatus = SyncStatus.PENDENTE
            )

            val result = repository.addTransaction(transaction)
            
            if (result.isSuccess) {
                // A gravação local com status PENDENTE foi um sucesso. O WorkManager fará o resto.
                _uiState.update { it.copy(isSaving = false, isSuccess = true) }
            } else {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Erro ao salvar transação. Tente novamente.") }
            }
        }
    }

    fun resetSuccessState() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    class Factory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddViewModel::class.java)) {
                return AddViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

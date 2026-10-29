package com.financeapp.nativeapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.financeapp.nativeapp.domain.model.Transaction
import com.financeapp.nativeapp.domain.model.TransactionType
import com.financeapp.nativeapp.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Period(val label: String) {
    TODAY("Hoje"),
    WEEK("Semana"),
    MONTH("Mês"),
    ALL("Todos")
}

class HomeViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    // ═══════════════════════════════════════════════════════════
    // Estado do filtro de período
    // ═══════════════════════════════════════════════════════════
    
    private val _selectedPeriod = MutableStateFlow(Period.ALL)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    fun selectPeriod(period: Period) {
        _selectedPeriod.value = period
    }

    // ═══════════════════════════════════════════════════════════
    // Todas as transações do banco local de forma reativa (StateFlow)
    // ═══════════════════════════════════════════════════════════
    
    private val allTransactions: StateFlow<List<Transaction>> = repository.getTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ═══════════════════════════════════════════════════════════
    // Transações filtradas pelo período selecionado
    // ═══════════════════════════════════════════════════════════

    val transactions: StateFlow<List<Transaction>> = combine(
        allTransactions,
        _selectedPeriod
    ) { transactions, period ->
        filterByPeriod(transactions, period)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ═══════════════════════════════════════════════════════════
    // Cálculos financeiros (sempre sobre TODAS as transações)
    // ═══════════════════════════════════════════════════════════

    val totalReceitas: StateFlow<Double> = allTransactions.map { list ->
        list.filter { it.type == TransactionType.RECEITA }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalDespesas: StateFlow<Double> = allTransactions.map { list ->
        list.filter { it.type == TransactionType.DESPESA }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val saldo: StateFlow<Double> = combine(totalReceitas, totalDespesas) { r, d ->
        r - d
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ═══════════════════════════════════════════════════════════
    // Contagem de pendências de sincronização
    // ═══════════════════════════════════════════════════════════

    val pendingSyncCount: StateFlow<Int> = repository.getPendingCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // ═══════════════════════════════════════════════════════════
    // Transação selecionada para edição/exclusão
    // ═══════════════════════════════════════════════════════════

    private val _selectedTransaction = MutableStateFlow<Transaction?>(null)
    val selectedTransaction: StateFlow<Transaction?> = _selectedTransaction.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    fun onTransactionClick(transaction: Transaction) {
        _selectedTransaction.value = transaction
        _showEditDialog.value = true
    }

    fun onTransactionLongClick(transaction: Transaction) {
        _selectedTransaction.value = transaction
        _showDeleteDialog.value = true
    }

    fun dismissEditDialog() {
        _showEditDialog.value = false
        _selectedTransaction.value = null
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = false
        _selectedTransaction.value = null
    }

    // ═══════════════════════════════════════════════════════════
    // Operações CRUD
    // ═══════════════════════════════════════════════════════════

    fun updateTransaction(
        title: String,
        amount: Double,
        category: String,
        type: TransactionType
    ) {
        val transaction = _selectedTransaction.value ?: return
        val updatedTransaction = transaction.copy(
            title = title,
            amount = amount,
            category = category,
            type = type
        )
        viewModelScope.launch {
            repository.updateTransaction(updatedTransaction)
            _showEditDialog.value = false
            _selectedTransaction.value = null
        }
    }

    fun deleteSelectedTransaction() {
        val transaction = _selectedTransaction.value ?: return
        viewModelScope.launch {
            repository.deleteTransaction(transaction.id)
            _showDeleteDialog.value = false
            _selectedTransaction.value = null
        }
    }

    fun forceSync() {
        viewModelScope.launch {
            repository.syncPending()
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Filtragem por período
    // ═══════════════════════════════════════════════════════════

    private fun filterByPeriod(transactions: List<Transaction>, period: Period): List<Transaction> {
        if (period == Period.ALL) return transactions

        val now = System.currentTimeMillis()
        val cutoff = when (period) {
            Period.TODAY -> {
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            Period.WEEK -> now - (7L * 24 * 60 * 60 * 1000)
            Period.MONTH -> now - (30L * 24 * 60 * 60 * 1000)
            Period.ALL -> 0L
        }

        return transactions.filter { it.date >= cutoff }
    }

    // ═══════════════════════════════════════════════════════════
    // Factory para injeção manual
    // ═══════════════════════════════════════════════════════════

    class Factory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

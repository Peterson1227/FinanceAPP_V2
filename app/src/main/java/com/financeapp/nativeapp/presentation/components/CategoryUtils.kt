package com.financeapp.nativeapp.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Utilitário para mapeamento de categorias de transação para ícones e cores.
 * Inspirado no mapeamento do app híbrido (FinanceAppP2).
 */
data class CategoryInfo(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

object CategoryUtils {

    // ═══════════════════════════════════════════════════════════
    // Categorias de Despesa
    // ═══════════════════════════════════════════════════════════

    private val expenseCategories = listOf(
        CategoryInfo("Alimentação", Icons.Default.Restaurant, Color(0xFFFF7043)),
        CategoryInfo("Moradia", Icons.Default.Home, Color(0xFF5C6BC0)),
        CategoryInfo("Transporte", Icons.Default.DirectionsCar, Color(0xFF42A5F5)),
        CategoryInfo("Saúde", Icons.Default.LocalHospital, Color(0xFFEF5350)),
        CategoryInfo("Educação", Icons.Default.School, Color(0xFF7E57C2)),
        CategoryInfo("Lazer", Icons.Default.SportsEsports, Color(0xFFEC407A)),
        CategoryInfo("Vestuário", Icons.Default.Checkroom, Color(0xFFAB47BC)),
        CategoryInfo("Contas", Icons.Default.Receipt, Color(0xFF78909C)),
        CategoryInfo("Supermercado", Icons.Default.ShoppingCart, Color(0xFFFF8A65)),
        CategoryInfo("Assinaturas", Icons.Default.Subscriptions, Color(0xFF26C6DA)),
        CategoryInfo("Pet", Icons.Default.Pets, Color(0xFFFFCA28)),
        CategoryInfo("Viagens", Icons.Default.Flight, Color(0xFF29B6F6)),
        CategoryInfo("Presentes", Icons.Default.CardGiftcard, Color(0xFFE91E63)),
        CategoryInfo("Outros", Icons.Default.MoreHoriz, Color(0xFF90A4AE)),
    )

    // ═══════════════════════════════════════════════════════════
    // Categorias de Receita
    // ═══════════════════════════════════════════════════════════

    private val incomeCategories = listOf(
        CategoryInfo("Salário", Icons.Default.AccountBalance, Color(0xFF66BB6A)),
        CategoryInfo("Freelance", Icons.Default.Laptop, Color(0xFF26A69A)),
        CategoryInfo("Investimentos", Icons.Default.TrendingUp, Color(0xFF42A5F5)),
        CategoryInfo("Vendas", Icons.Default.Storefront, Color(0xFFFFA726)),
        CategoryInfo("Presente", Icons.Default.CardGiftcard, Color(0xFFE91E63)),
        CategoryInfo("Reembolso", Icons.Default.Replay, Color(0xFF78909C)),
        CategoryInfo("Outros", Icons.Default.MoreHoriz, Color(0xFF90A4AE)),
    )

    // ═══════════════════════════════════════════════════════════
    // Acesso Público
    // ═══════════════════════════════════════════════════════════

    fun getExpenseCategories(): List<CategoryInfo> = expenseCategories
    fun getIncomeCategories(): List<CategoryInfo> = incomeCategories

    fun getAllCategories(): List<CategoryInfo> = expenseCategories + incomeCategories

    /**
     * Busca as informações de uma categoria pelo nome.
     * Se não encontrada, retorna um fallback genérico.
     */
    fun getCategoryInfo(categoryName: String): CategoryInfo {
        return getAllCategories().find { 
            it.label.equals(categoryName, ignoreCase = true) 
        } ?: CategoryInfo(
            label = categoryName,
            icon = Icons.Default.Label,
            color = Color(0xFF78909C)
        )
    }

    /**
     * Retorna as categorias relevantes baseado no tipo de transação.
     */
    fun getCategoriesForType(isExpense: Boolean): List<CategoryInfo> {
        return if (isExpense) expenseCategories else incomeCategories
    }
}

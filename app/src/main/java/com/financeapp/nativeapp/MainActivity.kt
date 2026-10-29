package com.financeapp.nativeapp

import android.os.Bundle
import com.financeapp.nativeapp.presentation.security.SecureActivity
import com.financeapp.nativeapp.presentation.security.UnlockScreen
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.financeapp.nativeapp.data.local.AppDatabase
import com.financeapp.nativeapp.data.remote.FirebaseRemoteDataSourceImpl
import com.financeapp.nativeapp.data.repository.TransactionRepositoryImpl
import com.financeapp.nativeapp.presentation.add.AddTransactionScreen
import com.financeapp.nativeapp.presentation.add.AddViewModel
import com.financeapp.nativeapp.presentation.home.HomeScreen
import com.financeapp.nativeapp.presentation.home.HomeViewModel
import com.financeapp.nativeapp.presentation.profile.ProfileScreen
import com.financeapp.nativeapp.presentation.reminders.ReminderScreen
import com.financeapp.nativeapp.presentation.theme.*
import com.financeapp.nativeapp.sync.ConnectivityObserver
import com.financeapp.nativeapp.sync.NetworkObserver
import com.financeapp.nativeapp.sync.SyncManager

class MainActivity : SecureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configuração de Dependências Manuais (Service Locator Pattern)
        // Em um app de produção usaríamos Hilt (Dagger)
        val database = AppDatabase.getDatabase(this)
        val remoteSource = FirebaseRemoteDataSourceImpl()
        val repository = TransactionRepositoryImpl(database.transactionDao(), remoteSource)
        
        val networkObserver = ConnectivityObserver(this)
        
        // Inicializa o orquestrador que ouve a rede e agenda os workers
        SyncManager(this, networkObserver)

        setContent {
            FinanceAppTheme {
                val isOnline by networkObserver.observe().collectAsState(
                    initial = NetworkObserver.Status.Unavailable
                )

                if (!unlocked) {
                    UnlockScreen(message, pending, credentialAvailable,
                        onUnlock = ::authenticate, onCredential = ::authenticateCredential)
                } else FinanceAppNavigation(
                    repository = repository,
                    isOnline = isOnline == NetworkObserver.Status.Available
                )
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Extrato", Icons.Default.Home)
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)
    object Reminders : Screen("reminders", "Lembretes", Icons.Default.Notifications)
    object Add : Screen("add", "Nova Transação", Icons.Default.Add) // Não mostrado na NavBar
}

@Composable
fun FinanceAppNavigation(
    repository: TransactionRepositoryImpl,
    isOnline: Boolean
) {
    val navController = rememberNavController()
    
    // Instanciando ViewModels através das fábricas manuais
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(repository)
    )

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                contentColor = OnSurface,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                val items = listOf(Screen.Home, Screen.Reminders, Screen.Profile)
                
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.label
                            )
                        },
                        label = {
                            Text(
                                screen.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            indicatorColor = Primary.copy(alpha = 0.12f),
                            unselectedIconColor = OnSurfaceVariant,
                            unselectedTextColor = OnSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    isOnline = isOnline,
                    onNavigateToAdd = { navController.navigate(Screen.Add.route) }
                )
            }
            
            composable(Screen.Reminders.route) {
                ReminderScreen()
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = homeViewModel,
                    isOnline = isOnline
                )
            }
            
            composable(Screen.Add.route) {
                val addViewModel: AddViewModel = viewModel(
                    factory = AddViewModel.Factory(repository)
                )
                AddTransactionScreen(
                    viewModel = addViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

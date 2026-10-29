package com.financeapp.nativeapp

import android.app.Application
import com.financeapp.nativeapp.reminders.ReminderScheduler

class FinanceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.createChannel(this)
        ReminderScheduler.restorePending(this)
        // Ponto de entrada da aplicação.
        // A inicialização do banco de dados (Room), WorkManager e o monitor de conectividade
        // será integrada aqui nas próximas etapas (Injeção de dependências ou inicialização manual).
    }
}

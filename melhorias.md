# ✈️ Desafio: Modo Avião — Offline First + Sincronização de Dados
> **Disciplina:** Laboratório de Desenvolvimento de Aplicativos Nativos · Engenharia de Software  
> **Docente:** Prof. Lucas Hermenegildo do Nascimento  
> **Contexto:** Migração e Implementação no Aplicativo Nativo Android de Gestão Financeira (`FinanceAppNative`)

---

## 🎯 Objetivo do Desafio
Projetar e implementar uma arquitetura **Offline First com Sincronização Automática** no aplicativo nativo Android. O aplicativo não pode depender de conexão ativa à internet para que as ações principais (ex.: cadastro e gerenciamento de transações financeiras) sejam realizadas com sucesso pelo usuário.

> ⚠️ **Regra Fundamental de Avaliação:**  
> Mudanças exclusivamente visuais (trocar cores, fontes ou reorganizar componentes sem implementar a lógica de persistência e sincronização) **não pontuam**. O que precisa funcionar de verdade:
> 1. Operação normal sem internet (Modo Avião ligado).
> 2. Persistência real em disco local (sobrevive ao encerramento/kill do app).
> 3. Sincronização automática em segundo plano ao reconectar (Modo Avião desligado).
> 4. Comunicação visual clara e em tempo real para o usuário sobre cada estado da conexão e dos dados.

---

## 🔄 Fluxo Arquitetural Offline First

```
[ Usuário cria/edita Transação ]
                │
                ▼
      ┌──────────────────┐
      │ Salva no SQLite/ │  ──► Status: "PENDENTE" (ou "SINCRONIZANDO")
      │  Room Database   │  ──► Sobrevive a fechar/reiniciar o app
      └──────────────────┘
                │
     Verifica Conectividade
        (ConnectivityManager)
        ├──[ Sem Internet ]──► Mantém "PENDENTE" na fila + Alerta visual Offline (🔴)
        │
        └──[ Com Internet ]──► Dispara Sincronização Automática (🔄)
                                      │
                                      ▼
                             ┌──────────────────┐
                             │ Backend / API /  │
                             │ Firestore Server │
                             └──────────────────┘
                                      │
                   ┌──────────────────┴──────────────────┐
                   ▼                                     ▼
             [ Sucesso 200 ]                      [ Falha / Timeout ]
                   │                                     │
                   ▼                                     ▼
        Atualiza no Room para:                 Permanece "PENDENTE"
        Status: "SINCRONIZADO" (🟢)            Agenda retentativa (WorkManager)
        Feedback: "Concluído" (✅)             Feedback: "Offline / Aguardando conexão"
```

---

## 📋 Requisitos e Estados de Sincronização

### 1. Estados do Dado no Banco Local
Cada transação/registro deve conter explicitamente o campo de status de sincronização (`syncStatus`):
* 🔴 **`PENDENTE`**: O dado foi gravado no banco de dados local do dispositivo, mas ainda não foi enviado/confirmado no servidor remoto.
* 🔄 **`SINCRONIZANDO`**: O aplicativo detectou rede e está atualmente no processo de envio para a API/servidor.
* 🟢 **`SINCRONIZADO`**: O servidor retornou confirmação de persistência com sucesso. Nenhuma ação pendente.

### 2. Estados da Conexão e Comunicação ao Usuário
A UI deve refletir o estado de forma inequívoca através de um componente visual (Banner, Chip ou Barra de Status):
* 🔴 **Offline:** `"Você está sem conexão. Suas alterações foram salvas localmente e serão sincronizadas quando a internet retornar."`
* 🔄 **Sincronizando:** `"Sincronizando dados… X alterações pendentes."`
* 🟢 **Conectado:** `"Conexão ativa — Pronto para sincronizar."`
* ✅ **Concluído:** `"Dados sincronizados com sucesso — Última sincronização: HH:mm."`

---

## 🛠️ Especificação Técnica para Android Nativo (Kotlin)

Ao converter do React Native (`FinanceAppP2`) para Android Nativo, as tecnologias equivalentes e recomendadas são:

| Conceito do Desafio | Referência Híbrida (React Native) | **Implementação Nativa Android (Kotlin)** |
|---|---|---|
| **Banco de Dados Local** | `AsyncStorage` / SQLite | **Room Database** (`@Entity`, `@Dao`, `@Database`) |
| **Fila de Sincronização** | Array em JSON (`@dados`) | Tabela Room com coluna `sync_status` indexada |
| **Monitor de Conectividade** | `@react-native-community/netinfo` | **`ConnectivityManager` + `NetworkCallback`** emitindo `StateFlow<NetworkStatus>` |
| **Processamento em Background** | Event listener em memória | **`WorkManager`** (`CoroutineWorker`) com `NetworkType.CONNECTED` |
| **Arquitetura de Estado** | Zustand Store | **ViewModel + StateFlow / LiveData** (MVVM) |
| **Interface do Usuário** | React Native Paper / JSX | **Jetpack Compose** (ou Material XML Views) |

---

## 🧱 Componentes a Serem Implementados no Nativo

### 1. Entidade e Modelo de Dados (Room)
```kotlin
// Exemplo arquitetural do modelo Room
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val amount: Double,
    val type: String, // "receita" | "despesa"
    val category: String,
    val date: Long, // timestamp
    val location: String? = null,
    val receiptLocalUri: String? = null,
    val receiptRemoteUrl: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDENTE, // PENDENTE, SINCRONIZANDO, SINCRONIZADO
    val lastModifiedAt: Long = System.currentTimeMillis()
)

enum class SyncStatus {
    PENDENTE,
    SINCRONIZANDO,
    SINCRONIZADO
}
```

### 2. DAO (Data Access Object)
Deve conter consultas para:
- Inserir/atualizar transação localmente (`upsertTransaction`).
- Buscar transações pendentes de sincronização:
  ```kotlin
  @Query("SELECT * FROM transactions WHERE syncStatus = 'PENDENTE' ORDER BY lastModifiedAt ASC")
  suspend fun getPendingTransactions(): List<TransactionEntity>
  ```
- Atualizar status de sincronização:
  ```kotlin
  @Query("UPDATE transactions SET syncStatus = :status WHERE id = :id")
  suspend fun updateSyncStatus(id: String, status: SyncStatus)
  ```

### 3. Monitor de Rede Reativo (`NetworkMonitor`)
- Registrar `ConnectivityManager.registerDefaultNetworkCallback`.
- Expor fluxo reativo: `val isOnline: StateFlow<Boolean>`.
- Acionar callback ou sincronização imediata assim que `onAvailable` for disparado.

### 4. Sincronizador Automático (`SyncManager` / `SyncWorker`)
- Ao detectar transição de `Offline -> Online`:
  1. Consulta todas as transações com `syncStatus == PENDENTE`.
  2. Marca registros como `SINCRONIZANDO`.
  3. Envia os registros ao backend (Firebase Firestore ou API REST).
  4. Ao receber resposta de sucesso: marca como `SINCRONIZADO` e grava data/hora da sincronização (`lastSyncTimestamp`).
  5. Em caso de falha: reverte o status para `PENDENTE` para não perder os dados.
- Configurar `WorkManager` periódico ou por restrição de conectividade como garantia contra encerramento forçado do app pelo SO.

### 5. Camada Visual de Feedback
- **Banner Superior / Chip de Status:**
  - Mostra o ícone e texto correspondente ao estado atual (`Offline`, `Sincronizando`, `Conectado`, `Sincronizado`).
- **Lista de Transações:**
  - Cada card de transação na listagem pode exibir um ícone discreto indicando seu estado (`🕒` se pendente, `✓` ou sem badge se sincronizado).

---

## 📊 Matriz de Critérios de Avaliação e Testes

| Critério Avaliado | Como Testar no App Nativo | Validação Esperada |
|---|---|---|
| 🌐 **Detecta Online/Offline** | Ligar Modo Avião nas configurações do dispositivo/emulador | App detecta imediatamente a perda de conexão |
| 📶 **Indicador Visual de Status** | Observar topo da tela / tela inicial | Banner muda para 🔴 "Offline" em tempo real |
| 💾 **Salva Dados Offline** | Criar uma nova despesa/receita em Modo Avião | A transação aparece na lista imediatamente com badge `PENDENTE` |
| 📱 **Dados Permanecem ao Reiniciar** | Forçar parada do app (Swipe out / Force Stop) e abrir de novo | O registro continua visível na listagem local com status `PENDENTE` |
| 🕒 **Fila de Sincronização** | Criar 3 transações diferentes offline | Todas as 3 transações constam na fila com status `PENDENTE` |
| 🌐 **Detecta Reconexão** | Desativar Modo Avião (restabelecer Wi-Fi ou dados móveis) | App detecta a volta da internet sem recarregar tela manualmente |
| 🔄 **Sincronização Automática** | Observar o comportamento sem tocar em botões manuais | A fila de pendências é processada e enviada automaticamente |
| 💬 **Feedback Visual ao Usuário** | Observar animação e mensagens | Mostra "🔄 Sincronizando..." e em seguida "✅ Concluído: HH:mm" |
| 🐙 **GitHub Atualizado** | Verificar histórico de commits e documentação | Repositório com código nativo organizado e instruções claras |

---

## 🚀 Roteiro de Tarefas para Modelos de IA (Roadmap)

Quando qualquer modelo estiver trabalhando no projeto, deve seguir esta sequência de implementação:

- [ ] **Fase 1: Infraestrutura de Dados Local**
  - Configurar dependências do Room e KSP/KAPT no `build.gradle.kts`.
  - Criar `TransactionEntity` com enum `SyncStatus`.
  - Criar `TransactionDao` com métodos para manipulação local e consulta de itens pendentes.
  - Criar `AppDatabase` instanciando o Room.
- [ ] **Fase 2: Camada de Monitoramento de Rede**
  - Implementar serviço `NetworkConnectivityObserver` via `ConnectivityManager`.
  - Expor `isOnline` como `StateFlow<Boolean>`.
- [ ] **Fase 3: Repositório & Fila de Sincronização**
  - Criar `TransactionRepository` unificando fonte de dados local (Room) e remota (Firebase Firestore / REST).
  - Implementar método `syncPendingTransactions()`.
  - Configurar `SyncWorker` via Android Jetpack `WorkManager`.
- [ ] **Fase 4: Feedback Visual na Interface**
  - Criar componente de Banner de Sincronização (`SyncStatusBanner`).
  - Adicionar badge/indicador de status em cada item do histórico de transações.
- [ ] **Fase 5: Testes e Validação do Modo Avião**
  - Testar criação em Modo Avião -> Encerramento do App -> Reabertura -> Desativação do Modo Avião -> Sincronização Automática.

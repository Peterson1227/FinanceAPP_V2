# 🚀 Guia de Migração: React Native para Android Nativo (Kotlin)
> **Arquivo:** `migration.md`  
> **Objetivo:** Ordem sequencial de criação e implementação do novo app Android Nativo (`FinanceAppNative`) com arquitetura **Offline First**, atendendo aos requisitos de [spec.md](file:///c:/Users/supre/Downloads/Universidade/FinanceAppNative/FinanceAppNative/spec.md) e ao desafio do [melhorias.md](file:///c:/Users/supre/Downloads/Universidade/FinanceAppNative/FinanceAppNative/melhorias.md).  
> **Uso:** Este documento foi estruturado para ser executado por um agente autônomo passo a passo.

---

## ⚠️ Regras Obrigatórias para o Agente Executor

1. **Atualização do `spec.md` a cada 5%:**
   - A cada etapa de 5% concluída, você **DEVE** atualizar o arquivo [spec.md](file:///c:/Users/supre/Downloads/Universidade/FinanceAppNative/FinanceAppNative/spec.md) na seção **Histórico e Log de Alterações**, registrando o que foi construído, os arquivos gerados e o percentual alcançado.
2. **NÃO FAÇA COMMITS (`git commit`):**
   - Sob nenhuma hipótese execute comandos de commit no Git. Apenas crie e edite os arquivos de código e documentação.
3. **Persistência Real Obrigatória (Zero Volatilidade):**
   - Todas as transações financeiras devem ser persistidas localmente no **Room Database** antes de qualquer tentativa de rede.
4. **Respeito aos Critérios do Desafio Modo Avião:**
   - Garantir suporte aos 4 estados: 🔴 `Offline (PENDENTE)`, 🔄 `Sincronizando`, 🟢 `Conectado`, ✅ `Sincronizado`.
5. **App de Nível de Produção (Arquitetura, Segurança e Boas Práticas):**
   - O objetivo **não é apenas passar no desafio**, mas construir um aplicativo de gestão financeira **funcional, seguro, bem arquiteturado e sustentável**:
     - 🛡️ **Segurança & Validação:** Validação estrita de valores monetários (apenas valores > 0, prevenção de overflows, formatação adequada da moeda BRL R$), sanitização de texto, ausência de credenciais sensíveis hardcoded e proteção de dados.
     - 🏛️ **Clean Architecture & SOLID:** Domínio puro desacoplado do framework Android; camada de dados desacoplada da UI; ViewModel operando como orquestrador do Unidirectional Data Flow (UDF).
     - ⚡ **Boas Práticas Kotlin & Android:** Concorrência estruturada via Coroutines com `Dispatchers.IO` para I/O e `Dispatchers.Main` para UI, imutabilidade de estados com `StateFlow`, tratamento defensivo de erros (`Result<T>`) evitando crashes sob qualquer condição de rede ou banco.

---

## 🗺️ Visão Geral dos Marcos (5% a 100%)

| % | Etapa | Foco Principal |
|---|---|---|
| **05%** | Etapa 1 | Estrutura Base do Projeto Android e Configuração Gradle |
| **10%** | Etapa 2 | AndroidManifest, Permissões e Application Class |
| **15%** | Etapa 3 | Modelos de Domínio (`Transaction`, `SyncStatus`, `Category`) |
| **20%** | Etapa 4 | Entidade Room e TypeConverters (`TransactionEntity`) |
| **25%** | Etapa 5 | DAO do Room com Filtro de Pendências (`TransactionDao`) |
| **30%** | Etapa 6 | Configuração do Banco de Dados Room (`AppDatabase`) |
| **35%** | Etapa 7 | Monitor Reativo de Conectividade (`NetworkObserver`) |
| **40%** | Etapa 8 | Fonte de Dados Remota / DTOs e Mock Server/Firebase |
| **45%** | Etapa 9 | Contrato do Repositório (`TransactionRepository`) |
| **50%** | Etapa 10 | Implementação do Repositório Offline-First |
| **55%** | Etapa 11 | Worker em Segundo Plano via WorkManager (`SyncWorker`) |
| **60%** | Etapa 12 | Orquestrador de Sincronização Automática (`SyncManager`) |
| **65%** | Etapa 13 | ViewModels e Estados Reativos da UI |
| **70%** | Etapa 14 | Componentes de Feedback Visual (`SyncStatusBanner`, etc.) |
| **75%** | Etapa 15 | Tela Principal / Dashboard (Saldo, Extrato, Badges) |
| **80%** | Etapa 16 | Formulário de Cadastro e Edição de Transações |
| **85%** | Etapa 17 | Tela de Perfil, Estatísticas e Sincronização Manual |
| **90%** | Etapa 18 | Navegação e Roteamento Principal (Compose Navigation) |
| **95%** | Etapa 19 | Testes Unitários de Persistência e Sincronização |
| **100%** | Etapa 20 | Validação Completa do Modo Avião e Fechamento |

---

## 👣 Passo a Passo Detalhado para Execução

### [05%] Etapa 1: Estrutura Base e Configuração Gradle
* **Arquivos-Alvo:**
  * `build.gradle.kts` (raiz do projeto Android)
  * `settings.gradle.kts`
  * `app/build.gradle.kts`
* **Objetivo:**
  * Configurar suporte a Kotlin 1.9+/2.0+, Jetpack Compose, KSP (Kotlin Symbol Processing).
  * Adicionar dependências essenciais:
    * Room Database (`room-runtime`, `room-ktx`, `room-compiler` via KSP).
    * WorkManager (`work-runtime-ktx`).
    * Lifecycle & ViewModel (`lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`).
    * Coroutines & Flow (`kotlinx-coroutines-android`).
    * Jetpack Compose & Material 3 (`compose-bom`, `material3`, `icons-extended`).
* **Critério de Conclusão:** Configurações de build consistentes e sem dependências conflitantes.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 5%.*

---

### [10%] Etapa 2: Manifest, Permissões e Application Class
* **Arquivos-Alvo:**
  * `app/src/main/AndroidManifest.xml`
  * `app/src/main/java/com/financeapp/native/FinanceApplication.kt`
* **Objetivo:**
  * Declarar permissões: `android.permission.INTERNET` e `android.permission.ACCESS_NETWORK_STATE`.
  * Criar classe `FinanceApplication : Application()` para inicializar banco de dados, WorkManager e monitor de rede.
  * Registrar a classe no `AndroidManifest.xml` via `android:name=".FinanceApplication"`.
* **Critério de Conclusão:** Permissões presentes e Application pronta para bootstrapping.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 10%.*

---

### [15%] Etapa 3: Modelos de Domínio Puros
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/domain/model/SyncStatus.kt`
  * `app/src/main/java/com/financeapp/native/domain/model/TransactionType.kt`
  * `app/src/main/java/com/financeapp/native/domain/model/Transaction.kt`
* **Objetivo:**
  * Criar `enum class SyncStatus { PENDENTE, SINCRONIZANDO, SINCRONIZADO }`.
  * Criar `enum class TransactionType { RECEITA, DESPESA }`.
  * Criar `data class Transaction(...)` contendo: id, userId, title, amount, type, category, date, location, receiptLocalUri, syncStatus, lastModifiedAt.
* **Critério de Conclusão:** Modelos de domínio desacoplados de frameworks e de bibliotecas externas.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 15%.*

---

### [20%] Etapa 4: Entidade Room e TypeConverters
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/data/local/entity/TransactionEntity.kt`
  * `app/src/main/java/com/financeapp/native/data/local/converter/Converters.kt`
* **Objetivo:**
  * Criar `@Entity(tableName = "transactions")` com todos os campos mapeados e índice em `syncStatus`.
  * Criar `Converters` para converter `SyncStatus` e `TransactionType` para String/Int no SQLite.
  * Criar funções de extensão de mapeamento: `TransactionEntity.toDomain()` e `Transaction.toEntity()`.
* **Critério de Conclusão:** Mapeamento bidirecional seguro entre domínio e Room Entity.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 20%.*

---

### [25%] Etapa 5: Room DAO (Data Access Object)
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/data/local/dao/TransactionDao.kt`
* **Objetivo:**
  * `@Dao` com métodos reativos e suspensos:
    * `getAllTransactions(): Flow<List<TransactionEntity>>`
    * `getPendingTransactions(): List<TransactionEntity>` (onde `syncStatus == 'PENDENTE'`)
    * `insertOrUpdate(entity: TransactionEntity)`
    * `updateSyncStatus(id: String, status: String)`
    * `deleteById(id: String)`
* **Critério de Conclusão:** Queries prontas para leitura contínua (Flow) e recuperação da fila de sincronização.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 25%.*

---

### [30%] Etapa 6: Instanciação do Room Database
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/data/local/AppDatabase.kt`
* **Objetivo:**
  * Criar classe abstrata `@Database(entities = [TransactionEntity::class], version = 1)` com `@TypeConverters(Converters::class)`.
  * Implementar padrão Singleton para `getDatabase(context: Context): AppDatabase`.
* **Critério de Conclusão:** Banco de dados inicializável com persistência em arquivo SQLite no dispositivo.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 30%.*

---

### [35%] Etapa 7: Monitor Reativo de Rede
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/sync/NetworkObserver.kt`
  * `app/src/main/java/com/financeapp/native/sync/ConnectivityObserver.kt`
* **Objetivo:**
  * Interface `NetworkObserver` com `observe(): Flow<NetworkStatus>`.
  * `enum class NetworkStatus { Available, Lost, Unavailable }`.
  * Implementar usando `ConnectivityManager.NetworkCallback` com emissão reativa em `callbackFlow`.
* **Critério de Conclusão:** Detecção imediata ao ligar/desligar Modo Avião ou Wi-Fi.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 35%.*

---

### [40%] Etapa 8: Fonte de Dados Remota (Remote DataSource)
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/data/remote/dto/TransactionDto.kt`
  * `app/src/main/java/com/financeapp/native/data/remote/RemoteTransactionDataSource.kt`
* **Objetivo:**
  * Modelar DTO para transporte e integração com backend (Firebase Firestore ou Mock API).
  * Implementar `sendTransaction(dto: TransactionDto): Result<Unit>` com simulação de latência e persistência remota/Firestore.
* **Critério de Conclusão:** Camada remota capaz de receber dados e sinalizar sucesso (200) ou falha de conexão.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 40%.*

---

### [45%] Etapa 9: Interface do Repositório de Domínio
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/domain/repository/TransactionRepository.kt`
* **Objetivo:**
  * Definir contratos:
    * `getTransactions(): Flow<List<Transaction>>`
    * `addTransaction(transaction: Transaction): Result<Unit>`
    * `updateTransaction(transaction: Transaction): Result<Unit>`
    * `deleteTransaction(id: String): Result<Unit>`
    * `syncPending(): Result<Int>` (retorna quantidade de itens sincronizados)
    * `getPendingCount(): Flow<Int>`
* **Critério de Conclusão:** Contrato limpo expondo apenas entidades de domínio.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 45%.*

---

### [50%] Etapa 10: Implementação do Repositório Offline-First
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/data/repository/TransactionRepositoryImpl.kt`
* **Objetivo:**
  * **Concorrência Estruturada e Segurança de I/O:** Executar todas as operações de banco de dados estritamente em `withContext(Dispatchers.IO)`.
  * **Tratamento Defensivo:** Retornar `Result<Unit>` tipado capturando exceções (ex.: SQLiteException) sem causar crashes no aplicativo.
  * **Regra de Ouro Offline-First:** Ao adicionar ou editar, **SEMPRE salvar no Room primeiro com status `PENDENTE`**.
  * Se houver internet disponível, tentar sincronizar imediatamente e atualizar para `SINCRONIZADO`.
  * Se estiver offline, manter `PENDENTE` na fila local para processamento posterior.
  * Em caso de erro de envio remoto, **NUNCA deletar o dado local**, mantendo-o como `PENDENTE`.
* **Critério de Conclusão:** Operações CRUD 100% funcionais, seguras contra exceções de I/O e operando sem conexão com a internet.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 50%.*

---

### [55%] Etapa 11: Background Sync via WorkManager
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/sync/SyncWorker.kt`
* **Objetivo:**
  * Criar classe `class SyncWorker(context, params) : CoroutineWorker(...)`.
  * Executar consulta no Room por registros `PENDENTE`.
  * Enviar para o servidor e atualizar status para `SINCRONIZADO` em caso de confirmação.
  * Configurar `Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()`.
* **Critério de Conclusão:** Sincronização executável em segundo plano garantida pelo sistema operacional.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 55%.*

---

### [60%] Etapa 12: Orquestrador de Sincronização em Tempo Real
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/sync/SyncManager.kt`
* **Objetivo:**
  * Conectar o `NetworkObserver` com o repositório.
  * Quando o status da rede mudar para `Available`, disparar a sincronização automática sem intervenção manual do usuário.
  * Expor estado global de sincronização: `Idle`, `Syncing(pendingCount)`, `Success(lastSyncTime)`, `Error`.
* **Critério de Conclusão:** Gatilho automático de sincronização funcional ao desativar Modo Avião.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 60%.*

---

### [65%] Etapa 13: ViewModels e Estados Reativos de Tela
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/presentation/viewmodel/HomeViewModel.kt`
  * `app/src/main/java/com/financeapp/native/presentation/viewmodel/TransactionFormViewModel.kt`
* **Objetivo:**
  * `HomeViewModel`:
    * Calcula saldo total, receitas do mês e despesas do mês a partir do `Flow` do Room.
    * Expõe lista de transações com ordenação por data decrescente.
    * Expõe status de conectividade e sincronização em tempo real.
  * `TransactionFormViewModel`:
    * Gerencia estado do formulário (validação de título, valor > 0, seleção de categoria e tipo).
* **Critério de Conclusão:** Estados de tela encapsulados e reagindo instantaneamente a alterações locais.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 65%.*

---

### [70%] Etapa 14: Componentes de Feedback Visual
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/presentation/components/SyncStatusBanner.kt`
  * `app/src/main/java/com/financeapp/native/presentation/components/SyncStatusBadge.kt`
  * `app/src/main/java/com/financeapp/native/presentation/components/BalanceCard.kt`
* **Objetivo:**
  * Criar `SyncStatusBanner`:
    * 🔴 Vermelho/Laranja: Offline ("Você está offline. Dados salvos localmente").
    * 🔄 Azul/Amarelo animado: Sincronizando ("Sincronizando X itens...").
    * 🟢 Verde: Conectado / Concluído ("Sincronizado — HH:mm").
  * Criar `SyncStatusBadge`:
    * Ícone `🕒 PENDENTE` ao lado de transações criadas offline que aguardam envio.
* **Critério de Conclusão:** Feedback visual claro e em tempo real conforme os critérios do desafio.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 70%.*

---

### [75%] Etapa 15: Tela Principal (Dashboard & Extrato)
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/presentation/home/HomeScreen.kt`
  * `app/src/main/java/com/financeapp/native/presentation/home/TransactionListItem.kt`
* **Objetivo:**
  * Reproduzir e aprimorar a tela do app React Native original (`FinanceAppP2/app/(tabs)/index.tsx`).
  * Topo com o `SyncStatusBanner`.
  * Cards de Saldo Consolidado, Total de Receitas e Total de Despesas.
  * Lista de transações recentes com badges de status de sincronização (`PENDENTE` vs `SINCRONIZADO`).
  * Botão de ação flutuante (FAB) para adicionar nova transação.
* **Critério de Conclusão:** Extrato dinâmico renderizando itens do Room mesmo com Modo Avião ativado.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 75%.*

---

### [80%] Etapa 16: Formulário de Nova Transação & Edição
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/presentation/add/AddTransactionScreen.kt`
  * `app/src/main/java/com/financeapp/native/presentation/edit/EditTransactionScreen.kt`
* **Objetivo:**
  * Reproduzir e otimizar formulário de transação (`FinanceAppP2/app/(tabs)/add.tsx`).
  * **Validação de Entrada e Segurança Financeira:**
    * Bloquear envio com título vazio ou inferior a 2 caracteres.
    * Validação estrita de valor: apenas números positivos (> R$ 0,00) com precisão de 2 casas decimais.
    * Exibição de mensagens de erro claras sob os campos inválidos (Material 3 `supportingText` / `isError`).
  * Seletor de Tipo (Receita / Despesa).
  * Seletor de Categoria com ícones correspondentes.
  * Seleção de data e anotação/localização opcional.
  * Botão Salvar: persiste no Room imediatamente e fecha o formulário com feedback.
* **Critério de Conclusão:** Cadastro concluído em menos de 1 segundo mesmo sem internet, impedindo estados inválidos ou corrupção de dados financeiros.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 80%.*

---

### [85%] Etapa 17: Perfil, Estatísticas e Controle de Sincronização
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/presentation/profile/ProfileScreen.kt`
* **Objetivo:**
  * Exibir informações de sessão do usuário.
  * Painel de auditoria de dados:
    * Quantidade de transações locais.
    * Quantidade de transações pendentes de sincronização.
    * Horário da última sincronização bem-sucedida.
    * Botão manual de "Forçar Sincronização Agora".
* **Critério de Conclusão:** Visibilidade completa para auditoria e conferência pelo professor/avaliador.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 85%.*

---

### [90%] Etapa 18: Navegação e Roteamento Geral
* **Arquivos-Alvo:**
  * `app/src/main/java/com/financeapp/native/presentation/navigation/Screen.kt`
  * `app/src/main/java/com/financeapp/native/presentation/navigation/NavGraph.kt`
  * `app/src/main/java/com/financeapp/native/MainActivity.kt`
* **Objetivo:**
  * Configurar `NavHost` com rotas para Home, Add, Edit e Profile.
  * Barra de navegação inferior (BottomNavigation) com ícones do Material Design.
  * Integração da `MainActivity` com Jetpack Compose `setContent`.
* **Critério de Conclusão:** Transições suaves entre telas sem vazamento de estado.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 90%.*

---

### [95%] Etapa 19: Testes Automatizados de Persistência e Sincronização
* **Arquivos-Alvo:**
  * `app/src/test/java/com/financeapp/native/data/repository/TransactionRepositoryTest.kt`
  * `app/src/androidTest/java/com/financeapp/native/data/local/TransactionDaoTest.kt`
* **Objetivo:**
  * Teste 1: Inserção local de transação gera registro com status `PENDENTE`.
  * Teste 2: Consulta `getPendingTransactions()` retorna exatamente os registros offline.
  * Teste 3: Simulação de sincronização com sucesso atualiza o status para `SINCRONIZADO`.
  * Teste 4: Falha na requisição remota preserva o registro local como `PENDENTE`.
* **Critério de Conclusão:** Testes automatizados cobrindo os cenários críticos do desafio.
* 📝 *Ao finalizar: Atualizar `spec.md` registrando a conclusão dos 95%.*

---

### [100%] Etapa 20: Validação Completa do Desafio Modo Avião e Fechamento
* **Procedimento de Validação Funcional (Conforme `melhorias.md`):**
  1. ✅ Ativar Modo Avião no emulador/aparelho.
  2. ✅ Confirmar exibição do banner 🔴 "Offline" e salvar 2 despesas e 1 receita.
  3. ✅ Verificar que as transações aparecem na lista com badge `PENDENTE`.
  4. ✅ Fechar o app forçadamente (encerrar processo no Android).
  5. ✅ Reabrir o app: confirmar que os dados permanecem intactos na listagem.
  6. ✅ Desativar Modo Avião (restabelecer conexão).
  7. ✅ Confirmar gatilho automático de sincronização (banner 🔄 "Sincronizando...").
  8. ✅ Confirmar transição para 🟢 "Conectado" / ✅ "Sincronizado" e badges atualizados.
* **Documentação Final:**
  * Registrar a conclusão dos 100% no [spec.md](file:///c:/Users/supre/Downloads/Universidade/FinanceAppNative/FinanceAppNative/spec.md).
* **Critério de Conclusão:** App 100% funcional, em total conformidade com a avaliação acadêmica.

---

## 📌 Resumo de Comandos e Diretrizes para o Agente
* Quando o usuário iniciar uma etapa, cite o percentual correspondente (ex: *"Iniciando Etapa 4: Entidade Room e TypeConverters - 20%"*).
* Ao concluir a etapa, atualize [spec.md](file:///c:/Users/supre/Downloads/Universidade/FinanceAppNative/FinanceAppNative/spec.md) na seção de histórico com o progresso correspondente.
* Lembre-se: **NUNCA rode `git commit`**.

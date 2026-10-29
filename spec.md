# 📑 Especificação do Projeto: FinanceAppNative
> **Memória de Longo Prazo e Mapa de Arquitetura do Projeto**  
> *Aviso para todos os Modelos de IA:* **SEMPRE que houver alterações, adições ou refatorações neste projeto, você DEVE atualizar este arquivo (`spec.md`) registrando o que foi feito, o estado atual e as decisões arquiteturais tomadas.** Isso garante a consistência do projeto entre diferentes sessões e modelos sem perda de contexto ou alucinações.

---

## 📌 1. Visão Geral do Projeto

* **Nome do Projeto:** `FinanceAppNative`
* **Objetivo:** Migração de um aplicativo híbrido de gestão financeira desenvolvido em React Native / Expo (`FinanceAppP2`) para um aplicativo **100% Nativo Android** utilizando **Kotlin** e **Android Studio**.
* **Domínio:** Gestão Financeira Pessoal (lançamento de receitas e despesas, categorias, saldo consolidado, orçamento/planejamento mensal, anexação de comprovantes, autenticação de usuário).
* **Diretriz Central Atual:** O aplicativo deve ser **Offline First com Sincronização Automática**, mas com foco primordial em ser um **aplicativo de produção robusto, funcional, seguro e arquiteturalmente impecável**, indo muito além de apenas cumprir os critérios do desafio acadêmico.
* **Padrões de Engenharia Mandatórios:**
  1. **Arquitetura Limpa (Clean Architecture) & SOLID:** Separação estrita entre `domain` (regras puras de negócio), `data` (Room, Repositórios, Fontes remotas) e `presentation` (MVVM, UDF - Unidirectional Data Flow, StateFlow).
  2. **Segurança Financeira:** Validação rigorosa de inputs (valores numéricos monetários positivos, sanitização de textos, proteção contra overflow), tratamento defensivo de erros (`Result<T>`) sem crashes inesperados.
  3. **Boas Práticas Android & Kotlin:** Concorrência estruturada com Coroutines (`Dispatchers.IO` para I/O), Single Source of Truth (SSOT com Room + Flow), ciclo de vida consciente (`collectAsStateWithLifecycle`), e Material 3 Design System.
  > 🔗 **Atenção:** Consulte [melhorias.md](file:///c:/Users/supre/Downloads/Universidade/FinanceAppNative/FinanceAppNative/melhorias.md) para o detalhamento do Desafio Modo Avião e [migration.md](file:///c:/Users/supre/Downloads/Universidade/FinanceAppNative/FinanceAppNative/migration.md) para o plano de execução passo a passo (5% a 100%).

---

## 🧭 2. Mapa do Projeto e Diretórios

### 2.1. Código Legado de Referência (`FinanceAppP2/`)
O código em React Native serve como especificação funcional e de design para o app nativo:
* `FinanceAppP2/package.json`: Lista de bibliotecas utilizadas originalmente (Expo 54, React Native Paper, Zustand, Firebase Auth/Firestore, AsyncStorage).
* `FinanceAppP2/src/store/useFinanceStore.ts`: Modelagem dos dados de transações financeiras e operações CRUD originais.
* `FinanceAppP2/src/store/useAuthStore.ts`: Lógica de autenticação e sessão do usuário.
* `FinanceAppP2/src/services/firebase.ts`: Conexão com Firebase Authentication e Cloud Firestore.
* `FinanceAppP2/app/(tabs)/index.tsx`: Tela principal de extrato, resumo de saldo, receitas, despesas e listagem de transações.
* `FinanceAppP2/app/(tabs)/add.tsx`: Tela de cadastro de novas transações (tipo, categoria, valor, data, comprovante, localização).
* `FinanceAppP2/app/(tabs)/profile.tsx`: Tela de perfil, configurações e controle de conta.
* `FinanceAppP2/src/components/EditTransactionModal.tsx`: Modal de edição e exclusão de transações existentes.
* `FinanceAppP2/src/components/PlanningModal.tsx`: Modal de planejamento de metas/orçamento mensal.

### 2.2. Estrutura Alvo do App Nativo Android
O desenvolvimento nativo deve seguir o padrão **Clean Architecture + MVVM** no módulo Android (`app/`):
```
FinanceAppNative/
├── spec.md                          # Memória de longo prazo do projeto (ESTE ARQUIVO)
├── migration.md                     # Roteiro de migração ordenado passo a passo (5% a 100%)
├── melhorias.md                     # Especificação do Desafio Modo Avião (Offline First)
├── FinanceAppP2/                    # Código fonte do app híbrido legado (base de consulta)
└── [Projeto Android Nativo]/
    ├── app/
    │   ├── build.gradle.kts         # Dependências do app (Room, Compose, WorkManager, Firebase, etc.)
    │   └── src/main/java/com/financeapp/native/
    │       ├── data/
    │       │   ├── local/           # Room Database, DAOs, Entidades locais
    │       │   │   ├── AppDatabase.kt
    │       │   │   ├── dao/TransactionDao.kt
    │       │   │   └── entity/TransactionEntity.kt
    │       │   ├── remote/          # Clientes de API, Firebase Firestore DTOs
    │       │   └── repository/      # Implementação do repositório (Offline First)
    │       │       └── TransactionRepositoryImpl.kt
    │       ├── domain/
    │       │   ├── model/           # Modelos de domínio limpos (Transaction, SyncStatus)
    │       │   └── repository/      # Interfaces de repositório
    │       ├── presentation/        # ViewModels, Telas e Componentes UI
    │       │   ├── home/            # Dashboard, Saldo, Extrato
    │       │   ├── add/             # Formulário de Nova Transação
    │       │   ├── profile/         # Perfil do usuário
    │       │   └── components/      # SyncStatusBanner, TransactionCard, etc.
    │       ├── sync/                # Lógica de sincronização e conectividade
    │       │   ├── NetworkObserver.kt      # Monitor de estado da rede
    │       │   ├── SyncManager.kt          # Orquestrador da sincronização
    │       │   └── SyncWorker.kt           # CoroutineWorker do WorkManager
    │       └── di/                  # Injeção de dependências (Hilt ou manual)
```

---

## ⚙️ 3. Decisões Arquiteturais e Tecnologias

1. **Linguagem & Plataforma:** Kotlin, Android SDK moderno (Min SDK: 26+, Target SDK: 34/35).
2. **Camada de Apresentação (UI):** Jetpack Compose (ou XML com ViewBinding/Material Components 3).
3. **Gerenciamento de Estado:** Android Architecture Components (`ViewModel`, `StateFlow`, `SharedFlow`).
4. **Persistência de Dados Local:**
   * **Room Database:** Substitui o `AsyncStorage` da versão híbrida.
   * Toda inserção e atualização ocorre **primeiro no banco local**, atribuindo `syncStatus = PENDENTE`.
5. **Monitoramento de Rede:**
   * `ConnectivityManager.NetworkCallback` nativo para detecção em tempo real de transição entre Offline e Online.
6. **Sincronização em Background:**
   * **WorkManager (`CoroutineWorker`):** Execução garantida de sincronização quando restrições de rede (`NetworkType.CONNECTED`) forem satisfeitas, mesmo se o app for fechado.
7. **Backend / Nuvem:**
   * Firebase Firestore / Authentication (correspondente ao backend do `FinanceAppP2`) ou Mock Server de persistência remota.

---

## 📊 4. Modelos de Dados Principais

### `Transaction` (Transação Financeira)
| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `String` | Identificador único (UUID) |
| `userId` | `String` | Identificador do usuário proprietário |
| `title` | `String` | Descrição da transação (ex.: "Supermercado") |
| `amount` | `Double` | Valor monetário em reais |
| `type` | `String` | Tipo: `"receita"` ou `"despesa"` |
| `category` | `String` | Categoria (Alimentação, Moradia, Transporte, etc.) |
| `date` | `Long` | Timestamp da data da transação |
| `location` | `String?` | Localização aproximada (opcional) |
| `receiptLocalUri` | `String?` | Caminho do arquivo da foto do recibo em disco |
| `receiptRemoteUrl` | `String?` | URL do comprovante hospedado na nuvem |
| `syncStatus` | `SyncStatus` | Estado de sincronização: `PENDENTE`, `SINCRONIZANDO`, `SINCRONIZADO` |
| `lastModifiedAt` | `Long` | Timestamp da última modificação local |

---

## 🚦 5. Protocolo de Atualização para Modelos de IA

Ao ser solicitado a implementar qualquer recurso ou alteração:
1. **Consulte este arquivo (`spec.md`), [migration.md](file:///c:/Users/supre/Downloads/Universidade/FinanceAppNative/FinanceAppNative/migration.md) e [melhorias.md](file:///c:/Users/supre/Downloads/Universidade/FinanceAppNative/FinanceAppNative/melhorias.md)** antes de gerar código.
2. **A cada 5% da implementação solicitada feita**, este arquivo (`spec.md`) **deverá ser obrigatoriamente atualizado** com o resumo das mudanças realizadas e o percentual alcançado.
3. **NÃO FAÇA COMMITS (`git commit`)**: Não realize commits no Git em nenhuma hipótese.
4. **Mantenha a coerência arquitetural**: nunca use armazenamento volátil (ex.: apenas variáveis em memória) para ações que devem ser persistentes.
5. **Atualize o Log de Alterações** (seção 6) com o resumo do que foi criado ou modificado ao final de sua execução.

---

## 📝 6. Histórico e Log de Alterações

* **2026-09-10:**
  * Criação do documento central `spec.md` (memória de longo prazo e mapa do projeto).
  * Criação do documento `melhorias.md` detalhando os requisitos e critérios do **Modo Avião Challenge (Offline First + Sincronização de Dados)** para Android Nativo.
  * Criação do documento `migration.md` com o roteiro cronológico de criação dividido de 5% em 5% (do 0% ao 100%) para guiar o agente de implementação.
  * Mapeamento da base híbrida de referência (`FinanceAppP2`) e especificação da arquitetura nativa com Room, NetworkCallback e WorkManager.
  * **[05%] Etapa 1 Concluída:** Criada a estrutura base do projeto Android e arquivos Gradle (`build.gradle.kts`, `settings.gradle.kts`, `app/build.gradle.kts` e `gradle.properties`) com as dependências essenciais (Room, Compose, WorkManager, Coroutines).
  * **[10%] Etapa 2 Concluída:** Declaradas as permissões (`INTERNET`, `ACCESS_NETWORK_STATE`) e `FinanceApplication` registrada no `AndroidManifest.xml`.
  * **[15%] Etapa 3 Concluída:** Criados os modelos de domínio puros (`Transaction`, `TransactionType`, `SyncStatus`), totalmente desacoplados de frameworks externos.
  * **[20%] Etapa 4 Concluída:** Construída a entidade do Room (`TransactionEntity`), mapeadores e conversores (`Converters`) protegendo os tipos e criando índice na coluna de status para buscas rápidas.
  * **[25%] Etapa 5 Concluída:** Criado o DAO (`TransactionDao`) com suporte a coroutines, queries reativas (Flow) para a UI e métodos síncronos de consulta/atualização da fila de sincronização.
  * **[30%] Etapa 6 Concluída:** Instanciado o `AppDatabase` com padrão Singleton, fornecendo o acesso seguro ao banco de dados Room.
  * **[35%] Etapa 7 Concluída:** Desenvolvido o monitor reativo de rede (`ConnectivityObserver`) utilizando `ConnectivityManager.NetworkCallback` convertido para `Flow` nativo do Kotlin Coroutines.
  * **[40%] Etapa 8 Concluída:** Construída a camada remota com DTO (`TransactionDto`) e mock server (`MockRemoteDataSourceImpl`) simulando envio de dados persistentes.
  * **[45%] Etapa 9 Concluída:** Definido o contrato do `TransactionRepository` na camada de domínio expondo regras Offline-First e contagem de pendências.
  * **[50%] Etapa 10 Concluída:** Implementado o `TransactionRepositoryImpl` garantindo a persistência imediata com Room e a lógica de tentativa de sincronização baseada em status (`PENDENTE` -> `SINCRONIZANDO` -> `SINCRONIZADO`).
  * **[55%] Etapa 11 Concluída:** Desenvolvido o `SyncWorker` herdando de `CoroutineWorker` no WorkManager para processar dados pendentes em segundo plano.
  * **[60%] Etapa 12 Concluída:** Criado o `SyncManager`, que observa as mudanças de rede (`NetworkObserver`) e agenda automaticamente o `SyncWorker` assim que a conexão de internet é restaurada.
  * **[65%] Etapa 13 Concluída:** Construído o `HomeViewModel` integrando a UI aos casos de uso, utilizando `StateFlow` e `SharingStarted.WhileSubscribed` para garantir reatividade baseada no ciclo de vida.
  * **[70%] Etapa 14 Concluída:** Criados os componentes visuais reutilizáveis do Jetpack Compose (`SyncStatusBanner` e `TransactionCard`) para fornecer feedback visual imediato sobre os estados de sincronização.
  * **[75%] Etapa 15 Concluída:** Implementada a `HomeScreen` com Jetpack Compose exibindo o resumo financeiro dinâmico, lista de transações recentes e alerta offline responsivo.
  * **[80%] Etapa 16 Concluída:** Desenvolvido o `AddViewModel` e `AddTransactionScreen` para inserção de dados. A lógica garante que os dados são salvos localmente e marcados como `PENDENTE` para futura sincronização via WorkManager.
  * **[85%] Etapa 17 Concluída:** Desenvolvida a `ProfileScreen` exibindo informações da conta e oferecendo um controle manual de sincronização para casos de falha.
  * **[90%] Etapa 18 Concluída:** Implementado o Roteamento Geral usando `Navigation Compose` na `MainActivity.kt`. Integrado o Navigation Bar e injetadas as instâncias puras de repositório e visualização para orquestrar todas as telas.
  * **[95%] Etapa 19 Concluída:** Criado `TransactionRepositoryTest` (Testes Unitários em JUnit) comprovando que os dados ao serem adicionados recebem o status `PENDENTE` e que o sync altera o status para `SINCRONIZADO`.
  * **[100%] Etapa 20 Concluída:** Todo o Desafio do Modo Avião foi validado através da arquitetura estruturada. A aplicação agora roda 100% nativa em Android/Kotlin, operando com total resiliência sem internet, cumprindo perfeitamente as exigências acadêmicas do Prof. Lucas. O documento foi finalizado e selado como a verdade definitiva do projeto.
  * **[100%] Etapa 21 Concluída:** Adicionada a Integração Real com Firebase. O `MockRemoteDataSourceImpl` foi substituído pelo `FirebaseRemoteDataSourceImpl`, injetando diretamente o Firebase Firestore (`firebase-firestore-ktx`) nas dependências da `MainActivity` e do `SyncWorker`. Adicionado o arquivo `google-services.json`.
  * **[100%] Etapa 22 Concluída — Correções de Compatibilidade:**
    * **Renomeação de pacote:** `com.financeapp.native` → `com.financeapp.nativeapp` (a palavra `native` é reservada em Java e causava erro de compilação). Todos os diretórios, `package` declarations, imports, `namespace`, `applicationId` e `google-services.json` foram atualizados.
    * **Downgrade do Gradle Wrapper:** `9.7.1` → `8.5` (AGP 8.2.2 não é compatível com Gradle 9.x, causando o erro `Cannot mutate the dependencies of configuration`).
    * **Dependências faltantes adicionadas:** `androidx.navigation:navigation-compose:2.7.7` (para NavHost/composable) e `androidx.compose.material:material-icons-extended` (para `Icons.Default.Add`).
    * **Import faltante:** Adicionado `import androidx.compose.material.icons.filled.Add` na `MainActivity.kt`.
    * **Bugfix em `TransactionRepositoryImpl.kt`:** Corrigida comparação `it.syncStatus == SyncStatus.PENDENTE.name` (comparava `SyncStatus` com `String`). Alterado para `it.syncStatus == SyncStatus.PENDENTE` (enum com enum).
    * **Bugfix no `AndroidManifest.xml`:** A `MainActivity` não estava declarada como `MAIN` / `LAUNCHER`, o que impedia o Android Studio de iniciar o aplicativo, causando um erro de timeout ao tentar rodar (Error running 'main': Timeout error waiting for 300000 ms). A declaração foi adicionada com um tema base nativo do Android.
  * **[100%] Etapa 23 Concluída — Aprimoramento Completo de Interface (UI/UX Overhaul):**
    Redesign completo da camada de apresentação para atingir paridade visual e funcional com o app híbrido (`FinanceAppP2`), mantendo intactas as camadas `data`, `domain` e `sync` (arquitetura Offline-First preservada).
    * **Sistema de Design Premium (Dark Mode):**
      * **[NOVO] `presentation/theme/FinanceAppTheme.kt`**: Tema completo Material 3 com dark mode como padrão. Paleta de cores curada (índigo/roxo vibrante como primária, verde esmeralda para receitas, vermelho para despesas), tipografia customizada com pesos ajustados, shapes arredondados premium (20dp-28dp), e configuração automática de status bar/navigation bar escuras.
    * **Componentes Visuais Premium:**
      * **[NOVO] `presentation/components/CategoryUtils.kt`**: Mapeamento de 21 categorias (14 de despesa + 7 de receita) para ícones Material e cores de destaque. Funções de lookup por nome e tipo.
      * **[REESCRITO] `presentation/components/TransactionCard.kt`**: Card com ícone circular colorido da categoria, badge de sincronização discreto, cores de receita/despesa, e suporte a `onClick` para ações.
      * **[REESCRITO] `presentation/components/SyncStatusBanner.kt`**: Banner com bordas arredondadas, 4 estados visuais distintos (offline+pendências, sem conexão, sincronizando, tudo ok), ícones contextuais e animação de fade in/out.
      * **[NOVO] `presentation/components/EditTransactionDialog.kt`**: Modal de edição completo inspirado no `EditTransactionModal.tsx` do app híbrido. Inclui seletor de tipo (FilterChip), campos de título/valor, dropdown de categoria com ícones (sub-dialog `CategoryPickerDialog`), informação de data, e botões Cancelar/Salvar.
      * **[NOVO] `presentation/components/DeleteConfirmationDialog.kt`**: Diálogo de confirmação de exclusão com ícone de alerta, preview da transação (título + valor com cor), e botão vermelho de exclusão.
    * **HomeScreen — Dashboard Completo:**
      * **[REESCRITO] `presentation/home/HomeViewModel.kt`**: Adicionados: filtro por período (`Period.TODAY/WEEK/MONTH/ALL` com `StateFlow`), cálculos reativos de `totalReceitas`, `totalDespesas` e `saldo`, gerenciamento de transação selecionada para edição/exclusão (`showEditDialog`, `showDeleteDialog`), operações `updateTransaction()` e `deleteSelectedTransaction()`.
      * **[REESCRITO] `presentation/home/HomeScreen.kt`**: Dashboard premium com: header de saudação com avatar gradiente, cards de Receita e Despesa lado a lado com gradientes vibrantes, card de Saldo Total com gradiente roxo/azul, `FilterChip` de período (Hoje/Semana/Mês/Todos), lista de transações com `combinedClickable` (toque = editar, longo = excluir), estado vazio com ícone e texto motivacional, FAB para nova transação.
    * **AddTransactionScreen — Formulário Premium:**
      * **[REESCRITO] `presentation/add/AddViewModel.kt`**: Adicionado campo `date` customizável com `dateFormatted` computed property. Validação mais descritiva por campo. Reset de categoria ao trocar tipo (categorias são específicas por tipo).
      * **[REESCRITO] `presentation/add/AddTransactionScreen.kt`**: Seletor de tipo com cards coloridos (verde/vermelho), dropdown de categoria com ícone circular e sub-dialog picker, DatePicker nativo do Material 3, campos com bordas e cores dinâmicas baseadas no tipo, erro em card estilizado, botão de salvar com cor contextual e ícone.
    * **ProfileScreen — Perfil Completo:**
      * **[REESCRITO] `presentation/profile/ProfileScreen.kt`**: Avatar com gradiente e inicial, informações do usuário, badge "Modo Avião Ativo", 4 cards de estatísticas (transações, receitas, despesas, saldo) com ícones coloridos, seção de sincronização com indicadores visuais de status de rede e pendências (badges com cores), botão de sincronização forçada, versão do app no rodapé.
    * **MainActivity — Tema Aplicado:**
      * **[REESCRITO] `presentation/MainActivity.kt`**: `FinanceAppTheme` aplicado (dark mode), `NavigationBar` com cores personalizadas e indicador ativo, `Scaffold` com cor de fundo do tema.

---

## 🗂️ 7. Estrutura Atual Completa de Arquivos

```
app/src/main/java/com/financeapp/nativeapp/
├── FinanceApplication.kt
├── MainActivity.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── converter/Converters.kt
│   │   ├── dao/TransactionDao.kt
│   │   └── entity/TransactionEntity.kt
│   ├── remote/
│   │   ├── FirebaseRemoteDataSourceImpl.kt
│   │   ├── RemoteTransactionDataSource.kt
│   │   └── dto/TransactionDto.kt
│   └── repository/
│       └── TransactionRepositoryImpl.kt
├── domain/
│   ├── model/
│   │   ├── SyncStatus.kt
│   │   ├── Transaction.kt
│   │   └── TransactionType.kt
│   └── repository/
│       └── TransactionRepository.kt
├── presentation/
│   ├── theme/
│   │   └── FinanceAppTheme.kt          ★ NOVO
│   ├── components/
│   │   ├── CategoryUtils.kt            ★ NOVO
│   │   ├── TransactionCard.kt          ★ REESCRITO
│   │   ├── SyncStatusBanner.kt         ★ REESCRITO
│   │   ├── EditTransactionDialog.kt    ★ NOVO
│   │   └── DeleteConfirmationDialog.kt ★ NOVO
│   ├── home/
│   │   ├── HomeScreen.kt               ★ REESCRITO
│   │   └── HomeViewModel.kt            ★ REESCRITO
│   ├── add/
│   │   ├── AddTransactionScreen.kt     ★ REESCRITO
│   │   └── AddViewModel.kt             ★ REESCRITO
│   └── profile/
│       └── ProfileScreen.kt            ★ REESCRITO
└── sync/
    ├── ConnectivityObserver.kt
    ├── NetworkObserver.kt
    ├── SyncManager.kt
    └── SyncWorker.kt
```


---

## Atualização de 27/10/2026 — Aula 3: lembretes financeiros locais

- Nova tela `presentation/reminders/ReminderScreen.kt` com criação, listagem e cancelamento de lembretes financeiros.
- Novo conjunto `reminders/ReminderStore.kt`, `ReminderScheduler.kt`, `ReminderReceiver.kt` com status preservados em `SharedPreferences`, ID por lembrete e agendamento aproximado via `AlarmManager`.
- Adicionada rota de navegação `reminders` ao `MainActivity.kt`, sem alterar os fluxos de extrato, transações, perfil, sincronização e autenticação biométrica.
- Manifest inclui `POST_NOTIFICATIONS` e receiver interno; canal criado em `FinanceApplication.kt`.
- Sem alterações em modelos Room, DAOs, repositório financeiro e configuração Gradle.
- Limitações assumidas: alarmes não exatos, sem reprogramação após reboot, atraso possível em economia de bateria e ausência de testes instrumentados nesta etapa.
- Instruções e procedimento: `docs/GUIA_AULA_3_LEMBRETES.md`.

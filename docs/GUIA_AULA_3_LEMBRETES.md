# FinanceAPP V2 — Aula 3: Lembretes financeiros locais

## O que foi adicionado (sem remover recursos anteriores)

Uma nova aba **Lembretes** na navegação inferior, acessível após a autenticação biométrica já existente.
Permite **criar**, **agendar**, **listar** e **cancelar** lembretes financeiros com título,
mensagem, data e hora. Exemplos: conta de luz, aluguel, fatura do cartão.
Cada lembrete tem ID próprio e status (Agendado, Disparado, Cancelado ou Não notificado).
O histórico permanece em `SharedPreferences` mesmo após fechar e reabrir o aplicativo.

- `app/src/main/java/com/financeapp/nativeapp/reminders/ReminderStore.kt` — modelo, histórico e status locais.
- `app/src/main/java/com/financeapp/nativeapp/reminders/ReminderScheduler.kt` — canal, permissão, alarmes locais e cancelamento.
- `app/src/main/java/com/financeapp/nativeapp/reminders/ReminderReceiver.kt` — recebe o alarme e publica a notificação.
- `app/src/main/java/com/financeapp/nativeapp/presentation/reminders/ReminderScreen.kt` — tela Compose (formulário + listagem + cancelamento).
- `MainActivity.kt` — apenas nova rota e item de navegação Lembretes.
- `FinanceApplication.kt` — criação do canal de notificações ao inicializar.
- `AndroidManifest.xml` — permissão `POST_NOTIFICATIONS` e receiver não exportado.

Nenhuma biblioteca adicional foi exigida; as dependências existentes de AndroidX já dão suporte.
O armazenamento de transações financeiras **continua no Room/Firebase**; só o histórico
de lembretes é salvo em `SharedPreferences`, conforme o slide da Aula 3.

## Como usar (VS Code → Android Studio)

1. Faça uma cópia da pasta do projeto original. Extraia o ZIP completo e abra a pasta **FinanceAPP_V2** no VS Code.
2. Para sincronizar, abra **a mesma pasta** no Android Studio (`File > Open`, selecione `FinanceAPP_V2`) e aguarde o Gradle Sync.
3. Conecte um celular Android com depuração USB ou use um emulador configurado e execute o módulo `app`.
4. Autentique-se pelo fluxo biométrico que já existia; toque na nova aba **Lembretes**.
5. Escreva título e mensagem, selecione data e hora futuras e toque em **Agendar lembrete**.
6. Se o Android 13+ solicitar permissão de notificações, conceda-a e toque em **Agendar lembrete novamente** (a permissão isoladamente não cria um alarme).
7. Deixe o aplicativo em segundo plano (não use *Forçar parada*) e observe o aviso. Registre a hora de criação, a hora prevista e a hora efetiva.
8. Repita com um segundo lembrete, teste horário passado e cancele um antes do disparo; confira os status no histórico.

## Tratamento de permissão e segurança

- Android 13 ou superior: a permissão `POST_NOTIFICATIONS` só é pedida quando necessária.
- Android 8 ou superior: o canal `Lembretes financeiros` é criado antes da publicação.
- Sem permissão ou com canal desabilitado: o formulário não confirma um agendamento inexistente.
- O recebimento de alarmes já agendados após revogação da permissão marca o status `BLOCKED`.
- O toque na notificação abre o app e preserva a barreira biométrica existente.
- Detalhes financeiros do lembrete são ocultados na versão pública da notificação da tela bloqueada.

## Limitações conhecidas / escopo da base didática

- Usa `AlarmManager.set`, portanto **não é alarme exato**: sistema e modo de economia de bateria podem atrasar o disparo.
- Os alarmes não são restaurados depois de reiniciar o celular. A lista persiste, mas ela sozinha não garante um novo disparo após reboot.
- A função *Forçar parada* pode impedir a execução dos alarmes.
- Este é um lembrete configurado **manualmente** pelo usuário. Não calcula vencimentos automaticamente das transações existentes.
- Não usa internet, Firebase, notificações push nem servidor para agendamento.
- Conteúdo dos avisos fica armazenado localmente em `SharedPreferences`; não inserir dados bancários sensíveis no título/mensagem.

## Testes / o que falta para entregar

O arquivo separado `FinanceAPP_Aula_3_Plano_de_Testes.xlsx` inclui 10 casos acumulados:
CT01–CT04 preservados da Aula 2 (biometria) + CT05–CT10 (notificações).
O documento tem critérios de aceite, técnicas, procedimento de teste, campo para
**resultado observado** e lugar para evidência. NÃO preencha resultados antes de executar.

- Executar testes em celular/emulador com versão do Android registrada.
- Fotografar/gravar o aviso publicado e o histórico com cancelamento.
- Preencher as execuções observadas, inclusive diferenças de horário e defeitos.
- Reexecutar os 4 casos biométricos para regressão.
- Publicar o código testado no GitHub (nenhum push foi executado neste ambiente).

**Validação realizada até aqui:** inspeção dos arquivos, consistência do Manifest e análise estática da integração. **Build e testes em Android real não foram executados neste ambiente**, porque não há Android SDK instalado.

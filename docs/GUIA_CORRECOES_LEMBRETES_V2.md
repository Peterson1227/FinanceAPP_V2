# FinanceAPP_V2 — Correções dos lembretes + ícone (revisão da Aula 3)

## O que foi corrigido

1. **Notificação do Android com aplicativo fora da tela**: o agendamento passa a usar
   `AlarmManager.setAndAllowWhileIdle()` e `PendingIntent` para um `BroadcastReceiver` do Manifest.
   O canal `Alertas financeiros` usa importância alta, para permitir alertas visíveis e sonoros
   conforme as configurações do telefone. Não exige internet nem serviço em primeiro plano.
   É **aproximado**: não promete minuto exato, nem funciona após **Forçar parada** pelo usuário.
2. **Persistência e recuperação**: os lembretes futuros salvos são reagendados ao abrir o app,
   depois de reiniciar o celular (`BOOT_COMPLETED`) e quando o APK é atualizado
   (`MY_PACKAGE_REPLACED`). Os que venceram com o celular desligado são classificados
   como "Não disparou"; não são falsamente marcados "Disparados".
3. **Histórico completo**: agora há filtros `Todos`, `Agendados`, `Disparados`, `Cancelados`
   e `Não enviados`. Registros anteriores da Aula 3 permanecem no mesmo `SharedPreferences`.
4. **Edição**: `Editar` permite ajustar título, mensagem e data/hora. `Salvar alterações e reagendar`
   mantém o ID e substitui o aviso pendente. Editar `Cancelado` ou `Disparado` agenda o lembrete
   novamente; o status atual será `Agendado`. Não existe criação de lançamento financeiro automática.
5. **Ícone FinanceAPP**: ícone adaptativo com moeda e gráfico crescente, acompanhado de
   um ícone simples para a barra de notificações.

## Arquivos alterados/adicionados

- `app/src/main/java/com/financeapp/nativeapp/reminders/ReminderScheduler.kt`
- `app/src/main/java/com/financeapp/nativeapp/reminders/ReminderReceiver.kt`
- `app/src/main/java/com/financeapp/nativeapp/reminders/ReminderStore.kt`
- `app/src/main/java/com/financeapp/nativeapp/reminders/ReminderRescheduleReceiver.kt` (novo)
- `app/src/main/java/com/financeapp/nativeapp/presentation/reminders/ReminderScreen.kt`
- `app/src/main/java/com/financeapp/nativeapp/FinanceApplication.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/drawable/ic_launcher_foreground.xml` (novo)
- `app/src/main/res/drawable/ic_stat_finance.xml` (novo)
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` (novo)
- `app/src/main/res/values/colors.xml` (novo)

O login biométrico, as transações, o banco Room, o Firebase e a navegação financeira
**não foram modificados**. Não são necessárias novas bibliotecas nem mudanças no Gradle.

## Como colocar a correção no projeto (sem perder seus dados)

1. Faça uma cópia de segurança de `D:\AplicativosNativoV2\FinanceAPP_V2`.
2. Use o arquivo **Somente_Alteracoes.zip** e **extraia na pasta PAI**
   `D:\AplicativosNativoV2\` (e não dentro de `FinanceAPP_V2`). O ZIP já contém
   a pasta `FinanceAPP_V2/`. Confirme substituir os arquivos existentes de mesmo nome.
   **Não apague** `.gradle`, `.git` ou os arquivos locais de SDK.
3. Abra **a pasta FinanceAPP_V2** no Android Studio e sincronize o Gradle.
4. Menu **Build > Generate App Bundles or APKs > Generate APKs**.
5. Clique `Locate`: em geral o novo APK fica em `app/build/outputs/apk/debug/app-debug.apk`.
6. Instale **por cima do APK antigo** no mesmo aparelho. O pacote continua
   `com.financeapp.nativeapp`; com a **mesma assinatura** do Android Studio, as
   preferências e o histórico ficam preservados.
   **Não desinstale** o app antigo: isso normalmente apaga os dados locais.
   Se o Android reclamar de assinatura incompatível, não desinstale sem fazer backup;
   considere gerar o APK com a chave original.

## Testes importantes no celular (fora do Android Studio)

1. Abra Configurações > Apps > FinanceAPP > Notificações e ative as notificações e o canal
   **Alertas financeiros**. No Android 13+, aceite a permissão ao cadastrar o lembrete.
2. Agende um lembrete para **5 a 10 minutos no futuro**. Observe a hora prevista.
3. Saia para a tela inicial ou remova o app da visão de recentes **sem usar Forçar parada**.
   Apague a tela e confira a **barra de notificações** ao atingir o horário.
   A entrega pode atrasar por Doze ou otimizações do fabricante.
4. Reabra o app, autentique, abra Lembretes > **Disparados**. Confira título e status.
5. Crie um segundo lembrete, toque em **Editar**, altere horário e mensagem,
   salve e confira que apenas um alerta referente ao novo horário chega.
6. Crie um terceiro, toque em **Cancelar**, abra filtro **Cancelados** e confirme que
   o aviso não chega no horário cancelado.
7. Edite um item já disparado/cancelado com hora futura; confira que volta para **Agendados**.
8. Reinicie o celular com um compromisso futuro; quando o telefone iniciar, confira se
   o aviso volta a ficar agendado (sem precisar abrir o app).
9. Teste com notificações negadas: o formulário deve informar o problema, não salvar
   um agendamento que não foi criado.

**Limites normais do Android:** `setAndAllowWhileIdle()` não é exato. Modo de economia de
bateria e limitações de fabricantes podem atrasar ou bloquear alertas. **Forçar parada**
impede o sistema de entregar alarmes até o usuário abrir o app novamente. Fechar a tela
ou voltar para a Home é diferente de Forçar parada. Se o dispositivo bloquear notificações
na tela de bloqueio, verifique a configuração de privacidade do Android.

## Evidências para a disciplina

Capture a notificação com o app fora da tela, o filtro Disparados, um lembrete cancelado,
um lembrete editado/reagendado e o horário observado. Anote se o disparo atrasou.
Não marque o teste como aprovado apenas porque o APK compilou: é preciso executá-lo.

## Verificação realizada nesta revisão

- Mantidos os arquivos do módulo de transações, Firebase e biometria.
- Revisadas as APIs Android e as permissões declaradas.
- Validada a estrutura dos recursos XML e o arquivo ZIP.
- **Não houve compilação APK nem testes reais neste ambiente** por falta do Android SDK.
  O primeiro build e a verificação no dispositivo são necessários.

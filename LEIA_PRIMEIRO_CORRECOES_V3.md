# FinanceAPP_V2 — atualização de lembretes (versão 3)

Esta atualização é incremental. **Não remova nem desinstale** a versão atual do aplicativo para aplicar a correção. O código de receitas, despesas, banco de dados Room, autenticação biométrica, Firebase, navegação e ícone foi preservado.

## O que mudou

- **Excluir**: todos os lembretes (agendados, disparados, cancelados ou não enviados) agora têm botão `Excluir`, com confirmação. Diferente de `Cancelar`, esta ação exclui o item definitivamente do histórico, cancela o alarme pendente e remove a notificação associada.
- **Alarmes em segundo plano**: a arquitetura mantém `AlarmManager` + `BroadcastReceiver` registrado no Manifest; a tela não precisa permanecer aberta. Se o usuário **autorizar o acesso especial “Alarmes e lembretes”** no Android 12+, novos avisos usam `setExactAndAllowWhileIdle()`. Sem essa autorização, o app continua usando `setAndAllowWhileIdle()` (horário aproximado), para não bloquear o cadastro.
- **Configurações mais acessíveis**: uma nova seção em Lembretes abre os detalhes do FinanceAPP no Android para ajustar **Notificações** e **Bateria / Uso em segundo plano**. Também há botão opcional para permitir alarmes precisos.
- **Restauração**: avisos futuros são reagendados ao abrir o app, após atualização do APK ou reinício do celular, ao mudar hora/fuso e quando a autorização de alarmes precisos é concedida.

## Limitações importantes do Android

**Fechar a tela** ou **sair para a tela inicial** não equivale a **Forçar parada**. Normalmente, retirar a janela do app da lista de recentes também não cancela os alarmes em Android padrão. No entanto, alguns fabricantes fazem uma parada agressiva ao retirar o app de recentes ou em modo de economia de bateria.

**Não existe solução local suportada que garanta o disparo de notificações depois de “Forçar parada”** em Configurações > Apps, até que o usuário abra o aplicativo novamente. Android 15+ cancela `PendingIntent`s de apps em estado *stopped*. A permissão de alarmes precisos NÃO contorna isso. O app também não pode prometer comportamento idêntico a aplicativos que usam servidores e Firebase Cloud Messaging, e mesmo push remoto está sujeito ao estado de parada forçada.

Em Android 12+, a opção de alarmes precisos é um acesso especial controlado pelo usuário e pode estar desativada após instalação. Para distribuição na Play Store, o uso dessa permissão depende das regras da loja; esta revisão atende ao contexto de APK acadêmico.

## Instalar a correção sem perder dados

1. Faça um **backup** da pasta `D:\AplicativosNativoV2\FinanceAPP_V2`.
2. Baixe **Somente_Alteracoes_V3.zip** e extraia **na pasta pai** `D:\AplicativosNativoV2\`, confirmando a substituição. O ZIP contém a pasta `FinanceAPP_V2/`.
3. Abra `D:\AplicativosNativoV2\FinanceAPP_V2` no Android Studio.
4. Build > Generate App Bundles or APKs > Generate APKs.
5. Instale `app/build/outputs/apk/debug/app-debug.apk` **por cima do APK existente**. Deve ser a mesma assinatura usada anteriormente. Se o Android pedir para desinstalar antes, pare e verifique as assinaturas: a desinstalação apaga dados locais.
6. Abra FinanceAPP > **Lembretes** e ajuste as opções abaixo.

## Ajustes recomendados no celular para testar app fechado

- Em **Lembretes**, abra **Configurações de notificações e bateria**. Na tela do Android, habilite **Notificações** > **Alertas financeiros**.
- Em **Bateria** / **Uso da bateria do app** (nomes variam por fabricante), permita **uso em segundo plano**. Em alguns aparelhos a opção chama-se **Sem restrições** ou **Não otimizado**. Use somente se for adequado ao seu dispositivo.
- Se disponível, toque em **Permitir alarmes precisos (opcional)** e conceda o acesso especial **Alarmes e lembretes**. Volte ao FinanceAPP para reagendar os lembretes futuros.

## Testes para documentar na Aula 3

1. **Excluir agendado:** agende um lembrete para 10 min no futuro, toque em Excluir > Voltar (deve preservar), repita e confirme Excluir (deve sumir e não notificar).
2. **Excluir histórico:** exclua um item de Disparados e outro de Cancelados; confirme que ambos somem do histórico e a notificação publicada é removida.
3. **Fechar normalmente:** agende para 10 min no futuro, saia pela tecla Home ou gesto, bloqueie a tela e verifique a barra de notificações.
4. **Remover dos recentes:** agende um novo aviso e deslize a janela do FinanceAPP na lista de apps recentes, sem usar Forçar parada; observe se chega. Caso não chegue, verifique restrição de bateria e fabricante.
5. **Forçar parada (limitação conhecida):** agende, use Configurações > Apps > FinanceAPP > Forçar parada. O aviso NÃO é garantido (bloqueio do Android). Reabra o app antes de criar novos avisos; registre essa limitação em vez de marcar o caso como aprovado.
6. **Regressão:** edite e reagende um lembrete, cancele outro e confirme biometria, transações e ícone sem alterações.

**Validação:** os arquivos foram revisados estruturalmente. Não foi gerado nem testado um APK nesta sessão; compile e execute no Android Studio antes da entrega.

Fontes técnicas: https://developer.android.com/develop/background-work/services/alarms e https://developer.android.com/about/versions/15/behavior-changes-all

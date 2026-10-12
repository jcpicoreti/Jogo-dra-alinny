# Preparação e Otimização para Google Play Console

Este plano detalha as alterações necessárias para corrigir erros de construção (`lint`), otimizar o tamanho do aplicativo e garantir que o projeto cumpra os requisitos básicos exigidos pela Google Play Store para submissão.

## Open Questions
- Você já possui os ícones do aplicativo (`ic_launcher`) com o design final na pasta de recursos? Os arquivos padrões podem gerar avisos de forma, mas costumam ser aceitos para testes. Recomenda-se gerar ícones finais usando o *Image Asset Studio* do Android Studio antes de lançar.

## Proposed Changes

### [app/build.gradle.kts]
- **Minificação e Ofuscação:** Habilitar `isMinifyEnabled = true` e `isShrinkResources = true` para o tipo de build `release`. Isso é mandatório para reduzir drasticamente o tamanho do arquivo que vai para a loja (o Android App Bundle) e dificultar a engenharia reversa do seu jogo.
- **Target SDK:** Mudar `targetSdk` de `36` para `35`. A Play Store exige no mínimo API 34, mas o SDK 36 (Android 16) ainda não é totalmente estável, o que pode causar alertas ou rejeições prematuras. O SDK 35 é o mais recomendado no momento atual.

### [app/src/main/java/com/example/MainActivity.kt]
- Adicionar anotação `@SuppressLint("RestrictedApi")` no método `dispatchKeyEvent`. O analisador estático (Lint) está acusando erro nesta sobrescrita por conta de bibliotecas internas da Google, travando o build de release. Ignorar esse aviso especificamente liberará a geração do pacote final.

### [app/src/main/AndroidManifest.xml]
- Remover o atributo `android:label` redundante da tag `<activity>`, que já é herdado da aplicação.

### [Vários arquivos de UI (GameScreen.kt, MatchEndScreen.kt, RankingScreen.kt)]
- Corrigir bugs em formatação de strings `String.format("%02d:%02d", minutes, seconds)`. Na Play Store, a formatação de tempo assim sem passar `Locale.getDefault()` pode causar comportamentos inesperados em certos idiomas e gera avisos do Lint. Será corrigido para `String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)`.

## Verification Plan
1. Rodar `:app:lintRelease` para garantir que nenhum erro impeça a criação do pacote final.
2. Rodar `:app:bundleRelease` (a partir do Build -> Generate Signed Bundle) para validar a construção de arquivos `.aab` (Android App Bundle), obrigatórios no Play Console.
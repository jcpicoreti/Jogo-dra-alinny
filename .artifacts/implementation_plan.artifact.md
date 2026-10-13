# Plano de Implementação: Otimizações Finais, Gráficos e Dificuldade

Este plano detalha as alterações necessárias para adicionar melhorias visuais aos personagens (incluindo o tom de pele da Raynara), ajuste no texto do Julio, interface de ícones de armas, adição de níveis de dificuldade, avanço automático de fases e checagem da viabilidade de evolução das armas.

## Open Questions
- Ícones das armas: Como o projeto não possui arquivos de imagem próprios para cada arma, substituirei os emojis atuais por ícones estilizados vetoriais nativos do Compose (`Material Icons`) combinados com cores distintas, garantindo um visual muito mais profissional. Está de acordo?
- Níveis de Dificuldade: Criarei as dificuldades: "Fácil" (0.8x status inimigo), "Normal" (1.0x) e "Difícil" (1.5x status e mais inimigos). O multiplicador afetará a vida e o dano dos germes.

## Proposed Changes

### [app/src/main/java/com/example/data/model/GameModels.kt]
- **Texto Julio:** Alterar `description` do `JULIO` de "Oficial da marinha..." para "Militar da marinha...".
- **Nova Enum `Difficulty`:** Adicionar enumeração de dificuldades com os multiplicadores de Vida e Dano.
- **Fusões Garantidas:** O jogo possui 15 armas e 15 itens passivos emparelhados perfeitamente. Garantirei que o motor do jogo (em `generateLevelUpChoices`) sempre ofereça o item passivo necessário para a arma caso o jogador alcance o nível 4 da arma (se ainda não possuir o passivo).

### [app/src/main/java/com/example/game/GameEngine.kt]
- **Garantia de Evolução:** Ajustar a lógica no `generateLevelUpChoices` para aumentar absurdamente o peso de *drop* do item passivo parceiro da arma assim que a arma chega ao nível alto, para garantir que o jogador nunca fique preso sem conseguir evoluir a arma.
- **Aplicar Dificuldade:** O `GameEngine` passará a receber a `Difficulty` no construtor e multiplicará `baseHp` e `baseDamage` dos germes na hora do *spawn*.

### [app/src/main/java/com/example/ui/viewmodel/GameViewModel.kt]
- **Avanço Automático:** No momento em que a partida finaliza (`stopGameLoop`), se o jogador for vitorioso, alterar a fase selecionada (`_selectedStage.value`) automaticamente para a próxima destravada.
- **Seleção de Dificuldade:** Criar estado (`_selectedDifficulty`) para a UI se comunicar com a *Engine*.

### [app/src/main/java/com/example/ui/screens/StageSelectScreen.kt]
- **Interface de Dificuldade:** Incluir botões para o jogador escolher a dificuldade da partida (Fácil, Normal, Difícil) antes de iniciar a partida.

### [app/src/main/java/com/example/ui/graphics/DentalCanvasRenderer.kt]
- **Melhorias Gráficas nos Personagens:** Refatorar `drawPlayer` para desenhar sombras (*shading*), detalhes de roupas específicos e luzes nos personagens.
- **Cor da Raynara:** Alterar a paleta de cores base da `RAYNARA` para apresentar o tom de pele pardo/marrom (`0xFF8D5524` ou equivalente), com sombreado apropriado.

### [app/src/main/java/com/example/ui/screens/GameScreen.kt]
- **Ícones Profissionais (HUD e Level Up):** Remover emojis e usar `Material Theme Icons` com fundos arredondados, bordas e gradientes de cores baseados na raridade/tipo da arma.

## Verification Plan
1. Rodar `:app:lintRelease` novamente para garantir zero bloqueios de Play Console.
2. Construir e executar o jogo (`:app:assembleDebug`) para testar o avanço de estágio, a garantia da receita de arma no sub-nível 5, o texto corrigido, a seleção de dificuldade e as melhorias gráficas da Raynara.
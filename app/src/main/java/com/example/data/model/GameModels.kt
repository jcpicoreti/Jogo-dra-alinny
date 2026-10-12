package com.example.data.model

import androidx.compose.ui.graphics.Color

// Character Definitions
enum class CharacterId(
    val displayName: String,
    val title: String,
    val description: String,
    val baseHp: Float,
    val baseSpeed: Float,
    val baseDamageMultiplier: Float,
    val baseMagnetRadius: Float,
    val baseCritChance: Float,
    val startingWeapon: WeaponType,
    val specialTraitDescription: String
) {
    DRA_ALINNY(
        displayName = "Dra. Alinny",
        title = "Dentista Heroína",
        description = "Cirurgiã-dentista dedicada e destemida. Perfeita no combate a bactérias com técnicas avançadas.",
        baseHp = 100f,
        baseSpeed = 1.0f,
        baseDamageMultiplier = 1.2f,
        baseMagnetRadius = 120f,
        baseCritChance = 0.15f,
        startingWeapon = WeaponType.ESCOVA_DENTE,
        specialTraitDescription = "+20% Dano de Armas e +15% Ganho de XP de Higiene."
    ),
    LORENZO(
        displayName = "Lorenzo",
        title = "Garoto Hiperativo (7 anos)",
        description = "Menino loirinho cheio de energia que adora pasta de dente sabor chiclete e corre sem parar.",
        baseHp = 80f,
        baseSpeed = 1.35f,
        baseDamageMultiplier = 1.0f,
        baseMagnetRadius = 130f,
        baseCritChance = 0.10f,
        startingWeapon = WeaponType.PASTA_EXPLOSIVA,
        specialTraitDescription = "+35% Velocidade de Movimento e bombas de espuma de chiclete."
    ),
    RAYNARA(
        displayName = "Raynara",
        title = "Mestre dos Braquetes (14 anos)",
        description = "Adolescente estilosa de aparelho fixo colorido que transforma braquetes e elásticos em armas afiadas.",
        baseHp = 90f,
        baseSpeed = 1.1f,
        baseDamageMultiplier = 1.15f,
        baseMagnetRadius = 110f,
        baseCritChance = 0.30f,
        startingWeapon = WeaponType.FIO_DENTAL,
        specialTraitDescription = "+30% Chance Crítica e +25% de Velocidade de Disparo."
    ),
    JULIO(
        displayName = "Julio",
        title = "Militar da Marinha",
        description = "Oficial da marinha com disciplina de aço. Higiene bucal tática de alta resistência.",
        baseHp = 160f,
        baseSpeed = 0.9f,
        baseDamageMultiplier = 1.25f,
        baseMagnetRadius = 100f,
        baseCritChance = 0.08f,
        startingWeapon = WeaponType.ENXAGUANTE_AURA,
        specialTraitDescription = "+60% de Vida Máxima e -20% de dano recebido (Armadura Marinha)."
    ),
    RICK(
        displayName = "Rick",
        title = "Border Collie Clássico",
        description = "Cãozinho preto e branco inteligentíssimo que fareja e busca itens de higiene a longa distância.",
        baseHp = 95f,
        baseSpeed = 1.25f,
        baseDamageMultiplier = 1.05f,
        baseMagnetRadius = 240f,
        baseCritChance = 0.12f,
        startingWeapon = WeaponType.BROCA_ODONTO,
        specialTraitDescription = "+100% Raio Magnético de coleta de loot e agilidade canina."
    ),
    BART(
        displayName = "Bart",
        title = "Border Collie Red",
        description = "Cãozinho red de pelagem avermelhada e latido estrondoso que afasta os germes mais duros.",
        baseHp = 120f,
        baseSpeed = 1.2f,
        baseDamageMultiplier = 1.3f,
        baseMagnetRadius = 140f,
        baseCritChance = 0.20f,
        startingWeapon = WeaponType.JATO_BICARBONATO,
        specialTraitDescription = "+30% Força de Ataque e ondas sonoras de latido sônico."
    )
}

// Stages
enum class StageId(
    val stageNumber: Int,
    val title: String,
    val subtitle: String,
    val durationSeconds: Int,
    val bossName: String,
    val bossDescription: String,
    val themeColorHex: Long,
    val groundColorHex: Long
) {
    STAGE_1_ESMALTE(
        stageNumber = 1,
        title = "Superfície do Esmalte",
        subtitle = "Primeira linha de defesa dentária",
        durationSeconds = 300,
        bossName = "Cárie Gigante Negra",
        bossDescription = "Uma cárie invasiva armada com ácidos corrosivos e tentáculos de placa.",
        themeColorHex = 0xFF0284C7,
        groundColorHex = 0xFFF0FDF4
    ),
    STAGE_2_GENGIVA(
        stageNumber = 2,
        title = "Sulco Gengival",
        subtitle = "Zona vascularizada e inflamada",
        durationSeconds = 300,
        bossName = "Gengivite Furiosa",
        bossDescription = "Monstro pulsante avermelhado que espalha inflamação e toxinas periódicas.",
        themeColorHex = 0xFFE11D48,
        groundColorHex = 0xFFFFF1F2
    ),
    STAGE_3_CANAL(
        stageNumber = 3,
        title = "Canal Radicular",
        subtitle = "As profundezas da polpa dentária",
        durationSeconds = 300,
        bossName = "Abcesso Periapical",
        bossDescription = "Bactéria anaeróbia gigante com bolhas de pus tóxico e ondas de choque.",
        themeColorHex = 0xFF7C3AED,
        groundColorHex = 0xFFFAF5FF
    ),
    STAGE_4_APARELHO(
        stageNumber = 4,
        title = "Arena dos Braquetes",
        subtitle = "Labirinto de fios e elásticos metálicos",
        durationSeconds = 300,
        bossName = "Biofilme Oculto de Braquete",
        bossDescription = "Aglomerado de restos de comida calcificados escondido entre os fios do aparelho.",
        themeColorHex = 0xFF0D9488,
        groundColorHex = 0xFFF0FDFA
    ),
    STAGE_5_TARTARO(
        stageNumber = 5,
        title = "Cidadela do Tártaro",
        subtitle = "A fortaleza de cálculo dental petrificado",
        durationSeconds = 300,
        bossName = "Tártaro Titânico & Siso Sombrio",
        bossDescription = "O soberano das infecções orais, couraça de pedra de tártaro e siso impactado!",
        themeColorHex = 0xFFD97706,
        groundColorHex = 0xFFFFFBEB
    ),
    STAGE_INFINITE(
        stageNumber = 6,
        title = "Plantão Noturno Infinito",
        subtitle = "Emergência 24h sem fim! Teste seus limites",
        durationSeconds = 999999,
        bossName = "Horda dos Chefões Supremos",
        bossDescription = "Chefões contínuos a cada 2 minutos com dificuldade crescente sem limites.",
        themeColorHex = 0xFF1E293B,
        groundColorHex = 0xFFF8FAFC
    )
}

// Dental Weapons (15 unique weapons)
enum class WeaponType(
    val displayName: String,
    val description: String,
    val baseDamage: Float,
    val cooldownSeconds: Float,
    val rangeRadius: Float,
    val iconName: String,
    val maxLevel: Int = 5,
    val evolutionItem: PassiveType? = null,
    val evolvedWeapon: EvolutionType? = null,
    val unlockedAtStage: Int = 1
) {
    ESCOVA_DENTE(
        displayName = "Escova de Dente",
        description = "Gira cerdas de higiene ao redor da heroína, empurrando e limpando bactérias.",
        baseDamage = 24f,
        cooldownSeconds = 1.2f,
        rangeRadius = 95f,
        iconName = "toothbrush",
        maxLevel = 5,
        evolutionItem = PassiveType.PASTA_FLUOR,
        evolvedWeapon = EvolutionType.ESCOVA_ULTRASSONICA,
        unlockedAtStage = 1
    ),
    FIO_DENTAL(
        displayName = "Fio Dental",
        description = "Chicoteia linhas afiadas de fio de náilon que cortam fileiras de germes.",
        baseDamage = 38f,
        cooldownSeconds = 1.5f,
        rangeRadius = 240f,
        iconName = "floss",
        maxLevel = 5,
        evolutionItem = PassiveType.ESPELHO_PRECISAO,
        evolvedWeapon = EvolutionType.FIO_LASER_TITANIO,
        unlockedAtStage = 1
    ),
    JATO_BICARBONATO(
        displayName = "Jato de Bicarbonato",
        description = "Dispara rajadas de água sob pressão e micropartículas perfurantes.",
        baseDamage = 20f,
        cooldownSeconds = 0.5f,
        rangeRadius = 260f,
        iconName = "water_jet",
        maxLevel = 5,
        evolutionItem = PassiveType.FIO_EXTRA_FORTE,
        evolvedWeapon = EvolutionType.TSUNAMI_BICARBONATO,
        unlockedAtStage = 1
    ),
    BROCA_ODONTO(
        displayName = "Broca Diamantada",
        description = "Pontas diamantadas giratórias de alta rotação que orbitam destruindo placas duras.",
        baseDamage = 28f,
        cooldownSeconds = 1.8f,
        rangeRadius = 115f,
        iconName = "drill",
        maxLevel = 5,
        evolutionItem = PassiveType.MATRIZ_ACO,
        evolvedWeapon = EvolutionType.BROCA_INDELAVEL,
        unlockedAtStage = 1
    ),
    ENXAGUANTE_AURA(
        displayName = "Aura Antisséptica",
        description = "Campo aromático de menta que causa dano constante a qualquer germe próximo.",
        baseDamage = 14f,
        cooldownSeconds = 0.35f,
        rangeRadius = 135f,
        iconName = "mouthwash_aura",
        maxLevel = 5,
        evolutionItem = PassiveType.ENXAGUANTE_MENTA,
        evolvedWeapon = EvolutionType.VENTANIA_CRIOGENICA,
        unlockedAtStage = 1
    ),
    LUZ_FOTOPOLIMERIZADORA(
        displayName = "Luz UV Curing",
        description = "Feixe concentrado de luz azul que incinera linhas inteiras de germes.",
        baseDamage = 80f,
        cooldownSeconds = 2.4f,
        rangeRadius = 330f,
        iconName = "uv_light",
        maxLevel = 5,
        evolutionItem = PassiveType.OCULOS_PROTECAO,
        evolvedWeapon = EvolutionType.RAIO_SOLAR_ODONTO,
        unlockedAtStage = 2
    ),
    PASTA_EXPLOSIVA(
        displayName = "Bomba de Pasta",
        description = "Dispara tubos e bolotas de pasta de dente que explodem em espuma refrescante.",
        baseDamage = 48f,
        cooldownSeconds = 1.9f,
        rangeRadius = 210f,
        iconName = "toothpaste_bomb",
        maxLevel = 5,
        evolutionItem = PassiveType.GEL_XILITOL,
        evolvedWeapon = EvolutionType.BOMBARDEIO_XILITOL,
        unlockedAtStage = 2
    ),
    ESPELHO_CLINICO(
        displayName = "Espelho Refletor",
        description = "Reflete pulsos de luz dental e repele germes com feixes ofuscantes.",
        baseDamage = 30f,
        cooldownSeconds = 1.3f,
        rangeRadius = 190f,
        iconName = "dental_mirror",
        maxLevel = 5,
        evolutionItem = PassiveType.SELANTE_FOSSULAS,
        evolvedWeapon = EvolutionType.ESPELHO_PRISMATICO,
        unlockedAtStage = 3
    ),
    SERINGA_ANESTESICA(
        displayName = "Seringa Anestésica",
        description = "Dispara dardos tranquilizantes que anestesiam e reduzem drasticamente a velocidade dos germes.",
        baseDamage = 32f,
        cooldownSeconds = 1.4f,
        rangeRadius = 250f,
        iconName = "syringe",
        maxLevel = 5,
        evolutionItem = PassiveType.MASCARA_CIRURGICA,
        evolvedWeapon = EvolutionType.NEBULIZADOR_CIRURGICO,
        unlockedAtStage = 3
    ),
    SUGADOR_SALIVA(
        displayName = "Sugador Cirúrgico",
        description = "Vórtice giratório de sucção que drena a energia vital e puxa germes para o centro.",
        baseDamage = 22f,
        cooldownSeconds = 1.6f,
        rangeRadius = 170f,
        iconName = "saliva_ejector",
        maxLevel = 5,
        evolutionItem = PassiveType.PASTILHA_REVELADORA,
        evolvedWeapon = EvolutionType.VORTEX_GRAVITACIONAL,
        unlockedAtStage = 4
    ),
    CURETA_GRACEY(
        displayName = "Cureta Periodontal",
        description = "Lâmina semicircular de aço cirúrgico que raspa camadas inteiras de bactérias em arco duplo.",
        baseDamage = 42f,
        cooldownSeconds = 1.5f,
        rangeRadius = 180f,
        iconName = "curette",
        maxLevel = 5,
        evolutionItem = PassiveType.LUVAS_LATEX,
        evolvedWeapon = EvolutionType.GUILHOTINA_PERIODONTAL,
        unlockedAtStage = 4
    ),
    SPRAY_CLOREXIDINA(
        displayName = "Spray de Clorexidina",
        description = "Névoa desinfetante potente que queima continuamente bactérias na área frontal.",
        baseDamage = 16f,
        cooldownSeconds = 0.6f,
        rangeRadius = 220f,
        iconName = "spray_bottle",
        maxLevel = 5,
        evolutionItem = PassiveType.EXTRATO_PROPOLIS,
        evolvedWeapon = EvolutionType.NEVOA_BIOATIVA,
        unlockedAtStage = 4
    ),
    FITA_EXPANSIVA(
        displayName = "Fita Dental Expansível",
        description = "Ondas circulares expansivas de fita odontológica que empurram multidões.",
        baseDamage = 36f,
        cooldownSeconds = 2.2f,
        rangeRadius = 200f,
        iconName = "dental_tape",
        maxLevel = 5,
        evolutionItem = PassiveType.RESINA_COMPOSTA,
        evolvedWeapon = EvolutionType.REDE_CONTENCAO_TITANIO,
        unlockedAtStage = 5
    ),
    ULTRASSOM_ODONTO(
        displayName = "Ultrassom Odontológico",
        description = "Ponteira micro-oscilatória que emite pulsos de choque que estilhaçam cascas duras de bactérias.",
        baseDamage = 50f,
        cooldownSeconds = 2.0f,
        rangeRadius = 280f,
        iconName = "ultrasonic_scaler",
        maxLevel = 5,
        evolutionItem = PassiveType.VITAMINA_C,
        evolvedWeapon = EvolutionType.EMISSOR_CAVITACAO,
        unlockedAtStage = 5
    ),
    PINCA_CIRURGICA(
        displayName = "Pinça Extratora",
        description = "Pinça mecânica que esmaga os inimigos de maior porte ao redor com dano devastador.",
        baseDamage = 90f,
        cooldownSeconds = 2.8f,
        rangeRadius = 260f,
        iconName = "forceps",
        maxLevel = 5,
        evolutionItem = PassiveType.AVENTAL_CHUMBO,
        evolvedWeapon = EvolutionType.GARRA_EXTRATORA_PESADA,
        unlockedAtStage = 5
    )
}

// 15 Unique Fusions (Fusões Lendárias: 1 Arma Nível Max + 1 Equipamento)
enum class EvolutionType(
    val displayName: String,
    val description: String,
    val damage: Float,
    val cooldownSeconds: Float,
    val rangeRadius: Float,
    val requiredPassive: PassiveType
) {
    ESCOVA_ULTRASSONICA(
        displayName = "Escova Elétrica Ultrassônica",
        description = "FUSÃO LENDÁRIA! Vibra em alta frequência supersônica contínua, vaporizando germes em 360° com ondas e espuma densa!",
        damage = 65f,
        cooldownSeconds = 0.45f,
        rangeRadius = 170f,
        requiredPassive = PassiveType.PASTA_FLUOR
    ),
    FIO_LASER_TITANIO(
        displayName = "Fio Dental de Titânio Laser",
        description = "FUSÃO LENDÁRIA! Feixes entrelaçados de titânio energizado que fatiam a tela inteira com queimadura antisséptica!",
        damage = 100f,
        cooldownSeconds = 0.9f,
        rangeRadius = 390f,
        requiredPassive = PassiveType.ESPELHO_PRECISAO
    ),
    TSUNAMI_BICARBONATO(
        displayName = "Tsunami de Bicarbonato",
        description = "FUSÃO LENDÁRIA! Dilúvio colossal de microesferas de bicarbonato que limpa a tela e empurra todas as hordas.",
        damage = 55f,
        cooldownSeconds = 0.4f,
        rangeRadius = 320f,
        requiredPassive = PassiveType.FIO_EXTRA_FORTE
    ),
    BROCA_INDELAVEL(
        displayName = "Broca Indelével de Titânio",
        description = "FUSÃO LENDÁRIA! Quatro brocas orbitais perpétuas de carboneto de tungstênio que nunca param de triturar.",
        damage = 75f,
        cooldownSeconds = 0.2f,
        rangeRadius = 140f,
        requiredPassive = PassiveType.MATRIZ_ACO
    ),
    VENTANIA_CRIOGENICA(
        displayName = "Ventania Criogênica Menta",
        description = "FUSÃO LENDÁRIA! Aura glacial ártica que congela e estilhaça germes continuamente restaurando a saúde bucal.",
        damage = 38f,
        cooldownSeconds = 0.25f,
        rangeRadius = 180f,
        requiredPassive = PassiveType.ENXAGUANTE_MENTA
    ),
    RAIO_SOLAR_ODONTO(
        displayName = "Supernova de Plasma UV",
        description = "FUSÃO LENDÁRIA! Três feixes simultâneos de plasma azul de altíssima intensidade que desintegram bactérias.",
        damage = 180f,
        cooldownSeconds = 1.6f,
        rangeRadius = 400f,
        requiredPassive = PassiveType.OCULOS_PROTECAO
    ),
    BOMBARDEIO_XILITOL(
        displayName = "Bombardeio de Chiclete Doce",
        description = "FUSÃO LENDÁRIA! Chuva contínua de meteoros de chiclete refrescante que criam crateras explosivas curativas.",
        damage = 110f,
        cooldownSeconds = 1.3f,
        rangeRadius = 260f,
        requiredPassive = PassiveType.GEL_XILITOL
    ),
    ESPELHO_PRISMATICO(
        displayName = "Prisma Sagrado Refletor",
        description = "FUSÃO LENDÁRIA! Anel de espelhos que disparam feixes prismáticos multicoloridos que ricocheteiam infinitamente.",
        damage = 85f,
        cooldownSeconds = 0.9f,
        rangeRadius = 280f,
        requiredPassive = PassiveType.SELANTE_FOSSULAS
    ),
    NEBULIZADOR_CIRURGICO(
        displayName = "Nebulizador Anestésico Tático",
        description = "FUSÃO LENDÁRIA! Nuvem densa de aerossol que paralisa completamente grupos de bactérias e drena vida.",
        damage = 70f,
        cooldownSeconds = 1.0f,
        rangeRadius = 270f,
        requiredPassive = PassiveType.MASCARA_CIRURGICA
    ),
    VORTEX_GRAVITACIONAL(
        displayName = "Vórtice Singular Quântico",
        description = "FUSÃO LENDÁRIA! Buraco negro cirúrgico que suga todos os germes da arena, engolindo-os e gerando moedas.",
        damage = 60f,
        cooldownSeconds = 1.2f,
        rangeRadius = 250f,
        requiredPassive = PassiveType.PASTILHA_REVELADORA
    ),
    GUILHOTINA_PERIODONTAL(
        displayName = "Guilhotina Periodontal Relâmpago",
        description = "FUSÃO LENDÁRIA! Lâminas duplas ultra-velozes com 100% de chance crítica que decapitam germes instantaneamente.",
        damage = 130f,
        cooldownSeconds = 0.9f,
        rangeRadius = 240f,
        requiredPassive = PassiveType.LUVAS_LATEX
    ),
    NEVOA_BIOATIVA(
        displayName = "Névoa Bioativa de Própolis",
        description = "FUSÃO LENDÁRIA! Névoa dourada de própolis que cura o herói enquanto dissolve bactérias de forma permanente.",
        damage = 50f,
        cooldownSeconds = 0.4f,
        rangeRadius = 260f,
        requiredPassive = PassiveType.EXTRATO_PROPOLIS
    ),
    REDE_CONTENCAO_TITANIO(
        displayName = "Rede de Contenção Maxilar",
        description = "FUSÃO LENDÁRIA! Anéis expansivos de titânio que aprisionam hordas inteiras e implodem no centro.",
        damage = 95f,
        cooldownSeconds = 1.5f,
        rangeRadius = 280f,
        requiredPassive = PassiveType.RESINA_COMPOSTA
    ),
    EMISSOR_CAVITACAO(
        displayName = "Canhão de Cavitação Acústica",
        description = "FUSÃO LENDÁRIA! Ondas de choque supersônicas que implodem membranas celulares de todos os germes na tela.",
        damage = 140f,
        cooldownSeconds = 1.4f,
        rangeRadius = 360f,
        requiredPassive = PassiveType.VITAMINA_C
    ),
    GARRA_EXTRATORA_PESADA(
        displayName = "Garra Extratora Mecatrônica",
        description = "FUSÃO LENDÁRIA! Braço mecânico titânico que esmaga os maiores germes e chefões com dano colossal crítico.",
        damage = 250f,
        cooldownSeconds = 1.8f,
        rangeRadius = 300f,
        requiredPassive = PassiveType.AVENTAL_CHUMBO
    );

    val baseWeapon: WeaponType
        get() = when (this) {
            ESCOVA_ULTRASSONICA -> WeaponType.ESCOVA_DENTE
            FIO_LASER_TITANIO -> WeaponType.FIO_DENTAL
            TSUNAMI_BICARBONATO -> WeaponType.JATO_BICARBONATO
            BROCA_INDELAVEL -> WeaponType.BROCA_ODONTO
            VENTANIA_CRIOGENICA -> WeaponType.ENXAGUANTE_AURA
            RAIO_SOLAR_ODONTO -> WeaponType.LUZ_FOTOPOLIMERIZADORA
            BOMBARDEIO_XILITOL -> WeaponType.PASTA_EXPLOSIVA
            ESPELHO_PRISMATICO -> WeaponType.ESPELHO_CLINICO
            NEBULIZADOR_CIRURGICO -> WeaponType.SERINGA_ANESTESICA
            VORTEX_GRAVITACIONAL -> WeaponType.SUGADOR_SALIVA
            GUILHOTINA_PERIODONTAL -> WeaponType.CURETA_GRACEY
            NEVOA_BIOATIVA -> WeaponType.SPRAY_CLOREXIDINA
            REDE_CONTENCAO_TITANIO -> WeaponType.FITA_EXPANSIVA
            EMISSOR_CAVITACAO -> WeaponType.ULTRASSOM_ODONTO
            GARRA_EXTRATORA_PESADA -> WeaponType.PINCA_CIRURGICA
        }

    val unlockedAtStage: Int
        get() = maxOf(baseWeapon.unlockedAtStage, requiredPassive.unlockedAtStage)
}

// 15 Passive Oral Hygiene Equipment Items
enum class PassiveType(
    val displayName: String,
    val description: String,
    val iconName: String,
    val maxLevel: Int = 5,
    val perLevelBonus: String,
    val unlockedAtStage: Int = 1
) {
    PASTA_FLUOR(
        displayName = "Pasta com Flúor",
        description = "Fortalece o esmalte dentário e aumenta o poder de todos os ataques.",
        iconName = "fluoride_paste",
        perLevelBonus = "+15% Dano Geral",
        unlockedAtStage = 1
    ),
    ESPELHO_PRECISAO(
        displayName = "Espelho de Precisão",
        description = "Permite enxergar os pontos fracos dos germes. Mais crítico e velocidade.",
        iconName = "precision_mirror",
        perLevelBonus = "+8% Chance Crítica, +10% Vel. Ataque",
        unlockedAtStage = 1
    ),
    FIO_EXTRA_FORTE(
        displayName = "Fio Reforçado",
        description = "Aumenta a área de alcance e espessura de todas as armas de higiene.",
        iconName = "strong_floss",
        perLevelBonus = "+15% Tamanho e Área de Ataque",
        unlockedAtStage = 1
    ),
    ENXAGUANTE_MENTA(
        displayName = "Menta Refrescante",
        description = "Reduz o tempo de recarga de todos os equipamentos odontológicos.",
        iconName = "mint_rinse",
        perLevelBonus = "-10% Tempo de Recarga",
        unlockedAtStage = 1
    ),
    MASCARA_CIRURGICA(
        displayName = "Máscara N95 Odonto",
        description = "Barreira contra toxinas e bactérias. Reduz o dano sofrido pela heroína.",
        iconName = "surgical_mask",
        perLevelBonus = "-12% Dano Recebido (Armadura)",
        unlockedAtStage = 3
    ),
    LUVAS_LATEX(
        displayName = "Luvas de Nitrilo",
        description = "Melhor aderência e mobilidade ágil na clínica odontológica.",
        iconName = "latex_gloves",
        perLevelBonus = "+10% Velocidade de Movimento",
        unlockedAtStage = 2
    ),
    PASTILHA_REVELADORA(
        displayName = "Pastilha Reveladora",
        description = "Tinge a placa bacteriana facilitando a atração de itens e gotas de flúor.",
        iconName = "disclosing_tablet",
        perLevelBonus = "+30% Raio de Atração Magnética",
        unlockedAtStage = 4
    ),
    VITAMINA_C(
        displayName = "Vitamina C Dental",
        description = "Regenera a vitalidade do tecido gengival continuamente.",
        iconName = "vitamin_c",
        perLevelBonus = "+1.5 HP regenerado por segundo",
        unlockedAtStage = 5
    ),
    OCULOS_PROTECAO(
        displayName = "Óculos de Proteção UV",
        description = "Visão tática ampliada que adiciona projéteis adicionais aos disparos.",
        iconName = "goggles",
        perLevelBonus = "+1 Projétil a cada 2 níveis",
        unlockedAtStage = 2
    ),
    RESINA_COMPOSTA(
        displayName = "Resina Composta",
        description = "Material de restauração que expande a resistência e vida máxima.",
        iconName = "composite_resin",
        perLevelBonus = "+25 Vida Máxima",
        unlockedAtStage = 5
    ),
    SELANTE_FOSSULAS(
        displayName = "Selante Protetor",
        description = "Camada blindada sobre fissuras dentárias que anula periodicamente um golpe.",
        iconName = "sealant",
        perLevelBonus = "Gera barreira defensiva a cada 15s",
        unlockedAtStage = 3
    ),
    AVENTAL_CHUMBO(
        displayName = "Avental de Chumbo",
        description = "Pesada proteção radiológica que reduz empurrões e concede estabilidade.",
        iconName = "lead_apron",
        perLevelBonus = "-30% Empurrão e +15% Defesa",
        unlockedAtStage = 5
    ),
    GEL_XILITOL(
        displayName = "Gel de Xilitol",
        description = "Substância doce não cariogênica que multiplica dentes de ouro e XP obtidos.",
        iconName = "xylitol_gel",
        perLevelBonus = "+25% Moedas e XP Coletados",
        unlockedAtStage = 2
    ),
    MATRIZ_ACO(
        displayName = "Matriz de Aço",
        description = "Faixa de contenção metálica que prolonga a duração e perfuração dos ataques.",
        iconName = "steel_matrix",
        perLevelBonus = "+20% Duração e Perfuração",
        unlockedAtStage = 1
    ),
    EXTRATO_PROPOLIS(
        displayName = "Extrato de Própolis",
        description = "Bactericida natural que concede aos ataques chance de paralisar germes.",
        iconName = "propolis",
        perLevelBonus = "+15% Chance de Atordoar Inimigos",
        unlockedAtStage = 4
    )
}

// Drops / Loot & Chest Rarities
enum class LootType(
    val displayName: String,
    val xpValue: Int,
    val healAmount: Float,
    val scoreValue: Int,
    val color: Long
) {
    FLUOR_XP("Gota de Flúor", xpValue = 10, healAmount = 0f, scoreValue = 10, color = 0xFF38BDF8),
    CRISTAL_XILITOL("Cristal de Xilitol", xpValue = 50, healAmount = 0f, scoreValue = 50, color = 0xFF22C55E),
    PASTILHA_VACUUM("Ímã Revelador", xpValue = 20, healAmount = 0f, scoreValue = 100, color = 0xFFEC4899),
    MACA_HEAL("Maçã Crocante", xpValue = 0, healAmount = 40f, scoreValue = 20, color = 0xFF10B981),
    CAPSULA_ANALGESICO("Cápsula Analgésica", xpValue = 0, healAmount = 85f, scoreValue = 60, color = 0xFFF43F5E),
    ENXAGUANTE_FREEZE("Frasco Congelante", xpValue = 0, healAmount = 0f, scoreValue = 150, color = 0xFF06B6D4),
    BAU_PRATA("Estojo de Prata", xpValue = 60, healAmount = 15f, scoreValue = 250, color = 0xFFCBD5E1),
    BAU_DOURADO("Estojo Dourado", xpValue = 120, healAmount = 35f, scoreValue = 500, color = 0xFFF59E0B),
    BAU_DIAMANTE("Estojo de Diamante", xpValue = 250, healAmount = 80f, scoreValue = 1000, color = 0xFF06B6D4),
    BAU_MEGA_XP("Tomo de Experiência Odonto", xpValue = 9999, healAmount = 50f, scoreValue = 800, color = 0xFFA855F7),
    DENTE_OURO("Dente de Ouro", xpValue = 0, healAmount = 0f, scoreValue = 100, color = 0xFFFBBF24)
}

// Enemy Types (including Elites before bosses)
enum class EnemyType(
    val displayName: String,
    val baseHp: Float,
    val baseSpeed: Float,
    val baseDamage: Float,
    val radius: Float,
    val colorHex: Long,
    val xpDrop: LootType = LootType.FLUOR_XP,
    val isElite: Boolean = false
) {
    STREPTOCOCCUS(
        displayName = "Streptococcus Mutans",
        baseHp = 25f,
        baseSpeed = 1.1f,
        baseDamage = 8f,
        radius = 16f,
        colorHex = 0xFF22C55E
    ),
    PLACA_AMARELA(
        displayName = "Monstro de Placa Amarela",
        baseHp = 50f,
        baseSpeed = 0.85f,
        baseDamage = 12f,
        radius = 20f,
        colorHex = 0xFFEAB308
    ),
    ESPIROQUETA(
        displayName = "Espiroqueta Rápida",
        baseHp = 20f,
        baseSpeed = 1.8f,
        baseDamage = 7f,
        radius = 14f,
        colorHex = 0xFFF97316
    ),
    ACIDO_CORROSIVO(
        displayName = "Gota de Ácido Bacteriano",
        baseHp = 35f,
        baseSpeed = 1.25f,
        baseDamage = 15f,
        radius = 18f,
        colorHex = 0xFFA855F7
    ),
    TARTARO_MINION(
        displayName = "Casca de Tártaro Duro",
        baseHp = 110f,
        baseSpeed = 0.65f,
        baseDamage = 18f,
        radius = 24f,
        colorHex = 0xFF78716C,
        xpDrop = LootType.CRISTAL_XILITOL
    ),
    // Pre-Boss Elite Enemies (Drop Level-Up Mega Chests!)
    ELITE_SUPER_BACTERIA(
        displayName = "Super-Bactéria Mutante",
        baseHp = 450f,
        baseSpeed = 0.95f,
        baseDamage = 22f,
        radius = 32f,
        colorHex = 0xFF10B981,
        xpDrop = LootType.BAU_MEGA_XP,
        isElite = true
    ),
    ELITE_BIOFILME_TITAN(
        displayName = "Biofilme Blindado Titã",
        baseHp = 750f,
        baseSpeed = 0.80f,
        baseDamage = 26f,
        radius = 36f,
        colorHex = 0xFFD97706,
        xpDrop = LootType.BAU_MEGA_XP,
        isElite = true
    ),
    // Bosses
    BOSS_CARIE_GIGANTE(
        displayName = "Cárie Gigante Negra",
        baseHp = 1400f,
        baseSpeed = 0.75f,
        baseDamage = 25f,
        radius = 45f,
        colorHex = 0xFF1E1B4B,
        xpDrop = LootType.BAU_DIAMANTE
    ),
    BOSS_GENGIVITE(
        displayName = "Gengivite Furiosa",
        baseHp = 2400f,
        baseSpeed = 0.85f,
        baseDamage = 30f,
        radius = 48f,
        colorHex = 0xFFDC2626,
        xpDrop = LootType.BAU_DIAMANTE
    ),
    BOSS_ABCESSO(
        displayName = "Abcesso Periapical",
        baseHp = 3500f,
        baseSpeed = 0.70f,
        baseDamage = 35f,
        radius = 52f,
        colorHex = 0xFF6B21A8,
        xpDrop = LootType.BAU_DIAMANTE
    ),
    BOSS_BIOFILME_APARELHO(
        displayName = "Biofilme de Braquetes",
        baseHp = 4600f,
        baseSpeed = 0.90f,
        baseDamage = 38f,
        radius = 50f,
        colorHex = 0xFF0F766E,
        xpDrop = LootType.BAU_DIAMANTE
    ),
    BOSS_TARTARO_TITANICO(
        displayName = "Tártaro Titânico Supremo",
        baseHp = 7000f,
        baseSpeed = 0.70f,
        baseDamage = 45f,
        radius = 60f,
        colorHex = 0xFF451A03,
        xpDrop = LootType.BAU_DIAMANTE
    )
}

// Runtime Game Entities
data class ActiveWeapon(
    val type: WeaponType,
    var level: Int = 1,
    var lastFiredTime: Long = 0L,
    var isEvolved: Boolean = false,
    var evolvedType: EvolutionType? = null
)

data class ActivePassive(
    val type: PassiveType,
    var level: Int = 1
)

data class EnemyInstance(
    val id: Long,
    var x: Float,
    var y: Float,
    var hp: Float,
    val maxHp: Float,
    val type: EnemyType,
    var speed: Float,
    val damage: Float,
    val radius: Float,
    val isBoss: Boolean = false,
    var flashHitTimer: Float = 0f,
    var attackCooldown: Float = 0f,
    var isDead: Boolean = false
)

data class ProjectileInstance(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val damage: Float,
    var durationRemaining: Float,
    var pierceLeft: Int,
    val weaponType: WeaponType,
    var rotation: Float = 0f,
    val radius: Float = 12f,
    val isEvolved: Boolean = false,
    val isLaser: Boolean = false,
    val targetEnemyId: Long = 0L,
    var isDead: Boolean = false,
    val level: Int = 1
)

data class LootInstance(
    val id: Long,
    var x: Float,
    var y: Float,
    val type: LootType,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var isAttracted: Boolean = false,
    var isDead: Boolean = false
)

data class DamageNumber(
    val id: Long,
    var x: Float,
    var y: Float,
    val text: String,
    val isCrit: Boolean,
    var life: Float = 1.0f,
    val color: Long = 0xFFFFFFFF
)

data class VisualParticle(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Long,
    val radius: Float,
    var life: Float,
    val maxLife: Float
)

// Level-up Card Choice
sealed class LevelUpOption {
    data class NewWeapon(val weaponType: WeaponType) : LevelUpOption()
    data class UpgradeWeapon(val weapon: ActiveWeapon) : LevelUpOption()
    data class NewPassive(val passiveType: PassiveType) : LevelUpOption()
    data class UpgradePassive(val passive: ActivePassive) : LevelUpOption()
    data class EvolveWeapon(val weapon: ActiveWeapon, val evolution: EvolutionType) : LevelUpOption()
    data class HealthBonus(val healAmount: Float = 50f) : LevelUpOption()
    data class CoinBonus(val coinAmount: Int = 100) : LevelUpOption()
}

// Permanent Clinic Meta-Upgrades
enum class ClinicUpgradeType(
    val id: String,
    val displayName: String,
    val description: String,
    val maxLevel: Int,
    val baseCost: Int,
    val costMultiplier: Float,
    val bonusPerLevel: String
) {
    VITALIDADE_ESMALTE("hp", "Resistência do Esmalte", "Aumenta a vida máxima inicial de todos os heróis.", 5, 100, 1.6f, "+20 HP Máx"),
    POTENCIA_CLINICA("damage", "Potência dos Motores", "Aumenta o dano de todas as armas odontológicas.", 5, 120, 1.8f, "+8% Dano"),
    MOBILIDADE_AGIL("speed", "Sapatos Antiderrapantes", "Aumenta a velocidade de corrida pela clínica.", 5, 80, 1.5f, "+6% Velocidade"),
    IMA_HIGIENICO("magnet", "Aspirador de Saliva", "Aumenta o raio de atração de gotas de flúor e loot.", 5, 90, 1.5f, "+20% Raio"),
    RECARGA_RAPIDA("cooldown", "Esterilização Rápida", "Reduz o tempo de recarga de todos os poderes.", 5, 150, 2.0f, "-5% Recarga"),
    SORRISO_DE_OURO("gold", "Dentes de Ouro", "Aumenta a quantidade de moedas obtidas por partida.", 5, 100, 1.7f, "+25% Moedas")
}

enum class ObstacleType(
    val displayName: String,
    val colorHex: Long,
    val defaultWidth: Float,
    val defaultHeight: Float,
    val maxHp: Float = 300f,
    val isDestructible: Boolean = false
) {
    DENTE_MOLAR_GIGANTE("Molar Gigante", 0xFFFFFFFF, 80f, 80f, 500f, false),
    CADEIRA_ODONTO("Cadeira Odontológica", 0xFF0284C7, 100f, 60f, 400f, false),
    EQUIPO_CLINICO("Equipo Clínico", 0xFF38BDF8, 70f, 70f, 350f, false),
    REATOR_AUTOCLAVE("Autoclave Esterilizadora", 0xFF64748B, 65f, 65f, 400f, false),
    REFLETOR_ODONTO("Refletor de LED", 0xFFFDE047, 50f, 50f, 250f, false),
    RAIO_X_COLUNA("Coluna de Raio-X", 0xFF475569, 55f, 75f, 300f, false),
    BRAQUETE_TITANIO("Braquete de Titânio", 0xFF94A3B8, 60f, 60f, 350f, false),
    BLOCO_TARTARO("Bloco de Tártaro Petrificado", 0xFF78350F, 70f, 70f, 250f, true),
    FRASCO_FLUOR("Frasco de Gel Flúor", 0xFF10B981, 50f, 50f, 150f, true),
    CAIXA_GAZE("Caixa de Gaze Cirúrgica", 0xFFF8FAFC, 50f, 50f, 120f, true),
    ESTOJO_INSTRUMENTAL("Estojo de Pinças", 0xFFCBD5E1, 60f, 45f, 180f, true),
    TUBO_PASTA_DENTE("Tubo de Pasta Profilática", 0xFF06B6D4, 55f, 55f, 160f, true),
    AMPOULA_ANESTESICO("Ampola de Anestésico", 0xFFA855F7, 45f, 60f, 140f, true)
}

data class DentalObstacle(
    val id: Long,
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val type: ObstacleType,
    var hp: Float = type.maxHp,
    var flashHitTimer: Float = 0f,
    var isDestroyed: Boolean = false
)

enum class StructureType(
    val displayName: String,
    val colorHex: Long,
    val defaultWidth: Float = 220f,
    val defaultHeight: Float = 170f
) {
    CONSULTORIO_CABINE("Consultório Odontológico", 0xFF0284C7, 220f, 170f),
    SALA_ESTERILIZACAO("Sala de Esterilização", 0xFF7C3AED, 200f, 160f),
    ARMARIO_INSUMOS("Almoxarifado de Insumos", 0xFFD97706, 190f, 150f),
    CABINE_RAIO_X("Cabine de Raio-X Blindada", 0xFF0D9488, 200f, 160f),
    LABORATORIO_PROTESE("Laboratório de Prótese", 0xFFE11D48, 210f, 170f)
}

data class DentalStructure(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: StructureType,
    val doorDirection: String = "BOTTOM"
)

data class GameState(
    val characterId: CharacterId = CharacterId.DRA_ALINNY,
    val stageId: StageId = StageId.STAGE_1_ESMALTE,
    val playerX: Float = 0f,
    val playerY: Float = 0f,
    val playerHp: Float = 100f,
    val playerMaxHp: Float = 100f,
    val level: Int = 1,
    val currentXp: Int = 0,
    val xpForNextLevel: Int = 30,
    val kills: Int = 0,
    val dentalCoins: Int = 0,
    val gameTimeSeconds: Float = 0f,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val isPaused: Boolean = false,
    val activeBossName: String? = null,
    val activeBossHpFraction: Float = 0f,
    val weapons: List<ActiveWeapon> = emptyList(),
    val passives: List<ActivePassive> = emptyList(),
    val isLevelingUp: Boolean = false,
    val levelUpChoices: List<LevelUpOption> = emptyList(),
    val isOpeningChest: Boolean = false,
    val chestReward: LevelUpOption? = null,
    val chestCoins: Int = 0,
    val isFreezeActive: Boolean = false,
    val screenShakeX: Float = 0f,
    val screenShakeY: Float = 0f,
    val obstacles: List<DentalObstacle> = emptyList(),
    val structures: List<DentalStructure> = emptyList(),
    val currentStructureInside: DentalStructure? = null
)


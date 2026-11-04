# Chapters fold (package `chapters`)

Behaviour and datapack format follow **Chapters** 2.2 by gabinx (https://github.com/GabinFqt/chapters, branch `26`).
Licence: MIT. Verified from the repository's LICENSE file ("MIT License, Copyright (c) 2026 gabinx"); Modrinth also lists MIT.

Changes: rewritten as one small server-side package for NeoForge 26.1.2. Kept: stage JSON under `chapters/stages` (`items` with ids, `#tags`, `@namespace`; `recipes`; `dimensions`), the rule that owning any one defining stage unlocks, pickup block, craft block, inventory drop about once a second, dimension travel block, and the `/chapters add|remove|list|check` commands. Left out: JEI hiding, KubeJS, FTB Quests/Teams, Mekanism chemicals, fluid locks and the mixins. No upstream art is used.
No turn-based mechanics.

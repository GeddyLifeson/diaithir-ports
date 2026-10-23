# Untamed Wilds 3.0.1-diaithir.1

Licence: GPL-3.0-only (neoforge.mods.toml). Modification and redistribution allowed; source must be offered (upstream https://github.com/RayTrace082/untamedwilds, plus the patch here).

Cause: species data (ComplexMob.ENTITY_DATA_HASH) is only loaded on the server side, so on a client joining a dedicated server getEntityData() returns null and EntityBigCat.getTexture NPEs.

Change: EntityBigCat.getTexture null-checks the data holder and falls back to the synched hasDimorphism() flag. Only EntityBigCat.class changed (recompiled from decompiled source; other methods are control-flow-equivalent). mods.toml version is 3.0.1-diaithir.1, mod id unchanged.

Not fixed: baleen_whale / thrown_shimmer_spear ShapeType NPE noise (harmless log noise, from another mod's shape query).

Test: minimal dedicated server plus headless client (only this jar, NeoForge 26.1.2.116, Xvfb) in /home/user/uwtest. Joined as Tester, summoned three untamedwilds:big_cat (variants 0, 2, 4) 4 to 5 blocks away. Client rendered all three with no NullPointerException and no crash report (test-render-cats.png, client-test-latest.log). One variant shows the missing-texture magenta because the mod ships no cave_lion.png (upstream asset gap, a warning, not a crash). The unpatched control run was not repeated; the cause matches the original crash logs. The full 341-mod pack was not booted with this jar.

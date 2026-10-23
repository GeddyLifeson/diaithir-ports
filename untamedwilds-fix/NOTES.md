# Untamed Wilds 3.0.1-diaithir.1 (UNTESTED at runtime so far)

Licence: GPL-3.0-only (neoforge.mods.toml). Modification and redistribution allowed; source must be offered (upstream https://github.com/RayTrace082/untamedwilds, plus the patch here).

Cause: species data (ComplexMob.ENTITY_DATA_HASH) is only loaded on the server side, so on a client joining a dedicated server getEntityData() returns null and EntityBigCat.getTexture NPEs.

Change: EntityBigCat.getTexture null-checks the data holder and falls back to the synched hasDimorphism() flag. Only EntityBigCat.class changed (recompiled from decompiled source; other methods are control-flow-equivalent). mods.toml version is 3.0.1-diaithir.1, mod id unchanged.

Not fixed: baleen_whale / thrown_shimmer_spear ShapeType NPE noise (harmless log noise, from another mod's shape query).

Test: pending. Harness in /home/user/uwtest (minimal server and client, ports 25601/25602, display :93) is ready but was not run because free memory was about 3 GB while the Valkyrien Skies flight test held the host.

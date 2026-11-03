# Alex's Mobs Continued 2.2.4 on the Diaithir pack (2026-11-03)

Server: NeoForge 26.1.2.116, JDK 25, the full pack (390 jars) + alexsmobs 2.2.4 + codxlib 1.6.1, flat world, pause-when-empty-seconds=-1, ports 28881/28882.
- Boot: the first boot failed. Walkers 8.2's AlexMobsIntegration looks up entity ids that Alex's Mobs Continued does not register (crimson-mosquito, giant-squid, komodo-dragon, crocodile, sunbird_scorch), and the datapack load aborts. Fixed with diaithir-compat/tools/patch_walkers_alexsmobs.py (jar walkers-neoforge-8.2-diaithir.2.jar). After that: Done (29.9 s).
- Summon test (mobtest): 114 of the 117 `entity.alexsmobs.*` lang keys summoned with no error; the other 3 (cachalot_part, crocodile_egg, giant_squid_part) are not registered entity types in this build. 20 s of ticking with all 114 alive, then kill: no exceptions or ERROR lines from alexsmobs or codxlib (the only ERROR lines are tconstruct/mysticalagradditions, already in the pack).
- Not tested: client world-join and rendering, natural spawns on the Tellus world (needs network PMTiles), the Walkers traits in play, Alex's Mobs recipes/loot beyond load.

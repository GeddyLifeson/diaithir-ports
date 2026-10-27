# PC job: tech/transport batch (10-27)
Do not ask Marlon. Never run local models. Only kill PIDs you launched.
1. Boot test batch A: NeoForge 26.1.2.116 server, mods = jars from batchA csv (url + sha512, curl each) in a fresh dir, offline, flat world; pass if "Done" and no mod errors. Then client join with the pack test instance (PC_GAME_TEST.md), jars added to DiaithirTest.
2. Source builds (JDK 25, ./gradlew build -x test): git clone --depth 1 -b 26.1 https://github.com/mekanism/Mekanism ; -b 26.1 GregTechCEu/GregTech-Modern ; -b port/26.1 AztechMC/Modern-Industrialization ; -b 26.1.2 TeamPneumatic/pnc-repressurized ; -b 26.1.2 CreeperHost/StevesCarts2. Leave jars (8 MB parts + sha256) in /mnt/project-files/tech-transport/builds/.
3. Boot Mekanism family with AE2/powah present; report conflicts.

# Tech, transport and storage sweep (10-27)

Rule: 1) latest 26.1 build, 2) port older, 3) clean-room homemade (Marlon 10-27: anything ARR or unportable we still want gets a clean-room mod, never dropped).
Data: neo261.json = 1,582 Modrinth mods with a 26.1.x NeoForge build in technology/transportation/storage. older2.json = top 400 older-only. lsr.out = which have 26.x git branches. batchA csv = verified jars (sha512 checked).

## A. Add now: latest 26.1.2 builds (batchA_modrinth_26.1.2.csv, 50 jars incl. libs)
Storage/logistics: Tom's Storage, Storage Drawers, Carry On, Colossal Chests, Hopper Gadgetry, Copper Hopper, Super Factory Manager, XNet (+RFTools Base, McJtyLib), AE2 Wireless Terminals, ME Requester, AppFlux, AE2 Import/Export Card, Echo Chest, Nether Chested, New Linked Chests, Tiny Pipes.
Tech: BuildCraft Community Edition (old BuildCraft already ported upstream), Immersive Machinery, Extended Crafting, SecurityCraft, RFTools Power, Horse Powered (hand and animal power, Age I-III), Quarry, Effortless Building, Just Hammers.
Transport: Hang Glider, Horseman, Call Your Horse, Better Boat Movement, Minecart Trains, Highspeed Rail, Grappling Hooks, Mob Lassos, Cargo Boats, ChunkCarts, Simple Teleporters, Rail Optimization.
Skipped as duplicates of pack mods: Iron Chests/Furnaces, Beyond Dimensions, Xaero maps (JourneyMap), VeinMiner (FTB Ultimine), backpacks (Sophisticated).

## B. Build from source: official 26.1 branch exists, no jar published yet
Mekanism 10.8.0 (MIT, branch 26.1; Generators, Tools, Additions in same repo), GregTech Modern (LGPL, 26.1), Modern Industrialization (MIT, port/26.1), PneumaticCraft (LGPL, 26.1.2), Modern Dynamics (Technici4n, 26.1), Mekanism MoreMachine (26.1), Steve's Carts 2 (26.1.2), Modular Machinery Reborn (26.1-NeoForge), Alloy Forgery (26.1). Age: Mekanism VI, PneumaticCraft V, MI V, GTCEu VI.

## C. Port older (open licence, no 26.1 branch)
Industrial Foregoing (MIT, 1.21), Immersive Engineering (1.21.1, custom licence, check), RFTools Utility/Builder (MIT, 1.21.1), Extreme Reactors (MIT, 1.21.1), Tinkers (MIT, builds/tcfix has a 26.1 fix), Mekanism Turrets (needs B), Storage Network style mods.

## D. Clean-room homemade (ARR or limited licence, still wanted)
Moving Elevators, MFFS force fields, Thermal series, Simple Storage Network, Chunk Loaders, Shippy Ships, Solar Generation, Ultimate Car Mod/Plane Mod (vehicles beyond Immersive Vehicles), Cyclic, Tesseract, Wormhole, Chococraft (Diaithir bird mounts), Modern Minecarts. Name via Diaithiris, Factorio look, Age-gated.

## Blocked here
Cloud auto-mode refuses to build or boot code from outside (Mekanism gradle build, NeoForge server with downloaded jars). Boot test and source builds go to the PC thread: see PC_TECH_BUILD.md.

Drop-in jars for the Diaithir pack (MC 26.1.2, NeoForge 26.1.2.116). Replace the old Valkyrien Skies jar in mods/ (delete valkyrienskies-26-1-2-2.5.0-diaithir.8.jar).

- valkyrienskies-26-1-2-2.5.0-diaithir.9.jar: fixes the server crash (NullPointerException in vs-core Et.i / postTickGame) that happens as soon as a chunk is loaded in a dimension the ship world does not know (Nether, End and every modded dimension were never registered). Also contains the .8 fix for dimensions created at runtime and the .7 Tellus claim-area fix.
- eureka-26-1-2-1.6.3-diaithir.1.jar: unchanged.

Source: branch valkyrienskies of diaithir-ports (tools/shipyard-claims/NOTES.md).

Drop-in jars for the Diaithir pack (MC 26.1.2, NeoForge 26.1.2.116). Replace the old Valkyrien Skies jar in mods/ (delete valkyrienskies-26-1-2-2.5.0-diaithir.9.jar).

- valkyrienskies-26-1-2-2.5.0-diaithir.10.jar: fixes the slow/hanging server tick (game thread parked in FQ.postTickGame with "Can't keep up" of 6-40 s). vs-core defaults physicsThreads to cores-4 and starts native Krunch workers per registered level per step; with ~90 levels that was thousands of JVM thread attaches per tick. .10 runs Krunch on the calling thread (physicsThreads 0); -Dvs.physicsThreads=N opts back in. It also logs the Physics thread's stack when the game thread waits more than 1.5 s. Contains the .9 fix (every level registered with the ship world), the .8 fix for dimensions created at runtime and the .7 Tellus claim-area fix.
- eureka-26-1-2-1.6.3-diaithir.1.jar: unchanged.

Source: branch valkyrienskies of diaithir-ports (tools/shipyard-claims/NOTES.md).

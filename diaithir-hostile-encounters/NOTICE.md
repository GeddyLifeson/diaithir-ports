# Notice and credit

The lunar events part of this module is a port of **Enhanced Celestials** (CorgiTaco-MC/Enhanced-Celestials, branch 1.21.X, version 6.0.2.6, MC 1.21.1) by Corgitaco, licensed **LGPL-3.0**. The licence text is in `LICENSE`. This module as a whole is released under LGPL-3.0-only.

What was taken: the event model (default, blood, blue, harvest and super variants), their spawn rules and numbers, the forecast scheduling rules (100-day year, 2 to 98 days between events, per-event chance, minimum nights apart, valid moon phases) and the effect values (XP, anvil cost, harvest drops, luck).

What changed: rebuilt on plain NeoForge 26.1.2 events and datapack JSON. CorgiLib, Data Anchor and every mixin are gone. The upstream art, sounds, lang text and the EC2 announcement screens are not used; all text in `assets/diaithirhostile/lang` is original. Spawn pressure is a small horde spawner instead of mob-cap mixins.

Global Events bridge: when diaithir-worldevents is loaded, every lunar event is registered through WorldEvents.register (weight 0, so the lunar forecast stays the only moon scheduler; `/worldevents start blood_moon` forces it tonight). Global Events has its own blood moon; at merge, drop that one in favour of the lunar blood moon.

Not ported yet: the sky and moon tint (client rendering), beacon radius and enchanting table cost changes, meteors.

## Raided (package `net.diaithir.hostile.raided`)
Raiders added to the raid wave table: Inquisitor, Incinerator, Savager, Necromancer, Electromancer. Source: Raided 0.1.7 (MC 26.1.2, branch 26.1) by Mrbysco and silentine, MIT licence, Copyright (c) 2022 Mrbysco. Code moved into this module with the package renamed and the `@Mod` entry replaced by `Raided.init`; data generators and the config screen hook dropped. Ids stay in the `raided` namespace so existing worlds keep their mobs. Assets and loot tables are the mod's own.

## Horde Hoard / Zombie Hordes (package `net.diaithir.hostile.hordes`)
Horde stacking and multiplying, intelligent zombie teams, leaping mobs, grave robbers, iron golem regeneration, a nightly horde spawner, game rules and a Thinking effect. Source: Zombie Hordes (Horde Hoard) 1.1.0, MC 26.1.2 NeoForge, by nojustgavin / warwa, MIT licence, Copyright (c) 2024 nojustgavin. The Fabric entry point and mixin were dropped, the NeoForge platform helper kept, the `@Mod` class replaced by `HordeHoardModNeoForge.init`. Ids stay in the `horde_hoard` namespace. Overlaps with the clean-room horde nights in `diaithir-encounters`; tags (`horde_hoard:hordes` and others) let the two share mobs.

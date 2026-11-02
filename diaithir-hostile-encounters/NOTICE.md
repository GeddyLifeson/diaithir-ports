# Notice and credit

The lunar events part of this module is a port of **Enhanced Celestials** (CorgiTaco-MC/Enhanced-Celestials, branch 1.21.X, version 6.0.2.6, MC 1.21.1) by Corgitaco, licensed **LGPL-3.0**. The licence text is in `LICENSE`. This module as a whole is released under LGPL-3.0-only.

What was taken: the event model (default, blood, blue, harvest and super variants), their spawn rules and numbers, the forecast scheduling rules (100-day year, 2 to 98 days between events, per-event chance, minimum nights apart, valid moon phases) and the effect values (XP, anvil cost, harvest drops, luck).

What changed: rebuilt on plain NeoForge 26.1.2 events and datapack JSON. CorgiLib, Data Anchor and every mixin are gone. The upstream art, sounds, lang text and the EC2 announcement screens are not used; all text in `assets/diaithirhostile/lang` is original. Spawn pressure is a small horde spawner instead of mob-cap mixins.

Global Events bridge: when diaithir-worldevents is loaded, every lunar event is registered through WorldEvents.register (weight 0, so the lunar forecast stays the only moon scheduler; `/worldevents start blood_moon` forces it tonight). Global Events has its own blood moon; at merge, drop that one in favour of the lunar blood moon.

Not ported yet: the sky and moon tint (client rendering), beacon radius and enchanting table cost changes, meteors.

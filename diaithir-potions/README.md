# Diaithir Potions

Clean-room potion reforms, written from the public description of Home Brewed (All Rights Reserved, so no code, data or art is used). Our own numbers:

- Potions and water bottles stack to 8, drink in 1 second, and put a 2 second cooldown on the next drink. Stews (mushroom, rabbit, beetroot, suspicious) stack to 16.
- Coal and charcoal fuel the brewing stand beside blaze powder (`data/minecraft/tags/item/brewing_fuel.json`).
- Lingering potions can be brewed from a splash potion and a glow ink sac, beside the vanilla dragon's breath recipe.

Numbers live in `DiaithirPotions.java`. `/potions selftest` (owners) checks all of it and prints PASS or what failed. Honeycomb lengthens (long variants) and amethyst shards strengthen (strong variants) the potions vanilla gates behind redstone and glowstone, so both reach early Overworld play; vanilla recipes stay. Per-Age gating of those items is done by the pack's age_gate.js.

Source: https://github.com/GeddyLifeson/diaithir-ports/tree/claude/diaithir-brewing/diaithir-potions. Licence: MIT.

# Spartan Weaponry (Diaithir port)

Port of Spartan Weaponry 3.2.1 (Apache-2.0, ObliviousSpartan) to NeoForge 26.1.2. See NOTICE and LICENSE.

Included: 18 melee patterns (dagger, parrying dagger, longsword, katana, saber, rapier, greatsword, battle hammer, warhammer, spear, halberd, pike, lance, battleaxe, flanged mace, glaive, quarterstaff, scythe) and the four throwing patterns (throwing knife, tomahawk, javelin, boomerang, melee only for now) in wood, stone, copper, bronze and iron; wooden and studded club; cestus and studded cestus; handles and poles. 114 weapons, 4 parts.

Weapons use 26.1 data components: attack damage and speed attributes, entity reach attributes for polearms, sweeping ratio plus the `minecraft:swords` tag for sweep types, knockback attribute for the battle hammer, `minecraft:axes` and the axe tool rules for the battleaxe, and the weapon component (halberd and battleaxe disable shields). Conditional habits (dagger backstab, rapier against the unarmoured, flanged mace against the undead, lance when mounted, club and battle hammer nausea) are in `TraitEvents`.

Metals are Age-gated in `age_gates.json` (wood and stone I, copper II, bronze III, iron IV). Names are Diaithiris (`names_map.json` has English too). Bronze uses the `c:ingots/bronze` tag; its repair tag is `spartanweaponry:bronze_materials`.

`/spartanselftest` builds every weapon stack and checks its components. `tools/gen.py <upstream checkout>` regenerates models, textures, recipes, tags, lang and `weapon_types.json`.

Not ported: longbows, heavy crossbows, arrows, bolts, quivers, weapon oils, dynamite, mob heads, throwing entities, tin/steel/silver and the other metals, parry/two-handed/head and chest damage traits, compat with Curios, JEI, EMI.

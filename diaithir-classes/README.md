# diaithir-classes (MIT)

Clean-room class kits for the Diaithir pack. Replaces the all-rights-reserved Paladins & Priests, Archers, Rogues & Warriors, Wizards, Runes and Jewelry content packs (spell schools live in diaithir-druidry).

- Four classes: Laech (id `fian`, spear/sword, shouts and charge), Bogaduir (`sleagh`, bow/sling, volley and traps), Sielguir (`ceilg`, dagger, stealth and ambush), Ligichi (`cleirech`, crozier, mercy and aegis). Display names are Diaithiris, made by script from the lexicon.
- Gear for Age III (bronze), V (steel), VII (composite): 4 armour pieces plus weapons per class. Armour and tool numbers, repair tags and worn-armour layers come from diaithir-arms; each piece is crafted from the arms piece plus a class emblem.
- 28 Spell Engine spells (16 actives bound to weapons via `spell_assignments`, 12 tiered passives) and 12 Spell Engine equipment sets (2 piece attributes, 4 piece attributes plus passive).
- Ogham runes (11, craftable) are pressed into rings and charms (Curios `ring`/`charm` slots, 1 to 3 sockets) by right-clicking the rune onto the ring in an inventory. Rune effects are data in `classes.json` (attribute, amount, operation).
- `tools/gen.py` writes all JSON, `tools/textures.py` makes textures from vanilla sprites, `tools/validate.py` checks spell keys against the Spell Engine API, `/classesselftest` checks the loaded game.
- Required: spell_engine, spell_power, diaithirarms. Optional: curios (rune attributes), ranged_weapon_api (ranged damage attribute).

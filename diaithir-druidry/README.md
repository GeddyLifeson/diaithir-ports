# diaithir-druidry

Celtic schools of magic as pure data on Spell Engine (GPL-3.0) and Spell Power (LGPL). MIT. Clean room: every spell, name, number and
texture here is original; nothing is copied from Iron's Spells 'n Spellbooks or from the all-rights-reserved Wizards, Paladins, Archers,
Rogues or Runes packs. Their behaviour ideas (cast, aim, projectile, cloud, heal) are generic Spell Engine pipeline stages documented in
Spell Engine's own `docs/`.

It is a data-only mod (no Java). The jar holds `data/` and `assets/` and loads on NeoForge 26.1.2 next to `spell_engine` 1.10.9+ and `spell_power` 1.6.2+.

## Schools

Spell Power attributes are registered in Java, so a data-only module cannot add new ones. Each Celtic school is a spell line
(`group` prefix) mapped onto the existing Spell Power schools, so staves and gear from any pack scale it. Seven schools, seven spells each, tiers 1 to 4:

| School | Power | Spells (tier, Age) |
|---|---|---|
| Draeid (Druid) | healing | Boildidielg (T1, III), Driùchdu (T1, III), Ribidries (T2, III), Kraikenddaurach (T2, III), Slànachaddoiri (T3, IV), Leimdaum (T3, IV), Sliegoilìuk (T4, V) |
| Fiusaichi (Ovate) | arcane | Sraudagsieldad (T1, III), Fradurk (T1, III), Snàddànu (T2, III), Keimskàili (T2, III), Eòlasubradun (T3, IV), Fairi (T3, IV), Boildibruodur (T4, V) |
| Bàrdu (Bard) | lightning | Bilegtàirnenach (T1, III), Tàlad (T1, III), Kuma (T2, III), Gàiri (T2, III), Adurkglaed (T3, IV), Òrunbrautach (T3, IV), Koseirmstoirmi (T4, V) |
| Filid (Filid) | soul | Boildirand (T1, III), Randskiad (T1, III), Aeiri (T2, III), Fìrind (T2, III), Randkeid (T3, IV), Toirmesk (T3, IV), Maldachd (T4, V) |
| Fitech (Raven) | physical melee | Boildifiendag (T1, III), Glaedkad (T1, III), Leimfitech (T2, III), Bàinid (T2, III), Roudspoiri (T3, IV), Oildti (T3, IV), Àdbàsu (T4, V) |
| Sìtichi (Otherworld) | frost | Kietach (T1, III), Loindskàili (T1, III), Keimknok (T2, III), Tiudlak (T2, III), Geigairged (T3, IV), Uoirsìtichi (T3, IV), Klachmelduin (T4, V) |
| Gouba (Smith) | fire | Sraudag (T1, III), Kaisk (T1, III), Iarnaigitied (T2, III), Kaisiiarnaigi (T2, III), Skòdsaliaruind (T3, IV), Anuilkum (T3, IV), Oiskiliegti (T4, V) |

Names are Diaithiris compounds (head first) built from `LEXICON.json` words; ids are English snake case and never change.

## Pieces

* `data/diaithir_druidry/spell/*.json` (49 spells), the Spell Engine datapack registry.
* `tags/spell/spell_book/<school>.json` and `spell_scroll/<school>.json`: the pools. The spell binding table lists every `spell_book/*` tag as a tome you can make (Liebur <school>), and scrolls bind one spell.
* `tags/spell/weapon/<school>_staff.json`: the staff's own tier-1 spells.
* Staves: `recipe/staff_<school>_1.json` (Age III: bronze ingot, school focus, sticks) and `_2` (Age V: steel ingot, two foci, sticks). Results are Spell Engine components on a stick (spell container, spell power attribute, item model, name), so no Java item is needed. Ingredients are the pack tags `#c:ingots/bronze` and `#c:ingots/steel`; empty tags are declared here so the recipes parse even when no mod fills them.
* Loot: `loot_table/chests/age3|age4|age5.json` injected by NeoForge loot modifiers (`loot_modifiers/inject_age*.json`) into vanilla structure chests: Age III dungeons, mineshafts, temples and shipwrecks; Age IV strongholds, mansions, bastions and outposts; Age V ancient cities, end cities and trial vaults. Scrolls of every school, rare staves.
* `age_gates.json`: namespace to Age (III) plus per item and per spell Age (tier 1-2 spells III, tier 3 IV, tier 4 V).
* `library-druidry.json` (also copied to library-sync/druidry.json): Library entries. `codex/druidry_handbook.json`: chapter text for the Handbooks codex builder.
* Textures: `tools/textures.py` composes 16x16 icons from recoloured vanilla item sprites on a slate tile with a bronze rim, enamel studs, a glow in soft green, silver-blue or amber, and an ogham tally (one stroke per tier), after the Celtic magic rule.

## Build

```
python3 -I tools/gen.py            # JSON from tools/spells.py
python3 -I tools/textures.py <minecraft merged or client jar>
python3 -I tools/validate.py       # static checks
python3 build.py 0.1.0             # build/diaithir_druidry-0.1.0.jar
```

## Limits

* No new Spell Power attributes (Java only). A later small Java module can register `druid`, `ovate`, ... as real schools and the spell JSON only needs its `school` changed.
* Staves are components on a vanilla stick, so Age gating of the staff is by recipe materials, not by item id.
* No custom projectile models or sounds yet: spells use Spell Engine's particles and sounds.

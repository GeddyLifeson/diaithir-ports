# diaithir-druidry

Celtic schools of magic as pure data on Spell Engine (GPL-3.0) and Spell Power (LGPL). MIT. Clean room: every spell, name, number and
texture here is original; nothing is copied from Iron's Spells 'n Spellbooks or from the all-rights-reserved Wizards, Paladins, Archers,
Rogues or Runes packs. Their behaviour ideas (cast, aim, projectile, cloud, heal) are generic Spell Engine pipeline stages documented in
Spell Engine's own `docs/`.

It is a data-only mod (no Java). The jar holds `data/` and `assets/` and loads on NeoForge 26.1.2 next to `spell_engine` 1.10.9+ and `spell_power` 1.6.2+.

## Schools

Spell Power attributes are registered in Java, so a data-only module cannot add new ones. Each Celtic school is a spell line
(`group` prefix) mapped onto the existing Spell Power schools, so staves and gear from any pack scale it. Seven Celtic schools of seven spells (tiers 1 to 4, Ages III to V), plus the Age VI Glyph school below:

| School | Power | Spells (tier, Age) |
|---|---|---|
| Draeid (Druid) | healing | Boildidielg (T1, III), Driùchdu (T1, III), Ribidries (T2, III), Kraikenddaurach (T2, III), Slànachaddoiri (T3, IV), Leimdaum (T3, IV), Sliegoilìuk (T4, V) |
| Fiusaichi (Ovate) | arcane | Sraudagsieldad (T1, III), Fradurk (T1, III), Snàddànu (T2, III), Keimskàili (T2, III), Eòlasubradun (T3, IV), Fairi (T3, IV), Boildibruodur (T4, V) |
| Bàrdu (Bard) | lightning | Bilegtàirnenach (T1, III), Tàlad (T1, III), Kuma (T2, III), Gàiri (T2, III), Adurkglaed (T3, IV), Òrunbrautach (T3, IV), Koseirmstoirmi (T4, V) |
| Filid (Filid) | soul | Boildirand (T1, III), Randskiad (T1, III), Aeiri (T2, III), Fìrind (T2, III), Randkeid (T3, IV), Toirmesk (T3, IV), Maldachd (T4, V) |
| Fitech (Raven) | physical melee | Boildifiendag (T1, III), Glaedkad (T1, III), Leimfitech (T2, III), Bàinid (T2, III), Roudspoiri (T3, IV), Oildti (T3, IV), Àdbàsu (T4, V) |
| Sìtichi (Otherworld) | frost | Kietach (T1, III), Loindskàili (T1, III), Keimknok (T2, III), Tiudlak (T2, III), Geigairged (T3, IV), Uoirsìtichi (T3, IV), Klachmelduin (T4, V) |
| Gouba (Smith) | fire | Sraudag (T1, III), Kaisk (T1, III), Iarnaigitied (T2, III), Kaisiiarnaigi (T2, III), Skòdsaliaruind (T3, IV), Anuilkum (T3, IV), Oiskiliegti (T4, V) |

### Glyph school (Age VI)

Lietirrùan (Glyph), arcane power, eight spells, all learnt at Age VI. Each is one form joined to one effect cut into a rune, after the compose-a-spell idea of glyph magic (Ars Nouveau, LGPL; the idea only, nothing copied): Rune Bolt (aim, harm, pierces one foe), Rune Touch (touch, harm, slows), Rune Mend (self or aim, heal and cleanse), Rune Blink (self, 16 blocks), Rune Light (self, night vision 2 minutes), Rune Pull (area, drags foes in), Rune Wall (self, resistance and absorption), Rune Seal (cloud, holds foes slow and weak). Staves: `staff_glyph_1` (silver ingot, lapis lazuli, Age VI) and `staff_glyph_2` (steel ingot, two lapis, Age VI). Glyph scrolls and staves drop from `chests/age6` (end city treasure, ancient city ice box, ominous trial rewards). The silver ingot tag `c:ingots/silver` is declared empty so the recipe parses without a silver mod.

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

## Source as energy (Ars Nouveau port)

1 Source = 1000 J = 1000 FE (1 FE = 1 J), the pack rate for magic (mana is 100 J, so Source is ten mana; `ars_nouveau:source_gem` 10 kJ, source gem block 90 kJ in `magic_fuel.csv`, `source` in `fuels.csv`). Source jars and arcane relays expose their stored Source as the NeoForge energy capability, so a jar of N Source reports N * 1000 FE. It is extract-only (nothing makes Source from FE) and takes whole Source only; the creative jar is not exposed. Everything in the `ars_nouveau` namespace is Age VI and tied to tech node `magic.glyphs` (`age_gates.json`, `tech_gates.json`).

## Limits

* No new Spell Power attributes (Java only). A later small Java module can register `druid`, `ovate`, ... as real schools and the spell JSON only needs its `school` changed.
* Staves are components on a vanilla stick, so Age gating of the staff is by recipe materials, not by item id.
* No custom projectile models or sounds yet: spells use Spell Engine's particles and sounds.

## Rites from the Library (52k)

`data/diaithir_druidry/rites/<universe>.json` (215 files, `_index.json` lists counts): one rite per Library SPELL entry (51,884), format `rite.v1`. Name, text, Age and school come from the entry and its Realm (Martial Fitech, Ki Filid, Arcane Fiusaichi, Stellar Bardu, Mythic Draeid, Grounded Gouba, Outer Sitichi). Steps use the Ascendant rite vocabulary (offer, hunt, stones, hour, feast, coop, defend), picked deterministically from the entry id and Age tier. Loaded per universe, so no 52k registrations. Regenerate: `python3 -I tools/gen_rites.py <content.sqlite> <library.sqlite> <planets.tsv>`; each rite also has `planet`, `dimension` and `gate_age` (planet opens_age + tier - 1) (Global-Repo S-id in the PR).

The jar for the pack is `ars-nouveau/build/libs/diaithir_druidry-<ver>.jar` (`./gradlew jar` there) or `python3 build.py`; both hold the data, mods.toml, classes and LICENSE-Ars.

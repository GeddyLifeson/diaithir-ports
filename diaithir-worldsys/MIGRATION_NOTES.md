# Id migration notes

`id_migrations.json` maps the old ids this module has an equivalent for (read from the replaced jars' item and block registries). Not mapped, because this module has no equivalent: Easy Villagers blocks and villager item, most Jewelry rings, necklaces and gems (only ruby, sapphire and topaz rings and gems), Utilitarian carpets, lamps, sand variants and tools, the other Crafting on a Stick stations, Simple Magnets demagnetization coils, Guard Villagers illusioner egg, and all FTB / Pufferfish data (quests, ranks, ultimine settings, skill trees), which live in world and config files, not registries. Those need a data conversion step owned by the quest and permission systems.

## Legacy content (added)
`legacy/LegacyFeature` registers plain items and blocks for every other id found in the replaced jars' registries (jewelry, utilitarian, simple magnets, stick stations, easy villagers, guard villagers). They have no behaviour, so saved stacks and placed blocks survive instead of vanishing; their behaviour is not rebuilt. `tools/gen_assets.py` generates their placeholder 64 px textures, models, blockstates and lang (it also fills the textures and block models the first version of the 15 base items and blocks lacked). The placeholder art is procedural, not final.

## Data conversion (FTB Quests, Ranks, Ultimine, Pufferfish)
The pack ships no FTB Quests, Ranks or Pufferfish data in its overrides, only the quest listing audit stub, so there is nothing in the repo to convert. Player progress and ranks live in the live server's world and config folders (C:\DiaithirServer), which the cloud cannot read. A converter needs a copy of those files.

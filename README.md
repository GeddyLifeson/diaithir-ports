# Super Factory Manager (Diaithir patched build)

This is a patched build of the official 26.1.2 release of [Super Factory Manager](https://www.curseforge.com/minecraft/mc-mods/super-factory-manager) by TeamDman. The original authors did not make it and do not support it, so please report problems with this build at https://github.com/GeddyLifeson/diaithir-ports/issues and not to them.

## What changed

The official 26.1.2 release with its Mekanism integration switched off (the mod id string `mekanism` in `SFMModCompat.class` changed to `mekanis_`, so the check never matches): it was compiled against Mekanism 10.7's `IChemicalHandler`, which Mekanism 10.8 removed. Items, fluids and energy still move in and out of Mekanism machines; only Mekanism chemicals are not handled. Use the official release unless you run Mekanism 10.8.

## Credits and licence

- Original mod: [Super Factory Manager](https://www.curseforge.com/minecraft/mc-mods/super-factory-manager) by TeamDman
- Original source: https://github.com/TeamDman/SuperFactoryManager
- Licence: MPL-2.0. The full licence text ships inside the jar (`LICENSE_sfm` and any file starting with that name).

This build will be retired once an official 26.1.2 release is out. Use the official one when it arrives.

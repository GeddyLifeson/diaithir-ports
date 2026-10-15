# KubeJS (Diaithir patched build)

This is a patched build of the official 26.1.2 release of [KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs) by LatvianModder (latvian.dev). The original authors did not make it and do not support it, so please report problems with this build at https://github.com/GeddyLifeson/diaithir-ports/issues and not to them.

## What changed

The official 8.0.6 release with one constant in `WorldLoaderMixin` changed so its injection matches `RegistryDataLoader.load` by name: NeoForge 26.1.2.116 added an overload and the original target no longer matched, which stopped servers from starting. The patch script is `tools/ports/kubejs116/patch_jar.py` in the source repository.

## Credits and licence

- Original mod: [KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs) by LatvianModder (latvian.dev)
- Original source: https://github.com/KubeJS-Mods/KubeJS
- Licence: LGPL-3.0. The full licence text ships inside the jar (`LICENSE_kubejs` and any file starting with that name).

This build will be retired once an official 26.1.2 release is out. Use the official one when it arrives.

# diaithir-compat: the single id-migration loader

Any module may ship `data/<modid>/id_migrations.json`:

```json
[{"registry": "minecraft:block", "old": "oldmod:thing", "new": "newmod:thing"}]
```

NeoForge 26.1 has no missing-mappings event, so each entry becomes a registry alias (`Registry#addAlias`) added at lowest priority while that registry is filled, after every mod has registered. Saved worlds, recipes, loot tables, tags, commands and datapacks that name the old id then resolve to the new one. An entry is skipped with a log line when the old id is still registered or the new id does not exist. Registry names are the usual ones (`minecraft:item`, `minecraft:block`, `minecraft:entity_type`, `minecraft:block_entity_type`, `minecraft:menu`, `minecraft:sound_event`, `minecraft:mob_effect`, `minecraft:recipe_type` ...).

When a fold, port or clean-room rewrite renames or removes an id, the module ships the entry here; ids still never change silently. Built with javac against the NeoForge 26.1.2.116 libraries (no extra dependencies); tested in a real server: alias resolved in `/setblock`.

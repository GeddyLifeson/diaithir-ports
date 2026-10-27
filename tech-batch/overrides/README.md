Batch A boot fixes (laptop log 10-27). Copy overrides/kubejs into the pack kubejs folder.
- sfm buffer loot table: item sfm:buffer is not registered in 4.34.0, empty loot table.
- tinypipes tiny/* recipes: tiny pipe items are not registered, recipes disabled. full/*_filter_pipe: ingredients used the old {"item":..} form, rewritten to plain strings.
- c:ingots/silver: tag was missing for Colossal Chests silver wall, now lists silver ingots from pack mods (optional).
- sfm *_copy recipe warnings are harmless (empty ingredient recipes ignored).

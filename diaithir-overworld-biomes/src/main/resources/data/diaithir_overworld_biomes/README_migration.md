# Migration

This module registers no blocks or items. Feature and biome-modifier ids are never persisted in chunk data, so existing worlds load unchanged.
If legacy ids are discovered, populate `legacy_id_map.json` (`bloomingbiosphere:<name>` -> `diaithir_overworld_biomes:<name>`) and mirror in `BloomingMigration.LEGACY_MAP`.

# B4 result (11-08), ALL UNTESTED (no boot run; Verify runs it)

Method: each of the 37 stems resolved to its Modrinth file by sha512 of the Prism jar, then its modId compared with every modId inside the pack's jars (incl. jar-in-jar). Licence: Modrinth project plus the GitHub repo licence (`gh api`); they agree except as noted in plans/PACK_CONSOLIDATION_AUDIT.csv.

## Folded (1, new jar, jar unmodified, modId kept so no id migration)
- deepnull-reforged (MIT) -> mods/diaithir-open-folds-neoforge-26.1.2-1.0.0.jar (tools/distribution/fold_open_mods.py + patch_module.py)

## Left as original jar, new mods/*.pw.toml (route=port, would need a rewrite) (3)
- combat-roll (GPL-3.0), critical-strike (GPL-3.0), jaopca (MIT)

## Already in the pack (33): WAVE17_OPEN_FOLDS.tsv "NOT IN PACK" was stale
Their modIds are already inside diaithir-client-suite (wave15, hosted), diaithir-audio-client, diaithir-platform-suite, diaithir-content-suite, diaithir-tech-ext-suite or diaithir-realms-suite. Adding the originals would crash on duplicate modIds, so none was added. List in the CSV `present_in` column.

## Needs Marlon
- enchantmentdescriptions: you said ARR, Modrinth says LGPL-2.1. It is inside diaithir-client-suite (and tooltips-client) today. Not touched here; remove from the client suite if ARR.
- diaithir-branding: our own jar, not found on the PC or Modrinth; nothing to fold.
- keybindbundles: Prism had 2.0.0; the copy in the pack is whatever the suite holds. No new file added.
- Mixed licences (LGPL/GPL/AGPL e.g. InventoryProfilesNext AGPL) already sit inside the suites; B5 LICENSES.md rows for the older suites are not part of this.

## Verify: one boot, check
`diaithir-open-folds` loads (modId deepnullreforged), combat_roll, critical_strike, jaopca load, and no duplicate-modId errors.

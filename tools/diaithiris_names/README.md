# Diaithiris item names

`diaithir-tooltips` shows an item's name in the player's starting culture, in small grey italics under its normal
(lang) name. The player's culture is a slug from DiaithirRPG `culture_starts.csv`, stored as a synced player attachment
(`/culture set <players> <slug>`, or `player.setData(DiaithirTooltips.CULTURE, slug)` from the picker); empty reads as
`diaithir`. Names are tables, not lang files, so the line is the same in every game language:
`assets/diaithirtooltips/cultures/<slug>.json` = `{"item": {id: name}, "block": {...}, "entity": {...}}`. A culture with
no file, or an id with no entry, shows no line. `CultureNames.name(kind, id)` is the lookup for other UIs (Jade, Codex).
`cultures/diaithir.json` is what this generator writes.

Regenerate after the modlist changes:

    python3 tools/diaithiris_names/scan_items.py MC_CLIENT_JAR MODS_DIR items.json
    python3 tools/diaithiris_names/build_names.py diaithiris-archaic/LEXICON.json items.json REF_DIR names.json

`LEXICON.json` is the archaic layer (project files, diaithiris-archaic). `REF_DIR` holds `ga.dic` and `gd.dic`
(Hunspell lists of Irish and Scottish Gaelic) and `oldirish_known.txt`; any word that equals one of them takes the
archaic layer's second shift, so no name is a living Gaelic word.

Naming: head first, the way Goidelic phrases run (`Iron Pickaxe` becomes `Kladuiri klach iarund`, "stone digger, iron").
Words missing from the lexicon are said with lexicon words (`TERMS` in build_names.py), split into two lexicon words,
or respelled as loans.

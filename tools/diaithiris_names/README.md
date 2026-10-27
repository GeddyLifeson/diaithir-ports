# Diaithiris item names

`diaithir-tooltips` shows an item's Diaithiris name in grey italics under its normal name. The names are a table
(`diaithir-tooltips/src/main/resources/assets/diaithirtooltips/diaithiris_names.json`, item id to name), so the line is
the same in every game language.

Regenerate after the modlist changes:

    python3 tools/diaithiris_names/scan_items.py MC_CLIENT_JAR MODS_DIR items.json
    python3 tools/diaithiris_names/build_names.py diaithiris-archaic/LEXICON.json items.json REF_DIR names.json

`LEXICON.json` is the archaic layer (project files, diaithiris-archaic). `REF_DIR` holds `ga.dic` and `gd.dic`
(Hunspell lists of Irish and Scottish Gaelic) and `oldirish_known.txt`; any word that equals one of them takes the
archaic layer's second shift, so no name is a living Gaelic word.

Naming: head first, the way Goidelic phrases run (`Iron Pickaxe` becomes `Kladuiri klach iarund`, "stone digger, iron").
Words missing from the lexicon are said with lexicon words (`TERMS` in build_names.py), split into two lexicon words,
or respelled as loans.

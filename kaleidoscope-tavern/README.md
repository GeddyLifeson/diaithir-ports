# Kaleidoscope Tavern (Diaithir build)

Upstream's own NeoForge 26.1.2 branch of [Kaleidoscope Tavern](https://github.com/KaleidoscopeMods/KaleidoscopeTavern) (ysbbbbbb, tartaric_acid), built here against NeoForge 26.1.2.109 with our own Gradle files so it needs no extra maven except JEI and Jade (compile-only). Upstream commit 9b8f165 (2026-08-22). The Java source under `src/` is unchanged. Display names in `lang/en_us.json` are Diaithiris kennings written by `tools/gen_names.py` from the archaic lexicon (ids never change); upstream's English is in git history. `tools/age_gates.json` is the Age fragment for the pack's `content/tech_ages.json`.

Licences are kept as upstream ships them: code is BSD-3-Clause (`LICENSE-CODE`), art and other assets are CC BY-NC-SA 4.0 (`LICENSE-ASSETS`). Any changed art in this module is therefore also CC BY-NC-SA 4.0 with the same credit, and the pack stays non-commercial.

Build: `JAVA_HOME=<jdk 25> ./gradlew build`. Following upstream: re-copy `src/`, then run `python3 -I tools/gen_names.py`.

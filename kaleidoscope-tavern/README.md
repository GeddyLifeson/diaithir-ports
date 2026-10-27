# Kaleidoscope Tavern (Diaithir build)

Upstream's own NeoForge 26.1.2 branch of [Kaleidoscope Tavern](https://github.com/KaleidoscopeMods/KaleidoscopeTavern) (ysbbbbbb, tartaric_acid), built here against NeoForge 26.1.2.109 with our own Gradle files so it needs no extra maven except JEI and Jade (compile-only). Upstream commit 9b8f165 (2026-08-22). The source under `src/` is unchanged.

Licences are kept as upstream ships them: code is BSD-3-Clause (`LICENSE-CODE`), art and other assets are CC BY-NC-SA 4.0 (`LICENSE-ASSETS`). Any changed art in this module is therefore also CC BY-NC-SA 4.0 with the same credit, and the pack stays non-commercial.

Build: `JAVA_HOME=<jdk 25> ./gradlew build`. Diaithir changes (Diaithiris display names, Age gates) go in the pack's overrides, not in this source, so the module can follow upstream.

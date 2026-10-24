Diaithir Bestiary 0.3.0 (diaithir-bestiary-neoforge-26.1.2-0.3.0.jar). Our own mod, MIT licence (see LICENSE inside the source archive).

Source: diaithir-bestiary-0.3.0-source.tar.gz in this folder (git archive of branch claude/bestiary-0.3, folder diaithir-bestiary), also at
https://github.com/GeddyLifeson/diaithir-ports/tree/claude/bestiary-0.3/diaithir-bestiary

Build: JDK 25, ./gradlew build in the extracted folder (output build/libs/diaithir-bestiary-neoforge-26.1.2-0.3.0.jar).
Art: python3 tools/gen_content.py rebuilds every model, animation, texture and glow mask from tools/art (numpy and Pillow); the generated
files are also committed under src/main/resources/assets/diaithirbestiary.
Runtime dependency: GeckoLib (MIT), not bundled.

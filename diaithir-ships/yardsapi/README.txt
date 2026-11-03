Verbatim copy of net.diaithir.yards from diaithir-carts (merged #186, identical to diaithircompat).
Compile-only here (sourceSet yardsApi, NOT packaged in the ship jar): at runtime the classes come from the diaithir-carts mod.
Dev runs/selftest add the sourceSet to the runtime classpath. Refresh the copy when the API changes.
Why a copy: each module is a standalone gradle build (own settings.gradle, moddev), so depending on diaithir-carts would mean an includeBuild of a second NeoForge moddev project; revisit when the modules share one build.

Verbatim copy of net.diaithir.yards from PR #186 (diaithir-carts/src/main/java/net/diaithir/yards, commit d90374a102).
Compile-only here (sourceSet yardsApi, NOT packaged in the ship jar): at runtime the classes come from the diaithir-carts mod.
Dev runs/selftest add the sourceSet to the runtime classpath. Refresh the copy when the API changes.

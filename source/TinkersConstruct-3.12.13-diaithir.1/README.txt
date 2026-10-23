Tinkers' Construct 3.12.13-diaithir.1 (MIT). Original by SlimeKnights, https://github.com/SlimeKnights/TinkersConstruct.
The jar is our 26.1.2 build (full source: https://github.com/GeddyLifeson/diaithir-ports/tree/tconstruct) with one post-build patch:
Patch.java (ASM) redirects the registry lookup in TinkerLoadables$1.decode to TConRegistryFix.registry, so a packet decoded in the play phase uses the connection's registry access.
TConRegistryFix.java is the class that was added to the jar (dev/diaithir/fix/). No other class changed.

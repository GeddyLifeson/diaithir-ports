Create 6.0.9-diaithir.4 for NeoForge 26.1.2 with our own art (worn Factorio style, 64px).

Only a finished slice is playable; every other Create block and item stays registered but hidden, with no recipe, advancement, loot or lang entry (list: data/create/shipped_items.txt in the jar).

Source: branch claude/create of https://github.com/GeddyLifeson/diaithir-ports (the whole mod, including tools/assetgen which
generates every texture, model, blockstate, sound, particle and language file in the jar).

Code base: the "Create Fly" 26.1 port of Create (MIT code and data, with Create Fly's own additions CC0), moved from Fabric to
NeoForge. Create's All Rights Reserved art (the assets directory of the upstream jars) is not used: the jar's assets/ share no
byte-identical file with the upstream jar (checked by sha256, see the report), and the only asset namespace is create.

Not in this build: Ponder scenes, Flywheel shader backends (the Flywheel backend runs "off"), JEI/REI/EMI/RRV compat, copycat
re-rendering of the held block, the symmetry wand placement hook, the factory panel break hook, landing effects, the diving
helmet lava/water breathing hook, external storage interop.

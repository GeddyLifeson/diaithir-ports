# diaithir-create-gantries

Gantry split shafts for the Create port (`claude/create`, mod id `diaithir_create`), NeoForge 26.1.2, Java 25. GPL-3.0-only (see `NOTICE`). Folded from Create: Extended Gantries (MR-Spagetty).

- `diaithirgantries:gantry_split`: a gantry shaft that cuts a gantry into runs. For carriages, power, pinion speed, placement and contraption assembly it counts as a gantry shaft (six mixins in `net.diaithir.gantries.mixin` make Create's `is(GANTRY_SHAFT)` and `getBlock() == GANTRY_SHAFT` checks include it). A plain shaft still pulls a split beside it along its axis; a split does not pull another split. It does not show itself as joined to another split.
- It reuses Create's gantry shaft block entity type (`BlockEntityTypeAddBlocksEvent`), so Create's renderer draws it: it looks like a plain shaft until it gets its own art.
- Recipe: 8 from andesite alloy, iron nuggets and redstone (tags `c:nuggets/iron`, `c:dusts/redstone`); Age V gate; Library row `library-sync/diaithirgantries.json`; `id_migrations.json` from `extendedgantries:`. `tools/gen_assets.py` writes the data.

Build: the Create port is not on Maven. Build its jar (`./gradlew jar` in a checkout of `claude/create`) and run `./gradlew compileJava -Pcreate_jar=/path/to/create-neoforge-*.jar` (or `CREATE_JAR`). `runServer` loads the same jar.
Not done: art for the split, a carriage actually moving across a split in a live kinetic setup (only boot, mixin application and block registration are checked), Diaithiris name, Codex entry, stress-impact entry (Create port sets no impact for the shaft, so none needed).

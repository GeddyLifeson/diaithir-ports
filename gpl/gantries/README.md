# Gantry split (GPL module in the Create port jar)

Gantry split shafts for the Create port (`claude/create`, mod id `diaithir_create`), NeoForge 26.1.2, Java 25. GPL-3.0-only (see `src/main/resources/gpl/gantries/NOTICE`). Folded from Create: Extended Gantries (MR-Spagetty).

- `diaithirgantries:gantry_split`: a gantry shaft that cuts a gantry into runs. For carriages, power, pinion speed, placement and contraption assembly it counts as a gantry shaft (six mixins in `net.diaithir.gantries.mixin` make Create's `is(GANTRY_SHAFT)` and `getBlock() == GANTRY_SHAFT` checks include it). A plain shaft still pulls a split beside it along its axis; a split does not pull another split. It does not show itself as joined to another split.
- It reuses Create's gantry shaft block entity type (`BlockEntityTypeAddBlocksEvent`), so Create's renderer draws it: it looks like a plain shaft until it gets its own art.
- Recipe: 8 from andesite alloy, iron nuggets and redstone (tags `c:nuggets/iron`, `c:dusts/redstone`); Age V gate; Library row `gpl/gantries/id_migrations.json` from `extendedgantries:`. `tools/gen_assets.py` writes the data.

Where it lives: a GPL-3.0-only module inside the Create port jar (`create-neoforge`), not a jar of its own. Code in `src/main/java/net/diaithir/gantries`, mixin config `diaithirgantries.mixins.json`, namespaces `assets|data/diaithirgantries`, licence text and NOTICE in `src/main/resources/gpl/gantries/`. The MIT code reaches it through one call, `DiaithirGantries.init(bus)`, in `CreateNeoForge`; Create's `mineable/pickaxe` tag lists the block (one line).

Build and test: `./gradlew jar`, `./gradlew runServer`.
Not done: art for the split, a carriage actually moving across a split in a live kinetic setup (only boot, mixin application and block registration are checked), Diaithiris name, Codex entry. `library_row.json` is the Library row to merge into `library-sync/diaithirgantries.json` in the ports repo.

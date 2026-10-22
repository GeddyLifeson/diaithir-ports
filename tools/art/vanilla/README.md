# Vanilla mobs, Diaithir creature art (64x)

The same method as the Untamed Wilds animals (locked 2026-10-22), applied to the game's own models.

1. `ExportLayers.java` dumps every vanilla model layer as posed polygons with pixel UVs (compile and run with JDK 25 against the
   moddev merged Minecraft jar and the Gradle cache jars; pass `-Dmcjar=<merged jar>`). It calls each static
   `create*Layer`/`create*Mesh` method directly, so no game bootstrap is needed.
2. `vanilla.py BLENDER_PYTHON LAYERS_JSON VANILLA_TEXTURES OUT [mob ...]` bakes AO on each model's own UVs with `poly_ao.py`
   (headless Blender, bpy 5.2.3), then paints every face fresh with `../paint.py` materials, keeps each face's eyes and nose
   from the original as crisp marks, and adds hoof bands where the animal has hooves. `MOBS` lists layer, skins, hidden parts
   and the material rules per mob.
3. `build_page.py` makes the preview page (turntable, every skin, old-look toggle).

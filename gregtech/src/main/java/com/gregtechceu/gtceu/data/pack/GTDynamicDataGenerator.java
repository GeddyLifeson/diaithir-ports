package com.gregtechceu.gtceu.data.pack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTRecipes;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

/** Fills {@link GTDynamicDataPack} with everything the 1.21.1 mod generated at reload time. */
public final class GTDynamicDataGenerator {

    public static final java.util.Set<Identifier> REMOVALS = new java.util.HashSet<>();

    /** Resources of the reload in progress; 26.1 binds item components only after a reload, we need them earlier. */
    public static volatile net.minecraft.server.ReloadableServerResources RESOURCES;

    private GTDynamicDataGenerator() {}

    public static HolderLookup.Provider registries() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    public static void addJson(String folder, Identifier id, JsonElement json) {
        GTDynamicDataPack.addResource(id.withPath(folder + "/" + id.getPath() + ".json"),
                json.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void collectTagRefs(JsonElement json, java.util.Set<Identifier> out) {
        if (json.isJsonPrimitive()) {
            String str = json.getAsString();
            if (str.startsWith("#")) {
                Identifier id = Identifier.tryParse(str.substring(1));
                if (id != null) out.add(id);
            }
        } else if (json.isJsonArray()) {
            json.getAsJsonArray().forEach(e -> collectTagRefs(e, out));
        } else if (json.isJsonObject()) {
            json.getAsJsonObject().asMap().values().forEach(e -> collectTagRefs(e, out));
        }
    }

    private static <T> void tagsFor(net.minecraft.core.Registry<T> registry, String folder) {
        var map = new java.util.HashMap<Identifier, java.util.List<net.minecraft.tags.TagLoader.EntryWithSource>>();
        com.gregtechceu.gtceu.core.MixinHelpers.generateGTDynamicTags(map, registry);
        var ops = JsonOps.INSTANCE;
        for (var e : map.entrySet()) {
            var entries = e.getValue().stream().map(net.minecraft.tags.TagLoader.EntryWithSource::entry).distinct()
                    .toList();
            var file = new net.minecraft.tags.TagFile(entries, false);
            try {
                JsonElement json = net.minecraft.tags.TagFile.CODEC.encodeStart(ops, file).getOrThrow();
                addJson("tags/" + folder, e.getKey(), json);
            } catch (Throwable t) {
                GTCEu.LOGGER.error("Could not encode tag {}: {}", e.getKey(), t.toString());
            }
        }
        GTCEu.LOGGER.info("Generated {} {} tags", map.size(), folder);
    }

    private static void generateTags() {
        tagsFor(BuiltInRegistries.ITEM, "item");
        tagsFor(BuiltInRegistries.BLOCK, "block");
        tagsFor(BuiltInRegistries.FLUID, "fluid");
    }

    public static void generateTagsAndLoot() {
        generateTags();
        try {
            generateLoot();
        } catch (Throwable t) {
            GTCEu.LOGGER.error("Failed to generate GTCEu loot tables", t);
        }
        precreateTags();
    }

    // ---- loot tables, written as JSON so no item stacks or dynamic registries are needed ----

    private static final String SILK = "{\"condition\":\"minecraft:match_tool\",\"predicate\":{\"predicates\":{"
            + "\"minecraft:enchantments\":[{\"enchantments\":\"minecraft:silk_touch\",\"levels\":{\"min\":1}}]}}}";
    private static final String NO_SILK = "{\"condition\":\"minecraft:inverted\",\"term\":" + SILK + "}";

    private static int lootCount;

    private static void addLoot(Identifier lootId, String json) {
        GTDynamicDataPack.addResource(lootId.withPath("loot_table/" + lootId.getPath() + ".json"),
                json.getBytes(StandardCharsets.UTF_8));
        lootCount++;
    }

    private static String lootTable(String blockPath, String entry, String extraPools) {
        return "{\"type\":\"minecraft:block\",\"pools\":[{\"rolls\":1.0,\"bonus_rolls\":0.0,\"entries\":[" + entry +
                "]}" + extraPools + "],\"random_sequence\":\"" + blockPath + "\"}";
    }

    private static void selfDrop(Identifier blockId) {
        Identifier lootId = blockId.withPrefix("blocks/");
        String entry = "{\"type\":\"minecraft:item\",\"name\":\"" + blockId + "\",\"conditions\":["
                + "{\"condition\":\"minecraft:survives_explosion\"}]}";
        addLoot(lootId, lootTable(lootId.toString(), entry, ""));
    }

    private static Identifier itemId(net.minecraft.world.item.Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    private static net.minecraft.world.item.Item firstItem(com.gregtechceu.gtceu.api.data.tag.TagPrefix prefix,
                                                           com.gregtechceu.gtceu.api.data.chemical.material.Material mat) {
        try {
            if (prefix.hasItemTable() && prefix.doGenerateItem(mat)) {
                return prefix.getItemFromTable(mat).get().asItem();
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static void generateLoot() {
        lootCount = 0;
        var gem = com.gregtechceu.gtceu.api.data.tag.TagPrefix.gem;
        var dust = com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
        var raw = com.gregtechceu.gtceu.api.data.tag.TagPrefix.rawOre;
        com.gregtechceu.gtceu.common.data.GTMaterialBlocks.MATERIAL_BLOCKS.rowMap().forEach((prefix, map) -> {
            var ore = com.gregtechceu.gtceu.api.data.tag.TagPrefix.ORES.get(prefix);
            map.forEach((material, blockEntry) -> {
                Identifier blockId = blockEntry.getId();
                if (ore == null) {
                    selfDrop(blockId);
                    return;
                }
                var drop = firstItem(raw, material);
                if (drop == null) drop = firstItem(gem, material);
                if (drop == null) drop = firstItem(dust, material);
                Identifier lootId = blockId.withPrefix("blocks/");
                if (drop == null) {
                    selfDrop(blockId);
                    return;
                }
                int mult = ore.isDoubleDrops() ? 2 : 1;
                String silk = "{\"type\":\"minecraft:item\",\"name\":\"" + blockId + "\",\"conditions\":[" + SILK + "]}";
                String normal = "{\"type\":\"minecraft:item\",\"name\":\"" + itemId(drop) + "\",\"functions\":["
                        + "{\"function\":\"minecraft:set_count\",\"count\":" + mult + ".0},"
                        + "{\"function\":\"minecraft:explosion_decay\"}]}";
                String entry = "{\"type\":\"minecraft:alternatives\",\"children\":[" + silk + "," + normal + "]}";
                StringBuilder extra = new StringBuilder();
                var sec = new java.util.ArrayList<String>();
                for (var secondary : prefix.secondaryMaterials()) {
                    if (secondary.material().hasProperty(com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey.DUST)) {
                        var item = firstItem(gem, secondary.material());
                        if (item == null) item = firstItem(dust, secondary.material());
                        if (item == null) continue;
                        sec.add("{\"type\":\"minecraft:item\",\"name\":\"" + itemId(item) + "\",\"conditions\":[" + NO_SILK + "],"
                                + "\"functions\":[{\"function\":\"minecraft:set_count\",\"count\":{\"type\":\"minecraft:uniform\",\"min\":0.0,\"max\":1.0}},"
                                + "{\"function\":\"minecraft:limit_count\",\"limit\":{\"min\":0,\"max\":2}},"
                                + "{\"function\":\"minecraft:explosion_decay\"}]}");
                    }
                }
                if (!sec.isEmpty()) {
                    extra.append(",{\"rolls\":1.0,\"bonus_rolls\":0.0,\"entries\":[").append(String.join(",", sec)).append("]}");
                }
                addLoot(lootId, lootTable(lootId.toString(), entry, extra.toString()));
            });
        });
        com.gregtechceu.gtceu.common.data.GTMaterialBlocks.CABLE_BLOCKS.values().forEach(b -> selfDrop(b.getId()));
        com.gregtechceu.gtceu.common.data.GTMaterialBlocks.FLUID_PIPE_BLOCKS.values().forEach(b -> selfDrop(b.getId()));
        com.gregtechceu.gtceu.common.data.GTMaterialBlocks.ITEM_PIPE_BLOCKS.values().forEach(b -> selfDrop(b.getId()));
        com.gregtechceu.gtceu.common.data.GTMaterialBlocks.SURFACE_ROCK_BLOCKS.forEach((material, blockEntry) -> {
            var item = firstItem(com.gregtechceu.gtceu.api.data.tag.TagPrefix.dustTiny, material);
            if (item == null) return;
            Identifier lootId = blockEntry.getId().withPrefix("blocks/");
            String entry = "{\"type\":\"minecraft:item\",\"name\":\"" + itemId(item) + "\",\"functions\":["
                    + "{\"function\":\"minecraft:set_count\",\"count\":{\"type\":\"minecraft:uniform\",\"min\":3.0,\"max\":5.0}},"
                    + "{\"function\":\"minecraft:explosion_decay\"}]}";
            addLoot(lootId, lootTable(lootId.toString(), entry, ""));
        });
        com.gregtechceu.gtceu.api.registry.GTRegistries.MACHINES.forEach(machine -> selfDrop(machine.getId()));
        GTCEu.LOGGER.info("Generated {} loot tables", lootCount);
    }

    private static void precreateTags() {
        var empty = "{\"values\":[]}".getBytes(StandardCharsets.UTF_8);
        int n = 0;
        for (var material : com.gregtechceu.gtceu.api.GTCEuAPI.materialManager) {
            for (var prefix : com.gregtechceu.gtceu.api.data.tag.TagPrefix.values()) {
                try {
                    for (var tag : prefix.getAllItemTags(material)) n += addEmptyTag("item", tag.location(), empty);
                } catch (Throwable ignored) {}
            }
            try {
                n += addEmptyTag("fluid", material.getFluidTag().location(), empty);
            } catch (Throwable ignored) {}
        }
        for (var f : com.gregtechceu.gtceu.data.recipe.CustomTags.class.getFields()) {
            try {
                Object v = f.get(null);
                java.util.List<Object> tags = new java.util.ArrayList<>();
                if (v instanceof net.minecraft.tags.TagKey<?>) tags.add(v);
                else if (v instanceof Object[] arr) tags.addAll(java.util.Arrays.asList(arr));
                for (Object o : tags) {
                    if (o instanceof net.minecraft.tags.TagKey<?> tk) {
                        String folder = tk.registry().equals(Registries.ITEM) ? "item"
                                : tk.registry().equals(Registries.BLOCK) ? "block"
                                : tk.registry().equals(Registries.FLUID) ? "fluid" : null;
                        if (folder != null) n += addEmptyTag(folder, tk.location(), empty);
                    }
                }
            } catch (Throwable ignored) {}
        }
        GTCEu.LOGGER.info("Pre-created {} empty tags", n);
    }

    private static int addEmptyTag(String folder, Identifier id, byte[] empty) {
        Identifier file = id.withPath("tags/" + folder + "/" + id.getPath() + ".json");
        if (GTDynamicDataPack.has(file)) return 0;
        GTDynamicDataPack.addResource(file, empty);
        return 1;
    }

    public static void generateRecipes() {
        // 26.1 binds item components only after a reload; item stacks are needed before that.
        var res = RESOURCES;
        if (res != null) res.newComponents.forEach(net.minecraft.core.component.DataComponentInitializers.PendingComponents::apply);
        var registries = registries();
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        int[] count = new int[2];
        java.util.Set<Identifier> refs = new java.util.HashSet<>();
        RecipeOutput output = new RecipeOutput() {

            @Override
            public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
                accept(id, recipe, advancement, new net.neoforged.neoforge.common.conditions.ICondition[0]);
            }

            @Override
            public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement,
                               net.neoforged.neoforge.common.conditions.ICondition... conditions) {
                try {
                    JsonElement json = Recipe.CODEC.encodeStart(ops, recipe).getOrThrow();
                    collectTagRefs(json, refs);
                    addJson("recipe", id.identifier(), json);
                    count[0]++;
                } catch (Throwable t) {
                    count[1]++;
                    if (count[1] <= 20) GTCEu.LOGGER.error("Could not encode recipe {}: {}", id.identifier(),
                            t.toString());
                }
            }

            @SuppressWarnings("removal")
            @Override
            public Advancement.Builder advancement() {
                return Advancement.Builder.recipeAdvancement();
            }

            @Override
            public void includeRootAdvancement() {}
        };
        com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.init();
        REMOVALS.clear();
        GTRecipes.recipeRemoval(REMOVALS::add);
        GTRecipes.recipeAddition(output);
        for (Identifier ref : refs) {
            for (String folder : new String[] { "item", "fluid" }) {
                Identifier file = ref.withPath("tags/" + folder + "/" + ref.getPath() + ".json");
                if (!GTDynamicDataPack.has(file)) {
                    GTDynamicDataPack.addResource(file, "{\"values\":[]}".getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        GTCEu.LOGGER.info("Encoded {} GT recipes ({} failed)", count[0], count[1]);
    }
}

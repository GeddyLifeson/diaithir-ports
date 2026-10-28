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
        precreateTags();
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

package io.github.tr100000.researcher;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.Reader;
import java.util.Collections;
import java.util.List;

public final class LockableRecipeTypesList {
    private LockableRecipeTypesList() {}

    public static final Codec<List<RecipeSerializer<?>>> CODEC = BuiltInRegistries.RECIPE_SERIALIZER.byNameCodec().listOf();
    private static List<RecipeSerializer<?>> types = Collections.emptyList();

    @UnmodifiableView
    public static List<RecipeSerializer<?>> getTypes() {
        return types;
    }

    public static void reload(ResourceManager manager) {
        List<RecipeSerializer<?>> lockable = new ObjectArrayList<>();
        List<Resource> resources = manager.getResourceStack(ModUtils.id("lockable_recipe_types.json"));
        resources.forEach(resource -> {
            try (Reader reader = resource.openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                List<RecipeSerializer<?>> types = LockableRecipeTypesList.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
                lockable.addAll(types);
            }
            catch (Exception e) {
                Researcher.LOGGER.error("Error reading file in pack {}", resource.sourcePackId(), e);
            }
        });
        types = List.copyOf(lockable);
    }
}

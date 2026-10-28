package com.github.alexmodguy.alexscaves.server.recipe;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.item.CaveInfoItem;
import com.github.alexmodguy.alexscaves.server.item.CaveMapItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/** Eight paper around a Cave Codex makes a Cave Map for the codex's biome. */
public class RecipeCaveMap extends CustomRecipe {
    public static final MapCodec<RecipeCaveMap> MAP_CODEC = MapCodec.unit(RecipeCaveMap::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeCaveMap> STREAM_CODEC = StreamCodec.unit(new RecipeCaveMap());
    public static final RecipeSerializer<RecipeCaveMap> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != 3 || input.height() != 3) return false;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                ItemStack stack = input.getItem(x, y);
                if (x == 1 && y == 1) {
                    if (!stack.is(ACItemRegistry.CAVE_CODEX.get())) return false;
                } else if (!stack.is(Items.PAPER)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack codex = input.getItem(1, 1);
        ResourceKey<Biome> key = CaveInfoItem.getCaveBiome(codex);
        return key == null ? ItemStack.EMPTY : CaveMapItem.createMap(key);
    }

    @Override
    public RecipeSerializer<RecipeCaveMap> getSerializer() {
        return SERIALIZER;
    }
}

package me.desht.pneumaticcraft.common.recipes.special;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.jline.utils.Log;

import java.util.List;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class CompressorUpgradeCrafting extends WrappedShapedRecipe {
    private static final Supplier<List<Item>> COMPRESSORS = Suppliers.memoize(() -> List.of(
            getItem("advanced_air_compressor"),
            getItem("advanced_liquid_compressor"),
            getItem("air_compressor"),
            getItem("liquid_compressor")
    ));

    public CompressorUpgradeCrafting(ShapedRecipe wrapped) {
        super(wrapped);
    }

    private static Item getItem(String id) {
        return BuiltInRegistries.ITEM.getValue(RL(id));
    }

    @SuppressWarnings("unchecked")
    @Override
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) ModRecipeSerializers.COMPRESSOR_UPGRADE_CRAFTING.get();
    }

    @NotNull
    @Override
    public ItemStack assemble(CraftingInput inv) {
        ItemStack result = wrapped.assemble(inv);

        int index = getMainItem(inv);
        if (index == -1) {
            Log.warn("Just crafted a PNC Compressor upgrade recipe but couldn't find a compressor in the input!");
            return ItemStack.EMPTY;
        }
        ItemStack input = inv.getItem(index);

        var data = input.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            result.set(DataComponents.BLOCK_ENTITY_DATA, data);
        }
        return result;
    }

    private int getMainItem(CraftingInput container) {
        int i;
        boolean matchFound = false;

        for (i = 0; i < container.size(); i++) {
            ItemStack item = container.getItem(i);
            if (COMPRESSORS.get().contains(item.getItem())) {
                matchFound = true;
                break;
            }
        }
        if (!matchFound) {
            return -1;
        }
        return i;
    }

    public static class Serializer {
        public static final MapCodec<CompressorUpgradeCrafting> CODEC
                = ShapedRecipe.MAP_CODEC.xmap(CompressorUpgradeCrafting::new, CompressorUpgradeCrafting::wrapped);
        public static final StreamCodec<RegistryFriendlyByteBuf, CompressorUpgradeCrafting> STREAM_CODEC
                = ShapedRecipe.STREAM_CODEC.map(CompressorUpgradeCrafting::new, CompressorUpgradeCrafting::wrapped);
        public MapCodec<CompressorUpgradeCrafting> codec() {
            return CODEC;
        }
        public StreamCodec<RegistryFriendlyByteBuf, CompressorUpgradeCrafting> streamCodec() {
            return STREAM_CODEC;
        }

        public RecipeSerializer<CompressorUpgradeCrafting> toSerializer() {
            return new RecipeSerializer<>(codec(), streamCodec());
        }
    }
}

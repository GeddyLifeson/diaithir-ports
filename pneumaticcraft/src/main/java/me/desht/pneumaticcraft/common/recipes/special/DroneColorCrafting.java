package me.desht.pneumaticcraft.common.recipes.special;

import com.mojang.serialization.MapCodec;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;
import me.desht.pneumaticcraft.common.item.DroneItem;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.world.item.DyeColor;

public class DroneColorCrafting extends CustomRecipe {
    public static final MapCodec<DroneColorCrafting> CODEC = MapCodec.unit(DroneColorCrafting::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, DroneColorCrafting> STREAM_CODEC = StreamCodec.unit(new DroneColorCrafting());
    public static RecipeSerializer<DroneColorCrafting> serializer() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }

    private static final List<Predicate<ItemStack>> ITEM_PREDICATES = List.of(
            stack -> stack.getItem() instanceof DroneItem,
            stack -> DyeColor.getColor(stack) != null
    );

    public DroneColorCrafting() {
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return ModCraftingHelper.allPresent(container, ITEM_PREDICATES);
    }

    @Override
    public ItemStack assemble(CraftingInput container) {
        List<ItemStack> stacks = ModCraftingHelper.findItems(container, ITEM_PREDICATES);
        if (stacks.size() < 2) return ItemStack.EMPTY;
        ItemStack drone = stacks.get(0).copyWithCount(1);
        DyeColor dyeColor = DyeColor.getColor(stacks.get(1));
        if (drone.isEmpty() || dyeColor == null) {
            return ItemStack.EMPTY;
        }

        drone.set(ModDataComponents.DRONE_COLOR, dyeColor.getId());

        return drone;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipeSerializers.DRONE_COLOR_CRAFTING.get();
    }
}

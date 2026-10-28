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
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.util.Util;

public class DroneUpgradeCrafting extends CustomRecipe {
    public static final MapCodec<DroneUpgradeCrafting> CODEC = MapCodec.unit(DroneUpgradeCrafting::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, DroneUpgradeCrafting> STREAM_CODEC = StreamCodec.unit(new DroneUpgradeCrafting());
    public static RecipeSerializer<DroneUpgradeCrafting> serializer() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }

    public static final List<Predicate<ItemStack>> ITEM_PREDICATES = List.of(
            DroneItem::isBasicDrone,
            stack -> stack.getItem() == ModItems.PRINTED_CIRCUIT_BOARD.get()
    );

    public DroneUpgradeCrafting() {
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return ModCraftingHelper.allPresent(container, ITEM_PREDICATES);
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        List<ItemStack> items = ModCraftingHelper.findItems(inv, ITEM_PREDICATES);
        if (items.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return Util.make(new ItemStack(ModItems.DRONE.get()),
                drone -> drone.applyComponents(items.getFirst().getComponents()));
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipeSerializers.DRONE_UPGRADE_CRAFTING.get();
    }
}

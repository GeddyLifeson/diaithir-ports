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
import me.desht.pneumaticcraft.common.item.minigun.StandardGunAmmoItem;
import net.minecraft.world.item.PotionItem;

public class GunAmmoPotionCrafting extends CustomRecipe {
    public static final MapCodec<GunAmmoPotionCrafting> CODEC = MapCodec.unit(GunAmmoPotionCrafting::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, GunAmmoPotionCrafting> STREAM_CODEC = StreamCodec.unit(new GunAmmoPotionCrafting());
    public static RecipeSerializer<GunAmmoPotionCrafting> serializer() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }

    private static final List<Predicate<ItemStack>> ITEM_PREDICATES = List.of(
            stack -> stack.getItem() instanceof PotionItem,
            stack -> stack.getItem() instanceof StandardGunAmmoItem
    );

    public GunAmmoPotionCrafting() {
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return ModCraftingHelper.allPresent(container, ITEM_PREDICATES);
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        List<ItemStack> stacks = ModCraftingHelper.findItems(inv, ITEM_PREDICATES);
        if (stacks.size() == 2) {
            ItemStack potion = stacks.get(0);
            ItemStack ammo = stacks.get(1).copy();
            if (!ammo.isEmpty() && !potion.isEmpty()) {
                StandardGunAmmoItem.setPotion(ammo, potion);
                return ammo;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipeSerializers.GUN_AMMO_POTION_CRAFTING.get();
    }
}

package net.diaithir.arms;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;

/**
 * Transmog: two armour pieces of the same slot in any crafting grid. The first (reading order) keeps its stats, enchantments and
 * durability; it takes the second one's worn model and inventory icon. Craft a piece with its own original look to undo it.
 */
public final class TransmogRecipe extends CustomRecipe {
    public static final TransmogRecipe INSTANCE = new TransmogRecipe();
    public static final MapCodec<TransmogRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, TransmogRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<TransmogRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private static boolean armour(ItemStack s) {
        if (s.getCount() != 1) return false;
        Equippable e = s.get(DataComponents.EQUIPPABLE);
        return e != null && e.assetId().isPresent() && e.slot().isArmor();
    }

    /** Returns {base, look} or null. */
    private static ItemStack[] pair(CraftingInput in) {
        if (in.ingredientCount() != 2) return null;
        ItemStack a = null;
        for (int i = 0; i < in.size(); i++) {
            ItemStack s = in.getItem(i);
            if (s.isEmpty()) continue;
            if (a == null) a = s;
            else {
                if (!armour(a) || !armour(s)) return null;
                if (a.get(DataComponents.EQUIPPABLE).slot() != s.get(DataComponents.EQUIPPABLE).slot()) return null;
                return new ItemStack[]{a, s};
            }
        }
        return null;
    }

    @Override public boolean matches(CraftingInput in, Level level) { return pair(in) != null; }

    @Override
    public ItemStack assemble(CraftingInput in) {
        ItemStack[] p = pair(in);
        return p == null ? ItemStack.EMPTY : apply(p[0], p[1]);
    }

    /** Public for the selftest. */
    public static ItemStack apply(ItemStack base, ItemStack look) {
        ItemStack out = base.copy();
        Equippable b = base.get(DataComponents.EQUIPPABLE), l = look.get(DataComponents.EQUIPPABLE);
        out.set(DataComponents.EQUIPPABLE, new Equippable(b.slot(), b.equipSound(), l.assetId(), b.cameraOverlay(), b.allowedEntities(),
                b.dispensable(), b.swappable(), b.damageOnHurt(), b.equipOnInteract(), b.canBeSheared(), b.shearingSound()));
        var model = look.get(DataComponents.ITEM_MODEL);
        out.set(DataComponents.ITEM_MODEL, model != null ? model : look.getItem().components().get(DataComponents.ITEM_MODEL));
        return out;
    }

    @Override public RecipeSerializer<TransmogRecipe> getSerializer() { return SERIALIZER; }
}

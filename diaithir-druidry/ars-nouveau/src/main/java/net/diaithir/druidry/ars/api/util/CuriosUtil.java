package net.diaithir.druidry.ars.api.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/** Port stub: Curios is not available, so there are never any worn curios. */
public class CuriosUtil {

    public static @Nullable IItemHandlerModifiable getAllWornItems(@NotNull LivingEntity living) {
        return null;
    }

    public static boolean hasItem(@Nullable LivingEntity entity, ItemStack stack) {
        return false;
    }

    public static boolean hasItem(LivingEntity entity, Item item) {
        return false;
    }
}

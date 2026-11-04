package dev.shadowsoffire.apothic_enchanting.compat;

import java.util.List;
import java.util.function.Predicate;

import dev.shadowsoffire.apothic_enchanting.Ench;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class CuriosCompat {

    public static List<ItemStack> getLifeMendingCurios(LivingEntity entity) {
        Predicate<ItemStack> hasLifeMend = stack -> stack.getEnchantmentLevel(entity.registryAccess().holderOrThrow(Ench.Enchantments.LIFE_MENDING)) > 0;
        List<SlotResult> slots = CuriosApi.getCuriosInventory(entity).map(handler -> handler.findCurios(hasLifeMend)).orElse(List.of());
        return slots.stream().map(SlotResult::stack).toList();
    }

}

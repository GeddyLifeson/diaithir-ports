package net.diaithir.druidry.ars.api.perk;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ITickablePerk {

    void tick(ItemStack stack, Level world, LivingEntity player, PerkInstance strength);

}

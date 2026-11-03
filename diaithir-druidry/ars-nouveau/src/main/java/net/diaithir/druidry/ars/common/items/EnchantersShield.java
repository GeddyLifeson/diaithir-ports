package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.common.perk.RepairingPerk;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static net.diaithir.druidry.ars.setup.registry.ItemsRegistry.defaultItemProperties;

public class EnchantersShield extends ShieldItem {

    public EnchantersShield() {
        super(defaultItemProperties().durability(500));
    }

    public EnchantersShield(Properties p_i48470_1_) {
        super(p_i48470_1_);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull net.minecraft.server.level.ServerLevel world, @NotNull Entity entity, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if(entity instanceof Player player)
            RepairingPerk.attemptRepair(stack, player);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level p_77659_1_, @NotNull Player p_77659_2_, @NotNull InteractionHand p_77659_3_) {
        return super.use(p_77659_1_, p_77659_2_, p_77659_3_);
    }





    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return false;
    }



}

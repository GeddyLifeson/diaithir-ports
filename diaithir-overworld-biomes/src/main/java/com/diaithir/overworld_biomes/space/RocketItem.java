package com.diaithir.overworld_biomes.space;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Use the rocket with a fluid cell (fuel) in your pack: Terra goes to the Moon, the Moon and orbit come back to Terra,
 * sneak-use on Terra goes to Earth orbit. One cell per launch. The rocket is not consumed.
 */
public final class RocketItem extends Item {
    public RocketItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResult use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
        ItemStack fuel = ItemStack.EMPTY;
        for (ItemStack s : sp.getInventory()) {
            if (BuiltInRegistries.ITEM.getKey(s.getItem()).toString().equals("stellaris:fluid_cell")) { fuel = s; break; }
        }
        if (fuel.isEmpty() && !sp.isCreative()) {
            sp.sendSystemMessage(Component.literal("The rocket needs a fluid cell of fuel."), true);
            return InteractionResult.FAIL;
        }
        String target = SpaceTravel.isVacuum(level) ? "terra" : (sp.isShiftKeyDown() ? "earth_orbit" : "moon");
        if (!sp.isCreative()) fuel.shrink(1);
        sp.getCooldowns().addCooldown(sp.getItemInHand(hand), 100);
        if (!SpaceTravel.launch(sp, target)) {
            sp.sendSystemMessage(Component.literal("Launch failed: " + target + " is not available."), true);
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }
}

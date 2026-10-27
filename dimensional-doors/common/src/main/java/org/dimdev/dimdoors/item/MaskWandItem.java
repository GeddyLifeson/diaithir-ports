package org.dimdev.dimdoors.item;

import net.minecraft.world.item.Item.TooltipContext;

import net.minecraft.world.InteractionResult;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MaskWandItem extends Item {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ID = "rift_configuration_tool";

    public MaskWandItem(Properties settings) {
    super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    HitResult hit = player.pick(RaycastHelper.REACH_DISTANCE, 0, false);

    if (world.isClientSide()) {
        return InteractionResult.FAIL;
    } else {
        if(hit.getType().equals(HitResult.Type.BLOCK)) {
//        MaskEntity mask = ModEntityTypes.MASK.create((ServerWorld) world, null, LiteralText.EMPTY, player, ((BlockHitResult) hit).getBlockPos(), SpawnReason.SPAWNER, true, false);
//        world.spawnEntity(mask);
        }
    }

    return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext level, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> list, TooltipFlag tooltipFlag) {
    if (I18n.exists(this.getDescriptionId() + ".info")) {
        list.accept(Component.translatable(this.getDescriptionId() + ".info"));
    }
    }
}

/*
 * This file ("ItemPlayerProbe.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import net.minecraft.world.entity.EquipmentSlot;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.item.Item;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import de.ellpeck.actuallyadditions.mod.components.ActuallyComponents;
import de.ellpeck.actuallyadditions.mod.items.base.ItemBase;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityPlayerInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class ItemPlayerProbe extends ItemBase {

    public ItemPlayerProbe() {
        super(ActuallyItems.defaultProps().stacksTo(1));
    }

    // TODO: [port] might be the wrong event
    @Override
    public void inventoryTick(@Nonnull ItemStack stack, ServerLevel world, @Nonnull Entity entity, EquipmentSlot slot) {
        if (!world.isClientSide()) {
            UUID uuid = stack.get(ActuallyComponents.UUID);
            if (uuid != null) {
                Player player = world.getPlayerByUUID(uuid);
                if (player != null) {
                    if (player.isShiftKeyDown()) {
                        ItemPhantomConnector.clearStorage(stack, ActuallyComponents.UUID.get(), ActuallyComponents.NAME.get());
                        ((Player) entity).sendSystemMessage(Component.translatable("tooltip.actuallyadditions.playerProbe.disconnect.1"));
                        player.sendSystemMessage(Component.translatable("tooltip.actuallyadditions.playerProbe.notice"));
                        //TheAchievements.GET_UNPROBED.get(player);
                    }
                } else {
                    ItemPhantomConnector.clearStorage(stack, ActuallyComponents.UUID.get(), ActuallyComponents.NAME.get());
                    ((Player) entity).sendSystemMessage(Component.translatable("tooltip.actuallyadditions.playerProbe.disconnect.2"));
                }
            }
        }
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        BlockEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
        if (tile instanceof TileEntityPlayerInterface face) {
            UUID uuid = stack.get(ActuallyComponents.UUID);
            String name = stack.get(ActuallyComponents.NAME);
            if (uuid != null) {
                if (!context.getLevel().isClientSide()) {
                    face.connectedPlayer = uuid;
                    face.playerName = name;
                    face.setChanged();
                    face.sendUpdate();

                    ItemPhantomConnector.clearStorage(stack, ActuallyComponents.UUID.get(), ActuallyComponents.NAME.get());
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    @Nonnull
    @Override
    public InteractionResult interactLivingEntity(@Nonnull ItemStack aStack, Player player, @Nonnull LivingEntity entity, @Nonnull InteractionHand hand) {
        if (!player.level().isClientSide()) {
            ItemStack stack = player.getMainHandItem();
            if (!stack.isEmpty() && stack.getItem() == this) {
                if (entity instanceof Player playerHit) {

                    if (!playerHit.isShiftKeyDown()) {
                        stack.set(ActuallyComponents.UUID, playerHit.getUUID());
                        stack.set(ActuallyComponents.NAME, playerHit.getName().getString());
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.FAIL;
    }

    
    @Override
    public void appendHoverText(ItemStack stack, @Nonnull Item.TooltipContext pContext, TooltipDisplay display, @Nonnull Consumer<Component> tooltip, @Nonnull TooltipFlag advanced) {
        String name = stack.get(ActuallyComponents.NAME);
        if (name != null) {
            tooltip.accept(Component.translatable("tooltip.actuallyadditions.playerProbe.probing").append(": " + name));
        }
    }
}

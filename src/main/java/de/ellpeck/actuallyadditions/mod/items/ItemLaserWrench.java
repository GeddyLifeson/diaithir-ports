/*
 * This file ("ItemLaserWrench.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import net.minecraft.world.item.Item;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.mod.components.ActuallyComponents;
import de.ellpeck.actuallyadditions.mod.items.base.ItemBase;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityLaserRelay;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class ItemLaserWrench extends ItemBase {

    public ItemLaserWrench() {
        super(ActuallyItems.defaultNonStacking());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level world = context.getLevel();
        Player player = context.getPlayer();

        ItemStack stack = context.getItemInHand();
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileEntityLaserRelay relay) {
            if (!world.isClientSide()) {
                if (ItemPhantomConnector.getStoredPosition(stack) == null) {
                    ItemPhantomConnector.storeConnection(stack, pos.getX(), pos.getY(), pos.getZ(), world);
                    if (player != null)
                        player.sendOverlayMessage(Component.translatable("tooltip.actuallyadditions.laser.stored.desc"));
                } else {
                    BlockPos savedPos = ItemPhantomConnector.getStoredPosition(stack);
                    if (savedPos != null) {
                        BlockEntity savedTile = world.getBlockEntity(savedPos);
                        if (savedTile instanceof TileEntityLaserRelay savedRelay) {
                            int distanceSq = (int) savedPos.distSqr(pos);

                            int lowestRange = Math.min(relay.getMaxRange(), savedRelay.getMaxRange());
                            int range = lowestRange * lowestRange;
                            if (ItemPhantomConnector.getStoredWorld(stack) == world.dimension() && savedRelay.type == relay.type && distanceSq <= range && ActuallyAdditionsAPI.connectionHandler.addConnection(savedPos, pos, relay.type, world, false, true)) {
                                ItemPhantomConnector.clearStorage(stack, ActuallyComponents.POSITION.get(), ActuallyComponents.LEVEL.get());

                                savedRelay.sendUpdate();
                                relay.sendUpdate();

                                if (player != null)
                                    player.sendOverlayMessage(Component.translatable("tooltip.actuallyadditions.laser.connected.desc"));

                                return InteractionResult.SUCCESS;
                            }
                        }

                        if (player != null)
                            player.sendSystemMessage(Component.translatable("tooltip.actuallyadditions.laser.cantConnect.desc"));
                        ItemPhantomConnector.clearStorage(stack, ActuallyComponents.POSITION.get(), ActuallyComponents.LEVEL.get());
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    //    // TODO: [port] ensure this is correct
    //    @Nullable
    //    @Override
    //    public CompoundNBT getShareTag(ItemStack stack) {
    //        return new CompoundNBT();
    //    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext playerIn, TooltipDisplay display, Consumer<Component> list, TooltipFlag advanced) {
        BlockPos coords = ItemPhantomConnector.getStoredPosition(stack);
        if (coords != null) {
            list.accept(Component.translatable("tooltip.actuallyadditions.boundTo.desc").append(":"));
            list.accept(Component.literal("X: " + coords.getX()));
            list.accept(Component.literal("Y: " + coords.getY()));
            list.accept(Component.literal("Z: " + coords.getZ()));
            list.accept(Component.translatable("tooltip.actuallyadditions.clearStorage.desc").withStyle(ChatFormatting.ITALIC));
        }
    }
}

// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: MessageSender replaced by PacketDistributor, config via ModConfigSpec.
package net.diaithir.hudoverlays.pickupnotifier.server;

import net.diaithir.hudoverlays.pickupnotifier.config.ServerConfig;
import net.diaithir.hudoverlays.pickupnotifier.network.TakeItemPayload;
import net.diaithir.hudoverlays.pickupnotifier.network.TakeItemStackPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PickupServerHandler {
    private static ItemStack currentStack = ItemStack.EMPTY;

    private PickupServerHandler() {}

    public static void onEntityItemPickupPre1(ItemEntityPickupEvent.Pre evt) {
        if (ServerConfig.BACKPACK_COMPAT.get()) {
            ItemStack itemStack = evt.getItemEntity().getItem();
            if (!itemStack.isEmpty() && !evt.getItemEntity().isRemoved()) {
                currentStack = itemStack.copy();
            }
        }
    }

    public static void onEntityItemPickupPre2(ItemEntityPickupEvent.Pre evt) {
        if (!(evt.getPlayer() instanceof ServerPlayer serverPlayer)) return;
        ItemEntity itemEntity = evt.getItemEntity();
        Player player = evt.getPlayer();
        ItemStack stack = itemEntity.getItem();
        if (ServerConfig.BACKPACK_COMPAT.get() && evt.canPickup() != TriState.DEFAULT) {
            if (!currentStack.isEmpty()) {
                boolean sendTakeMessage = false;
                int takeAmount = currentStack.getCount() - stack.getCount();
                if (takeAmount > 0) {
                    currentStack.setCount(takeAmount);
                    sendTakeMessage = true;
                } else if (itemEntity.isRemoved()) {
                    sendTakeMessage = true;
                }
                if (sendTakeMessage) {
                    PacketDistributor.sendToPlayer(serverPlayer, new TakeItemStackPayload(currentStack.copy()));
                }
            }
            currentStack = ItemStack.EMPTY;
        } else if (ServerConfig.PARTIAL_PICK_UPS.get() && !itemEntity.isRemoved()) {
            // requires additional checks as it might actually not be possible for the item to be picked up
            if (!itemEntity.hasPickUpDelay()
                    && (itemEntity.getOwner() == null || itemEntity.lifespan - itemEntity.getAge() <= 200
                    || itemEntity.getOwner().equals(player.getUUID()))) {
                int itemAmount = 0;
                if (player.getInventory().getFreeSlot() != -1) {
                    itemAmount = stack.getCount();
                } else {
                    int slotWithRemainingSpace = player.getInventory().getSlotWithRemainingSpace(stack);
                    if (slotWithRemainingSpace != -1) {
                        itemAmount = getSpaceAtIndex(player.getInventory(), slotWithRemainingSpace, stack);
                    }
                }
                if (itemAmount > 0) {
                    PacketDistributor.sendToPlayer(serverPlayer, new TakeItemPayload(itemEntity.getId(), itemAmount));
                }
            }
        }
    }

    public static void onItemPickupPost(ItemEntityPickupEvent.Post evt) {
        if (!(evt.getPlayer() instanceof ServerPlayer serverPlayer)) return;
        if (!ServerConfig.PARTIAL_PICK_UPS.get() && !evt.getItemEntity().isRemoved()) {
            PacketDistributor.sendToPlayer(serverPlayer,
                    new TakeItemPayload(evt.getItemEntity().getId(), evt.getOriginalStack().getCount()));
        }
    }

    private static int getSpaceAtIndex(Inventory inventory, int slotIndex, ItemStack stack) {
        int itemCount = stack.getCount();
        ItemStack itemstack = inventory.getItem(slotIndex);
        int itemSpace = itemCount;
        if (itemCount > itemstack.getMaxStackSize() - itemstack.getCount()) {
            itemSpace = itemstack.getMaxStackSize() - itemstack.getCount();
        }
        if (itemSpace > inventory.getMaxStackSize() - itemstack.getCount()) {
            itemSpace = inventory.getMaxStackSize() - itemstack.getCount();
        }
        return itemSpace;
    }
}

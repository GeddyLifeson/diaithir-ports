package net.diaithir.druidry.ars.common.entity.goal.wixie;

import net.diaithir.druidry.ars.api.event.EventQueue;
import net.diaithir.druidry.ars.api.event.ITimedEvent;
import net.diaithir.druidry.ars.api.util.BlockUtil;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.block.tile.WixieCauldronTile;
import net.diaithir.druidry.ars.common.entity.EntityFlyingItem;
import net.diaithir.druidry.ars.common.entity.EntityWixie;
import net.diaithir.druidry.ars.common.entity.goal.ExtendedRangeGoal;
import net.diaithir.druidry.ars.common.network.Networking;
import net.diaithir.druidry.ars.common.network.PacketAnimEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.*;

public class FindNextItemGoal extends ExtendedRangeGoal {
    EntityWixie wixie;
    BlockPos movePos;
    ItemStack getStack;
    boolean found;

    public record HandlerPos(BlockPos pos, ResourceHandler<ItemResource> handler) {
    }

    public static class FlyingItemEvent implements ITimedEvent {
        private final Level level;
        private final EntityFlyingItem entityFlyingItem;
        private final int delay;
        private int ticks;
        private boolean spawned;

        public FlyingItemEvent(Level level, EntityFlyingItem entityFlyingItem, int delay) {
            this.level = level;
            this.entityFlyingItem = entityFlyingItem;
            this.delay = delay;
        }

        @Override
        public void tick(boolean serverSide) {
            if (serverSide) {
                ticks++;
                if (!spawned && ticks >= delay) {
                    spawned = true;
                    level.addFreshEntity(entityFlyingItem);
                }
            }
        }

        @Override
        public boolean isExpired() {
            return spawned || ticks >= delay;
        }
    }

    public FindNextItemGoal(EntityWixie wixie) {
        super(10);
        this.wixie = wixie;
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }

    @Override
    public void start() {
        super.start();
        movePos = null;
        Level world = wixie.level();
        WixieCauldronTile tile = (WixieCauldronTile) world.getBlockEntity(wixie.cauldronPos);
        if (tile == null || tile.getInventories() == null) {
            found = true;
            return;
        }
        getStack = tile.craftManager.getNextItem();
        if (getStack.isEmpty()) {
            found = true;
            return;
        }
        Set<Item> itemSet = new HashSet<>();
        itemSet.add(getStack.getItem());
        for (BlockPos b : tile.getInventories()) {
            BlockEntity blockEntity = world.getBlockEntity(b);
            if(blockEntity == null)
                continue;
            ResourceHandler<ItemResource> itemHandler = world.getCapability(Capabilities.Item.BLOCK, b, null);
            if (itemHandler == null)
                continue;
            for(int i = 0; i < itemHandler.size(); i++){
                ItemResource resource = itemHandler.getResource(i);
                if(!resource.isEmpty() && resource.getItem() == getStack.getItem()){
                    movePos = b.immutable();
                    this.startDistance = BlockUtil.distanceFrom(wixie.position(), movePos);
                    break;
                }
            }
            if(movePos != null){
                break;
            }
        }
        found = false;
    }

    @Override
    public boolean canUse() {
        if (wixie.cauldronPos == null)
            return false;
        BlockEntity tileEntity = wixie.level().getBlockEntity(wixie.cauldronPos);
        if (!(tileEntity instanceof WixieCauldronTile cauldronTile))
            return false;

        boolean canStart = wixie.inventoryBackoff == 0
                && cauldronTile.hasSource
                && !cauldronTile.isCraftingDone()
                && !cauldronTile.isOff
                && !cauldronTile.craftManager.getNextItem().isEmpty();
        return canStart;
    }

    @Override
    public boolean canContinueToUse() {
        return !found;
    }

    @Override
    public void tick() {
        super.tick();

        if (!found && movePos != null && BlockUtil.distanceFrom(wixie.position(), Vec3.atCenterOf(movePos)) < 1.3 + this.extendedRange) {

            WixieCauldronTile tile = (WixieCauldronTile) wixie.level().getBlockEntity(wixie.cauldronPos);
            Level world = wixie.level();
            if (tile == null) {
                found = true;
                return;
            }
            List<ItemStack> neededStacks = new ArrayList<>(tile.craftManager.neededItems);
            boolean anyFound = false;
            int spawnDelay = 0;
            List<HandlerPos> handlers = new ArrayList<>();
            for (BlockPos b : tile.getInventories()) {
                BlockEntity blockEntity = world.getBlockEntity(b);
                if(blockEntity == null)
                    continue;
                ResourceHandler<ItemResource> itemHandler = world.getCapability(Capabilities.Item.BLOCK, b, null);
                if (itemHandler == null)
                    continue;
               handlers.add(new HandlerPos(b.immutable(), itemHandler));
            }
            for(ItemStack needed : neededStacks) {
                for (HandlerPos handler : handlers) {
                    if(tile.craftManager.neededItems.isEmpty()){
                        found = true;
                        return;
                    }
                    ResourceHandler<ItemResource> itemHandler = handler.handler();
                    for (int j = 0; j < itemHandler.size(); j++) {
                        ItemResource resource = itemHandler.getResource(j);
                        if (!resource.isEmpty() && resource.getItem() == needed.getItem()) {
                            final Item slotItem = resource.getItem();
                            // Extract from the same slot multiple times
                            int size = itemHandler.getAmountAsInt(j);
                            int numNeeded = (int) tile.craftManager.neededItems.stream().filter(stack -> stack.getItem() == slotItem).count();
                            int canExtract = Math.min(size, numNeeded);
                            for(int count = 0; count < canExtract; count++) {
                                ItemStack stackToGive = ItemStack.EMPTY;
                                try (Transaction tx = Transaction.openRoot()) {
                                    int extracted = itemHandler.extract(j, resource, 1, tx);
                                    if (extracted > 0) {
                                        stackToGive = resource.toStack(extracted);
                                        tx.commit();
                                    }
                                }
                                if (stackToGive.isEmpty()) {
                                    break;
                                }
                                spawnFlyingItem(tile.getLevel(), tile.getBlockPos(), handler.pos(), stackToGive, 1 + 3 * spawnDelay++);
                                tile.giveItem(stackToGive);
                                if (!anyFound) {
                                    Networking.sendToNearbyClient(world, wixie, new PacketAnimEntity(wixie.getId(), EntityWixie.Animations.SUMMON_ITEM.ordinal()));
                                    wixie.inventoryBackoff = 60;
                                    anyFound = true;
                                }
                            }
                        }
                    }
                }
            }
            found = true;
            return;
        }

        if (movePos != null && !found) {
            setPath(movePos.getX(), movePos.getY() + 1, movePos.getZ(), 1.2D);
        }
    }

    public void spawnFlyingItem(Level level, BlockPos worldPosition, BlockPos from, ItemStack stack, int delay) {
        BlockPos above = from.above();
        EntityFlyingItem flyingItem = new EntityFlyingItem(level,
                new Vec3(above.getX() + 0.5, above.getY(), above.getZ() + 0.5).add(ParticleUtil.inRange(-0.25, 0.25), 0, ParticleUtil.inRange(-0.25, 0.25)),
                new Vec3(worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5).add(ParticleUtil.inRange(-0.25, 0.25), 0, ParticleUtil.inRange(-0.25, 0.25)));
        flyingItem.getEntityData().set(EntityFlyingItem.HELD_ITEM, stack.copy());
        EventQueue.getServerInstance().addEvent(new FlyingItemEvent(level, flyingItem, delay));
    }


    public void setPath(double x, double y, double z, double speedIn) {
        wixie.getNavigation().moveTo(wixie.getNavigation().createPath(x + 0.5, y + 0.5, z + 0.5, 0), speedIn);
    }

}

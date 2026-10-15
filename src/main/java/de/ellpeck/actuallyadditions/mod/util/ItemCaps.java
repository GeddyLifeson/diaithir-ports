package de.ellpeck.actuallyadditions.mod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;

/** Item capability lookups (26.1 hands out ResourceHandlers), seen as the IItemHandler AA's machines use. */
public final class ItemCaps {
    private ItemCaps() {}

    @Nullable
    public static IItemHandler block(Level level, BlockPos pos, @Nullable Direction side) {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
        return handler == null ? null : IItemHandler.of(handler);
    }
}

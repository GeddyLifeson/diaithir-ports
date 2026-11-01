package com.github.alexmodguy.alexscaves.server.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Items whose animation state is ticked on the client. 26.1 only calls Item#inventoryTick on the server, so the client calls this instead. */
public interface ClientTickingItem {
    void clientInventoryTick(ItemStack stack, Level level, Entity entity);
}

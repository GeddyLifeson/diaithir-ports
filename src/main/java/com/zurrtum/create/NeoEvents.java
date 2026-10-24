package com.zurrtum.create;

import com.zurrtum.create.content.contraptions.glue.SuperGlueHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Replacements for hooks the Fabric build implemented with mixins. */
public class NeoEvents {
    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof ServerPlayer player) {
            SuperGlueHandler.glueListensForBlockPlacement(level, player, event.getPos());
        }
    }
}

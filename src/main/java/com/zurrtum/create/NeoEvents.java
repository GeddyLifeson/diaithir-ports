package com.zurrtum.create;

import com.zurrtum.create.content.contraptions.glue.SuperGlueHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.foundation.recipe.RecipeFinder;
import com.zurrtum.create.foundation.recipe.trie.RecipeTrieFinder;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Replacements for hooks the Fabric build implemented with mixins. */
public class NeoEvents {
    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof ServerPlayer player) {
            SuperGlueHandler.glueListensForBlockPlacement(level, player, event.getPos());
        }
    }

    @SubscribeEvent
    public void onEnteringSection(net.neoforged.neoforge.event.entity.EntityEvent.EnteringSection event) {
        com.zurrtum.create.content.trains.entity.CarriageEntityHandler.onEntityEnterSection(event.getEntity(), event.getPackedOldPos(), event.getPackedNewPos());
    }

    @SubscribeEvent
    public void onServerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(Create.MOD_ID, "recipe_finder"), RecipeFinder.LISTENER);
        event.addListener(Identifier.fromNamespaceAndPath(Create.MOD_ID, "recipe_trie_finder"), RecipeTrieFinder.LISTENER);
        event.addListener(Identifier.fromNamespaceAndPath(Create.MOD_ID, "belt_helper"), BeltHelper.LISTENER);
        event.addListener(Identifier.fromNamespaceAndPath(Create.MOD_ID, "configs"), AllConfigs.LISTENER);
    }
}

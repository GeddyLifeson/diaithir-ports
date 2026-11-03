package net.diaithir.worldsys.questbook;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;

/** Quest definitions, team based progress and reward handling. */
public final class QuestBookFeature {
    private QuestBookFeature() {}

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> QuestCommands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, e -> {
            if (e.getSource().getEntity() instanceof ServerPlayer p)
                QuestTracker.add(p, Quest.Task.KILL, BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType()).toString(), 1);
        });
        NeoForge.EVENT_BUS.addListener(BreakBlockEvent.class, e -> {
            if (e.getPlayer() instanceof ServerPlayer p)
                QuestTracker.add(p, Quest.Task.MINE, BuiltInRegistries.BLOCK.getKey(e.getState().getBlock()).toString(), 1);
        });
        NeoForge.EVENT_BUS.addListener(ItemEntityPickupEvent.Post.class, e -> {
            if (e.getPlayer() instanceof ServerPlayer p)
                QuestTracker.add(p, Quest.Task.COLLECT, BuiltInRegistries.ITEM.getKey(e.getOriginalStack().getItem()).toString(), e.getOriginalStack().getCount() - e.getItemEntity().getItem().getCount());
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.ItemCraftedEvent.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p)
                QuestTracker.add(p, Quest.Task.CRAFT, BuiltInRegistries.ITEM.getKey(e.getCrafting().getItem()).toString(), e.getCrafting().getCount());
        });
    }
}

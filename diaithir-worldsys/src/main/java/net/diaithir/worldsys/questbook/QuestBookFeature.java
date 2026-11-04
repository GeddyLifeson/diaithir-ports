package net.diaithir.worldsys.questbook;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/** Quest engine: data-driven quests (data/&lt;ns&gt;/questbook), team based progress, accept and abort, rewards. */
public final class QuestBookFeature {
    private QuestBookFeature() {}

    public static void register(IEventBus modBus) {
        QuestLoader.init();
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> QuestCommands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, e -> {
            if (e.getSource().getEntity() instanceof ServerPlayer p) {
                var type = e.getEntity().getType();
                QuestTracker.add(p, Quest.Task.KILL, BuiltInRegistries.ENTITY_TYPE.getKey(type), t -> e.getEntity().is(TagKey.create(Registries.ENTITY_TYPE, t)), 1);
            }
        });
        NeoForge.EVENT_BUS.addListener(BreakBlockEvent.class, e -> {
            if (e.getPlayer() instanceof ServerPlayer p) {
                var state = e.getState();
                QuestTracker.add(p, Quest.Task.MINE, BuiltInRegistries.BLOCK.getKey(state.getBlock()), t -> state.is(TagKey.create(Registries.BLOCK, t)), 1);
            }
        });
        NeoForge.EVENT_BUS.addListener(ItemEntityPickupEvent.Post.class, e -> {
            if (e.getPlayer() instanceof ServerPlayer p) {
                var stack = e.getOriginalStack();
                QuestTracker.add(p, Quest.Task.COLLECT, BuiltInRegistries.ITEM.getKey(stack.getItem()), t -> stack.is(TagKey.create(Registries.ITEM, t)),
                    stack.getCount() - e.getItemEntity().getItem().getCount());
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.ItemCraftedEvent.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p) {
                var stack = e.getCrafting();
                QuestTracker.add(p, Quest.Task.CRAFT, BuiltInRegistries.ITEM.getKey(stack.getItem()), t -> stack.is(TagKey.create(Registries.ITEM, t)), stack.getCount());
            }
        });
    }
}

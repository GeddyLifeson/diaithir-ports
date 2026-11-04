package net.diaithir.worldevents;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Schedules world events. Every 24000 ticks one event is rolled by weight (no sooner than 12000 ticks after the
 * last). The events are always on; there are no switches. Port of Global Events by MD22 (MIT).
 */
@Mod("diaithirworldevents")
public final class WorldEvents {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Random RANDOM = new Random();
    public static final int INTERVAL = 24000;
    public static final int MIN_GAP = 12000;

    private static final Map<String, WorldEvent> REGISTRY = new LinkedHashMap<>();
    private static int lastRoll;

    public WorldEvents() {
        register(new MeteorStrike());
        register(BloodMoon.INSTANCE);
        register(new WanderingCaravan());
        register(GoldenHour.INSTANCE);
        NeoForge.EVENT_BUS.register(this);
    }

    public static void register(WorldEvent event) {
        REGISTRY.put(event.id(), event);
    }

    public static void announce(MinecraftServer server, String text, ChatFormatting colour) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(text).withStyle(colour), false);
    }

    public static String start(ServerLevel level, WorldEvent event) {
        List<ServerPlayer> players = new ArrayList<>(level.players());
        LOG.info("Starting world event {}", event.id());
        String result = event.trigger(level, players);
        NeoForge.EVENT_BUS.post(new WorldEventStartedEvent(event, level));
        return result;
    }

    static WorldEvent pick(ServerLevel level) {
        List<WorldEvent> ok = REGISTRY.values().stream().filter(e -> e.possible(level)).toList();
        int total = ok.stream().mapToInt(WorldEvent::weight).sum();
        if (total <= 0) return null;
        int roll = RANDOM.nextInt(total);
        for (WorldEvent e : ok) {
            roll -= e.weight();
            if (roll < 0) return e;
        }
        return null;
    }

    static String status() {
        List<String> now = new ArrayList<>();
        if (GoldenHour.active()) now.add("Golden Hour (" + GoldenHour.remaining() / 20 + " s left)");
        if (BloodMoon.active()) now.add("Blood Moon");
        return "Running: " + (now.isEmpty() ? "no event." : String.join(", ", now));
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent.Post e) {
        MinecraftServer server = e.getServer();
        ServerLevel level = server.overworld();
        if (level == null) return;
        int tick = server.getTickCount();
        GoldenHour.tick(server);
        BloodMoon.tick(level);
        if (tick % INTERVAL != 0 || tick - lastRoll < MIN_GAP) return;
        lastRoll = tick;
        if (level.players().isEmpty()) return;
        WorldEvent event = pick(level);
        if (event != null) start(level, event);
    }

    @SubscribeEvent
    public void onStopped(ServerStoppedEvent e) {
        GoldenHour.reset();
        BloodMoon.reset();
        lastRoll = 0;
    }

    @SubscribeEvent
    public void onDrops(BlockDropsEvent e) {
        if (!GoldenHour.active() || !GoldenHour.isOre(e.getState())) return;
        List<net.minecraft.world.entity.item.ItemEntity> extra = new ArrayList<>();
        for (net.minecraft.world.entity.item.ItemEntity drop : e.getDrops()) {
            net.minecraft.world.entity.item.ItemEntity copy = new net.minecraft.world.entity.item.ItemEntity(
                    e.getLevel(), drop.getX(), drop.getY(), drop.getZ(), drop.getItem().copy());
            extra.add(copy);
        }
        e.getDrops().addAll(extra);
    }

    @SubscribeEvent
    public void onCommands(RegisterCommandsEvent e) {
        var root = Commands.literal("worldevents")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("status").executes(c -> {
                    c.getSource().sendSuccess(() -> Component.literal(status()), false);
                    return 1;
                }));
        var start = Commands.literal("start");
        for (WorldEvent ev : REGISTRY.values()) {
            start.then(Commands.literal(ev.id()).executes(c -> {
                String r = start(c.getSource().getServer().overworld(), ev);
                c.getSource().sendSuccess(() -> Component.literal(r), true);
                return 1;
            }));
        }
        e.getDispatcher().register(root.then(start));
    }
}

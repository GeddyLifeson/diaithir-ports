package net.diaithir.claims.log;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Grief log: who broke or placed which block where and when, with inspect and rollback.
 * Clean-room, from the behaviour of block-logging mods (GriefLogger is Apache-2.0 and needs Knot, UILib and YamlConfig libraries
 * plus 54 mixin points; no code taken). Events are kept in memory for the session (last 20000) and appended to
 * world/grieflog/YYYY-MM-DD.ndjson. /gl inspect [N], /gl rollback RADIUS MINUTES.
 */
public final class GriefLog {
    private static final int KEEP = 20000;

    record Event(long time, String dim, int x, int y, int z, UUID who, String name, boolean broke, String state) {}

    private static final List<Event> EVENTS = new CopyOnWriteArrayList<>();
    private static volatile Path dir;

    private GriefLog() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent e) -> {
            dir = e.getServer().getWorldPath(LevelResource.ROOT).resolve("grieflog");
        });
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener((BreakBlockEvent e) -> {
            if (e.getPlayer() instanceof ServerPlayer p) log(p, e.getPos(), true, e.getState());
        });
        NeoForge.EVENT_BUS.addListener((BlockEvent.EntityPlaceEvent e) -> {
            if (e.getEntity() instanceof ServerPlayer p) log(p, e.getPos(), false, e.getPlacedBlock());
        });
    }

    private static String enc(BlockState s) {
        return BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString();
    }

    static void log(ServerPlayer p, BlockPos pos, boolean broke, BlockState state) {
        Event ev = new Event(System.currentTimeMillis(), p.level().dimension().identifier().toString(), pos.getX(), pos.getY(), pos.getZ(),
            p.getUUID(), p.getGameProfile().name(), broke, enc(state));
        EVENTS.add(ev);
        while (EVENTS.size() > KEEP) EVENTS.remove(0);
        Path d = dir;
        if (d == null) return;
        try {
            Files.createDirectories(d);
            String line = "{\"t\":" + ev.time() + ",\"dim\":\"" + ev.dim() + "\",\"x\":" + ev.x() + ",\"y\":" + ev.y() + ",\"z\":" + ev.z()
                + ",\"who\":\"" + ev.who() + "\",\"name\":\"" + ev.name() + "\",\"act\":\"" + (broke ? "break" : "place") + "\",\"block\":\"" + ev.state() + "\"}\n";
            Files.writeString(d.resolve(LocalDate.now() + ".ndjson"), line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // The log is best effort; the in-memory list still works.
        }
    }

    private static int say(CommandSourceStack s, String t) {
        s.sendSystemMessage(Component.literal(t));
        return 1;
    }

    private static void register(CommandDispatcher<CommandSourceStack> d) {
        var admin = Commands.literal("gl").requires(src -> src.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER));
        d.register(admin
            .then(Commands.literal("inspect").executes(c -> inspect(c.getSource(), 10)).then(Commands.argument("n", IntegerArgumentType.integer(1, 100))
                .executes(c -> inspect(c.getSource(), IntegerArgumentType.getInteger(c, "n")))))
            .then(Commands.literal("rollback").then(Commands.argument("radius", IntegerArgumentType.integer(1, 64)).then(Commands.argument("minutes", IntegerArgumentType.integer(1, 10080))
                .executes(c -> rollback(c.getSource(), IntegerArgumentType.getInteger(c, "radius"), IntegerArgumentType.getInteger(c, "minutes")))))));
    }

    private static int inspect(CommandSourceStack s, int n) {
        ServerPlayer p = s.getPlayer();
        if (p == null) return say(s, "Players only.");
        BlockPos at = p.blockPosition();
        String dim = p.level().dimension().identifier().toString();
        Deque<String> out = new ArrayDeque<>();
        for (int i = EVENTS.size() - 1; i >= 0 && out.size() < n; i--) {
            Event e = EVENTS.get(i);
            if (e.dim().equals(dim) && Math.abs(e.x() - at.getX()) <= 5 && Math.abs(e.y() - at.getY()) <= 5 && Math.abs(e.z() - at.getZ()) <= 5) {
                out.add(((System.currentTimeMillis() - e.time()) / 1000) + " s ago: " + e.name() + (e.broke() ? " broke " : " placed ") + e.state() + " at " + e.x() + " " + e.y() + " " + e.z());
            }
        }
        if (out.isEmpty()) return say(s, "Nothing logged within 5 blocks this session.");
        out.forEach(l -> say(s, l));
        return out.size();
    }

    private static int rollback(CommandSourceStack s, int radius, int minutes) {
        ServerPlayer p = s.getPlayer();
        if (p == null) return say(s, "Players only.");
        ServerLevel level = p.level();
        String dim = level.dimension().identifier().toString();
        BlockPos at = p.blockPosition();
        long since = System.currentTimeMillis() - minutes * 60_000L;
        List<Event> hit = new ArrayList<>();
        for (Event e : EVENTS) {
            if (e.time() >= since && e.dim().equals(dim) && Math.abs(e.x() - at.getX()) <= radius && Math.abs(e.y() - at.getY()) <= radius && Math.abs(e.z() - at.getZ()) <= radius) hit.add(e);
        }
        int n = 0;
        for (int i = hit.size() - 1; i >= 0; i--) {
            Event e = hit.get(i);
            BlockPos pos = new BlockPos(e.x(), e.y(), e.z());
            if (e.broke()) {
                var b = BuiltInRegistries.BLOCK.get(net.minecraft.resources.Identifier.parse(e.state()));
                if (b.isPresent() && level.getBlockState(pos).isAir()) {
                    level.setBlockAndUpdate(pos, b.get().value().defaultBlockState());
                    n++;
                }
            } else if (enc(level.getBlockState(pos)).equals(e.state())) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                n++;
            }
        }
        EVENTS.removeAll(hit);
        return say(s, "Rolled back " + n + " changes.");
    }
}

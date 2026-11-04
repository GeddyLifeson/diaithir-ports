package net.diaithir.structurenavigation.structurememory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.Command;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Structure Memory, clean-room behaviour: each player keeps a persistent list of the structures
 * they have walked into. First entry shows an action-bar message; /structurememory lists them.
 * State is one JSON file in the world's data folder. No code from any existing mod was used.
 */
public final class Feature {
    private static final Logger LOG = LoggerFactory.getLogger("diaithir-structurememory");
    private static final Type MAP_TYPE = new TypeToken<Map<String, Set<String>>>() {}.getType();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int CHECK_INTERVAL = 20;

    private static final Map<UUID, Set<String>> KNOWN = new ConcurrentHashMap<>();
    private static volatile boolean dirty;
    private static Path file;

    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, e -> load(e.getServer()));
        NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, e -> save());
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.tick.ServerTickEvent.Post.class, e -> {
            if (e.getServer().getTickCount() % 6000 == 0) save();
        });
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p && p.tickCount % CHECK_INTERVAL == 0) check(p);
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e ->
                e.getDispatcher().register(Commands.literal("structurememory")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            Set<String> known = KNOWN.getOrDefault(p.getUUID(), Set.of());
                            if (known.isEmpty()) {
                                ctx.getSource().sendSuccess(() -> Component.literal("You have not discovered any structures yet."), false);
                            } else {
                                ctx.getSource().sendSuccess(() -> Component.literal("Discovered structures (" + known.size() + "): "
                                        + String.join(", ", new TreeSet<>(known))), false);
                            }
                            return Command.SINGLE_SUCCESS;
                        })));
    }

    private static void check(ServerPlayer player) {
        ServerLevel level = player.level();
        BlockPos pos = player.blockPosition();
        var manager = level.structureManager();
        if (!manager.hasAnyStructureAt(pos)) return;
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        for (Structure s : manager.getAllStructuresAt(pos).keySet()) {
            var start = manager.getStructureWithPieceAt(pos, s);
            if (!start.isValid()) continue;
            var key = registry.getKey(s);
            if (key == null) continue;
            String id = key.toString();
            if (KNOWN.computeIfAbsent(player.getUUID(), u -> ConcurrentHashMap.newKeySet()).add(id)) {
                dirty = true;
                player.sendSystemMessage(Component.literal("Discovered: " + id), true);
            }
        }
    }

    private static void load(MinecraftServer server) {
        KNOWN.clear();
        file = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("diaithir_structurememory.json");
        if (!Files.exists(file)) return;
        try {
            Map<String, Set<String>> raw = GSON.fromJson(Files.readString(file), MAP_TYPE);
            if (raw != null) raw.forEach((k, v) -> {
                Set<String> set = ConcurrentHashMap.newKeySet();
                set.addAll(v);
                KNOWN.put(UUID.fromString(k), set);
            });
        } catch (IOException | RuntimeException ex) {
            LOG.warn("Could not read structure memory", ex);
        }
    }

    private static void save() {
        if (!dirty || file == null) return;
        try {
            Files.createDirectories(file.getParent());
            Map<String, List<String>> out = new TreeMap<>();
            KNOWN.forEach((k, v) -> out.put(k.toString(), new ArrayList<>(new TreeSet<>(v))));
            Files.writeString(file, GSON.toJson(out));
            dirty = false;
        } catch (IOException ex) {
            LOG.warn("Could not save structure memory", ex);
        }
    }
}

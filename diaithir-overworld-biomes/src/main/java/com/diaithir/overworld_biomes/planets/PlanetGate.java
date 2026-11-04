package com.diaithir.overworld_biomes.planets;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panscriptum planets on demand. The 216 planet dimensions are NOT in the LevelStem registry (vanilla would load them all at
 * startup: about 30 MB each and a slow save). Their stems sit in data/diaithir_planets/planet_stems/&lt;id&gt;.json and become a
 * live ServerLevel the first time something asks for them; an empty planet is saved and unloaded again.
 * API for other mods: {@link #load(MinecraftServer, String)}, {@link #enter(ServerPlayer, String)}.
 */
public final class PlanetGate {
    public static final String NS = "diaithir_planets";
    private static final Logger LOG = LoggerFactory.getLogger("PlanetGate");
    private static final int CHECK_EVERY = 200, IDLE_CHECKS = 12;
    private static final Map<ResourceKey<Level>, Integer> IDLE = new HashMap<>();
    private static int tick;

    private PlanetGate() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, e -> tick(e.getServer()));
    }

    public static ResourceKey<Level> key(String id) {
        return ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(NS, id));
    }

    /** The live level of a planet, creating it from its stem if it is not loaded. Server thread only. Null if the id is unknown. */
    public static ServerLevel load(MinecraftServer s, String id) {
        ResourceKey<Level> key = key(id);
        ServerLevel have = s.getLevel(key);
        if (have != null) return have;
        Identifier res = Identifier.fromNamespaceAndPath(NS, "planet_stems/" + id + ".json");
        LevelStem stem;
        try (Reader r = s.getResourceManager().getResource(res).orElseThrow().openAsReader()) {
            JsonElement j = JsonParser.parseReader(r);
            stem = LevelStem.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, s.registryAccess()), j)
                    .resultOrPartial(err -> LOG.error("Planet {}: {}", id, err)).orElseThrow();
        } catch (Exception ex) {
            LOG.warn("Unknown or broken planet '{}': {}", id, ex.toString());
            return null;
        }
        var data = s.getWorldData();
        long zoom = BiomeManager.obfuscateSeed(s.getWorldGenSettings().options().seed());
        ServerLevel lvl = new ServerLevel(s, s.executor, s.storageSource, new DerivedLevelData(data, data.overworldData()),
                key, stem, data.isDebugWorld(), zoom, List.of(), false);
        s.forgeGetWorldMap().put(key, lvl);
        s.markWorldsDirty();
        NeoForge.EVENT_BUS.post(new LevelEvent.Load(lvl));
        lvl.getWorldBorder().setAbsoluteMaxSize(s.getAbsoluteMaxWorldSize());
        s.getPlayerList().addWorldborderListener(lvl);
        IDLE.put(key, 0);
        LOG.info("Planet {} loaded ({} planets live)", id, live(s));
        return lvl;
    }

    private static final java.util.Map<String, int[]> LAND = new java.util.concurrent.ConcurrentHashMap<>();
    private static final String[] ROLE = {"_sea", "_deep_sea", "_shore", "_river", "_hollows", "_grottoes"};

    private static boolean isLand(ServerLevel lvl, int x, int z) {
        var k = lvl.getUncachedNoiseBiome(x >> 2, 16, z >> 2).unwrapKey();
        if (k.isEmpty()) return false;
        String p = k.get().identifier().getPath();
        for (String r : ROLE) if (p.endsWith(r)) return false;
        return true;
    }

    /** Nearest point to 0,0 that is land with land all around it (biome check only, no chunks generated). */
    public static int[] findLand(ServerLevel lvl, String id) {
        return LAND.computeIfAbsent(id, i -> {
            int[][] d = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
            int[][] n4 = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int r = 0; r <= 12000; r += 32)
                for (int k = 0; k < 32; k++) {
                    int x = (int) Math.round(Math.cos(k * Math.PI / 16) * r), z = (int) Math.round(Math.sin(k * Math.PI / 16) * r);
                    boolean ok = isLand(lvl, x, z);
                    for (int[] b : n4) ok = ok && isLand(lvl, x + b[0] * 24, z + b[1] * 24);
                    if (ok) return new int[]{x, z};
                }
            return new int[]{0, 0};
        });
    }

    /** Moves a player to dry land near the planet's origin (loads the planet first). */
    public static boolean enter(ServerPlayer p, String id) {
        ServerLevel lvl = load(p.level().getServer(), id);
        if (lvl == null) return false;
        int[] xz = findLand(lvl, id);
        lvl.getChunk(xz[0] >> 4, xz[1] >> 4);
        int y = lvl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, xz[0], xz[1]) + 1;
        p.teleport(new TeleportTransition(lvl, new Vec3(xz[0] + 0.5, y, xz[1] + 0.5), Vec3.ZERO, p.getYRot(), 0f, TeleportTransition.DO_NOTHING));
        return true;
    }

    /** Called before a player's saved position is resolved: loads the planet they were last in (if any). */
    public static void preloadFor(MinecraftServer s, java.util.Optional<net.minecraft.nbt.CompoundTag> saved) {
        saved.flatMap(t -> t.getString("Dimension")).ifPresent(dim -> {
            Identifier id = Identifier.tryParse(dim);
            if (id != null && id.getNamespace().equals(NS)) load(s, id.getPath());
        });
    }

    public static int live(MinecraftServer s) {
        return (int) s.forgeGetWorldMap().keySet().stream().filter(k -> k.identifier().getNamespace().equals(NS)).count();
    }

    private static void tick(MinecraftServer s) {
        for (ServerLevel l : s.forgeGetWorldMap().values())
            if (l.dimension().identifier().getNamespace().equals(NS)) { PlanetWeather.tick(l); if (tick % 40 == 0) PlanetHazards.tick(l); }
        if (++tick % CHECK_EVERY != 0) return;
        for (ServerLevel lvl : List.copyOf(s.forgeGetWorldMap().values())) {
            ResourceKey<Level> k = lvl.dimension();
            if (!k.identifier().getNamespace().equals(NS)) continue;
            boolean busy = !lvl.players().isEmpty() || !lvl.getForceLoadedChunks().isEmpty();
            int idle = busy ? 0 : IDLE.merge(k, 1, Integer::sum);
            if (busy) IDLE.put(k, 0);
            if (idle >= IDLE_CHECKS) unload(s, lvl);
        }
    }

    private static void unload(MinecraftServer s, ServerLevel lvl) {
        ResourceKey<Level> k = lvl.dimension();
        try {
            lvl.save(null, true, false);
            NeoForge.EVENT_BUS.post(new LevelEvent.Unload(lvl));
            s.forgeGetWorldMap().remove(k);
            s.markWorldsDirty();
            lvl.close();
            IDLE.remove(k);
            PlanetWeather.forget(k);
            PlanetWeather.forget(k);
            LOG.info("Planet {} unloaded ({} planets live)", k.identifier().getPath(), live(s));
        } catch (Exception ex) {
            LOG.error("Could not unload planet {}", k.identifier(), ex);
        }
    }

    private static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("planet").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("load").then(Commands.argument("id", StringArgumentType.word()).executes(c -> {
                    String id = StringArgumentType.getString(c, "id");
                    boolean ok = load(c.getSource().getServer(), id) != null;
                    c.getSource().sendSuccess(() -> Component.literal(ok ? "Planet " + id + " is loaded" : "No planet " + id), false);
                    return ok ? 1 : 0;
                })))
                .then(Commands.literal("go").then(Commands.argument("id", StringArgumentType.word()).executes(PlanetGate::go)))
                .then(Commands.literal("preload").then(Commands.argument("player", StringArgumentType.word()).executes(c -> {
                    var list = c.getSource().getServer().getPlayerList();
                    var who = c.getSource().getServer().services().nameToIdCache().get(StringArgumentType.getString(c, "player"));
                    if (who.isEmpty()) return 0;
                    preloadFor(c.getSource().getServer(), list.getPlayerIo().load(who.get()));
                    return 1;
                })))
                .then(Commands.literal("weather").then(Commands.argument("id", StringArgumentType.word()).executes(c -> {
                    ServerLevel l = c.getSource().getServer().getLevel(key(StringArgumentType.getString(c, "id")));
                    String m = l == null ? "not loaded" : PlanetWeather.describe(l);
                    c.getSource().sendSuccess(() -> Component.literal(m), false);
                    return l == null ? 0 : 1;
                })))
                .then(Commands.literal("land").then(Commands.argument("id", StringArgumentType.word()).executes(c -> {
                    String id = StringArgumentType.getString(c, "id");
                    ServerLevel l = load(c.getSource().getServer(), id);
                    if (l == null) return 0;
                    int[] xz = findLand(l, id);
                    c.getSource().sendSuccess(() -> Component.literal("land at " + xz[0] + " " + xz[1]), false);
                    return 1;
                })))
                .then(Commands.literal("live").executes(c -> {
                    int n = live(c.getSource().getServer());
                    c.getSource().sendSuccess(() -> Component.literal(n + " planets loaded"), false);
                    return n;
                })));
    }

    private static int go(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String id = StringArgumentType.getString(c, "id");
        boolean ok = enter(c.getSource().getPlayerOrException(), id);
        if (!ok) c.getSource().sendFailure(Component.literal("No planet " + id));
        return ok ? 1 : 0;
    }
}

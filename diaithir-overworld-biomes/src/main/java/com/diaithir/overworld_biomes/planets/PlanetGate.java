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

    /** Moves a player to the planet's surface at 0,0 (loads the planet first). */
    public static boolean enter(ServerPlayer p, String id) {
        ServerLevel lvl = load(p.level().getServer(), id);
        if (lvl == null) return false;
        int y = lvl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0) + 1;
        p.teleport(new TeleportTransition(lvl, new Vec3(0.5, y, 0.5), Vec3.ZERO, p.getYRot(), 0f, TeleportTransition.DO_NOTHING));
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

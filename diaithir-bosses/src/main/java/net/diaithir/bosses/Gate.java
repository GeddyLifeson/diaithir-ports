package net.diaithir.bosses;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

/**
 * The Shardfall Gate rite and win ceremony (graph node win.gate, plans/PROGRESSION.md). gate.json lists 16 keystones, one per input of
 * the node. A keystone is attested by its owner module (attest) or offered as an item of tag diaithir:progression/&lt;resource&gt;. Order:
 * Last Wheel beaten and the rush cleared, all keystones, the hour step (use the Gate in the window), the defend step (hold the Gate);
 * then the Gate opens: stored once in AscendantData ("shardfall_gate"), announced to the server, end credits for fighters near,
 * GateOpenedEvent on the bus. Progress is stored in AscendantData under gate.* keys. Overworld only.
 */
public final class Gate {
    public static final String OPEN = "shardfall_gate", ATTEST = "gate.attest.", HOUR_DONE = "gate.hour";
    private static final Gson GSON = new Gson();

    public static final class Keystone { public String resource, tag, auto; }
    public static final class Cfg {
        public String id;
        public int min_age = 8;
        public List<String> requires_kills = new ArrayList<>();
        public List<Keystone> keystones = new ArrayList<>();
        public Ritual.Step hour = new Ritual.Step(), defend = new Ritual.Step();
    }

    public static final Cfg CFG;
    static {
        try (var r = new InputStreamReader(Gate.class.getResourceAsStream("/data/" + DiaithirBosses.ID + "/gate.json"), StandardCharsets.UTF_8)) {
            CFG = GSON.fromJson(r, Cfg.class);
        } catch (Exception e) {
            throw new IllegalStateException("gate.json missing", e);
        }
    }

    /** Posted once, when the Gate opens. */
    public static final class GateOpenedEvent extends Event {
        public final long gameTime;
        public GateOpenedEvent(long gameTime) { this.gameTime = gameTime; }
    }

    /** Test hooks: fighters outside the level's player list, a fixed clock, and extra item-to-resource matches. */
    static final List<ServerPlayer> TEST_PLAYERS = new ArrayList<>();
    static Long testClock;
    static final Map<Item, String> TEST_ITEMS = new HashMap<>();
    static boolean quiet;
    /** Game time at which a defend step began, by gate position (not saved: a restart restarts that step). */
    static final Map<String, Long> DEFEND_START = new HashMap<>();

    public static boolean isOpen(MinecraftServer server) { return AscendantData.get(server).beaten(OPEN); }

    /** An owner module says its resource exists in this world (a settlement capital stands, a depot network runs). Returns true if new. */
    public static boolean attest(MinecraftServer server, String resource) {
        if (CFG.keystones.stream().noneMatch(k -> k.resource.equals(resource))) return false;
        AscendantData d = AscendantData.get(server);
        if (d.beaten(ATTEST + resource)) return false;
        d.defeat(ATTEST + resource, server.overworld().getGameTime());
        return true;
    }

    /**
     * Soft link to the progression glue of DiaithirRPG (net.diaithir.ages.mc.ProgressionService, per-player res: flags). When that mod is
     * loaded, a fighter's keystones are the needs of node win.gate there: complete(player, "win.gate") is true once all 16 are held, and it
     * records the node. Reflection only (no compile dependency); false when the mod is absent or changed shape.
     */
    static boolean engineComplete(ServerPlayer p) {
        if (!net.neoforged.fml.ModList.get().isLoaded("diaithir_ages")) return false;
        try {
            Class<?> c = Class.forName("net.diaithir.ages.mc.ProgressionService");
            return (boolean) c.getMethod("complete", ServerPlayer.class, String.class).invoke(null, p, "win.gate");
        } catch (ReflectiveOperationException | RuntimeException e) {
            DiaithirBosses.LOG.warn("progression link not used: {}", e.toString());
            return false;
        }
    }

    public static boolean has(MinecraftServer server, Keystone k) {
        AscendantData d = AscendantData.get(server);
        return d.beaten(ATTEST + k.resource) || "rush".equals(k.auto) && d.beaten(Rush.CFG.id);
    }

    public static List<String> missing(MinecraftServer server) {
        List<String> out = new ArrayList<>();
        for (Keystone k : CFG.keystones) if (!has(server, k)) out.add(k.resource);
        return out;
    }

    /** Why the rite cannot start yet, or null. */
    public static Component locked(MinecraftServer server) {
        if (Ages.current(server) < CFG.min_age) return Component.translatable("gate.diaithirbosses.age");
        AscendantData d = AscendantData.get(server);
        if (!CFG.requires_kills.stream().allMatch(d::beaten) || !d.beaten(Rush.CFG.id)) return Component.translatable("gate.diaithirbosses.locked");
        return null;
    }

    private static String key(BlockPos p) { return p.getX() + "," + p.getY() + "," + p.getZ(); }

    private static List<ServerPlayer> fighters(ServerLevel sl, BlockPos at, int r) {
        List<ServerPlayer> all = new ArrayList<>(sl.players());
        for (ServerPlayer p : TEST_PLAYERS) if (!all.contains(p)) all.add(p);
        return all.stream().filter(p -> p.isAlive() && !p.isSpectator() && p.distanceToSqr(Vec3.atCenterOf(at)) < r * r).toList();
    }

    private static void tell(ServerLevel sl, BlockPos at, Component c) {
        fighters(sl, at, 48).forEach(p -> p.sendSystemMessage(c));
    }

    private static boolean matches(ItemStack held, Keystone k) {
        if (held.isEmpty()) return false;
        if (k.resource.equals(TEST_ITEMS.get(held.getItem()))) return true;
        return held.is(TagKey.create(Registries.ITEM, Identifier.parse(k.tag)));
    }

    // ------------------------------------------------------------------ the Gate block
    public static void use(ServerLevel sl, BlockPos gate, Player player, ItemStack held) {
        MinecraftServer server = sl.getServer();
        if (isOpen(server)) { player.sendSystemMessage(Component.translatable("gate.diaithirbosses.open")); return; }
        if (sl.getDifficulty() == Difficulty.PEACEFUL) { player.sendSystemMessage(Component.translatable("boss.diaithirbosses.peaceful")); return; }
        if (sl.dimension() != net.minecraft.world.level.Level.OVERWORLD) { player.sendSystemMessage(Component.translatable("boss.diaithirbosses.overworld_only")); return; }
        Component why = locked(server);
        if (why != null) { player.sendSystemMessage(why); return; }
        AscendantData d = AscendantData.get(server);
        List<String> miss = missing(server);
        if (!miss.isEmpty() && player instanceof ServerPlayer sp && engineComplete(sp)) {
            for (Keystone k : CFG.keystones) attest(server, k.resource);
            miss = missing(server);
        }
        if (!miss.isEmpty()) {
            for (Keystone k : CFG.keystones) {
                if (has(server, k) || !matches(held, k)) continue;
                if (!player.isCreative()) held.shrink(1);
                attest(server, k.resource);
                miss = missing(server);
                tell(sl, gate, Component.translatable("gate.diaithirbosses.keystone", CFG.keystones.size() - miss.size(), CFG.keystones.size()));
                break;
            }
            if (!miss.isEmpty()) {
                player.sendSystemMessage(Component.translatable("gate.diaithirbosses.missing", CFG.keystones.size() - miss.size(), CFG.keystones.size(), String.join(", ", miss)));
                return;
            }
        }
        if (!d.beaten(HOUR_DONE)) {
            long t = testClock != null ? testClock : sl.getOverworldClockTime() % 24000;
            Ritual.Step s = CFG.hour;
            boolean in = s.from <= s.to ? t >= s.from && t <= s.to : t >= s.from || t <= s.to;
            if (!in) { player.sendSystemMessage(Component.translatable("gate.diaithirbosses.wrong_hour")); return; }
            d.defeat(HOUR_DONE, sl.getGameTime());
            tell(sl, gate, Component.translatable("gate.diaithirbosses.hour_done", CFG.defend.seconds));
            return;
        }
        String k = key(gate);
        if (!DEFEND_START.containsKey(k)) {
            DEFEND_START.put(k, sl.getGameTime());
            tell(sl, gate, Component.translatable("gate.diaithirbosses.defend_start", CFG.defend.seconds));
        } else {
            player.sendSystemMessage(Component.translatable("gate.diaithirbosses.progress", (sl.getGameTime() - DEFEND_START.get(k)) / 20, CFG.defend.seconds));
        }
    }

    /** Every second: the defend step spawns waves, fails when the Gate is left alone, and opens the Gate when the time is up. */
    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0 || DEFEND_START.isEmpty()) return;
        ServerLevel sl = server.overworld();
        for (Map.Entry<String, Long> e : new ArrayList<>(DEFEND_START.entrySet())) {
            String[] p = e.getKey().split(",");
            BlockPos gate = new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
            var near = fighters(sl, gate, 32);
            long secs = (sl.getGameTime() - e.getValue()) / 20;
            if (near.isEmpty()) {
                DEFEND_START.remove(e.getKey());
                clearWaves(sl, gate);
                tell(sl, gate, Component.translatable("gate.diaithirbosses.defend_failed"));
            } else if (secs >= CFG.defend.seconds) {
                DEFEND_START.remove(e.getKey());
                clearWaves(sl, gate);
                open(sl, gate);
            } else {
                ring(sl, gate, 3.0 + 2.0 * secs / CFG.defend.seconds, 24, ParticleTypes.END_ROD);
                if (secs % 10 == 5) Ritual.wave(sl, gate, CFG.defend, near.size());
            }
        }
    }

    /** A ring of particles around the Gate (the hold: it widens as the time runs; the opening: a burst and a rising pillar). */
    private static void ring(ServerLevel sl, BlockPos gate, double radius, int n, net.minecraft.core.particles.SimpleParticleType type) {
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n;
            sl.sendParticles(type, gate.getX() + 0.5 + Math.cos(a) * radius, gate.getY() + 0.2, gate.getZ() + 0.5 + Math.sin(a) * radius, 1, 0, 0.4, 0, 0.02);
        }
    }

    private static void clearWaves(ServerLevel sl, BlockPos gate) {
        for (Mob m : sl.getEntitiesOfClass(Mob.class, new AABB(gate).inflate(48), m -> m.entityTags().contains(Ritual.WAVE_TAG))) m.discard();
    }

    // ------------------------------------------------------------------ the win ceremony
    static void open(ServerLevel sl, BlockPos gate) {
        MinecraftServer server = sl.getServer();
        AscendantData d = AscendantData.get(server);
        if (d.beaten(OPEN)) return;
        d.defeat(OPEN, sl.getGameTime());
        server.getPlayerList().broadcastSystemMessage(Component.translatable("gate.diaithirbosses.opened"), false);
        ring(sl, gate, 2.0, 16, ParticleTypes.END_ROD);
        ring(sl, gate, 6.0, 48, ParticleTypes.ENCHANT);
        sl.sendParticles(ParticleTypes.END_ROD, gate.getX() + 0.5, gate.getY() + 8, gate.getZ() + 0.5, 200, 0.3, 8, 0.3, 0.05);
        if (!quiet) {
            sl.playSound(null, gate, SoundEvents.END_PORTAL_SPAWN, SoundSource.AMBIENT, 4.0F, 1.0F);
            for (ServerPlayer p : fighters(sl, gate, 64)) {
                if (p instanceof net.neoforged.neoforge.common.util.FakePlayer) continue;
                p.showEndCredits();
            }
        }
        NeoForge.EVENT_BUS.post(new GateOpenedEvent(sl.getGameTime()));
    }

    private Gate() {}
}

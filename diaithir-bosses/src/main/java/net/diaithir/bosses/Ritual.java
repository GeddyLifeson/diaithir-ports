package net.diaithir.bosses;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Multi-step rites that wake the Ascendant bosses. A rite is data (data/diaithirbosses/ritual/&lt;boss&gt;.json): ordered steps, each with a
 * cryptic clue shown when the step opens. Step types: offer (give items to the altar), hunt (kill creatures near it), stones (seal
 * ogham runes in the order the clue hides), hour (use the altar in a time window), coop (several fighters at the altar), defend
 * (hold the altar through waves). Progress lives in AscendantData; the boss wakes when the last step is done. Overworld only.
 */
public final class Ritual {
    public static final String[] TREES = {"Birch", "Rowan", "Alder", "Willow", "Ash", "Hawthorn", "Oak", "Holly", "Hazel"};
    public static final String[] RUNES = {"ᚁ", "ᚂ", "ᚃ", "ᚄ", "ᚅ", "ᚆ", "ᚇ", "ᚈ", "ᚉ"};
    public static final String WAVE_TAG = "diaithirbosses.rite_wave";
    private static final Gson GSON = new Gson();

    public static final class Step {
        public String type, item, mob;
        public int count = 1, seconds = 60, from, to = 24000, players = 1;
        public List<Integer> sequence = new ArrayList<>();
        public List<String> mobs = new ArrayList<>();
    }

    public static final class Def { public String boss; public List<Step> steps = new ArrayList<>(); }

    private static final Map<String, Def> DEFS = new HashMap<>();
    /** Game time at which a defend step began, by altar key (not saved: a restart restarts that step). */
    static final Map<String, Long> DEFEND_START = new HashMap<>();
    /** Fighters that are not in the level's player list (the self test's fake player). */
    static final List<ServerPlayer> TEST_PLAYERS = new ArrayList<>();

    private static List<ServerPlayer> fighters(ServerLevel sl) {
        List<ServerPlayer> out = new ArrayList<>(sl.players());
        for (ServerPlayer p : TEST_PLAYERS) if (!out.contains(p)) out.add(p);
        return out;
    }

    public static Def def(String boss) {
        return DEFS.computeIfAbsent(boss, b -> {
            InputStream in = Ritual.class.getResourceAsStream("/data/" + DiaithirBosses.ID + "/ritual/" + b + ".json");
            if (in == null) return null;
            try (var r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return GSON.fromJson(r, Def.class);
            } catch (Exception e) {
                return null;
            }
        });
    }

    private record State(String boss, int step, int prog) {
        static State parse(String s) {
            String[] p = s.split("\\|");
            return new State(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        }
        @Override public String toString() { return boss + "|" + step + "|" + prog; }
    }

    private static String key(BlockPos p) { return p.getX() + "," + p.getY() + "," + p.getZ(); }

    private static BlockPos pos(String k) {
        String[] p = k.split(",");
        return new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
    }

    private static void tell(ServerLevel sl, BlockPos at, Component c) {
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(Vec3.atCenterOf(at)) < 48 * 48) p.sendSystemMessage(c);
    }

    private static Component clue(String boss, int step) { return Component.translatable("ritual.diaithirbosses." + boss + "." + step); }

    // ------------------------------------------------------------------ the altar
    /** A player uses the Ascendant altar, with something in hand or without. */
    public static void use(ServerLevel sl, BlockPos altar, Player player, ItemStack held) {
        if (sl.getDifficulty() == Difficulty.PEACEFUL) { player.sendSystemMessage(Component.translatable("boss.diaithirbosses.peaceful")); return; }
        if (sl.dimension() != net.minecraft.world.level.Level.OVERWORLD) { player.sendSystemMessage(Component.translatable("boss.diaithirbosses.overworld_only")); return; }
        AscendantData data = AscendantData.get(sl.getServer());
        String k = key(altar);
        String raw = data.rituals().get(k);
        if (raw == null) { begin(sl, altar, player, held, data); return; }
        State st = State.parse(raw);
        Def d = def(st.boss);
        if (d == null || st.step >= d.steps.size()) { data.removeRitual(k); return; }
        Step s = d.steps.get(st.step);
        switch (s.type) {
            case "offer" -> {
                boolean match = s.item.startsWith("#")
                    ? held.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.parse(s.item.substring(1))))
                    : BuiltInRegistries.ITEM.getOptional(Identifier.parse(s.item)).map(held::is).orElse(false);
                if (match) {
                    int n = Math.min(held.getCount(), s.count - st.prog);
                    if (!player.isCreative()) held.shrink(n);
                    progress(sl, altar, data, st, d, st.prog + n);
                    return;
                }
                player.sendSystemMessage(Component.translatable("ritual.diaithirbosses.progress", st.prog, s.count));
                player.sendSystemMessage(clue(st.boss, st.step));
            }
            case "hour" -> {
                long t = sl.getOverworldClockTime() % 24000;
                boolean in = s.from <= s.to ? t >= s.from && t <= s.to : t >= s.from || t <= s.to;
                if (in) advance(sl, altar, data, st, d); else player.sendSystemMessage(Component.translatable("ritual.diaithirbosses.wrong_hour"));
            }
            case "coop" -> {
                long near = fighters(sl).stream().filter(p -> p.isAlive() && !p.isSpectator() && p.distanceToSqr(Vec3.atCenterOf(altar)) < 100).count();
                if (near >= s.players) advance(sl, altar, data, st, d);
                else player.sendSystemMessage(Component.translatable("ritual.diaithirbosses.need_company", s.players));
            }
            case "defend" -> {
                if (!DEFEND_START.containsKey(k)) {
                    DEFEND_START.put(k, sl.getGameTime());
                    tell(sl, altar, Component.translatable("ritual.diaithirbosses.defend_start", s.seconds));
                } else {
                    player.sendSystemMessage(Component.translatable("ritual.diaithirbosses.progress", (sl.getGameTime() - DEFEND_START.get(k)) / 20, s.seconds));
                }
            }
            default -> {
                player.sendSystemMessage(Component.translatable("ritual.diaithirbosses.progress", st.prog, s.type.equals("stones") ? s.sequence.size() : s.count));
                player.sendSystemMessage(clue(st.boss, st.step));
            }
        }
    }


    /** A fresh altar wakes the next rite: the boss whose trophy is held, else the first one the Age and the kills allow. */
    private static void begin(ServerLevel sl, BlockPos altar, Player player, ItemStack held, AscendantData data) {
        int age = Ages.current(sl.getServer());
        Data.Profile pick = null;
        long wait = 0;
        boolean token = held.is(DiaithirBosses.TOKEN.get());
        for (Data.Profile p : Data.profiles().values()) {
            if (p.summon == null || !"ascendant_altar".equals(p.summon.rite) || age < p.age || def(p.id) == null) continue;
            if (!p.summon.requires_kills.stream().allMatch(data::beaten)) continue;
            var trophy = DiaithirBosses.TROPHIES.get(p.id);
            boolean viaTrophy = trophy != null && held.is(trophy.get());
            if (!viaTrophy && !(token && !data.beaten(p.id))) continue;
            long last = data.lastDefeat(p.id);
            long left = last < 0 ? 0 : p.respawn.ticks - (sl.getGameTime() - last);
            if (left > 0) { wait = wait == 0 ? left : Math.min(wait, left); continue; }
            pick = p;
            break;
        }
        if (pick == null) {
            player.sendSystemMessage(wait > 0 ? Component.translatable("boss.diaithirbosses.cooldown", wait / 1200 + 1) : Component.translatable("ritual.diaithirbosses.silent"));
            return;
        }
        if (!player.isCreative()) held.shrink(1);
        data.putRitual(key(altar), new State(pick.id, 0, 0).toString());
        tell(sl, altar, Component.translatable("ritual.diaithirbosses.begin"));
        tell(sl, altar, clue(pick.id, 0));
    }

    private static void progress(ServerLevel sl, BlockPos altar, AscendantData data, State st, Def d, int prog) {
        Step s = d.steps.get(st.step);
        int need = s.type.equals("stones") ? s.sequence.size() : s.count;
        if (prog >= need) { advance(sl, altar, data, st, d); return; }
        data.putRitual(key(altar), new State(st.boss, st.step, prog).toString());
        tell(sl, altar, Component.translatable("ritual.diaithirbosses.progress", prog, need));
    }

    private static void advance(ServerLevel sl, BlockPos altar, AscendantData data, State st, Def d) {
        String k = key(altar);
        DEFEND_START.remove(k);
        int next = st.step + 1;
        if (next >= d.steps.size()) {
            data.removeRitual(k);
            Data.Profile p = Data.byId(st.boss);
            tell(sl, altar, Component.translatable("ritual.diaithirbosses.done"));
            if (p != null) DiaithirBosses.spawn(sl, p, Vec3.atBottomCenterOf(altar.above(2)), altar);
            return;
        }
        data.putRitual(k, new State(st.boss, next, 0).toString());
        tell(sl, altar, Component.translatable("ritual.diaithirbosses.step_done"));
        tell(sl, altar, clue(st.boss, next));
    }

    private static void fail(ServerLevel sl, BlockPos altar, AscendantData data, State st, String why) {
        String k = key(altar);
        DEFEND_START.remove(k);
        data.putRitual(k, new State(st.boss, st.step, 0).toString());
        tell(sl, altar, Component.translatable(why));
        for (Mob m : sl.getEntitiesOfClass(Mob.class, new AABB(altar).inflate(48), e -> e.entityTags().contains(WAVE_TAG))) m.discard();
    }

    // ------------------------------------------------------------------ the world acts on a rite
    /** A player seals the rune of a standing stone: it must be the next rune of an open stones step within 24 blocks of an altar. */
    public static void seal(ServerLevel sl, BlockPos stone, int rune, Player player) {
        AscendantData data = AscendantData.get(sl.getServer());
        for (Map.Entry<String, String> e : new ArrayList<>(data.rituals().entrySet())) {
            BlockPos altar = pos(e.getKey());
            if (altar.distSqr(stone) > 24 * 24) continue;
            State st = State.parse(e.getValue());
            Def d = def(st.boss);
            if (d == null || st.step >= d.steps.size() || !d.steps.get(st.step).type.equals("stones")) continue;
            Step s = d.steps.get(st.step);
            if (s.sequence.get(st.prog) == rune) {
                player.sendSystemMessage(Component.translatable("ritual.diaithirbosses.rune_ok", RUNES[rune]));
                progress(sl, altar, data, st, d, st.prog + 1);
            } else {
                player.hurtServer(sl, sl.damageSources().source(DamageTypes.MAGIC), 4.0F);
                data.putRitual(e.getKey(), new State(st.boss, st.step, 0).toString());
                tell(sl, altar, Component.translatable("ritual.diaithirbosses.rune_bad"));
            }
            return;
        }
        player.sendSystemMessage(Component.translatable("ritual.diaithirbosses.rune_idle"));
    }

    /** A creature died: counts for any hunt step whose altar is within 48 blocks. */
    public static void onKill(LivingEntity victim) {
        if (!(victim.level() instanceof ServerLevel sl) || sl.getServer() == null) return;
        AscendantData data = AscendantData.get(sl.getServer());
        if (data.rituals().isEmpty()) return;
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
        for (Map.Entry<String, String> e : new ArrayList<>(data.rituals().entrySet())) {
            BlockPos altar = pos(e.getKey());
            if (altar.distSqr(victim.blockPosition()) > 48 * 48) continue;
            State st = State.parse(e.getValue());
            Def d = def(st.boss);
            if (d == null || st.step >= d.steps.size()) continue;
            Step s = d.steps.get(st.step);
            if (s.type.equals("hunt") && id.equals(s.mob)) progress(sl, altar, data, st, d, st.prog + 1);
        }
    }

    /** Every second: defend steps spawn waves, fail when the altar is left alone, finish when the time is up. */
    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 == 0) step(server);
    }

    static void step(MinecraftServer server) {
        ServerLevel sl = server.overworld();
        AscendantData data = AscendantData.get(server);
        if (data.rituals().isEmpty() || DEFEND_START.isEmpty()) return;
        for (Map.Entry<String, String> e : new ArrayList<>(data.rituals().entrySet())) {
            Long start = DEFEND_START.get(e.getKey());
            if (start == null) continue;
            BlockPos altar = pos(e.getKey());
            State st = State.parse(e.getValue());
            Def d = def(st.boss);
            if (d == null || st.step >= d.steps.size()) continue;
            Step s = d.steps.get(st.step);
            List<ServerPlayer> near = fighters(sl).stream().filter(p -> p.isAlive() && !p.isSpectator() && p.distanceToSqr(Vec3.atCenterOf(altar)) < 32 * 32).toList();
            long secs = (sl.getGameTime() - start) / 20;
            if (near.isEmpty()) { fail(sl, altar, data, st, "ritual.diaithirbosses.defend_failed"); continue; }
            if (secs >= s.seconds) {
                for (Mob m : sl.getEntitiesOfClass(Mob.class, new AABB(altar).inflate(48), m -> m.entityTags().contains(WAVE_TAG))) m.discard();
                advance(sl, altar, data, st, d);
                continue;
            }
            if (secs % 10 == 5) wave(sl, altar, s, near.size());
        }
    }

    private static void wave(ServerLevel sl, BlockPos altar, Step s, int players) {
        int alive = sl.getEntitiesOfClass(Mob.class, new AABB(altar).inflate(48), m -> m.isAlive() && m.entityTags().contains(WAVE_TAG)).size();
        int n = Math.min(3 + players * 2, 16 - alive);
        for (int i = 0; i < n && !s.mobs.isEmpty(); i++) {
            var type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(s.mobs.get(sl.getRandom().nextInt(s.mobs.size()))));
            if (type.isEmpty()) continue;
            double a = sl.getRandom().nextDouble() * Math.PI * 2;
            BlockPos at = sl.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                altar.offset((int) (Math.cos(a) * 12), 0, (int) (Math.sin(a) * 12)));
            var e = type.get().create(sl, EntitySpawnReason.MOB_SUMMONED);
            if (!(e instanceof Mob m)) continue;
            m.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
            m.addTag(WAVE_TAG);
            m.setPersistenceRequired();
            sl.addFreshEntity(m);
        }
    }

    private Ritual() {}
}

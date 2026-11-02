package net.diaithir.bosses;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** The boss rush: bosses of rush.json one after another at one spot, no healing in between, rewards by count, best time kept. */
public final class Rush {
    public static final class Reward { public int after; public String item; public int count = 1; }
    public static final class Config {
        public String id;
        public int min_age = 7;
        public List<String> requires_kills = new ArrayList<>(), order = new ArrayList<>();
        public List<Reward> rewards = new ArrayList<>();
        public Arena arena = new Arena();
        public Scale scale = new Scale();
        public static final class Arena { public int between_ticks = 200; }
        public static final class Scale { public double hp = 1.0; }
    }

    public static final Config CFG;
    private static final Identifier SCALE_ID = Identifier.fromNamespaceAndPath(DiaithirBosses.ID, "rush_hp");

    static {
        try (var r = new InputStreamReader(Rush.class.getResourceAsStream("/data/" + DiaithirBosses.ID + "/rush.json"), StandardCharsets.UTF_8)) {
            CFG = new Gson().fromJson(r, Config.class);
        } catch (Exception e) {
            throw new IllegalStateException("rush.json missing", e);
        }
    }

    private static boolean running;
    private static int index, wait;
    private static long startedAt;
    private static Vec3 centre = Vec3.ZERO;
    private static ServerLevel level;
    private static UUID current;
    private static final List<UUID> fighters = new ArrayList<>();

    public static boolean active() { return running; }

    /** Why a rush cannot start for these players, or null. */
    public static Component refusal(MinecraftServer server) {
        if (running) return Component.literal("A rush is already running.");
        if (Ages.current(server) < CFG.min_age) return Component.translatable("boss.diaithirbosses.dormant", Ages.name(CFG.min_age));
        AscendantData data = AscendantData.get(server);
        for (String id : CFG.requires_kills) if (!data.beaten(id)) return Component.translatable("boss.diaithirbosses.rush_locked");
        return null;
    }

    public static void start(ServerLevel sl, Vec3 at, List<ServerPlayer> players) {
        running = true;
        level = sl;
        centre = at;
        index = 0;
        wait = 40;
        current = null;
        startedAt = sl.getGameTime();
        fighters.clear();
        players.forEach(p -> fighters.add(p.getUUID()));
        players.forEach(p -> p.sendSystemMessage(Component.translatable("boss.diaithirbosses.rush_start", CFG.order.size())));
    }

    public static void stop(String why) {
        if (running) level.getServer().getPlayerList().broadcastSystemMessage(Component.literal(why), false);
        running = false;
        if (level != null && current != null) {
            var e = level.getEntity(current);
            if (e != null) e.discard();
        }
        current = null;
    }

    private static List<ServerPlayer> alive(MinecraftServer server) {
        List<ServerPlayer> out = new ArrayList<>();
        for (UUID id : fighters) {
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p != null && p.isAlive() && !p.isSpectator()) out.add(p);
        }
        return out;
    }

    /** Called every server tick. */
    public static void tick(MinecraftServer server) {
        if (!running) return;
        List<ServerPlayer> ps = alive(server);
        if (ps.isEmpty()) { stop("The rush ends: every fighter has fallen after " + index + " bosses."); return; }
        if (current != null) {
            var e = level.getEntity(current);
            if (e != null && e.isAlive()) return;
            current = null;
            index++;
            wait = CFG.arena.between_ticks;
            reward(ps);
        }
        if (index >= CFG.order.size()) { finish(server, ps); return; }
        if (--wait > 0) return;
        Data.Profile p = Data.byId(CFG.order.get(index));
        if (p == null) { index++; return; }
        Mob m = DiaithirBosses.spawn(level, p, centre, null);
        if (m == null) { index++; return; }
        var hp = m.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null && !p.ascendant() && CFG.scale.hp != 1.0) {
            hp.addPermanentModifier(new AttributeModifier(SCALE_ID, CFG.scale.hp - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            m.setHealth(m.getMaxHealth());
        }
        current = m.getUUID();
    }

    private static void reward(List<ServerPlayer> ps) {
        for (Reward r : CFG.rewards) {
            if (r.after != index) continue;
            var item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(r.item));
            if (item.isEmpty()) continue;
            for (ServerPlayer p : ps) p.getInventory().placeItemBackInInventory(new ItemStack(item.get(), r.count));
        }
    }

    private static void finish(MinecraftServer server, List<ServerPlayer> ps) {
        long ticks = level.getGameTime() - startedAt;
        boolean best = AscendantData.get(server).record(ticks);
        Component msg = Component.translatable("boss.diaithirbosses.rush_done", ticks / 20, best ? " New best." : "");
        ps.forEach(p -> p.sendSystemMessage(msg));
        AscendantData.get(server).defeat(CFG.id, level.getGameTime());
        running = false;
    }

    private Rush() {}
}

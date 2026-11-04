package net.diaithir.bosses;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The Shardfall Gate (plans/PROGRESSION.md node win.gate, the pack's win). Clean-room, original ceremony. The requirement list is the
 * graph node's inputs ({@code data/diaithirbosses/gate/requirements.json}); a quest or module meets one with {@code /gate grant <id>},
 * and {@code rush.cleared} is read from the Ascendant Rush. Using the Gate block lists what is missing, or when everything is met starts
 * the ceremony: seven beats, three seconds apart, with light and sound, ending in the world-wide opening. It happens once per world.
 */
public final class Gate {
    public static final Map<String, String> NEEDS = new LinkedHashMap<>();
    static final int BEAT = 60;
    static final int BEATS = 7;
    /** Active ceremony: ticks left, centre. Only one at a time. */
    private static int ticks = -1;
    private static BlockPos centre;
    private static ServerLevel level;

    static {
        try (var in = Gate.class.getResourceAsStream("/data/diaithirbosses/gate/requirements.json")) {
            JsonObject o = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
            for (var e : o.getAsJsonArray("needs")) {
                JsonObject n = e.getAsJsonObject();
                NEEDS.put(n.get("id").getAsString(), n.get("name").getAsString());
            }
        } catch (Exception ex) { throw new IllegalStateException("gate requirements", ex); }
    }

    private Gate() {}

    public static boolean met(MinecraftServer s, String id) {
        if (id.equals("rush.cleared") && AscendantData.get(s).beaten("ascendant_rush")) return true;
        return GateData.get(s).has(id);
    }

    public static List<String> missing(MinecraftServer s) {
        List<String> out = new ArrayList<>();
        for (String id : NEEDS.keySet()) if (!met(s, id)) out.add(id);
        return out;
    }

    /** Use of the Gate block. Returns a short status for the player. */
    public static void use(ServerLevel lv, BlockPos pos, ServerPlayer p) {
        MinecraftServer s = lv.getServer();
        GateData d = GateData.get(s);
        if (d.isOpen()) { p.sendSystemMessage(Component.translatable("gate.diaithirbosses.already_open")); return; }
        if (ticks >= 0) return;
        List<String> miss = missing(s);
        if (!miss.isEmpty()) {
            p.sendSystemMessage(Component.translatable("gate.diaithirbosses.closed", miss.size(), NEEDS.size()));
            for (String id : miss) p.sendSystemMessage(Component.literal(" - " + NEEDS.get(id)));
            return;
        }
        begin(lv, pos);
    }

    public static void begin(ServerLevel lv, BlockPos pos) { level = lv; centre = pos; ticks = BEAT * BEATS; }

    public static boolean running() { return ticks >= 0; }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post e) {
        if (ticks < 0 || level == null) return;
        int done = BEAT * BEATS - ticks;
        if (done % BEAT == 0) beat(done / BEAT);
        if (ticks % 10 == 0) {
            double a = ticks * 0.35;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, centre.getX() + 0.5 + Math.cos(a) * 2, centre.getY() + 1 + (done % BEAT) / 15.0,
                centre.getZ() + 0.5 + Math.sin(a) * 2, 3, 0.1, 0.1, 0.1, 0.02);
        }
        if (--ticks < 0) finish();
    }

    static void beat(int n) {
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.translatable("gate.diaithirbosses.beat" + (n + 1)), true);
            level.playSound(null, p.blockPosition(), n == BEATS - 1 ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : SoundEvents.BEACON_POWER_SELECT, SoundSource.AMBIENT, 1.0F, 0.6F + 0.1F * n);
        }
        LightningBolt b = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.EVENT);
        if (b != null) {
            b.setVisualOnly(true);
            double ang = n * 2 * Math.PI / BEATS;
            b.snapTo(centre.getX() + 0.5 + Math.cos(ang) * 4, centre.getY(), centre.getZ() + 0.5 + Math.sin(ang) * 4);
            level.addFreshEntity(b);
        }
    }

    static void finish() {
        MinecraftServer s = level.getServer();
        GateData.get(s).open(level.getGameTime());
        s.getPlayerList().broadcastSystemMessage(Component.translatable("gate.diaithirbosses.opened"), false);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new GateOpenedEvent(level.getGameTime()));
        level = null;
        centre = null;
    }

    /** Posted on the game bus when the Gate opens: the modpack is complete. */
    public static final class GateOpenedEvent extends net.neoforged.bus.api.Event {
        public final long gameTime;
        public GateOpenedEvent(long gameTime) { this.gameTime = gameTime; }
    }
}

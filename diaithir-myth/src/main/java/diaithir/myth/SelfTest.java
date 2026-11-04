package diaithir.myth;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Optional boot check, enabled with -Ddiaithir_myth.selftest=true (gradle: -Pselftest). Summons every creature with
 * the real /summon command, waits, checks they are alive and that avatars enter phase two, then stops the server.
 */
final class SelfTest {
    private static final Logger LOG = LoggerFactory.getLogger("diaithir_myth/selftest");
    private int tick;

    static void init() {
        if (Boolean.getBoolean("diaithir_myth.selftest")) {
            SelfTest t = new SelfTest();
            NeoForge.EVENT_BUS.addListener(t::onTick);
        }
    }

    private void onTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        tick++;
        ServerLevel level = server.overworld();
        if (tick == 5) {
            for (int cx = -2; cx <= 2; cx++) {
                for (int cz = -2; cz <= 2; cz++) {
                    level.setChunkForced(cx, cz, true);
                }
            }
        }
        if (tick == 40) {
            int i = 0;
            for (Creature c : Creature.all()) {
                int x = (i % 6) * 6 - 15, z = (i / 6) * 6 - 6;
                int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z) + 1;
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withLevel(level).withPosition(new Vec3(x, y, z)),
                        "summon " + DiaithirMyth.MODID + ":" + c.id + " " + x + " " + y + " " + z);
                i++;
            }
        }
        if (tick == 100) {
            int mine = 0;
            for (Entity e : level.getAllEntities()) {
                if (e instanceof MythBeast b) {
                    mine++;
                    if (b.def.boss) {
                        b.setHealth(b.getMaxHealth() * 0.4F);
                    }
                }
            }
            LOG.info("MYTH_SELFTEST summoned={} expected={}", mine, Creature.all().size());
        }
        if (tick == 160) {
            List<String> bad = new ArrayList<>();
            int alive = 0, enraged = 0, bosses = 0;
            for (Entity e : level.getAllEntities()) {
                if (e instanceof MythBeast b) {
                    if (((LivingEntity) b).isAlive()) {
                        alive++;
                    } else {
                        bad.add(b.def.id);
                    }
                    if (b.def.boss) {
                        bosses++;
                        if (b.isEnraged()) {
                            enraged++;
                        }
                    }
                }
            }
            LOG.info("MYTH_SELFTEST alive={} dead={} bosses={} enraged={}", alive, bad, bosses, enraged);
            LOG.info(alive >= Creature.all().size() && enraged == bosses && bosses > 0 ? "MYTH_SELFTEST PASS" : "MYTH_SELFTEST FAIL");
            for (Entity e : new ArrayList<>(java.util.stream.StreamSupport.stream(level.getAllEntities().spliterator(), false).toList())) {
                if (e instanceof MythBeast) {
                    e.discard();
                }
            }
            server.halt(false);
        }
    }
}

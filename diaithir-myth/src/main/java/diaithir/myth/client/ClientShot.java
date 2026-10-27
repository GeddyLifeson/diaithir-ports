package diaithir.myth.client;

import diaithir.myth.Creature;
import diaithir.myth.MythBeast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Optional headless check, enabled with -Ddiaithir_myth.clientshot=true (gradle: -Pclientshot). Once a world is
 * joined it summons the creatures three at a time in front of the player, checks that each one has a Myth renderer,
 * and writes a screenshot per group to run/screenshots, then quits.
 */
final class ClientShot {
    private static final Logger LOG = LoggerFactory.getLogger("diaithir_myth/clientshot");
    private int tick, phase, group;
    private double px, py, pz;

    static void init() {
        if (Boolean.getBoolean("diaithir_myth.clientshot")) {
            NeoForge.EVENT_BUS.addListener(new ClientShot()::onTick);
        }
    }

    private void run(MinecraftServer server, String cmd) {
        server.execute(() -> server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack().withLevel(server.overworld()).withPosition(new Vec3(px, py, pz)), cmd));
    }

    private void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        MinecraftServer server = mc.getSingleplayerServer();
        if (mc.player == null || mc.level == null || server == null) {
            return;
        }
        tick++;
        List<Creature> all = Creature.all();
        if (phase == 0 && tick > 40) {
            ServerLevel lvl = server.overworld();
            px = 8.5; pz = 8.5;
            py = 152;
            mc.options.hideGui = true;
            run(server, "gamemode creative @a");
            run(server, "kill @e[type=!minecraft:player]");
            run(server, "time set noon");
            run(server, "weather clear");
            run(server, "tp @a " + px + " " + py + " " + (pz - 7) + " 0 6");
            phase = 6; tick = 0;
        } else if (phase == 6 && tick > 200) {
            // a grass platform high above any terrain keeps every shot on flat, dry ground
            run(server, "fill -14 150 -6 30 150 26 minecraft:grass_block");
            phase = 1; tick = 0;
        } else if (phase == 1 && tick > 40) {
            // summon the next group, standing on the ground in front of the camera
            int base = group * 3;
            for (int k = 0; k < 3 && base + k < all.size(); k++) {
                Creature c = all.get(base + k);
                double x = px + (k - 1) * 5.5;
                int gy = 151;
                run(server, "summon diaithir_myth:" + c.id + " " + x + " " + gy + " " + pz
                        + " {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"mythshot\"],Rotation:[155f,0f]}");
            }
            phase = 2; tick = 0;
        } else if (phase == 2 && tick > 160) {
            int seen = 0;
            for (Entity e : mc.level.entitiesForRendering()) {
                if (e instanceof MythBeast) {
                    seen++;
                    Object r = mc.getEntityRenderDispatcher().getRenderer(e);
                    if (!(r instanceof MythRenderer)) {
                        LOG.error("MYTH_CLIENT NO_RENDERER {}", e.getType());
                    }
                }
            }
            LOG.info("MYTH_CLIENT group={} rendered_entities={}", group, seen);
            Screenshot.grab(mc.gameDirectory, "myth_group" + group + ".png", mc.getMainRenderTarget(), 1, c -> LOG.info("MYTH_CLIENT shot {}", c.getString()));
            phase = 3; tick = 0;
        } else if (phase == 3 && tick > 20) {
            run(server, "kill @e[tag=mythshot]");
            run(server, "kill @e[type=minecraft:item]");
            group++;
            phase = group * 3 >= all.size() ? 4 : 1;
            tick = 0;
        } else if (phase == 4 && tick > 20) {
            LOG.info("MYTH_CLIENT DONE");
            mc.stop();
            phase = 5;
        }
    }
}
